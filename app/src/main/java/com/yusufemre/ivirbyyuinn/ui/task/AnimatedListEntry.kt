package com.yusufemre.ivirbyyuinn.ui.task

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.yusufemre.ivirbyyuinn.ui.theme.Motion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Faz 10b: bir TaskRow ilk kez LazyColumn'a girdiğinde (liste ilk açıldığında
// ya da yeni bir görev eklendiğinde) kısa bir gecikmeyle solup kayarak
// belirsin diye: `LaunchedEffect(Unit)` sadece bu composable'ın İLK
// oluşturulduğu anda çalışıyor, aynı `key`'e (task.id) sahip satır sırf
// yeniden sıralandığı/filtrelendiği için yeniden ÇALIŞMIYOR (Compose o
// durumda zaten var olan slotu koruyor), yani bu animasyon gerçekten
// "yeni gelen satır" anlarına özel kalıyor, animateItem()'ın yer değiştirme
// animasyonuyla çakışmıyor.
@Composable
fun AnimatedListEntry(
    index: Int,
    reducedMotion: Boolean,
    content: @Composable () -> Unit,
) {
    val alpha = remember { Animatable(if (reducedMotion) 1f else 0f) }
    val offsetY = remember { Animatable(if (reducedMotion) 0f else 24f) }

    LaunchedEffect(Unit) {
        if (!reducedMotion) {
            // Ekranda aynı anda görünen satır sayısı zaten sınırlı (LazyColumn
            // sadece görünenleri composable ediyor), ama yine de MAX_STAGGER_INDEX
            // ile bir üst sınır koyuyoruz, çok uzun bir listede ekranın en
            // altındaki bir satır saniyelerce beklemesin.
            delay(minOf(index, MAX_STAGGER_INDEX) * STAGGER_STEP_MS)
            launch { alpha.animateTo(1f, animationSpec = tween(Motion.STANDARD)) }
            launch { offsetY.animateTo(0f, animationSpec = Motion.emphasizedSpring()) }
        }
    }

    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha.value
            translationY = offsetY.value
        },
    ) {
        content()
    }
}

private const val STAGGER_STEP_MS = 30L
private const val MAX_STAGGER_INDEX = 12
