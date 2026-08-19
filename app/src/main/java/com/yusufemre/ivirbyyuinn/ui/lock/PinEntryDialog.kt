package com.yusufemre.ivirbyyuinn.ui.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.domain.SetAppLockPinUseCase

// SettingsScreen'in "PIN belirle" / "PIN'i tekrar gir" / "kilidi kapatmak
// için PIN'i gir" akışlarının HEPSİNDE kullanılan tek, genel amaçlı bir
// diyalog: 4 hane girilince onPinEntered çağrılıyor, ne yapılacağına
// (yeni PIN mi, doğrulama mı) çağıran taraf karar veriyor. AppLockScreen'in
// kendisiyle karıştırmamak lazım: o, uygulama KİLİTLİYKEN tam ekranı
// kaplıyor; bu ise Ayarlar içinden açılan geçici bir adım.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinEntryDialog(
    title: String,
    errorMessage: String?,
    onPinEntered: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var pin by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.pin_entry_dismiss_cd),
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                PinDots(filledCount = pin.length)
                if (errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(32.dp))
                PinPad(
                    onDigit = { digit ->
                        if (pin.length < SetAppLockPinUseCase.PIN_LENGTH) {
                            pin += digit
                        }
                        if (pin.length == SetAppLockPinUseCase.PIN_LENGTH) {
                            val entered = pin
                            pin = ""
                            onPinEntered(entered)
                        }
                    },
                    onBackspace = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
