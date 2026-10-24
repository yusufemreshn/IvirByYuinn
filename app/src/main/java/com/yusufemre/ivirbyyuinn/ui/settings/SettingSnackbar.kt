package com.yusufemre.ivirbyyuinn.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.ui.theme.IvirIcons
import kotlinx.coroutines.launch

// Kullanıcı isteği: en küçük bir ayar değişikliğinde bile ekranın altında
// kısa bir "başarıyla değiştirildi" teyidi görünsün. Her Ayarlar alt
// ekranı (Görünüm, Bildirimler, Davranış, Güvenlik, Onay Mesajları) AYNI
// jenerik mesajı AYNI şekilde göstereceği için tek bir yerde: ekran kendi
// SnackbarHostState'ini kuruyor, onCheckedChange/onSelect gibi her
// handler'ın sonunda notifySettingChanged() çağırması yeterli.
@Composable
fun rememberSettingChangedSnackbar(): Pair<SnackbarHostState, () -> Unit> {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val message = stringResource(R.string.setting_updated_snackbar)
    val notify: () -> Unit = {
        coroutineScope.launch {
            // Üst üste hızlı değişikliklerde ÖNCEKİ mesajı beklemeden
            // yenisini göstermesi için önce eskisini kapatıyoruz, yoksa
            // kullanıcı art arda iki ayar değiştirince ikinci teyit
            // kuyrukta bekleyip geç görünürdü.
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }
    return snackbarHostState to notify
}

// Görsel öneri "A" (markalı yüzey): varsayılan M3 jenerik ters-yüzey
// snackbar'ı yerine, uygulamanın KENDİ renk temasını (23 temadan hangisi
// seçiliyse) kullanan bir kap. Tüm renkler MaterialTheme.colorScheme'den
// geliyor, hiçbir yerde sabit hex yok; böylece kullanıcı rengi ya da
// açık/koyu temayı değiştirdiğinde snackbar da otomatik takip ediyor.
//
// Cihazda test edilince (2026-10-24) iki şey ortaya çıktı: surfaceContainerHigh
// koyu temada arkaplana ÇOK yakın bir tondu, snackbar neredeyse görünmez
// oluyordu (bkz. surfaceContainerHighest + primary kenarlık) — ve ekranın
// en altındaki kartın son satırıyla çakışıyordu (bkz. her ekranın content
// Column'ündaki ekstra bottom padding).
@Composable
fun SettingSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(hostState) { data ->
        Snackbar(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .border(
                    width = 1.2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    shape = MaterialTheme.shapes.small,
                ),
            shape = MaterialTheme.shapes.small,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = IvirIcons.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(data.visuals.message)
            }
        }
    }
}
