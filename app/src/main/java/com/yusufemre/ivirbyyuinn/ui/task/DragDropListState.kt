package com.yusufemre.ivirbyyuinn.ui.task

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Compose'un resmi kütüphanesinde hazır bir "sürükle-bırak ile listeyi
// yeniden sırala" bileşeni yok, bu yüzden bunu elle yazdık. Mantık:
// LazyListState'in "hangi öğe ekranın neresinde" bilgisini (layoutInfo)
// okuyup, sürüklenen parmağın konumunu bu bilgiyle karşılaştırıyoruz.
//
// .NET tarafında bir WPF/UWP ListView'de ItemsControl üzerinde elle
// drag&drop yazmaya benzer, hazır bir "IsDraggable" özelliği yoksa,
// PreviewMouseMove/PreviewMouseUp gibi olaylarla elle hesaplaman
// gerekir; burada da aynı şeyi Compose'un dokunma olaylarıyla yapıyoruz.
class DragDropListState(
    private val lazyListState: LazyListState,
    // Faz 10b: kenara yaklaşınca otomatik kaydırma için animateScrollBy
    // benzeri bir suspend fonksiyon çağırmamız gerekiyor, bunun için bir
    // CoroutineScope lazım. ViewModelScope değil, çünkü bu sürüklemenin
    // kendisi kadar kısa ömürlü, composable'ın scope'u yeterli.
    private val coroutineScope: CoroutineScope,
    private val onMove: (fromIndex: Int, toIndex: Int) -> Unit,
) {
    // Şu an sürüklenmekte olan öğenin listedeki index'i: null ise
    // kimse sürüklenmiyor demek, satırlar normal yerlerinde duruyor.
    var draggingItemIndex by mutableStateOf<Int?>(null)
        private set

    private var draggingItemDraggedDelta by mutableFloatStateOf(0f)
    private var draggingItemInitialOffset by mutableFloatStateOf(0f)

    private val draggingItemLayoutInfo
        get() = lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == draggingItemIndex }

    // Sürüklenen öğenin, kendi "doğal" konumundan ne kadar kaymış
    // olduğu, TaskRow bunu Modifier.offset ile görsel olarak
    // uyguluyor, parmağın altında gerçekten hareket ediyormuş gibi
    // hissettiriyor.
    val draggingItemOffset: Float
        get() = draggingItemLayoutInfo?.let { item ->
            draggingItemInitialOffset + draggingItemDraggedDelta - item.offset
        } ?: 0f

    fun onDragStart(itemIndex: Int, itemOffset: Int) {
        draggingItemIndex = itemIndex
        draggingItemInitialOffset = itemOffset.toFloat()
        draggingItemDraggedDelta = 0f
    }

    fun onDragEnd() {
        draggingItemIndex = null
        draggingItemDraggedDelta = 0f
        draggingItemInitialOffset = 0f
    }

    // Parmak her hareket ettiğinde çağrılıyor. Sürüklenen öğenin YENİ
    // merkez konumunu hesaplayıp, o konuma denk gelen başka bir öğe
    // varsa ikisinin yerini değiştiriyoruz (onMove callback'i).
    fun onDrag(dragAmount: Offset) {
        draggingItemDraggedDelta += dragAmount.y

        val currentIndex = draggingItemIndex ?: return
        val currentItem = draggingItemLayoutInfo ?: return

        val startOffset = currentItem.offset + draggingItemOffset
        val endOffset = startOffset + currentItem.size
        val middleOffset = startOffset + (endOffset - startOffset) / 2f

        val targetItem = lazyListState.layoutInfo.visibleItemsInfo.find { item ->
            item.index != currentIndex &&
                middleOffset >= item.offset &&
                middleOffset <= item.offset + item.size
        }

        if (targetItem != null) {
            onMove(currentIndex, targetItem.index)
            draggingItemIndex = targetItem.index
        }

        maybeAutoScroll()
    }

    // Sürüklenen öğe, görünür alanın üst ya da alt kenarına
    // AUTOSCROLL_TRIGGER_DISTANCE kadar yaklaşınca listeyi o yöne doğru
    // küçük bir adım kaydırıyoruz. Aksi halde uzun bir listede sürükleme,
    // ekranın kenarında görünmeyen satırlara asla ulaşamazdı. onDrag zaten
    // parmak her hareket ettiğinde çağrıldığı için, burada da her seferinde
    // küçük bir adım atmak (tek seferlik büyük bir animasyon yerine) doğal
    // bir "kenara doğru sürünme" hissi veriyor.
    private fun maybeAutoScroll() {
        val item = draggingItemLayoutInfo ?: return
        val currentTop = item.offset + draggingItemOffset
        val currentBottom = currentTop + item.size
        val viewportStart = lazyListState.layoutInfo.viewportStartOffset
        val viewportEnd = lazyListState.layoutInfo.viewportEndOffset

        when {
            currentTop - viewportStart < AUTOSCROLL_TRIGGER_DISTANCE ->
                coroutineScope.launch { lazyListState.scrollBy(-AUTOSCROLL_STEP) }
            viewportEnd - currentBottom < AUTOSCROLL_TRIGGER_DISTANCE ->
                coroutineScope.launch { lazyListState.scrollBy(AUTOSCROLL_STEP) }
        }
    }

    private companion object {
        const val AUTOSCROLL_TRIGGER_DISTANCE = 120f
        const val AUTOSCROLL_STEP = 40f
    }
}

@Composable
fun rememberDragDropListState(
    lazyListState: LazyListState,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
): DragDropListState {
    val coroutineScope = rememberCoroutineScope()
    return remember(lazyListState) { DragDropListState(lazyListState, coroutineScope, onMove) }
}
