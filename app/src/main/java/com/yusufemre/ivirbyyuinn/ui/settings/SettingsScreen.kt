package com.yusufemre.ivirbyyuinn.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yusufemre.ivirbyyuinn.IvirApplication
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.data.LanguagePreference
import com.yusufemre.ivirbyyuinn.ui.lock.AppLockViewModel
import com.yusufemre.ivirbyyuinn.ui.lock.AppLockViewModelFactory
import com.yusufemre.ivirbyyuinn.ui.theme.IvirIcons
import com.yusufemre.ivirbyyuinn.ui.util.AnimatedDialog

// Kullanıcı geri bildirimi: Ayarlar 10 ayrı kartla kalabalıklaşmıştı.
// Bu ekran artık DETAY içermiyor, sadece 6 kategoriye açılan ince bir
// gruplu liste (tek Card, satırlar arasında ince ayraç — iOS Ayarlar'ın
// "grouped list" deseni). Her kategori kendi alt ekranına (DataBackup/
// Appearance/Behavior/Notification/Security/AboutSettingsScreen) AynI
// AnimatedDialog deseniyle açılıyor, diğer tüm alt ekranlar (Çöp kutusu,
// Takvim, İstatistikler) gibi.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onClose: () -> Unit) {
    val application = LocalContext.current.applicationContext as IvirApplication

    // Menüdeki sağ taraf "değer önizlemesi" (ör. "Türkçe", "Bağlı",
    // "Açık") için SADECE ihtiyaç duyulan üç ViewModel burada da
    // kuruluyor; Activity paylaşımlı oldukları için (bkz. her birinin
    // kendi dosyasındaki yorum) alt ekran açıldığında AYNI örnek
    // devam ediyor, ekstra bir maliyeti yok.
    val languageViewModel: LanguageViewModel = viewModel(
        factory = LanguageViewModelFactory(
            getLanguagePreferenceUseCase = application.getLanguagePreferenceUseCase,
            setLanguagePreferenceUseCase = application.setLanguagePreferenceUseCase,
        ),
    )
    val languagePreference by languageViewModel.languagePreference.collectAsStateWithLifecycle()

    val appLockViewModel: AppLockViewModel = viewModel(
        factory = AppLockViewModelFactory(
            isAppLockEnabledUseCase = application.isAppLockEnabledUseCase,
            verifyAppLockPinUseCase = application.verifyAppLockPinUseCase,
            setAppLockPinUseCase = application.setAppLockPinUseCase,
            disableAppLockUseCase = application.disableAppLockUseCase,
        ),
    )
    val isLockEnabled by appLockViewModel.isLockEnabled.collectAsStateWithLifecycle()

    val googleDriveViewModel: GoogleDriveViewModel = viewModel(
        factory = GoogleDriveViewModelFactory(
            getGoogleDriveSignInIntentUseCase = application.getGoogleDriveSignInIntentUseCase,
            completeGoogleDriveSignInUseCase = application.completeGoogleDriveSignInUseCase,
            getGoogleDriveAccountEmailUseCase = application.getGoogleDriveAccountEmailUseCase,
            signOutGoogleDriveUseCase = application.signOutGoogleDriveUseCase,
            uploadBackupToDriveUseCase = application.uploadBackupToDriveUseCase,
            restoreBackupFromDriveUseCase = application.restoreBackupFromDriveUseCase,
            getGoogleDriveBackupPreferencesUseCase = application.getGoogleDriveBackupPreferencesUseCase,
            setGoogleDriveAutoBackupEnabledUseCase = application.setGoogleDriveAutoBackupEnabledUseCase,
            setGoogleDriveAutoBackupScheduleUseCase = application.setGoogleDriveAutoBackupScheduleUseCase,
            deleteAllDriveBackupsUseCase = application.deleteAllDriveBackupsUseCase,
        ),
    )
    val driveAccountEmail by googleDriveViewModel.accountEmail.collectAsStateWithLifecycle()

    // rememberSaveable (düz remember DEĞİL): Yazı Boyutu değişince
    // Activity recreate() oluyor (bkz. AppearanceSettingsScreen'deki
    // FontSizePicker), bu da TÜM remember state'lerini sıfırlar. Hangi
    // alt ekranda olduğumuzu rememberSaveable'a taşıyarak, recreate
    // SONRASI kullanıcı AYNI alt ekranda kalıyor, Ayarlar'ın ana
    // listesine fırlatılmıyor.
    var showDataBackup by rememberSaveable { mutableStateOf(false) }
    var showAppearance by rememberSaveable { mutableStateOf(false) }
    var showBehavior by rememberSaveable { mutableStateOf(false) }
    var showNotifications by rememberSaveable { mutableStateOf(false) }
    var showSecurity by rememberSaveable { mutableStateOf(false) }
    var showAbout by rememberSaveable { mutableStateOf(false) }

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
    ) { innerPadding ->
        Card(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .fillMaxWidth(),
        ) {
            SettingsMenuRow(
                icon = IvirIcons.CloudBackup,
                title = stringResource(R.string.settings_category_data_title),
                subtitle = stringResource(R.string.settings_category_data_subtitle),
                value = if (driveAccountEmail != null) stringResource(R.string.settings_value_connected) else null,
                onClick = { showDataBackup = true },
            )
            SettingsMenuDivider()
            SettingsMenuRow(
                icon = IvirIcons.Palette,
                title = stringResource(R.string.settings_category_appearance_title),
                subtitle = stringResource(R.string.settings_category_appearance_subtitle),
                value = languagePreference.shortLabel(),
                onClick = { showAppearance = true },
            )
            SettingsMenuDivider()
            SettingsMenuRow(
                icon = IvirIcons.Toggle,
                title = stringResource(R.string.settings_category_behavior_title),
                subtitle = stringResource(R.string.settings_category_behavior_subtitle),
                value = null,
                onClick = { showBehavior = true },
            )
            SettingsMenuDivider()
            SettingsMenuRow(
                icon = IvirIcons.Bell,
                title = stringResource(R.string.notifications_title),
                subtitle = stringResource(R.string.settings_category_notifications_subtitle),
                value = null,
                onClick = { showNotifications = true },
            )
            SettingsMenuDivider()
            SettingsMenuRow(
                icon = IvirIcons.Shield,
                title = stringResource(R.string.security_title),
                subtitle = stringResource(R.string.settings_category_security_subtitle),
                value = stringResource(if (isLockEnabled) R.string.settings_value_on else R.string.settings_value_off),
                onClick = { showSecurity = true },
            )
            SettingsMenuDivider()
            SettingsMenuRow(
                icon = IvirIcons.Info,
                title = stringResource(R.string.about_title),
                subtitle = stringResource(R.string.settings_category_about_subtitle),
                value = null,
                onClick = { showAbout = true },
            )
        }
    }

    if (showDataBackup) {
        AnimatedDialog(onDismissRequest = { showDataBackup = false }) { dismiss ->
            DataBackupSettingsScreen(onClose = dismiss)
        }
    }
    if (showAppearance) {
        AnimatedDialog(onDismissRequest = { showAppearance = false }) { dismiss ->
            AppearanceSettingsScreen(onClose = dismiss)
        }
    }
    if (showBehavior) {
        AnimatedDialog(onDismissRequest = { showBehavior = false }) { dismiss ->
            BehaviorSettingsScreen(onClose = dismiss)
        }
    }
    if (showNotifications) {
        AnimatedDialog(onDismissRequest = { showNotifications = false }) { dismiss ->
            NotificationSettingsScreen(onClose = dismiss)
        }
    }
    if (showSecurity) {
        AnimatedDialog(onDismissRequest = { showSecurity = false }) { dismiss ->
            SecuritySettingsScreen(onClose = dismiss)
        }
    }
    if (showAbout) {
        AnimatedDialog(onDismissRequest = { showAbout = false }) { dismiss ->
            AboutSettingsScreen(onClose = dismiss)
        }
    }
}

// Gruplu listedeki tek bir kategori satırı: ikon karosu + başlık/alt
// başlık + (varsa) sağda kısa bir durum önizlemesi + ok. value=null ise
// önizleme hiç gösterilmiyor (Davranış/Bildirimler/Hakkında'da olduğu gibi).
@Composable
private fun SettingsMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    value: String?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 21.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsMenuDivider() {
    HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
}

@Composable
private fun LanguagePreference.shortLabel(): String = when (this) {
    LanguagePreference.SYSTEM -> stringResource(R.string.language_system)
    LanguagePreference.TURKISH -> stringResource(R.string.language_turkish)
    LanguagePreference.ENGLISH -> stringResource(R.string.language_english)
}
