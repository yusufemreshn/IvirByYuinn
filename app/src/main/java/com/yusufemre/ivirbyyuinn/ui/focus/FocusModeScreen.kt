package com.yusufemre.ivirbyyuinn.ui.focus

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.yusufemre.ivirbyyuinn.IvirApplication
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.data.FocusPhase
import com.yusufemre.ivirbyyuinn.data.FocusSessionState
import com.yusufemre.ivirbyyuinn.notification.FOCUS_CHANNEL_ID
import com.yusufemre.ivirbyyuinn.ui.util.WheelPicker
import kotlinx.coroutines.delay

private const val FOCUS_NOTIFICATION_ID = -2

// Navigation kütüphanesi olmadığı için diğer alt ekranlar gibi
// (Çöp kutusu/Takvim/İstatistikler) TaskListScreen tarafından tam ekran
// bir Dialog içinde açılıyor.
//
// Kullanıcı isteğiyle iki eksik giderildi:
//
// (1) Süre artık sabit 25 dakika değil. IDLE haldeyken ortadaki süre
// yazısına DOKUNMAK tam ekrana yakın, bulanık bir arka plan üzerinde açılan
// bir "seçici" (DurationPickerOverlay) açıyor, saat/dakika/saniye için üç
// bağımsız tekerlek (bkz. ui/util/WheelPicker.kt). Arka planın
// bulanıklaşması (Modifier.blur) API 31+ gerektiriyor, altındaki
// sürümlerde Compose bunu sessizce atlıyor (çökmüyor), sadece bulanıklık
// görünmüyor, geri kalan her şey (büyütme, seçici, Kaydet) aynı çalışıyor.
// Süre bir saati geçse de geçmese de HER ZAMAN "SS:DD:SS" biçiminde
// gösteriliyor (bkz. formatDuration). Kullanıcı saatin hiç kaybolmamasını
// istedi.
//
// 2026-08-09: İlk sürümde her tekerlek KENDİ elle yazılmış
// `detectDragGestures` + piksel-eşiği-sayma mantığıyla çalışıyordu, ve bu,
// tam ekran scrim'in DE kendi `detectDragGestures`'ı içinde İÇ İÇE
// yaşadığı için (aynı sürükleme akışını iki ayrı gesture detector aynı anda
// işlemeye çalışıyordu) tutarsız/atlamalı davranıyordu. Kullanıcı Takvim
// ekranındaki ay/yıl tekerlek seçicisini (bkz. MonthYearWheelPicker.kt)
// beğenince "aynı sistemi" burada da istedi, o seçicideki `LazyColumn` +
// resmi `rememberSnapFlingBehavior` deseni paylaşılan `WheelPicker`'a
// çıkarılıp buraya da uygulandı, elle piksel matematiği tamamen kalktı.
//
// (2) Sayaç artık SharedPreferences'a kaydediliyor (bkz.
// FocusSessionPreferencesRepository).
// Başka bir menüye geçip geri dönmek YA DA uygulamayı komple kapatıp
// açmak sayacı sıfırlamıyor, kaldığı yerden devam ediyor. Bunu, her
// tık'ta durumu kaydedip ekran her açıldığında "aradan ne kadar GERÇEK
// zaman geçti" hesabıyla (resolveInitialState) çözüyoruz. Arka planda
// sürekli çalışan bir servis/WorkManager işi YOK, buna gerek de yok:
// sayaç kapalıyken saymıyor, sadece son bilinen anı ve o andan bu yana
// geçen gerçek süreyi biliyor.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusModeScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val application = context.applicationContext as IvirApplication
    val haptics = LocalHapticFeedback.current

    val initialState = remember { resolveInitialState(application.getFocusSessionStateUseCase()) }

    var phase by remember { mutableStateOf(initialState.phase) }
    var remainingSeconds by remember { mutableIntStateOf(initialState.remainingSeconds) }
    var isRunning by remember { mutableStateOf(initialState.isRunning) }
    var workDurationSeconds by remember { mutableIntStateOf(initialState.workDurationSeconds) }
    val breakDurationSeconds by remember { mutableIntStateOf(initialState.breakDurationSeconds) }

    // Süre "düzenleme modu": ilk dokunuş bunu true yapıyor, tam ekrana
    // yakın bulanık bir seçici açılıyor; "Kaydet"e basınca false'a dönüp
    // seçici kapanıyor.
    var isEditingDuration by remember { mutableStateOf(false) }

    fun persist() {
        application.saveFocusSessionStateUseCase(
            FocusSessionState(
                phase = phase,
                isRunning = isRunning,
                remainingSeconds = remainingSeconds,
                workDurationSeconds = workDurationSeconds,
                breakDurationSeconds = breakDurationSeconds,
                lastTickEpochMillis = System.currentTimeMillis(),
            ),
        )
    }

    // İlk composition'da, resolveInitialState bir faz geçişi uygulamış
    // olabilir (ör. uzaktayken çalışma bitip molaya geçmiş). Bunu hemen
    // geri yazıyoruz ki lastTickEpochMillis "şimdi"ye güncellensin.
    LaunchedEffect(Unit) { persist() }

    val workCompleteTitle = stringResource(R.string.focus_work_complete_title)
    val workCompleteText = stringResource(R.string.focus_work_complete_text, breakDurationSeconds / 60)
    val breakCompleteTitle = stringResource(R.string.focus_break_complete_title)
    val breakCompleteText = stringResource(R.string.focus_break_complete_text)

    // isRunning false->true olduğunda (Başlat/Devam et) YENİ bir coroutine
    // başlıyor; bu tek coroutine, faz WORK'ten BREAK'e geçse bile (isRunning
    // DEĞİŞMEDEN) kendi içindeki while döngüsüyle devam ediyor, mola
    // bitince isRunning'i kendisi false yapıp döngüden çıkıyor.
    LaunchedEffect(isRunning) {
        while (isRunning) {
            if (remainingSeconds > 0) {
                delay(1_000)
                remainingSeconds -= 1
                persist()
            } else {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                when (phase) {
                    FocusPhase.WORK -> {
                        postFocusNotification(context, workCompleteTitle, workCompleteText)
                        phase = FocusPhase.BREAK
                        remainingSeconds = breakDurationSeconds
                    }
                    FocusPhase.BREAK -> {
                        postFocusNotification(context, breakCompleteTitle, breakCompleteText)
                        phase = FocusPhase.IDLE
                        remainingSeconds = workDurationSeconds
                        isRunning = false
                    }
                    FocusPhase.IDLE -> isRunning = false
                }
                persist()
            }
        }
    }

    val totalForPhase = if (phase == FocusPhase.BREAK) breakDurationSeconds else workDurationSeconds
    val progress = if (totalForPhase > 0) 1f - remainingSeconds / totalForPhase.toFloat() else 0f
    val phaseLabel = when (phase) {
        FocusPhase.IDLE -> stringResource(R.string.focus_phase_idle)
        FocusPhase.WORK -> stringResource(R.string.focus_phase_work)
        FocusPhase.BREAK -> stringResource(R.string.focus_phase_break)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            // Düzenleme modundayken arkadaki her şey (bu ekranın kendisi)
            // bulanıklaşıyor, üstteki DurationPickerOverlay net kalıyor.
            modifier = Modifier.blur(if (isEditingDuration) 24.dp else 0.dp),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.focus_mode_title)) },
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
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = phaseLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier
                        .padding(top = 24.dp, bottom = 32.dp)
                        .size(220.dp)
                        .then(
                            // Süre ayarı sadece IDLE'dayken, HENÜZ düzenleme
                            // moduna girilmemişken dokunmaya tepki veriyor,
                            // düzenleme moduna girince tüm sürükleme mantığı
                            // DurationPickerOverlay'e taşınıyor (bu Box zaten
                            // bulanıklaşıp arka planda kalıyor).
                            if (phase == FocusPhase.IDLE && !isEditingDuration) {
                                Modifier.pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            isEditingDuration = true
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                    )
                                }
                            } else {
                                Modifier
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    FocusRing(progress = progress, isBreak = phase == FocusPhase.BREAK)
                    // Sabit 220dp'lik daire içinde displayMedium metin,
                    // büyük sistem yazı tipi ölçeğinde "25:00" gibi metin
                    // dairenin dışına taşabiliyordu. maxLines/overflow, uç
                    // durumda taşmak yerine düzgün kırpılmasını sağlıyor.
                    Text(
                        text = formatDuration(remainingSeconds),
                        style = MaterialTheme.typography.displayMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                    )
                }
            if (phase == FocusPhase.IDLE) {
                Text(
                    text = stringResource(
                        if (isEditingDuration) R.string.focus_duration_hint_editing else R.string.focus_duration_hint_idle,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp),
                )
            }

            if (isEditingDuration) {
                // Düzenleme modundayken normal Başlat/Sıfırla düğmeleri
                // yerine SADECE Kaydet var, yarım kalmış bir süre ayarıyla
                // sayacı başlatmak kafa karıştırır.
                Button(onClick = {
                    isEditingDuration = false
                    persist()
                }) {
                    Text(stringResource(R.string.focus_save_duration))
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (isRunning) {
                        OutlinedButton(onClick = {
                            isRunning = false
                            persist()
                        }) {
                            Text(stringResource(R.string.focus_pause))
                        }
                    } else {
                        Button(onClick = {
                            if (phase == FocusPhase.IDLE) {
                                // workDurationSeconds zaten düzenleme modunda
                                // canlı güncellendi (varsayılan 25 dk, kullanıcı
                                // değiştirmediyse), burada tekrar hesaplamaya
                                // gerek yok.
                                phase = FocusPhase.WORK
                                remainingSeconds = workDurationSeconds
                            }
                            isRunning = true
                            persist()
                        }) {
                            Text(
                                if (phase == FocusPhase.IDLE) {
                                    stringResource(R.string.focus_start)
                                } else {
                                    stringResource(R.string.focus_resume)
                                },
                            )
                        }
                    }
                    OutlinedButton(onClick = {
                        isRunning = false
                        phase = FocusPhase.IDLE
                        remainingSeconds = workDurationSeconds
                        persist()
                    }) {
                        Text(stringResource(R.string.focus_reset))
                    }
                }
            }
        }
        }

        AnimatedVisibility(
            visible = isEditingDuration,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.85f),
        ) {
            DurationPickerOverlay(
                currentSeconds = workDurationSeconds,
                onChangeSeconds = { seconds ->
                    workDurationSeconds = seconds
                    remainingSeconds = seconds
                },
                onSave = {
                    isEditingDuration = false
                    persist()
                },
                haptics = haptics,
            )
        }
    }
}

// Ekran her açıldığında (dialog yeniden açılmış ya da uygulama sıfırdan
// başlamış olabilir) çağrılıyor: kaydedilmiş durum ÇALIŞIYOR olarak
// bırakılmışsa, o andan bu yana GERÇEKTE geçen süreyi hesaplayıp
// remainingSeconds'ı buna göre düşürüyor. Aradan geçen süre mevcut fazı
// bitirmişse TEK bir faz geçişi uyguluyor (WORK->BREAK ya da ->IDLE),
// kullanıcı günlerce uzak kalmış olsa bile art arda çalışma/mola
// döngülerini simüle etmiyoruz, bir odaklanma seansı için bunun anlamı yok.
private fun resolveInitialState(persisted: FocusSessionState): FocusSessionState {
    if (!persisted.isRunning || persisted.phase == FocusPhase.IDLE) return persisted

    val elapsedSeconds = ((System.currentTimeMillis() - persisted.lastTickEpochMillis) / 1000)
        .toInt()
        .coerceAtLeast(0)
    val newRemaining = persisted.remainingSeconds - elapsedSeconds
    if (newRemaining > 0) {
        return persisted.copy(remainingSeconds = newRemaining)
    }

    return when (persisted.phase) {
        FocusPhase.WORK -> {
            val overflow = -newRemaining
            val breakRemaining = persisted.breakDurationSeconds - overflow
            if (breakRemaining > 0) {
                persisted.copy(phase = FocusPhase.BREAK, remainingSeconds = breakRemaining)
            } else {
                persisted.copy(phase = FocusPhase.IDLE, isRunning = false, remainingSeconds = persisted.workDurationSeconds)
            }
        }
        FocusPhase.BREAK, FocusPhase.IDLE -> persisted.copy(
            phase = FocusPhase.IDLE,
            isRunning = false,
            remainingSeconds = persisted.workDurationSeconds,
        )
    }
}

// Saat HER ZAMAN görünüyor (ör. varsayılan 25 dk "00:25:00" olarak
// gösteriliyor). Kullanıcı özellikle "saat hiç kaybolmasın" dedi, saat
// sıfırken gizleyip sadece dakika:saniye göstermiyoruz. Öncesinde
// "%02d:%02d" (sadece dakika:saniye) kullanılıyordu, bu da 90 dakikayı
// yanlışlıkla "90:00" gibi gösteriyordu.
private fun formatDuration(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

// Süre düzenleme "seçicisi": saat/dakika/saniye YAN YANA, BAĞIMSIZ üç
// tekerlek, her biri kendi kaydırmasıyla ayrı ayrı ayarlanıyor (kullanıcı
// isteği: "saati dakikayı saniyeyi ayrı ayrı ayarlayabilelim"). Saat 0-23,
// dakika/saniye 0-59 aralığında SARIYOR (23'ün bir üstü 0, 0'ın bir altı 59
// gibi, gerçek bir saat kadranı gibi, bkz. WheelPicker'ın loop=true
// parametresi), üç tekerlek birbirinden bağımsız, birinin taşması diğerini
// ETKİLEMİYOR. Arka planı koyu bir scrim (altındaki ekran zaten
// Modifier.blur ile bulanıklaştırılmış oluyor).
@Composable
private fun DurationPickerOverlay(
    currentSeconds: Int,
    onChangeSeconds: (Int) -> Unit,
    onSave: () -> Unit,
    haptics: HapticFeedback,
) {
    val hours = currentSeconds / 3600
    val minutes = (currentSeconds % 3600) / 60
    val seconds = currentSeconds % 60

    // 0-23 ve 0-59 etiketleri, remember ile SABİT kalıyor, WheelPicker'ın
    // "sadece ilk açılışta ortala" mantığı (bkz. WheelPicker.kt'deki
    // LaunchedEffect(items) yorumu) bu referans hiç değişmediği için doğru
    // çalışıyor.
    val hourLabels = remember { (0..23).map { "%02d".format(it) } }
    val minuteSecondLabels = remember { (0..59).map { "%02d".format(it) } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.78f))
            // Boş alana (tekerleklerin/butonun DIŞINA) yapılan dokunuşları
            // burada tüketip altındaki (bulanık, ama hâlâ tıklanabilir
            // "Kaydet" butonunu barındıran) ekrana sızmasını engelliyoruz.
            // ESKİDEN burada ham bir detectDragGestures vardı, tekerleklerin
            // KENDİ LazyColumn sürüklemesiyle iç içe girip çakışıyordu (bkz.
            // yukarıdaki 2026-08-09 notu). clickable(indication = null),
            // sadece TIKLAMALARI tüketiyor, sürükleme akışına hiç karışmıyor.
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WheelPicker(
                    items = hourLabels,
                    selectedIndex = hours,
                    onSelectedIndexChange = { newHour ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onChangeSeconds(newHour * 3600 + minutes * 60 + seconds)
                    },
                    loop = true,
                    selectedTextStyle = MaterialTheme.typography.displayLarge,
                    unselectedTextStyle = MaterialTheme.typography.headlineSmall,
                    selectedColor = Color.White,
                    unselectedColor = Color.White.copy(alpha = 0.5f),
                    dividerColor = Color.White.copy(alpha = 0.3f),
                    // displayLarge (57sp/64sp satır yüksekliği), varsayılan
                    // 44dp satır yüksekliğine sığmıyordu, rakamlar iki çizgi
                    // arasından taşıp komşu satırlara bulaşıyordu. 76dp,
                    // PinPad'in tuş boyutuyla aynı mantıkla (bkz. PinPad.kt)
                    // büyük merkez rakama yeterli pay bırakıyor.
                    itemHeight = 76.dp,
                    modifier = Modifier.weight(1f),
                )
                DurationWheelSeparator()
                WheelPicker(
                    items = minuteSecondLabels,
                    selectedIndex = minutes,
                    onSelectedIndexChange = { newMinute ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onChangeSeconds(hours * 3600 + newMinute * 60 + seconds)
                    },
                    loop = true,
                    selectedTextStyle = MaterialTheme.typography.displayLarge,
                    unselectedTextStyle = MaterialTheme.typography.headlineSmall,
                    selectedColor = Color.White,
                    unselectedColor = Color.White.copy(alpha = 0.5f),
                    dividerColor = Color.White.copy(alpha = 0.3f),
                    // displayLarge (57sp/64sp satır yüksekliği), varsayılan
                    // 44dp satır yüksekliğine sığmıyordu, rakamlar iki çizgi
                    // arasından taşıp komşu satırlara bulaşıyordu. 76dp,
                    // PinPad'in tuş boyutuyla aynı mantıkla (bkz. PinPad.kt)
                    // büyük merkez rakama yeterli pay bırakıyor.
                    itemHeight = 76.dp,
                    modifier = Modifier.weight(1f),
                )
                DurationWheelSeparator()
                WheelPicker(
                    items = minuteSecondLabels,
                    selectedIndex = seconds,
                    onSelectedIndexChange = { newSecond ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onChangeSeconds(hours * 3600 + minutes * 60 + newSecond)
                    },
                    loop = true,
                    selectedTextStyle = MaterialTheme.typography.displayLarge,
                    unselectedTextStyle = MaterialTheme.typography.headlineSmall,
                    selectedColor = Color.White,
                    unselectedColor = Color.White.copy(alpha = 0.5f),
                    dividerColor = Color.White.copy(alpha = 0.3f),
                    // displayLarge (57sp/64sp satır yüksekliği), varsayılan
                    // 44dp satır yüksekliğine sığmıyordu, rakamlar iki çizgi
                    // arasından taşıp komşu satırlara bulaşıyordu. 76dp,
                    // PinPad'in tuş boyutuyla aynı mantıkla (bkz. PinPad.kt)
                    // büyük merkez rakama yeterli pay bırakıyor.
                    itemHeight = 76.dp,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Button(onClick = onSave) {
                Text(stringResource(R.string.focus_save_duration))
            }
        }
    }
}

@Composable
private fun DurationWheelSeparator() {
    Text(
        text = ":",
        style = MaterialTheme.typography.displayLarge,
        fontWeight = FontWeight.Bold,
        color = Color.White,
    )
}

// Kalan süreyi bir Canvas halkasıyla gösteriyor, arka planda soluk, tam
// bir daire; üzerinde faz rengiyle, geçen süreye göre büyüyen bir yay.
@Composable
private fun FocusRing(progress: Float, isBreak: Boolean) {
    val ringColor = if (isBreak) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 14.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(
            (size.width - diameter) / 2f,
            (size.height - diameter) / 2f,
        )
        val arcSize = Size(diameter, diameter)
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
        drawArc(
            color = ringColor,
            startAngle = -90f,
            sweepAngle = 360f * progress.coerceIn(0f, 1f),
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }
}

private fun postFocusNotification(context: Context, title: String, text: String) {
    val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED
    if (!hasPermission) return

    val notification = NotificationCompat.Builder(context, FOCUS_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(title)
        .setContentText(text)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()
    NotificationManagerCompat.from(context).notify(FOCUS_NOTIFICATION_ID, notification)
}
