package com.yusufemre.ivirbyyuinn.ui.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.domain.DailyCompletionCount
import com.yusufemre.ivirbyyuinn.ui.theme.IvirIcons

// Faz 5'in ekranı: streak sayısı + son 7 günün tamamlanan görev grafiği.
// Diğer ikincil ekranlar (Takvim, Çöp kutusu) gibi Navigation kütüphanesi
// olmadan, tam ekran bir Dialog içinde açılıyor, TaskListScreen'de aynı
// desen zaten kurulu.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    weeklyStats: List<DailyCompletionCount>,
    currentStreak: Int,
    onClose: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_title)) },
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
        // verticalScroll ekli DEĞİLDİ: büyük yazı boyutu tercihinde ya da
        // küçük ekranlarda içerik (kart + 7 günlük grafik) boyu aşınca
        // alttaki günler hem kaydırılamıyor hem de görünmüyordu. Ayrıca
        // WeeklyBarChart'taki asıl kök nedenin (aşağıdaki yorum) ortaya
        // çıkmasına da bu eksik scroll zemin hazırlıyordu.
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Faz 11: streak sıfırdan büyükse, sayı artık büyük bir
                    // rakam + elde çizilmiş bir alev ikonuyla öne çıkıyor.
                    // önceden tek satırlık düz bir cümleydi, "başarı" hissi
                    // hiç görsel karşılık bulmuyordu. Sıfırken alev
                    // gösterilmiyor (henüz kutlanacak bir şey yok).
                    if (currentStreak > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = IvirIcons.Flame,
                                contentDescription = null,
                                tint = StreakFlameColor,
                                modifier = Modifier.size(28.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = currentStreak.toString(),
                                style = MaterialTheme.typography.displaySmall,
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    Text(
                        text = streakMessage(currentStreak),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Text(
                text = stringResource(R.string.stats_last_7_days),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 24.dp, bottom = 12.dp),
            )
            WeeklyBarChart(data = weeklyStats, modifier = Modifier.fillMaxWidth())
        }
    }
}

// Alevin rengi, seçili renk temasından BAĞIMSIZ sabit bir sıcak ton:
// TaskRow'daki priorityColor()'ın aynı deseni (bkz. o dosyadaki 0xFFE8735C).
// 23 tema arasında "ateş" hissi hep aynı kalsın diye kasıtlı olarak
// MaterialTheme.colorScheme'den DEĞİL, sabit bir renkten geliyor.
private val StreakFlameColor = Color(0xFFE8735C)

// Streak sıfırsa motive edici ama abartısız bir başlangıç mesajı,
// değilse kaç gündür sürdüğünü söyleyen net bir cümle.
@Composable
private fun streakMessage(streak: Int): String = when {
    streak <= 0 -> stringResource(R.string.streak_zero)
    streak == 1 -> stringResource(R.string.streak_one)
    else -> String.format(stringResource(R.string.streak_many), streak)
}
