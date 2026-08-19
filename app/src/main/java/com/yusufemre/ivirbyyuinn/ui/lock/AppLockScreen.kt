package com.yusufemre.ivirbyyuinn.ui.lock

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.domain.SetAppLockPinUseCase
import com.yusufemre.ivirbyyuinn.ui.theme.Motion
import com.yusufemre.ivirbyyuinn.ui.theme.rememberReducedMotionEnabled

// Uygulama kilitliyken MainActivity'nin TaskListScreen yerine gösterdiği
// tam ekran: geri tuşuyla ya da başka bir yolla atlanamıyor (MainActivity
// tarafında koşullu olarak bu VEYA TaskListScreen gösteriliyor, ikisi
// birden değil).
@Composable
fun AppLockScreen(viewModel: AppLockViewModel, onUnlocked: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val biometricAvailable = remember { activity != null && isBiometricAvailable(context) }

    var pin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    val reducedMotion = rememberReducedMotionEnabled()

    // Faz 11: yanlış PIN girildiğinde noktalar artık yatayda kısa bir
    // "sallanma" yapıyor, önceden sadece altında kırmızı bir metin
    // çıkıyordu, dokunsal bir "hata" hissi hiç yoktu.
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(showError) {
        if (showError && !reducedMotion) {
            listOf(12f, -10f, 8f, -6f, 0f).forEach { target ->
                shakeOffset.animateTo(target, animationSpec = tween(Motion.MICRO / 2))
            }
        } else {
            shakeOffset.snapTo(0f)
        }
    }

    val biometricPromptTitle = stringResource(R.string.biometric_prompt_title)
    val biometricUsePinLabel = stringResource(R.string.biometric_use_pin)

    fun tryBiometric() {
        if (activity == null) return
        showBiometricPrompt(
            activity = activity,
            title = biometricPromptTitle,
            negativeButtonText = biometricUsePinLabel,
            onSuccess = {
                viewModel.unlockWithBiometric()
                onUnlocked()
            },
            onError = { /* kullanıcı vazgeçti ya da hata oldu, PIN girişinde kalıyoruz */ },
        )
    }

    // Ekran ilk açıldığında biyometrik varsa otomatik dene, kullanıcı her
    // seferinde ayrı bir "parmak izi ile aç" butonuna basmak zorunda
    // kalmasın, telefonu eline aldığı an sensöre dokunması yetsin.
    LaunchedEffect(Unit) { tryBiometric() }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Faz 11: çıplak bir ikon yerine, HomeLogo'nun rozetiyle aynı
            // dilde (renkli dairesel arka plan) bir "kilit rozeti".
            // HomeScreen ile bu "kapı" ekranı arasında görsel bir süreklilik.
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(text = stringResource(R.string.app_locked_title), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(24.dp))
            Box(modifier = Modifier.graphicsLayer { translationX = shakeOffset.value }) {
                PinDots(filledCount = pin.length)
            }
            if (showError) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.wrong_pin),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(Modifier.height(32.dp))
            PinPad(
                onDigit = { digit ->
                    if (pin.length < SetAppLockPinUseCase.PIN_LENGTH) {
                        pin += digit
                        showError = false
                    }
                    if (pin.length == SetAppLockPinUseCase.PIN_LENGTH) {
                        if (viewModel.verifyPin(pin)) {
                            onUnlocked()
                        } else {
                            showError = true
                        }
                        pin = ""
                    }
                },
                onBackspace = {
                    if (pin.isNotEmpty()) pin = pin.dropLast(1)
                },
                modifier = Modifier.fillMaxWidth(),
            )
            if (biometricAvailable) {
                Spacer(Modifier.height(24.dp))
                // material-icons-core'da "Fingerprint" ikonu yok (extended
                // pakete geçmemek için, bkz. rehber/faz5), düz metin yeterli.
                TextButton(onClick = ::tryBiometric) {
                    Text(stringResource(R.string.unlock_with_biometric))
                }
            }
        }
    }
}

// PIN'in kendi rakamlarını göstermiyoruz (omuz sörfü riski): kaç hane
// girildiğini dolu/boş daireciklerle gösteriyoruz, çoğu bankacılık
// uygulamasındaki gibi.
@Composable
fun PinDots(filledCount: Int, totalCount: Int = SetAppLockPinUseCase.PIN_LENGTH) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(totalCount) { index ->
            val filled = index < filledCount
            val color = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(color = color, shape = CircleShape),
            )
        }
    }
}
