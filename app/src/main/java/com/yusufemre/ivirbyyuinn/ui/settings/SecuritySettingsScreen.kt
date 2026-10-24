package com.yusufemre.ivirbyyuinn.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yusufemre.ivirbyyuinn.IvirApplication
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.ui.lock.AppLockViewModel
import com.yusufemre.ivirbyyuinn.ui.lock.AppLockViewModelFactory
import com.yusufemre.ivirbyyuinn.ui.lock.PinEntryDialog

// Ayarlar > Güvenlik: Uygulama Kilidi + PIN belirleme/doğrulama akışı.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(onClose: () -> Unit) {
    val application = LocalContext.current.applicationContext as IvirApplication
    // MainActivity'de kurulan AppLockViewModel ile AYNI Activity'yi
    // (ViewModelStoreOwner) paylaştığımız için burada da AYNI örnek
    // geliyor, kilidi buradan açıp kapatmak, MainActivity'nin gördüğü
    // kilit durumunu da anında etkiliyor.
    val appLockViewModel: AppLockViewModel = viewModel(
        factory = AppLockViewModelFactory(
            isAppLockEnabledUseCase = application.isAppLockEnabledUseCase,
            verifyAppLockPinUseCase = application.verifyAppLockPinUseCase,
            setAppLockPinUseCase = application.setAppLockPinUseCase,
            disableAppLockUseCase = application.disableAppLockUseCase,
        ),
    )
    val isLockEnabled by appLockViewModel.isLockEnabled.collectAsStateWithLifecycle()

    var lockFlowStep by remember { mutableStateOf<LockFlowStep?>(null) }
    var lockFlowError by remember { mutableStateOf<String?>(null) }
    val pinMismatchMessage = stringResource(R.string.pin_mismatch)
    val wrongPinMessage = stringResource(R.string.wrong_pin)

    val (snackbarHostState, notifySettingChanged) = rememberSettingChangedSnackbar()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.security_title)) },
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
            SettingsSectionCard {
                Text(text = stringResource(R.string.app_lock_title), style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
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
        }
    }

    // Üç adımlı küçük bir durum makinesi: kilidi AÇARKEN önce yeni PIN'i,
    // sonra aynı PIN'i tekrar (eşleşme kontrolü için) istiyoruz; kilidi
    // KAPATIRKEN tek adımda mevcut PIN'i doğruluyoruz. PinEntryDialog'un
    // kendisi bu üç senaryoda da AYNI, sadece başlık/davranış farklı.
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
                    notifySettingChanged()
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
                    notifySettingChanged()
                } else {
                    lockFlowError = wrongPinMessage
                }
            },
            onDismiss = { lockFlowStep = null },
        )

        null -> Unit
    }
}

// SecuritySettingsScreen'in kilit açma/kapama akışının hangi adımda
// olduğunu tutan küçük bir durum makinesi. sealed class kullanmamızın
// sebebi: bu `when` bloğunu derleyici tam olarak kontrol edebiliyor.
private sealed class LockFlowStep {
    data object EnterNewPin : LockFlowStep()
    data class ConfirmNewPin(val firstPin: String) : LockFlowStep()
    data object EnterCurrentPinToDisable : LockFlowStep()
}
