package com.yusufemre.ivirbyyuinn.ui.task

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yusufemre.ivirbyyuinn.R

// Faz 10a'ya kadar boş liste TEK bir mesajla karşılanıyordu: "hiç görev
// yok" ile "aradığın/filtrelediğin bir şey bulunamadı" aslında çok farklı
// durumlar, kullanıcının burada yapması gereken şey de farklı (biri "ilk
// görevini ekle", diğeri "aramanı/filtreni gözden geçir"). Bu enum, hangi
// senaryoda olduğumuzu TaskListScreen'den buraya taşıyor.
enum class EmptyListReason {
    NO_TASKS,
    SEARCH,
    FILTER,
}

// Dışarıdan bir ikon paketi/illüstrasyon indirmek yerine burada elimizle
// çiziyoruz. Faz 10c'ye kadar üçü de AYNI (dashed not defteri) çizimi
// paylaşıyordu, artık her `reason`'ın kendi elde çizilmiş küçük
// illüstrasyonu var (mikro-illüstrasyon seti), ama HEPSİ aynı çizgi dilini
// kullanıyor: yuvarlak uçlu/köşeli inceltilmiş bir kontur, uygulamanın
// kendi ikonundaki (bkz. ic_launcher_foreground) ve HomeScreen'deki
// tik rozetiyle aynı "elle çizilmiş" hissi. Lisans derdi yok, tamamen bizim.
//
// Faz 10b: `reason` NO_TASKS'tan farklıysa (yani liste aslında dolu ama
// arama/filtre yüzünden hiçbir şey görünmüyorsa), altına aramayı/filtreyi
// temizleyen bir aksiyon butonu ekleniyor, NO_TASKS'ta böyle bir buton
// yok çünkü zaten oradaki tek "aksiyon" FAB'a basıp ilk görevi eklemek.
@Composable
fun EmptyTaskList(
    reason: EmptyListReason = EmptyListReason.NO_TASKS,
    onClearAction: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val titleRes = when (reason) {
        EmptyListReason.NO_TASKS -> R.string.empty_list_title
        EmptyListReason.SEARCH -> R.string.empty_search_title
        EmptyListReason.FILTER -> R.string.empty_filter_title
    }
    val subtitleRes = when (reason) {
        EmptyListReason.NO_TASKS -> R.string.empty_list_subtitle
        EmptyListReason.SEARCH -> R.string.empty_search_subtitle
        EmptyListReason.FILTER -> R.string.empty_filter_subtitle
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        EmptyStateIllustration(reason)
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(subtitleRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (reason != EmptyListReason.NO_TASKS) {
            val actionLabelRes = if (reason == EmptyListReason.SEARCH) {
                R.string.empty_search_clear_action
            } else {
                R.string.empty_filter_clear_action
            }
            TextButton(onClick = onClearAction) {
                Text(stringResource(actionLabelRes))
            }
        }
    }
}

@Composable
private fun EmptyStateIllustration(reason: EmptyListReason) {
    val outlineColor = MaterialTheme.colorScheme.outline
    Canvas(modifier = Modifier.size(96.dp)) {
        val strokeWidth = 4.dp.toPx()
        when (reason) {
            EmptyListReason.NO_TASKS -> {
                // Kesik çizgili bir "boş not defteri": üç doldurulmamış
                // satır. Faz 1'den beri var olan çizim, aynen kalıyor.
                drawRoundRect(
                    color = outlineColor,
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)),
                    ),
                    cornerRadius = CornerRadius(24f, 24f),
                )
                listOf(0.35f, 0.55f, 0.75f).forEach { heightFraction ->
                    drawLine(
                        color = outlineColor,
                        start = Offset(size.width * 0.25f, size.height * heightFraction),
                        end = Offset(size.width * 0.75f, size.height * heightFraction),
                        strokeWidth = strokeWidth / 2,
                    )
                }
            }
            EmptyListReason.SEARCH -> drawMagnifyingGlass(outlineColor, strokeWidth)
            EmptyListReason.FILTER -> drawFunnel(outlineColor, strokeWidth)
        }
    }
}

// Büyüteç: bir daire + çapraz bir sap. "Bu aramayla eşleşen görev yok"
// durumunun evrensel sembolü, elde çizilmiş.
private fun DrawScope.drawMagnifyingGlass(color: Color, strokeWidth: Float) {
    val centerX = size.width * 0.42f
    val centerY = size.height * 0.42f
    val radius = size.width * 0.22f
    drawCircle(
        color = color,
        radius = radius,
        center = Offset(centerX, centerY),
        style = Stroke(width = strokeWidth),
    )
    val angleRad = Math.toRadians(45.0)
    val handleStart = Offset(
        centerX + (radius * Math.cos(angleRad)).toFloat(),
        centerY + (radius * Math.sin(angleRad)).toFloat(),
    )
    drawLine(
        color = color,
        start = handleStart,
        end = Offset(size.width * 0.82f, size.height * 0.82f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
    )
}

// Huni (filter/funnel): "Bu kategoride görev yok" durumunun sembolü.
// Geniş üstten dar bir gövdeye inen klasik filtre glifi.
private fun DrawScope.drawFunnel(color: Color, strokeWidth: Float) {
    val path = Path().apply {
        moveTo(size.width * 0.2f, size.height * 0.26f)
        lineTo(size.width * 0.8f, size.height * 0.26f)
        lineTo(size.width * 0.58f, size.height * 0.55f)
        lineTo(size.width * 0.58f, size.height * 0.78f)
        lineTo(size.width * 0.42f, size.height * 0.78f)
        lineTo(size.width * 0.42f, size.height * 0.55f)
        close()
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
}
