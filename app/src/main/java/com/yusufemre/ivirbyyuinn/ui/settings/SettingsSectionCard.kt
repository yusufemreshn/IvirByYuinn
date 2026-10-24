package com.yusufemre.ivirbyyuinn.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Ayarlar alt ekranlarının (Veri & Yedekleme, Görünüm, Bildirimler, vb.)
// her bir bölümünü sarmalayan ortak kart, HorizontalDivider'larla ayrılmış
// düz bir listeden, TaskRow/StatsScreen'in zaten kurduğu "kart" diline geçiş.
// Yeni bir ikon paketi riski almamak için (bkz. proje boyunca tekrar eden
// "material-icons-core'da yok" deneyimi) bölüm başlıklarına ikon EKLEMEDİK,
// sadece gruplama yaptık.
@Composable
fun SettingsSectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}
