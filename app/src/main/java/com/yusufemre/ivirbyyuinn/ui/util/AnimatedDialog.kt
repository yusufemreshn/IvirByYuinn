package com.yusufemre.ivirbyyuinn.ui.util

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yusufemre.ivirbyyuinn.ui.theme.Motion
import com.yusufemre.ivirbyyuinn.ui.theme.rememberReducedMotionEnabled
import kotlinx.coroutines.delay

// Navigation kütüphanesi kullanmadığımız için (kütüphane kullanmama kararımız gereği,
// Faz 10 UI toplantısında da tekrar konuşuldu) Trash/Calendar/Stats/Categories/
// Settings hâlâ birer Dialog olarak açılıyor, ama Faz 10a'dan önce ÇIPLAK bir
// Dialog'du, sistemin varsayılan (neredeyse anlık) pencere animasyonundan
// başka hiçbir geçiş yoktu. Bu composable, Dialog'un İÇERİĞİNE sağdan kayarak
// gelen/giden kasıtlı bir geçiş ekliyor, Baran'ın toplantıda söylediği gibi,
// artık "üstüne bir kutu bindi" değil, "yeni bir yere gittim" hissi veriyor.
//
// Zor kısım: kullanıcı kapatmak istediğinde (geri tuşu, dışarı tıklama, ya da
// içerideki bir "kapat" butonu) içeriği ANINDA yok edip Dialog'u kapatmak
// yerine önce çıkış animasyonunu oynatmamız, animasyon bitince GERÇEK
// dismiss'i (çağıranın state'ini false yapan lambda) tetiklememiz gerekiyor.
// Bu yüzden "kapatma isteği geldi mi" (dismissRequested) ile "içerik hâlâ
// görünür mü" (visible) durumlarını ayrı tutuyoruz; ikisi arasındaki gecikme,
// tam çıkış animasyonu kadar sürüyor.
//
// `content` bir dismiss lambda'sı ALIYOR: içerideki ekranın kendi "kapat"
// butonu (ör. TrashScreen'in onClose'u) doğrudan çağıranın state'ini değil,
// bu animasyonlu dismiss'i tetiklemeli. Böylece geri tuşu, dışarı tıklama ve
// ekran içi kapat butonu AYNI çıkış animasyonundan geçiyor, Dialog'un kendi
// `onDismissRequest`'ini de doğrudan `dismiss`'e bağladık, geri tuşuna
// basıldığında Compose'un varsayılan (dismissOnBackPress=true) davranışı
// zaten BİZİM iki aşamalı, animasyonlu dismiss'imizi çağırıyor.
//
// Not: Faz 10b'de burada Android 13+'ın "geri kaydır, önizle" (predictive
// back) jestini de denedik (PredictiveBackHandler ile içeriği kaydırma
// sırasında küçültüp geri jestinde gerçekten kapatmak) ama self-testte bu
// Dialog penceresi içinde geri tuşunun HİÇ bir şeyi tetiklemediği ortaya
// çıktı (dismissOnBackPress=false yapınca ne PredictiveBackHandler'ımız ne
// de Dialog'un kendisi geri tuşuna tepki veriyordu, muhtemelen Compose
// Dialog'un kendi penceresi, PredictiveBackHandler'ın beklediği geri
// çağırım dispatcher'ını bu bağlamda doğru sağlamıyor). Riski/faydayı
// tartıp geri aldık: gerçek predictive back önizlemesi, Navigation-Compose
// gibi kendi geri yığınını yöneten bir çözüm gerektiriyor, bu Faz 10c'nin
// kapsamına daha uygun. Şimdilik geri tuşu MEVCUT (aşağıdaki) animasyonlu
// dismiss ile çalışıyor, sadece parmakla "önizleme" kısmı yok.
//
// 2026-10-24: Yazı Boyutu'nu burada LocalDensity.current okuyup
// CompositionLocalProvider ile Dialog içine geri vermeyi DENEDİK — işe
// yaradı ama YENİ bir hata açtı: bu, AnimatedDialog'u HER yazı boyutu
// değişiminde recompose ettiriyor, ve iç içe açık iki Dialog (ör. Ayarlar
// açıkken içinde Görünüm de açık) AYNI ANDA recompose olunca Compose'un
// Dialog altyapısı dıştaki pencerenin içerik View'ını (ve onun remember'
// larını, ör. SettingsScreen'in showAppearance'ı) sıfırlayıp yeniden
// kuruyordu — kullanıcı "Ayarlar listesine fırlatılıyorum" olarak
// gördü. Çözüm artık burada DEĞİL: Yazı Boyutu, MainActivity.
// attachBaseContext'te Configuration.fontScale üzerinden uygulanıyor, bu
// sayede HER Window (her Dialog dahil) kendi density'sini native olarak
// doğru hesaplıyor, burada elle bir şey yapmaya gerek kalmıyor.
@Composable
fun AnimatedDialog(
    onDismissRequest: () -> Unit,
    content: @Composable (dismiss: () -> Unit) -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    var dismissRequested by remember { mutableStateOf(false) }
    val reducedMotion = rememberReducedMotionEnabled()
    val enterDuration = if (reducedMotion) 0 else Motion.SCREEN
    // Çıkış girişten daha kısa, bkz. Motion.SCREEN_EXIT'teki yorum: altındaki
    // Dialog penceresi bu süre boyunca ekranın tamamı için dokunuşları
    // yutmaya devam ediyor, o yüzden ne kadar kısaysa kullanıcı o kadar
    // çabuk arkadaki ekrana dokunabiliyor.
    val exitDuration = if (reducedMotion) 0 else Motion.SCREEN_EXIT

    // Dialog ekrana ilk geldiğinde girişi tetikliyoruz, bir sonraki
    // recomposition'da visible=true olunca AnimatedVisibility giriş
    // animasyonunu oynatıyor.
    LaunchedEffect(Unit) { visible = true }

    val dismiss: () -> Unit = { dismissRequested = true }

    // dismissRequested true olunca: önce visible=false (çıkış animasyonu
    // başlar), çıkış animasyonu kadar bekle, SONRA çağıranın gerçek
    // onDismissRequest'ini çağır (Dialog'u composition'dan tamamen kaldırır,
    // pencere ancak o an gerçekten kayboluyor).
    LaunchedEffect(dismissRequested) {
        if (dismissRequested) {
            visible = false
            delay(exitDuration.toLong())
            onDismissRequest()
        }
    }

    Dialog(
        onDismissRequest = dismiss,
        // Kullanıcı geri bildirimi: Yazı Boyutu değiştirilince (sadece o
        // anda, içerik ölçüleri AYNI dokunuşun ortasında değişince) bu
        // dialog bazen kendiliğinden kapanıyordu. Sebebi: içerik boyutu
        // tam dokunuş sırasında büyüyüp/küçülünce, Dialog'un varsayılan
        // "dışına tıklandı" algılayıcısı bunu yanlışlıkla dışarı tıklama
        // sanıyordu. Bu ekranların hepsi zaten TAM EKRAN (dışarısı diye
        // bir şey yok, TopAppBar'daki geri okuyla kapatılıyor), o yüzden
        // dismissOnClickOutside'ı güvenle kapatabiliyoruz; geri tuşu/jesti
        // (dismissOnBackPress, varsayılan açık) hâlâ çalışmaya devam ediyor.
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false),
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(enterDuration)) + slideInHorizontally(tween(enterDuration)) { fullWidth -> fullWidth / 6 },
            exit = fadeOut(tween(exitDuration)) + slideOutHorizontally(tween(exitDuration)) { fullWidth -> fullWidth / 6 },
        ) {
            content(dismiss)
        }
    }
}
