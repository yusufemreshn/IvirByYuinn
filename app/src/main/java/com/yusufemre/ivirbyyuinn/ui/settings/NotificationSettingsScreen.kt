package com.yusufemre.ivirbyyuinn.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yusufemre.ivirbyyuinn.IvirApplication
import com.yusufemre.ivirbyyuinn.R

// Ayarlar > Bildirimler: Sessiz saatler (Faz 12b) ve Haftalık özet,
// ikisi de hatırlatma/bildirim davranışını yönetiyor.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(onClose: () -> Unit) {
    val application = LocalContext.current.applicationContext as IvirApplication

    val quietHoursViewModel: QuietHoursViewModel = viewModel(
        factory = QuietHoursViewModelFactory(
            getQuietHoursUseCase = application.getQuietHoursUseCase,
            setQuietHoursUseCase = application.setQuietHoursUseCase,
        ),
    )
    val quietHours by quietHoursViewModel.quietHours.collectAsStateWithLifecycle()
    var showQuietHoursStartPicker by remember { mutableStateOf(false) }
    var showQuietHoursEndPicker by remember { mutableStateOf(false) }

    val weeklyDigestViewModel: WeeklyDigestViewModel = viewModel(
        factory = WeeklyDigestViewModelFactory(
            getWeeklyDigestOptInUseCase = application.getWeeklyDigestOptInUseCase,
            setWeeklyDigestOptInUseCase = application.setWeeklyDigestOptInUseCase,
        ),
    )
    val weeklyDigestOptedIn by weeklyDigestViewModel.optedIn.collectAsStateWithLifecycle()

    val snoozeDurationViewModel: SnoozeDurationViewModel = viewModel(
        factory = SnoozeDurationViewModelFactory(
            getSnoozeDurationUseCase = application.getSnoozeDurationUseCase,
            setSnoozeDurationUseCase = application.setSnoozeDurationUseCase,
        ),
    )
    val snoozeMinutes by snoozeDurationViewModel.snoozeMinutes.collectAsStateWithLifecycle()

    val (snackbarHostState, notifySettingChanged) = rememberSettingChangedSnackbar()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notifications_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_close_cd),
                        )
                    }
                },
            )
        },
        snackbarHost = { SettingSnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Kullanıcı geri bildirimi: Sessiz Saatler ve Haftalık Özet
            // eskiden TEK kartta, aralarında bir HorizontalDivider ile
            // bölünmüştü; uygulamanın geri kalanındaki "her konu kendi
            // kartında" kuralına uysun diye artık iki ayrı kart.
            SettingsSectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.quiet_hours_title), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = stringResource(R.string.quiet_hours_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = quietHours.enabled,
                        onCheckedChange = { checked ->
                            quietHoursViewModel.setQuietHours(quietHours.copy(enabled = checked))
                            notifySettingChanged()
                        },
                    )
                }
                QuietHoursRangePill(
                    startMinute = quietHours.startMinute,
                    endMinute = quietHours.endMinute,
                    onStartClick = { showQuietHoursStartPicker = true },
                    onEndClick = { showQuietHoursEndPicker = true },
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            SettingsSectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.weekly_digest_setting_title), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = stringResource(R.string.weekly_digest_setting_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = weeklyDigestOptedIn,
                        onCheckedChange = { checked ->
                            weeklyDigestViewModel.setOptIn(checked)
                            notifySettingChanged()
                        },
                    )
                }
            }

            SettingsSectionCard {
                Text(text = stringResource(R.string.settings_snooze_duration_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.settings_snooze_duration_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                )
                SnoozeDurationPicker(
                    selected = snoozeMinutes,
                    onSelect = {
                        snoozeDurationViewModel.setSnoozeMinutes(it)
                        notifySettingChanged()
                    },
                )
            }
        }
    }

    // TaskEditorSheet'in son tarih/saat seçici deseniyle AYNI,
    // rememberTimePickerState + AlertDialog içinde TimePicker.
    if (showQuietHoursStartPicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = quietHours.startMinute / 60,
            initialMinute = quietHours.startMinute % 60,
        )
        AlertDialog(
            onDismissRequest = { showQuietHoursStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    quietHoursViewModel.setQuietHours(
                        quietHours.copy(startMinute = timePickerState.hour * 60 + timePickerState.minute),
                    )
                    notifySettingChanged()
                    showQuietHoursStartPicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showQuietHoursStartPicker = false }) { Text(stringResource(R.string.action_cancel)) }
            },
            text = { TimePicker(state = timePickerState) },
        )
    }

    if (showQuietHoursEndPicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = quietHours.endMinute / 60,
            initialMinute = quietHours.endMinute % 60,
        )
        AlertDialog(
            onDismissRequest = { showQuietHoursEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    quietHoursViewModel.setQuietHours(
                        quietHours.copy(endMinute = timePickerState.hour * 60 + timePickerState.minute),
                    )
                    notifySettingChanged()
                    showQuietHoursEndPicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showQuietHoursEndPicker = false }) { Text(stringResource(R.string.action_cancel)) }
            },
            text = { TimePicker(state = timePickerState) },
        )
    }
}

// Basit "HH:mm" biçimi, locale'e göre değişen bir saat gösterimi
// (12/24 saat) burada gerekmiyor, sessiz saatler penceresi zaten 24
// saatlik bir Int aralığı olarak tutuluyor.
private fun formatMinuteOfDay(minute: Int): String = "%02d:%02d".format(minute / 60, minute % 60)

// Kullanıcı geri bildirimi: eskiden iki ayrı TextButton + aralarında bir
// "–" karakteriydi, aralık tek bakışta bir "şey" gibi okunmuyordu. Şimdi
// TEK bir hap: iki yarı, aralarında ince bir ayraç, her yarı kendi saat
// seçicisini açıyor.
@Composable
private fun QuietHoursRangePill(
    startMinute: Int,
    endMinute: Int,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pillShape = RoundedCornerShape(100)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(pillShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, pillShape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = formatMinuteOfDay(startMinute),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onStartClick)
                .padding(vertical = 12.dp),
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(20.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .size(16.dp),
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(20.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
        Text(
            text = formatMinuteOfDay(endMinute),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onEndClick)
                .padding(vertical = 12.dp),
        )
    }
}

// 60 dakika (eski sabit değer) listede YOK sanılmasın diye
// SNOOZE_MINUTE_OPTIONS'a dahil edildi, varsayılanı değiştirmeden seçilebilir
// kalsın diye. Kullanıcı isteğiyle 150 dk da eklendi; 7 seçenek artık
// tek satıra (LazyRow) sığmayacağı için ColorThemeSwatch ızgarasıyla AYNI
// "chunked(4) + satır satır Row" desenine geçtik, 4'erli iki satır.
private val SNOOZE_MINUTE_OPTIONS = listOf(5, 10, 15, 30, 60, 120, 150)

@Composable
private fun SnoozeDurationPicker(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SNOOZE_MINUTE_OPTIONS.chunked(4).forEach { rowOptions ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowOptions.forEach { minutes ->
                    FilterChip(
                        selected = selected == minutes,
                        onClick = { onSelect(minutes) },
                        label = { Text(stringResource(R.string.snooze_minutes_format, minutes)) },
                    )
                }
            }
        }
    }
}
