package com.yusufemre.ivirbyyuinn.ui.stats

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yusufemre.ivirbyyuinn.domain.DailyCompletionCount
import com.yusufemre.ivirbyyuinn.ui.theme.Motion
import com.yusufemre.ivirbyyuinn.ui.theme.rememberReducedMotionEnabled
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Roadmap'in istediği gibi 3. parti bir chart kütüphanesi kullanmadan,
// Compose'un kendi Canvas'ıyla elden çizilmiş sade bir çubuk grafik.
// .NET tarafında bir Chart kontrolü kullanmaya alışıksındır, burada
// kontrolün kendisini biz çiziyoruz, piksel piksel.
// Locale.getDefault(): bkz. TaskRow.dueDateFormatter'daki aynı yorum.
private val dayLabelFormatter get() = SimpleDateFormat("EEE", Locale.getDefault())

// Faz 11'in ilk sürümünde günler YAN YANA (dikey çubuklar) diziliydi,
// kullanıcı isteğiyle ALT ALTA (her gün kendi satırında, yatay bir çubuk)
// düzene geçtik. Görsel fikir aynı (en yüksek sayı en uzun çubuk), sadece
// eksenler 90 derece dönmüş gibi düşünülebilir.
@Composable
fun WeeklyBarChart(
    data: List<DailyCompletionCount>,
    modifier: Modifier = Modifier,
) {
    // En yüksek çubuğu referans alıp diğerlerini ona oranlıyoruz. Hiç
    // tamamlama yoksa (hepsi 0) bölme hatası olmasın diye en az 1 kabul
    // ediyoruz, o durumda zaten hiçbir çubuk uzamayacak.
    val maxCount = (data.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    val barColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val reducedMotion = rememberReducedMotionEnabled()

    // Her satır kendi Animatable'ı ile soldan sağa büyüyerek beliriyor,
    // Faz 11'in ilk (dikey) sürümündeki alttan-yukarı büyüme deseninin
    // aynısı, sadece yön değişti.
    val targetFractions = data.map { day -> day.count / maxCount.toFloat() }
    val animatedFractions = data.mapIndexed { index, day ->
        val target = targetFractions[index]
        val anim = remember(day.dayStartMillis) { Animatable(if (reducedMotion) target else 0f) }
        LaunchedEffect(target, reducedMotion) {
            if (reducedMotion) {
                anim.snapTo(target)
            } else {
                delay(index * 40L)
                anim.animateTo(target, animationSpec = Motion.emphasizedSpring())
            }
        }
        anim
    }

    val todayCalendar = remember { Calendar.getInstance() }
    fun isToday(millis: Long): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return cal.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        data.forEachIndexed { index, day ->
            val today = isToday(day.dayStartMillis)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // Burada eskiden heightIn(min = 26.dp) vardı, niyeti
                    // "büyük yazı tipinde satır kırpılmasın" idi, ama SADECE
                    // bir minimum vermek, Row'un üst sınırını (maxHeight)
                    // sınırsız bırakıyordu. Aşağıdaki Box.fillMaxHeight()
                    // sınırsız bir yüksekliği "doldurmaya" çalışınca, satırın
                    // (özellikle listedeki İLK günün) devasa büyümesine ve
                    // diğer günlerin ekrandan taşmasına sebep oluyordu,
                    // kullanıcının bildirdiği "Pzt kocaman, diğer günler yok"
                    // hatası tam olarak buydu. Sabit bir height() vermek
                    // Row'u kesin/sonlu bir yüksekliğe bağlıyor, fillMaxHeight()
                    // artık bu sonlu değeri dolduruyor, sarma/kırpma riski
                    // 28dp'nin makul bir metin yüksekliği olmasıyla zaten düşük.
                    .height(28.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Gün etiketi: bugünse kalın + marka rengiyle, Calendar/
                // önceki (dikey) sürümdeki "bugün" vurgusuyla aynı fikir.
                // width yerine widthIn(min=...) + tek satır: büyük yazı
                // tipinde daha uzun kısaltmalar (bazı diller "Prş" yerine
                // daha uzun kısaltma kullanabiliyor) sarmak yerine kesiliyor.
                Text(
                    text = dayLabelFormatter.format(Date(day.dayStartMillis)).replaceFirstChar { it.uppercase() },
                    modifier = Modifier.widthIn(min = 40.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (today) FontWeight.Bold else FontWeight.Normal,
                    color = if (today) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cornerRadius = CornerRadius(6.dp.toPx())
                        // Zemin: o günün "geçmiş" olduğunu, sadece hiç
                        // tamamlama olmadığını hissettiren ince bir iz.
                        drawRoundRect(
                            color = trackColor,
                            size = Size(size.width, size.height),
                            cornerRadius = cornerRadius,
                        )
                        if (day.count > 0) {
                            val barWidth = (size.width * animatedFractions[index].value)
                                .coerceAtLeast(6.dp.toPx())
                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(0f, 0f),
                                size = Size(barWidth, size.height),
                                cornerRadius = cornerRadius,
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                // Sayı etiketi: sıfırsa boş bırakıyoruz ki satırlar
                // arasında gereksiz bir "0" gürültüsü olmasın.
                Text(
                    text = if (day.count > 0) day.count.toString() else "",
                    modifier = Modifier.widthIn(min = 20.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
