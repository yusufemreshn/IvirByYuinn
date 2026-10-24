package com.yusufemre.ivirbyyuinn.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Faz 10a'dan önce menü/tema/sürükleme/geri silme ikonları düz metin
// glifleriydi ("☰", "☀️"/"🌙"/"🌓", "⋮⋮", "⌫"), pratikti (material-icons-core
// paketinde karşılıkları yoktu ya da hiç aranmamıştı) ama markanın kendine ait
// bir çizgisi yoktu. Burada, uygulama ikonu ve HomeScreen'deki mascot'la aynı
// dilde (yuvarlak uçlu çizgiler, sade geometrik şekiller) elden çizilmiş
// ImageVector'lar var: yeni bir ikon paketi bağımlılığı YOK, hepsi path
// komutlarıyla burada tanımlanıyor. Icon() composable'ı normalde olduğu gibi
// tint parametresiyle bu şekillerin rengini boyuyor, o yüzden path'lerin
// kendi rengi önemli değil (SolidColor(Color.Black) sadece bir yer tutucu).
object IvirIcons {
    val Menu: ImageVector by lazy(LazyThreadSafetyMode.NONE) { buildMenuIcon() }
    val Sun: ImageVector by lazy(LazyThreadSafetyMode.NONE) { buildSunIcon() }
    val Moon: ImageVector by lazy(LazyThreadSafetyMode.NONE) { buildMoonIcon() }
    val Auto: ImageVector by lazy(LazyThreadSafetyMode.NONE) { buildAutoIcon() }
    val DragHandle: ImageVector by lazy(LazyThreadSafetyMode.NONE) { buildDragHandleIcon() }
    val Backspace: ImageVector by lazy(LazyThreadSafetyMode.NONE) { buildBackspaceIcon() }
    val Flame: ImageVector by lazy(LazyThreadSafetyMode.NONE) { buildFlameIcon() }
    val Today: ImageVector by lazy(LazyThreadSafetyMode.NONE) { buildTodayIcon() }
    val Upcoming: ImageVector by lazy(LazyThreadSafetyMode.NONE) { buildUpcomingIcon() }
    val Mic: ImageVector by lazy(LazyThreadSafetyMode.NONE) { buildMicIcon() }
    val Timer: ImageVector by lazy(LazyThreadSafetyMode.NONE) { buildTimerIcon() }
    val Archive: ImageVector by lazy(LazyThreadSafetyMode.NONE) { buildArchiveIcon() }
    // Ayarlar menüsünün 6 kategori satırı için, Archive/Today gibi AYNI
    // "yeni ikon paketi yok, path'leri elle çiz" kararı.
    val CloudBackup: ImageVector by lazy(LazyThreadSafetyMode.NONE) { buildCloudBackupIcon() }
    val Palette: ImageVector by lazy(LazyThreadSafetyMode.NONE) { buildPaletteIcon() }
    val Toggle: ImageVector by lazy(LazyThreadSafetyMode.NONE) { buildToggleIcon() }
    val Bell: ImageVector by lazy(LazyThreadSafetyMode.NONE) { buildBellIcon() }
    val Shield: ImageVector by lazy(LazyThreadSafetyMode.NONE) { buildShieldIcon() }
    val Info: ImageVector by lazy(LazyThreadSafetyMode.NONE) { buildInfoIcon() }
    val Check: ImageVector by lazy(LazyThreadSafetyMode.NONE) { buildCheckIcon() }
}

// Bir çemberi, dört adet 90 derecelik yaydan örüyoruz (saat 3 -> 6 -> 9 -> 12
// -> 3). Tek bir 360 derecelik ya da iki adet 180 derecelik yay yerine dördü
// seçtik çünkü arcTo'nun "isMoreThanHalf" bayrağı tam yarımda/tamamında
// belirsizleşiyor: 90 derece her zaman net.
private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
    moveTo(cx + r, cy)
    arcTo(r, r, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = cx, y1 = cy + r)
    arcTo(r, r, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = cx - r, y1 = cy)
    arcTo(r, r, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = cx, y1 = cy - r)
    arcTo(r, r, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = cx + r, y1 = cy)
    close()
}

// Auto ikonundaki dolu yarım daire için circle()'ın orta iki çeyreğini
// (saat 12 -> 9 -> 6) kullanıyoruz ama TERS yönde geziyoruz (circle()
// saat 9->12 ve 6->9 yönünde ilerliyordu). Bir yayı ters yönde çizmek
// AYNI eğriyi vermiyor otomatik olarak: arcTo'nun "isPositiveArc"
// (sweep) bayrağı yön-bağımlı, o yüzden burada circle()'daki karşılık
// gelen çeyreklere göre TERS çevirdik (true yerine false). İlk denemede
// bunu unutup aynı `true` değeriyle yazmıştım, sonuç bir yarım daire
// değil üçgene benzer bozuk bir şekildi, bkz. rehber notları.
private fun PathBuilder.leftHalfDisc(cx: Float, cy: Float, r: Float) {
    moveTo(cx, cy - r)
    arcTo(r, r, 0f, isMoreThanHalf = false, isPositiveArc = false, x1 = cx - r, y1 = cy)
    arcTo(r, r, 0f, isMoreThanHalf = false, isPositiveArc = false, x1 = cx, y1 = cy + r)
    close()
}

private fun buildMenuIcon(): ImageVector =
    ImageVector.Builder(
        name = "Ivir.Menu",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Üç yuvarlak uçlu çizgi: TaskListScreen'in eski "☰" glifinin yerini
        // alıyor, drawer'ı açan hamburger buton.
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.2f,
            strokeLineCap = StrokeCap.Round,
        ) {
            moveTo(4.5f, 6.5f); lineTo(19.5f, 6.5f)
            moveTo(4.5f, 12f); lineTo(19.5f, 12f)
            moveTo(4.5f, 17.5f); lineTo(19.5f, 17.5f)
        }
    }.build()

private fun buildSunIcon(): ImageVector =
    ImageVector.Builder(
        name = "Ivir.Sun",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Orta disk...
        path(fill = SolidColor(Color.Black)) { circle(12f, 12f, 4.2f) }
        // ...ve etrafında 8 ışın. Açıları elle hesaplamak yerine bir döngüde
        // trigonometriyle buluyoruz, 45 derece aralıklarla.
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
        ) {
            val innerR = 6.4f
            val outerR = 9.4f
            for (i in 0 until 8) {
                val angle = Math.toRadians(i * 45.0)
                val cosA = cos(angle).toFloat()
                val sinA = sin(angle).toFloat()
                moveTo(12f + innerR * cosA, 12f + innerR * sinA)
                lineTo(12f + outerR * cosA, 12f + outerR * sinA)
            }
        }
    }.build()

private fun buildMoonIcon(): ImageVector =
    ImageVector.Builder(
        name = "Ivir.Moon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Hilal: büyük bir daireden, onun İÇİNDE tamamen kalan (dıştan
        // taşmayan) daha küçük bir daireyi EvenOdd dolgu kuralıyla
        // "çıkarıyoruz": iki dairenin kesişmediği/taşmadığı, küçüğün
        // tamamen büyüğün içinde kaldığı bir yerleşim seçtik ki taşan bir
        // parça yanlışlıkla dolgulanmasın.
        path(fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd) {
            circle(12f, 12f, 8.5f)
            circle(14.3f, 12f, 6.2f)
        }
    }.build()

private fun buildAutoIcon(): ImageVector =
    ImageVector.Builder(
        name = "Ivir.Auto",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // "Sistemi takip et": yarısı dolu yarısı sadece anahat bir daire,
        // güneş/ay ikilisinin ortasını temsil ediyor.
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f) {
            circle(12f, 12f, 8.5f)
        }
        path(fill = SolidColor(Color.Black)) {
            leftHalfDisc(12f, 12f, 8.5f)
        }
    }.build()

private fun buildDragHandleIcon(): ImageVector =
    ImageVector.Builder(
        name = "Ivir.DragHandle",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // İki sütun, üçer nokta: TaskRow'daki eski "⋮⋮" glifinin yerini
        // alıyor, sürükleme tutamacı.
        path(fill = SolidColor(Color.Black)) {
            val dotR = 1.5f
            for (x in listOf(9f, 15f)) {
                for (y in listOf(6f, 12f, 18f)) {
                    circle(x, y, dotR)
                }
            }
        }
    }.build()

private fun buildBackspaceIcon(): ImageVector =
    ImageVector.Builder(
        name = "Ivir.Backspace",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Sola sivrilen bir çerçeve + içinde bir "X": PinPad'deki eski "⌫"
        // glifinin yerini alan klasik geri silme simgesi.
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(3f, 12f)
            lineTo(8f, 5.5f)
            lineTo(20f, 5.5f)
            lineTo(20f, 18.5f)
            lineTo(8f, 18.5f)
            close()
            moveTo(11f, 9f); lineTo(16f, 15f)
            moveTo(16f, 9f); lineTo(11f, 15f)
        }
    }.build()

private fun buildTodayIcon(): ImageVector =
    ImageVector.Builder(
        name = "Ivir.Today",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Faz 12a: akıllı liste "Bugün": ince bir dış halka, ortada dolu
        // küçük bir nokta. "Şu an, tam burada" hissi veren sade bir hedef
        // şekli; Sun ikonundaki ışınlarla karışmasın diye bilerek çok daha
        // sade tutuldu.
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f) {
            circle(12f, 12f, 8.5f)
        }
        path(fill = SolidColor(Color.Black)) {
            circle(12f, 12f, 3f)
        }
    }.build()

private fun buildUpcomingIcon(): ImageVector =
    ImageVector.Builder(
        name = "Ivir.Upcoming",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Faz 12a: akıllı liste "Yaklaşan": sağa uzanan bir çizgi + ok başı,
        // zamanın ileriye doğru aktığını çağrıştıran sade bir ok motifi.
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(5f, 12f); lineTo(15f, 12f)
            moveTo(11f, 7.5f); lineTo(15.5f, 12f); lineTo(11f, 16.5f)
        }
    }.build()

private fun buildMicIcon(): ImageVector =
    ImageVector.Builder(
        name = "Ivir.Mic",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Faz 12c: sesli komutla görev ekleme butonu. Kapsül gövde: bir
        // dikdörtgen + üstte/altta birer tam daire (zaten kanıtlanmış
        // circle() ile), nonzero dolgu kuralıyla birleşip pürüzsüz bir
        // kapsül oluşturuyor, yeni bir yay-yönü riski almadan (bkz.
        // leftHalfDisc'in yorumu, o dersi burada tekrarlamıyoruz).
        path(fill = SolidColor(Color.Black)) {
            moveTo(9f, 5f); lineTo(15f, 5f); lineTo(15f, 11f); lineTo(9f, 11f); close()
            circle(12f, 5f, 3f)
            circle(12f, 11f, 3f)
        }
        // Sap + taban.
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
        ) {
            moveTo(12f, 14f); lineTo(12f, 19f)
            moveTo(8.5f, 19f); lineTo(15.5f, 19f)
        }
    }.build()

private fun buildTimerIcon(): ImageVector =
    ImageVector.Builder(
        name = "Ivir.Timer",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Faz 12c: odak modu (Pomodoro) drawer girdisi: kronometre gövdesi,
        // ince bir dış halka (circle() ile, zaten kanıtlanmış).
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.6f) {
            circle(12f, 13f, 7f)
        }
        // Üstteki düğme + gövdeye bağlayan sap + iki kol (akrep/yelkovan):
        // "Bugün"ün tek noktasından ve "Yaklaşan"ın okundan görsel olarak
        // ayrışsın diye bilerek iki kollu bir saat yüzü.
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
        ) {
            moveTo(9.5f, 2.5f); lineTo(14.5f, 2.5f)
            moveTo(12f, 2.5f); lineTo(12f, 6f)
            moveTo(12f, 13f); lineTo(12f, 9f)
            moveTo(12f, 13f); lineTo(15f, 14.5f)
        }
    }.build()

private fun buildArchiveIcon(): ImageVector =
    ImageVector.Builder(
        name = "Ivir.Archive",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // 2026-08-11: görev arşivi, TrashScreen'in EmptyBinIllustration'ındaki
        // çöp kutusuyla (kapak+tutamaç+dikey yivler) KARIŞMASIN diye bilerek
        // farklı bir motif: bir kutuya/tepsiye İNEN bir ok, "bunu kaldır"
        // değil "bunu sakla" hissi versin diye.
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            // İnen ok: sap + ok başı.
            moveTo(12f, 3f); lineTo(12f, 12f)
            moveTo(8.5f, 8.5f); lineTo(12f, 12f); lineTo(15.5f, 8.5f)
            // Tepsi: üst ağız çizgisi + yamuk gövde.
            moveTo(4f, 13f); lineTo(20f, 13f)
            moveTo(4f, 13f); lineTo(5f, 19f); lineTo(19f, 19f); lineTo(20f, 13f)
        }
    }.build()

private fun buildCloudBackupIcon(): ImageVector =
    ImageVector.Builder(
        name = "Ivir.CloudBackup",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Üç üst üste binen daire + düz bir taban: klasik bulut silüeti,
        // nonzero dolgu kuralıyla tek bir parça olarak birleşiyor.
        path(fill = SolidColor(Color.Black)) {
            circle(9.5f, 12f, 3.6f)
            circle(14f, 11f, 4.2f)
            moveTo(6.5f, 12.5f); lineTo(17.5f, 12.5f); lineTo(17.5f, 16.5f); lineTo(6.5f, 16.5f); close()
        }
        // Buluta inen küçük bir ok: "yedekleniyor/iniyor" hissi.
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(12f, 6f); lineTo(12f, 13.5f)
            moveTo(9.5f, 11f); lineTo(12f, 13.5f); lineTo(14.5f, 11f)
        }
    }.build()

private fun buildPaletteIcon(): ImageVector =
    ImageVector.Builder(
        name = "Ivir.Palette",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Ressam paleti: dış anahat + içinde üç küçük dolu "boya" noktası,
        // Renk Teması bölümünün (23 renkli daire ızgarası) özetini taşıyor.
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.7f) {
            circle(12f, 12.5f, 8f)
        }
        path(fill = SolidColor(Color.Black)) {
            circle(9f, 10f, 1.6f)
            circle(14.5f, 9.5f, 1.6f)
            circle(15.5f, 14.5f, 1.6f)
        }
    }.build()

private fun buildToggleIcon(): ImageVector =
    ImageVector.Builder(
        name = "Ivir.Toggle",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Yatay bir "stadyum" anahat (iki yarım daire + iki düz kenar) +
        // sağ tarafta dolu bir daire: bir switch'in "açık" haliyle AYNI
        // motif, Ayarlar'daki onay/switch davranışlarını temsil ediyor.
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.7f) {
            moveTo(8f, 7f)
            arcTo(5f, 5f, 0f, isMoreThanHalf = true, isPositiveArc = false, x1 = 8f, y1 = 17f)
            lineTo(16f, 17f)
            arcTo(5f, 5f, 0f, isMoreThanHalf = true, isPositiveArc = false, x1 = 16f, y1 = 7f)
            close()
        }
        path(fill = SolidColor(Color.Black)) {
            circle(16f, 12f, 3.2f)
        }
    }.build()

private fun buildBellIcon(): ImageVector =
    ImageVector.Builder(
        name = "Ivir.Bell",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Klasik zil: kubbe + iki yan kenar + taban çizgisi, altında küçük
        // bir tokmak (dolu daire).
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.7f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(7f, 15f)
            lineTo(7f, 11f)
            curveTo(7f, 7.7f, 9.2f, 5.5f, 12f, 5.5f)
            curveTo(14.8f, 5.5f, 17f, 7.7f, 17f, 11f)
            lineTo(17f, 15f)
            moveTo(6f, 15f); lineTo(18f, 15f)
        }
        path(fill = SolidColor(Color.Black)) {
            circle(12f, 18f, 1.6f)
        }
    }.build()

private fun buildShieldIcon(): ImageVector =
    ImageVector.Builder(
        name = "Ivir.Shield",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Üstten düz, alta doğru sivrilen klasik kalkan anahatı: Uygulama
        // Kilidi'nin (Güvenlik) özetini taşıyor.
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.7f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(12f, 3f)
            lineTo(18.5f, 5.5f)
            lineTo(18.5f, 11f)
            curveTo(18.5f, 15.5f, 15.6f, 18.7f, 12f, 20f)
            curveTo(8.4f, 18.7f, 5.5f, 15.5f, 5.5f, 11f)
            lineTo(5.5f, 5.5f)
            close()
        }
    }.build()

private fun buildInfoIcon(): ImageVector =
    ImageVector.Builder(
        name = "Ivir.Info",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Today ikonuyla (dış halka) AYNI temel, ama ortadaki tek nokta
        // yerine küçük bir "i" harfi: nokta + altında dikey çizgi.
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.7f) {
            circle(12f, 12f, 8.5f)
        }
        path(fill = SolidColor(Color.Black)) {
            circle(12f, 8.7f, 1.3f)
        }
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
        ) {
            moveTo(12f, 11.7f); lineTo(12f, 16f)
        }
    }.build()

private fun buildCheckIcon(): ImageVector =
    ImageVector.Builder(
        name = "Ivir.Check",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Ayar değişikliği teyit snackbar'ı için: tek bir kırık çizgi,
        // diğer ikonlardaki yuvarlak uçlu/köşeli çizgi diliyle aynı.
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(5f, 12.5f); lineTo(9.5f, 17f); lineTo(19f, 7f)
        }
    }.build()

private fun buildFlameIcon(): ImageVector =
    ImageVector.Builder(
        name = "Ivir.Flame",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Faz 11: StatsScreen'deki streak sayısının yanına, kübik Bezier
        // eğrileriyle (curveTo) elden çizilmiş bir alev silueti. Tek bir
        // kapalı yol: tepeden aşağı sol kenar, tabanda yuvarlanma, sağ
        // kenardan yukarı, son iki curveTo da alevin ortasındaki klasik
        // "bel" girintisini oluşturuyor.
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 2.5f)
            curveTo(9f, 6f, 6.5f, 9f, 6.5f, 13f)
            curveTo(6.5f, 17.5f, 9f, 21f, 12f, 21f)
            curveTo(15f, 21f, 17.5f, 17.5f, 17.5f, 13f)
            curveTo(17.5f, 10f, 16f, 8f, 14.5f, 6f)
            curveTo(14.8f, 8f, 13.5f, 9f, 12.5f, 8f)
            curveTo(11.5f, 7f, 12.5f, 4f, 12f, 2.5f)
            close()
        }
    }.build()
