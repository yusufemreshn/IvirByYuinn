package com.yusufemre.ivirbyyuinn.ui.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs

// Compose'da hazır bir "wheel picker" (iOS'un UIPickerView'ı gibi tık tık
// oturan kaydırmalı tekerlek) bileşeni yok. Kütüphane kullanmama kararımız
// gereği 3. parti bir kütüphane eklemeden, resmi
// androidx.compose.foundation.gestures.snapping API'siyle kuruyoruz.
// İlk olarak Takvim ekranının ay/yıl seçicisi (bkz. ui/task/MonthYearWheelPicker.kt)
// için yazıldı; Odak Modu'nun saat/dakika/saniye seçicisi de (elle
// piksel matematiğiyle sürüklemeye dayanan, buglu eski hali kaldırılarak)
// buna geçince ikinci kullanım ortaya çıktı, o yüzden paylaşılan bir
// bileşene çıkarıldı (bkz. rehber/2026-08-09-odak-modu-sure-secici-wheelpicker.md).
private val WHEEL_DEFAULT_ITEM_HEIGHT = 44.dp
private const val WHEEL_DEFAULT_VISIBLE_ROWS = 5

// loop=true iken gerçekte SONSUZ bir liste tutmuyoruz, gerçek öğe
// sayısının bu kadar KATI kadar "sanal" bir liste kullanıp her sanal
// index'i (sanalIndex % gerçekSayı) ile gerçek değere çeviriyoruz.
// Kullanıcı pratikte tek bir oturumda 1000 turun ucuna asla varamayacağı
// için sınır hiç hissedilmiyor, saat/dakika/saniye gerçek bir kadran
// gibi kesintisiz sarıyor.
private const val LOOP_REPEAT_COUNT = 1000

/**
 * Tek bir kaydırmalı tekerlek sütunu, sayı/etiket listesinden birini
 * seçtirir. [loop] false ise (Takvim'in ay/yıl seçicisi gibi) liste
 * sınırlı ve uçlarda durur; true ise (Odak Modu'nun saat/dakika/saniye
 * seçicisi gibi) liste dairesel sarar.
 */
@Composable
fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    loop: Boolean = false,
    itemHeight: Dp = WHEEL_DEFAULT_ITEM_HEIGHT,
    visibleRows: Int = WHEEL_DEFAULT_VISIBLE_ROWS,
    selectedTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    unselectedTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = LocalContentColor.current,
    dividerColor: Color = MaterialTheme.colorScheme.outlineVariant,
) {
    val realCount = items.size
    val virtualCount = if (loop) realCount * LOOP_REPEAT_COUNT else realCount
    // virtualCount/2, realCount'un tam katı olmayabilir (ör. LOOP_REPEAT_COUNT
    // tek sayı olsaydı) kalanı çıkarıp en yakın "tur başlangıcı"na
    // yuvarlıyoruz, sonra seçili index'i ekliyoruz.
    val initialVirtualIndex = if (loop) {
        (virtualCount / 2) - (virtualCount / 2) % realCount + selectedIndex
    } else {
        selectedIndex
    }

    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(listState)
    val coroutineScope = rememberCoroutineScope()

    // Pencere ilk açıldığında (ya da hangi değerle açıldıysak) o satırı
    // baştan ORTAYA getiriyoruz. "items" referansı bu picker'ın ömrü
    // boyunca SABİT kaldığı için (remember ile üretiliyor) bu sadece
    // İLK açılışta çalışıyor, kullanıcı kaydırdıkça selectedIndex prop'u
    // değişse bile burası TEKRAR tetiklenip parmağın altındaki tekerleği
    // geri sıçratmıyor.
    LaunchedEffect(items) {
        listState.scrollToItem(initialVirtualIndex)
    }

    // Kaydırma/fling durunca (parmak kalktı VE tekerlek yerine oturdu)
    // ekranın merkezine en yakın satırı "seçili" ilan ediyoruz,
    // SnapFlingBehavior o satırı zaten TAM ortaya hizalayarak durduruyor,
    // biz sadece HANGİ index olduğunu okuyup dışarı bildiriyoruz.
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            val centeredItem = layoutInfo.visibleItemsInfo.minByOrNull { item ->
                abs((item.offset + item.size / 2) - viewportCenter)
            }
            if (centeredItem != null) {
                val newRealIndex = centeredItem.index % realCount
                if (newRealIndex != selectedIndex) {
                    onSelectedIndexChange(newRealIndex)
                }
            }
        }
    }

    Box(
        modifier = modifier.height(itemHeight * visibleRows),
        contentAlignment = Alignment.Center,
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight * (visibleRows / 2)),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(count = virtualCount) { virtualIndex ->
                val realIndex = virtualIndex % realCount
                val isSelected = realIndex == selectedIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        // Büyük stiller (ör. Odak Modu'nun displayLarge merkez
                        // rakamı) satır yüksekliğinden taşabiliyordu, rakam
                        // iki çizgi arasına sığmayıp komşu satırların üstüne
                        // bulaşıyordu (bkz. kullanıcının paylaştığı ekran
                        // görüntüsü). clipToBounds, satırın DIŞINA taşan her
                        // ne olursa olsun bu Box'ın sınırlarında kesiyor,
                        // metin büyük sistem yazı tipi ölçeğinde bile komşu
                        // satırlara asla bulaşmıyor.
                        .clipToBounds()
                        .clickable {
                            coroutineScope.launch { listState.animateScrollToItem(virtualIndex) }
                            onSelectedIndexChange(realIndex)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = items[realIndex],
                        style = if (isSelected) selectedTextStyle else unselectedTextStyle,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) selectedColor else unselectedColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        // iOS tarzı tekerleğin imzası: ortadaki satırı çerçeveleyen iki
        // ince yatay çizgi, kullanıcı hangi satırın "seçili" sayılacağını
        // parmağını kaldırmadan bile görebiliyor.
        HorizontalDivider(
            color = dividerColor,
            modifier = Modifier.align(Alignment.Center).offset(y = -(itemHeight / 2)),
        )
        HorizontalDivider(
            color = dividerColor,
            modifier = Modifier.align(Alignment.Center).offset(y = (itemHeight / 2)),
        )
    }
}
