package com.yusufemre.ivirbyyuinn.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

// Faz 10a'ya kadar her animasyon (animateItem, animateColorAsState,
// AnimatedContent) kendi süresini/eğrisini kendi yazdığı yerde seçiyordu:
// bir yerde 150ms, başka bir yerde varsayılan 300ms gibi. Bu dosya artık
// "Ivır'ın hareket dili nasıl hissettiriyor" sorusuna TEK bir cevap veriyor,
// yeni eklenen her animasyon buradaki sabitleri kullanacak.
object Motion {
    // Checkbox rengi gibi anlık, göz ucuyla algılanan geri bildirimler.
    const val MICRO = 150

    // Bir satırın listeye girmesi/çıkması, kartın "pop" yapması gibi biraz
    // daha belirgin ama yine de gündelik kullanılan hareketler.
    const val STANDARD = 220

    // Dialog/ekran geçişleri: kullanıcı "yeni bir yere gittim" hissetmeli
    // ama beklemek zorunda kalmamalı.
    const val SCREEN = 320

    // AnimatedDialog'un ÇIKIŞ animasyonu için ayrı, daha kısa bir süre.
    // Neden ayrı: AnimatedDialog'un altındaki platform Dialog penceresi,
    // çıkış animasyonu oynarken hâlâ ekranın TAMAMI için dokunuşları
    // yutmaya devam ediyor (pencere gerçekten kaldırılana kadar), yani bu
    // süre boyunca kullanıcı arkadaki ekrana dokunamıyor. Kullanıcı
    // geri bildirimi: menü kapanışlarında "animasyon bitse bile bir süre
    // dokunamıyorum" şikayeti tam olarak buydu. Giriş animasyonunun
    // SCREEN kadar sürmesinde sorun yok (zaten yeni ekrana geçerken
    // dokunacak bir şey yok), ama çıkışın daha çabuk bitip pencereyi
    // serbest bırakması gerekiyor, Material'ın kendi rehberliği de
    // çıkışların girişlerden daha hızlı olmasını öneriyor.
    const val SCREEN_EXIT = 160

    // Tüm "pop"/kutlama anlarında kullanılacak tek yaylanma eğrisi. dampingRatio
    // 1'e ne kadar yakınsa o kadar sönük/ciddi olur; 0.7 civarı, abartmadan
    // fark edilen, hafif "canlı" bir his veriyor, Deniz'in toplantıda
    // savunduğu "cesur ama yorucu değil" dengesi.
    fun <T> emphasizedSpring(): SpringSpec<T> = spring(
        dampingRatio = 0.7f,
        stiffness = 380f,
    )
}

// Android'in kendi erişilebilirlik ayarında "Animasyon ölçeği"ni (Geliştirici
// seçenekleri > Pencere/Geçiş/Animatör ölçeği) kapatan kullanıcılar var:
// bu üçü sıfırlanınca sistem "hareket beni rahatsız ediyor" diyor demektir.
// Compose'un bunun için resmi, stabil bir composition local'i henüz yok, o
// yüzden ayarı doğrudan Settings.Global'dan okuyoruz. ANIMATOR_DURATION_SCALE
// üçünden sadece biri ama pratikte kullanıcılar bunları tek pakette açıp
// kapatıyor, tek başına yeterli bir sinyal.
@Composable
fun rememberReducedMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember {
        val scale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        )
        scale == 0f
    }
}
