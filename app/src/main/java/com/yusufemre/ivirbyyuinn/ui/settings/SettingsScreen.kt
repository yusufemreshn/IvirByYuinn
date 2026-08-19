package com.yusufemre.ivirbyyuinn.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yusufemre.ivirbyyuinn.IvirApplication
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.data.AppColorTheme
import com.yusufemre.ivirbyyuinn.data.FontScalePreference
import com.yusufemre.ivirbyyuinn.data.LanguagePreference
import com.yusufemre.ivirbyyuinn.data.QuietHours
import com.yusufemre.ivirbyyuinn.ui.home.ShowHomeScreenViewModel
import com.yusufemre.ivirbyyuinn.ui.home.ShowHomeScreenViewModelFactory
import com.yusufemre.ivirbyyuinn.ui.lock.AppLockViewModel
import com.yusufemre.ivirbyyuinn.ui.lock.AppLockViewModelFactory
import com.yusufemre.ivirbyyuinn.ui.lock.PinEntryDialog
import com.yusufemre.ivirbyyuinn.ui.theme.ColorThemeViewModel
import com.yusufemre.ivirbyyuinn.ui.theme.ColorThemeViewModelFactory
import com.yusufemre.ivirbyyuinn.ui.theme.FontScaleViewModel
import com.yusufemre.ivirbyyuinn.ui.theme.FontScaleViewModelFactory
import com.yusufemre.ivirbyyuinn.ui.theme.label
import com.yusufemre.ivirbyyuinn.ui.theme.swatchColor
import com.yusufemre.ivirbyyuinn.ui.util.findActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

// Navigation kütüphanesi olmadığı için diğer alt ekranlar gibi
// (Çöp kutusu, Takvim, İstatistikler) TaskListScreen tarafından tam ekran
// bir Dialog içinde açılıyor. Üç bölüm var: Veri Yönetimi (Faz 6),
// Güvenlik (Faz 7a) ve Dil (Faz 7c). Hepsi aynı Column'un altında.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onClose: () -> Unit) {
    val application = LocalContext.current.applicationContext as IvirApplication
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            exportBackupUseCase = application.exportBackupUseCase,
            importBackupUseCase = application.importBackupUseCase,
        ),
    )
    // MainActivity'de kurulan AppLockViewModel ile AYNI Activity'yi
    // (ViewModelStoreOwner) paylaştığımız için burada da AYNI örnek
    // geliyor, kilidi buradan açıp kapatmak, MainActivity'nin
    // gördüğü kilit durumunu da anında etkiliyor.
    val appLockViewModel: AppLockViewModel = viewModel(
        factory = AppLockViewModelFactory(
            isAppLockEnabledUseCase = application.isAppLockEnabledUseCase,
            verifyAppLockPinUseCase = application.verifyAppLockPinUseCase,
            setAppLockPinUseCase = application.setAppLockPinUseCase,
            disableAppLockUseCase = application.disableAppLockUseCase,
        ),
    )
    val isLockEnabled by appLockViewModel.isLockEnabled.collectAsStateWithLifecycle()

    // 2026-08-09: Ayarlar > Giriş Ekranı, MainActivity'de kurulan ile
    // AYNI (Activity paylaşımlı) örnek.
    val showHomeScreenViewModel: ShowHomeScreenViewModel = viewModel(
        factory = ShowHomeScreenViewModelFactory(
            getShowHomeScreenPreferenceUseCase = application.getShowHomeScreenPreferenceUseCase,
            setShowHomeScreenPreferenceUseCase = application.setShowHomeScreenPreferenceUseCase,
        ),
    )
    val showHomeScreenPreference by showHomeScreenViewModel.showHomeScreen.collectAsStateWithLifecycle()

    val languageViewModel: LanguageViewModel = viewModel(
        factory = LanguageViewModelFactory(
            getLanguagePreferenceUseCase = application.getLanguagePreferenceUseCase,
            setLanguagePreferenceUseCase = application.setLanguagePreferenceUseCase,
        ),
    )
    val languagePreference by languageViewModel.languagePreference.collectAsStateWithLifecycle()

    // 2026-08-09: Ayarlar > Yazı Boyutu, MainActivity'de kurulan ile AYNI
    // (Activity paylaşımlı) örnek, burada seçim değişince IvirTheme anında
    // güncelleniyor.
    val fontScaleViewModel: FontScaleViewModel = viewModel(
        factory = FontScaleViewModelFactory(
            getFontScalePreferenceUseCase = application.getFontScalePreferenceUseCase,
            setFontScalePreferenceUseCase = application.setFontScalePreferenceUseCase,
        ),
    )
    val fontScalePreference by fontScaleViewModel.fontScalePreference.collectAsStateWithLifecycle()

    val colorThemeViewModel: ColorThemeViewModel = viewModel(
        factory = ColorThemeViewModelFactory(
            getAppColorThemeUseCase = application.getAppColorThemeUseCase,
            setAppColorThemeUseCase = application.setAppColorThemeUseCase,
        ),
    )
    val colorTheme by colorThemeViewModel.colorTheme.collectAsStateWithLifecycle()

    // Faz 12b: sessiz saatler.
    val quietHoursViewModel: QuietHoursViewModel = viewModel(
        factory = QuietHoursViewModelFactory(
            getQuietHoursUseCase = application.getQuietHoursUseCase,
            setQuietHoursUseCase = application.setQuietHoursUseCase,
        ),
    )
    val quietHours by quietHoursViewModel.quietHours.collectAsStateWithLifecycle()
    var showQuietHoursStartPicker by remember { mutableStateOf(false) }
    var showQuietHoursEndPicker by remember { mutableStateOf(false) }

    // Faz 12b: haftalık özet, varsayılan KAPALI opt-in.
    val weeklyDigestViewModel: WeeklyDigestViewModel = viewModel(
        factory = WeeklyDigestViewModelFactory(
            getWeeklyDigestOptInUseCase = application.getWeeklyDigestOptInUseCase,
            setWeeklyDigestOptInUseCase = application.setWeeklyDigestOptInUseCase,
        ),
    )
    val weeklyDigestOptedIn by weeklyDigestViewModel.optedIn.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showRestoreWarning by remember { mutableStateOf(false) }
    var lockFlowStep by remember { mutableStateOf<LockFlowStep?>(null) }
    var lockFlowError by remember { mutableStateOf<String?>(null) }

    val backupSavedMessage = stringResource(R.string.backup_saved)
    val backupSaveFailedMessage = stringResource(R.string.backup_save_failed)
    val fileReadFailedMessage = stringResource(R.string.file_read_failed)
    val backupRestoredMessage = stringResource(R.string.backup_restored)
    val backupInvalidMessage = stringResource(R.string.backup_invalid)
    val pinMismatchMessage = stringResource(R.string.pin_mismatch)
    val wrongPinMessage = stringResource(R.string.wrong_pin)

    // CreateDocument, kullanıcının "nereye kaydedeyim" diye seçtiği bir
    // Uri döndürüyor (ya da vazgeçerse null), dosyanın kendisini biz
    // Storage Access Framework üzerinden, ekstra bir depolama izni
    // istemeden yazabiliyoruz.
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            val json = viewModel.exportBackup()
            val success = writeTextToUri(context.contentResolver, uri, json)
            snackbarHostState.showSnackbar(if (success) backupSavedMessage else backupSaveFailedMessage)
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            val json = readTextFromUri(context.contentResolver, uri)
            if (json == null) {
                snackbarHostState.showSnackbar(fileReadFailedMessage)
                return@launch
            }
            val result = viewModel.importBackup(json)
            val message = if (result.isSuccess) backupRestoredMessage else backupInvalidMessage
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Long)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        // Faz 9'da eklenen "Renk Teması" (22 seçenek) ve "Hakkında"
        // bölümleriyle içerik artık küçük ekranlarda taşabilir,
        // verticalScroll ekliyoruz, önceki fazlarda buna hiç gerek
        // yoktu çünkü içerik zaten sığıyordu.
        //
        // Faz 11: bölümler artık HorizontalDivider ile ayrılan düz bir
        // liste değil, her biri kendi tonal Card'ında, TaskRow/StatsScreen'in
        // zaten kurduğu "her şey bir kartın içinde yaşıyor" diline uyum.
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsSectionCard {
                Text(text = stringResource(R.string.data_management_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.data_management_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                )

                Button(
                    onClick = { createDocumentLauncher.launch(defaultBackupFileName()) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.export_backup))
                }

                OutlinedButton(
                    onClick = { showRestoreWarning = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.import_backup))
                }
            }

            SettingsSectionCard {
                Text(text = stringResource(R.string.security_title), style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.app_lock_title), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = stringResource(R.string.app_lock_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = isLockEnabled,
                        onCheckedChange = { checked ->
                            lockFlowError = null
                            lockFlowStep = if (checked) LockFlowStep.EnterNewPin else LockFlowStep.EnterCurrentPinToDisable
                        },
                    )
                }
            }

            SettingsSectionCard {
                Text(text = stringResource(R.string.home_screen_setting_title), style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.home_screen_setting_row_title), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = stringResource(R.string.home_screen_setting_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = showHomeScreenPreference,
                        onCheckedChange = { checked -> showHomeScreenViewModel.setShowHomeScreen(checked) },
                    )
                }
            }

            SettingsSectionCard {
                Text(text = stringResource(R.string.language_title), style = MaterialTheme.typography.titleMedium)
                LanguagePicker(
                    selected = languagePreference,
                    onSelect = { preference ->
                        languageViewModel.setLanguagePreference(preference)
                        // attachBaseContext sadece Activity İLK kurulurken
                        // çalışıyor, yeni dilin gerçekten uygulanması için
                        // Activity'yi baştan kurdurmamız (recreate) gerekiyor.
                        // context.findActivity() kullanıyoruz, düz "context as?
                        // Activity" DEĞİL, bu ekran bir Dialog içinde açıldığı
                        // için LocalContext.current burada doğrudan Activity
                        // değil, onu SARMALAYAN bir ContextWrapper (Dialog'un
                        // kendi penceresine ait). "as? Activity" bu yüzden hep
                        // null dönüyordu, recreate() hiç çağrılmıyordu.
                        context.findActivity()?.recreate()
                    },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            SettingsSectionCard {
                Text(text = stringResource(R.string.font_size_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.font_size_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                )
                FontSizePicker(
                    selected = fontScalePreference,
                    onSelect = { fontScaleViewModel.setFontScalePreference(it) },
                )
            }

            SettingsSectionCard {
                Text(text = stringResource(R.string.color_theme_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.color_theme_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                )
                // Eskiden FlowRow'du: sabit 68dp genişlikli daireleri soldan
                // sağa dizip sığmayınca alt satıra geçiriyordu, ama satırı
                // kartın tam genişliğine hiç YAYMIYORDU, 3 daire kartın
                // genişliğini doldurmayınca sağda çirkin bir boşluk
                // kalıyordu. Şimdi 3'erli satırlara elle bölüp her hücreye
                // weight(1f) veriyoruz, böylece 3 daire, aralarındaki
                // boşluklarla birlikte kartın TAM genişliğine yayılıyor.
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AppColorTheme.entries.chunked(3).forEach { rowThemes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            rowThemes.forEach { theme ->
                                ColorThemeSwatch(
                                    theme = theme,
                                    selected = theme == colorTheme,
                                    onClick = { colorThemeViewModel.setColorTheme(theme) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            // Son satır 3'ten azsa (23 tema ÷ 3 = 7 tam satır +
                            // 2 tanelik bir son satır), kalan hücreleri boş
                            // ama AYNI genişlikte bir Spacer ile dolduruyoruz,
                            // yoksa son satırdaki daireler weight(1f) yüzünden
                            // olduğundan geniş görünürdü.
                            repeat(3 - rowThemes.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            SettingsSectionCard {
                Text(text = stringResource(R.string.notifications_title), style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
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
                        onCheckedChange = { checked -> quietHoursViewModel.setQuietHours(quietHours.copy(enabled = checked)) },
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { showQuietHoursStartPicker = true }) {
                        Text(formatMinuteOfDay(quietHours.startMinute))
                    }
                    Text(text = "–", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { showQuietHoursEndPicker = true }) {
                        Text(formatMinuteOfDay(quietHours.endMinute))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

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
                        onCheckedChange = { checked -> weeklyDigestViewModel.setOptIn(checked) },
                    )
                }
            }

            SettingsSectionCard {
                AboutSection()
            }
        }
    }

    if (showRestoreWarning) {
        AlertDialog(
            onDismissRequest = { showRestoreWarning = false },
            title = { Text(stringResource(R.string.restore_warning_title)) },
            text = { Text(stringResource(R.string.restore_warning_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreWarning = false
                    openDocumentLauncher.launch(arrayOf("application/json"))
                }) { Text(stringResource(R.string.action_proceed)) }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreWarning = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }

    // Faz 12b: TaskEditorSheet'in son tarih/saat seçici deseniyle AYNI,
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
                    showQuietHoursEndPicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showQuietHoursEndPicker = false }) { Text(stringResource(R.string.action_cancel)) }
            },
            text = { TimePicker(state = timePickerState) },
        )
    }

    // Üç adımlı küçük bir durum makinesi: kilidi AÇARKEN önce yeni PIN'i,
    // sonra aynı PIN'i tekrar (eşleşme kontrolü için) istiyoruz; kilidi
    // KAPATIRKEN tek adımda mevcut PIN'i doğruluyoruz. PinEntryDialog'un
    // kendisi bu üç senaryoda da AYNI, sadece başlık/davranış farklı,
    // tek bileşeni tekrar tekrar kullanıyoruz.
    when (val step = lockFlowStep) {
        is LockFlowStep.EnterNewPin -> PinEntryDialog(
            title = stringResource(R.string.new_pin_title),
            errorMessage = lockFlowError,
            onPinEntered = { pin ->
                lockFlowError = null
                lockFlowStep = LockFlowStep.ConfirmNewPin(pin)
            },
            onDismiss = { lockFlowStep = null },
        )

        is LockFlowStep.ConfirmNewPin -> PinEntryDialog(
            title = stringResource(R.string.confirm_pin_title),
            errorMessage = lockFlowError,
            onPinEntered = { pin ->
                if (pin == step.firstPin) {
                    appLockViewModel.setNewPin(pin)
                    lockFlowStep = null
                    lockFlowError = null
                } else {
                    // Eşleşmedi, baştan başlatıyoruz, kullanıcı iki
                    // farklı PIN'i "hangisi doğruydu" diye hatırlamak
                    // zorunda kalmasın.
                    lockFlowError = pinMismatchMessage
                    lockFlowStep = LockFlowStep.EnterNewPin
                }
            },
            onDismiss = { lockFlowStep = null },
        )

        is LockFlowStep.EnterCurrentPinToDisable -> PinEntryDialog(
            title = stringResource(R.string.disable_lock_title),
            errorMessage = lockFlowError,
            onPinEntered = { pin ->
                if (appLockViewModel.disableLock(pin)) {
                    lockFlowStep = null
                    lockFlowError = null
                } else {
                    lockFlowError = wrongPinMessage
                }
            },
            onDismiss = { lockFlowStep = null },
        )

        null -> Unit
    }
}

// SettingsScreen'in kilit açma/kapama akışının hangi adımda olduğunu
// tutan küçük bir durum makinesi. sealed class kullanmamızın sebebi: bu
// `when` bloğunu derleyici tam olarak kontrol edebiliyor, yeni bir adım
// eklersek `when`'i güncellemeyi unutursak derleme hata veriyor, C#'taki
// bir switch expression'ın "exhaustive" olması gerekmesiyle aynı fikir.
private sealed class LockFlowStep {
    data object EnterNewPin : LockFlowStep()
    data class ConfirmNewPin(val firstPin: String) : LockFlowStep()
    data object EnterCurrentPinToDisable : LockFlowStep()
}

// Faz 11: her Ayarlar bölümünü (Veri Yönetimi/Güvenlik/Dil/Renk Teması/
// Hakkında) sarmalayan ortak kart, HorizontalDivider'larla ayrılmış düz
// bir listeden, TaskRow/StatsScreen'in zaten kurduğu "kart" diline geçiş.
// Yeni bir ikon paketi riski almamak için (bkz. proje boyunca tekrar eden
// "material-icons-core'da yok" deneyimi) bölüm başlıklarına ikon EKLEMEDİK,
// sadece gruplama yaptık.
@Composable
private fun SettingsSectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

// ThemeToggleButton'la aynı desen: buton + dropdown menü.
@Composable
private fun LanguagePicker(
    selected: LanguagePreference,
    onSelect: (LanguagePreference) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        TextButton(onClick = { menuExpanded = true }) {
            Text(selected.label())
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            LanguagePreference.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label()) },
                    onClick = {
                        onSelect(option)
                        menuExpanded = false
                    },
                )
            }
        }
    }
}

// Öncelik seçiciyle (TaskEditorSheet) aynı desen, dropdown yerine 3
// seçeneğin tamamı tek bakışta görünsün diye FilterChip satırı. LazyRow
// (düz Row DEĞİL): 2026-08-09'da tam bu türden bir satırda ("Her ay"
// chip'i) dar ekranda/büyük yazı tipinde metnin bölündüğü bir hata
// bulunmuştu, LazyRow chip'leri asla sıkıştırmadan yatay kaydırmaya
// izin veriyor.
@Composable
private fun FontSizePicker(
    selected: FontScalePreference,
    onSelect: (FontScalePreference) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items = FontScalePreference.entries, key = { it.name }) { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(option.label()) },
            )
        }
    }
}

@Composable
private fun FontScalePreference.label(): String = when (this) {
    FontScalePreference.SMALL -> stringResource(R.string.font_size_small)
    FontScalePreference.MEDIUM -> stringResource(R.string.font_size_medium)
    FontScalePreference.LARGE -> stringResource(R.string.font_size_large)
}

// Ayarlar > Renk Teması ızgarasındaki tek bir yuvarlak örnek. DYNAMIC
// tema tek bir sabit rengi temsil edemiyor (duvar kağıdına göre değişken)
// onun için sweep gradient (birkaç rengin dairesel geçişi) kullanıyoruz,
// "otomatik/karma" hissi versin diye. Diğer 22 tema düz kendi rengini
// gösteriyor.
@Composable
private fun ColorThemeSwatch(theme: AppColorTheme, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        val background = if (theme == AppColorTheme.DYNAMIC) {
            Brush.sweepGradient(listOf(Color(0xFFE8735C), Color(0xFF7C5CBF), Color(0xFF2E93C7), Color(0xFFE8735C)))
        } else {
            Brush.linearGradient(listOf(theme.swatchColor(), theme.swatchColor()))
        }
        // Faz 11: seçim kenarlığı artık anlık "çat" değil, yumuşakça kalınlaşıyor.
        val borderWidth by animateDpAsState(targetValue = if (selected) 3.dp else 1.dp, label = "swatch-border")
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(background)
                .border(
                    width = borderWidth,
                    color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape,
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
            }
        }
        Text(
            text = theme.label(),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

// Uygulamanın ne olduğunu, kimin yaptığını ve hangi kütüphanelerle
// yazıldığını anlatan basit bir bölüm, kullanıcının isteğiyle eklendi.
@Composable
private fun AboutSection() {
    val context = LocalContext.current
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "-"
    }

    Text(text = stringResource(R.string.about_title), style = MaterialTheme.typography.titleMedium)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.about_mascot),
            // Süsleme amaçlı, kendi başına bilgi taşımıyor, TalkBack
            // bunu atlasın diye contentDescription bilerek null.
            contentDescription = null,
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape),
        )
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = stringResource(R.string.about_version, versionName),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "${stringResource(R.string.about_developer_label)}: Yusuf Emre Şahin",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = stringResource(R.string.about_license_text),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun LanguagePreference.label(): String = when (this) {
    LanguagePreference.SYSTEM -> stringResource(R.string.language_system)
    LanguagePreference.TURKISH -> stringResource(R.string.language_turkish)
    LanguagePreference.ENGLISH -> stringResource(R.string.language_english)
}

// Basit "HH:mm" biçimi, locale'e göre değişen bir saat gösterimi
// (12/24 saat) burada gerekmiyor, sessiz saatler penceresi zaten 24
// saatlik bir Int aralığı olarak tutuluyor.
private fun formatMinuteOfDay(minute: Int): String = "%02d:%02d".format(minute / 60, minute % 60)

private fun defaultBackupFileName(): String {
    val timestamp = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.getDefault()).format(System.currentTimeMillis())
    return "ivirbyyuinn_yedek_$timestamp.json"
}

// contentResolver ile SAF Uri'lerine yazma/okuma her zaman diskle
// konuşuyor, o yüzden Dispatchers.IO'ya geçiyoruz. Compose'un ana
// thread'ini (UI'ı) bloklamamak için. Bu proje küçük bir kişisel uygulama
// olsa da "gerçek/standart pratik" öğrenme hedefine uygun olsun diye burada
// da doğru dispatcher'ı kullanıyoruz.
private suspend fun writeTextToUri(resolver: android.content.ContentResolver, uri: Uri, text: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            resolver.openOutputStream(uri)?.use { stream ->
                stream.write(text.toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: java.io.IOException) {
            false
        }
    }
}

private suspend fun readTextFromUri(resolver: android.content.ContentResolver, uri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            resolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
        } catch (e: java.io.IOException) {
            null
        }
    }
}
