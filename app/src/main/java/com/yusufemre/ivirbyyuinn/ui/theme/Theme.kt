package com.yusufemre.ivirbyyuinn.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.yusufemre.ivirbyyuinn.data.AppColorTheme

// "internal" (private değil), AppColorThemePalettes.kt'nin TEAL/DYNAMIC
// seçiliyken bu hazır, tam elden ayarlanmış paletleri kullanabilmesi için.
internal val IvirLightColorScheme = lightColorScheme(
    primary = IvirDeepTeal,
    primaryContainer = IvirDeepTealContainer,
    secondary = IvirSunsetCoral,
    secondaryContainer = IvirCoralContainer,
    background = IvirSoftCream,
    surface = IvirSoftCream,
    onPrimary = IvirSoftCream,
    onSecondary = IvirInkSlate,
    onBackground = IvirInkSlate,
    onSurface = IvirInkSlate,
    outline = IvirOutline,
)

internal val IvirDarkColorScheme = darkColorScheme(
    primary = IvirMintGlow,
    primaryContainer = IvirMintGlowContainer,
    secondary = IvirCoralGlow,
    secondaryContainer = IvirCoralGlowContainer,
    background = IvirCharcoal,
    surface = IvirCharcoal,
    onPrimary = IvirCharcoal,
    onSecondary = IvirCharcoal,
    onBackground = IvirMistWhite,
    onSurface = IvirMistWhite,
    outline = IvirMutedSlate,
)

@Composable
fun IvirTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Faz 9: eskiden düz bir "dynamicColor: Boolean" parametresiydi,
    // artık kullanıcının Ayarlar'dan seçtiği 20+ renk temasından biri
    // (DYNAMIC de bunlardan biri, Android 12+ cihazlarda duvar
    // kağıdından türetilen renkler, roadmap'te Zeynep'in önerisiydi).
    colorTheme: AppColorTheme = AppColorTheme.TEAL,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (colorTheme == AppColorTheme.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        // DYNAMIC seçili ama cihaz Android 12'den eskiyse, toColorScheme
        // zaten TEAL'e düşüyor (bkz. AppColorThemePalettes.kt).
        colorTheme.toColorScheme(darkTheme)
    }

    // MainActivity zaten enableEdgeToEdge() çağırdığı için status bar'ı elle
    // boyamıyoruz (o artık deprecated ve edge-to-edge ile birlikte gereksiz);
    // sadece üstteki saat/pil ikonlarının koyu mu açık mı görüneceğini,
    // temaya göre ayarlıyoruz ki koyu temada ikonlar kaybolmasın.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // 2026-10-24: Yazı Boyutu artık BURADA uygulanmıyor. Önceki yaklaşım
    // (LocalDensity.current'ı okuyup CompositionLocalProvider ile
    // ölçeklenmiş halini content'e sağlamak) ana ekranlar için çalışıyordu
    // ama her Dialog KENDİ Window'unu (dolayısıyla kendi LocalDensity'sini)
    // açtığı için Ayarlar gibi Dialog içindeki ekranlara hiç ulaşmıyordu.
    // Bunu AnimatedDialog.kt içinde CompositionLocalProvider ile elle
    // düzeltmeyi DENEDİK ama bu, iç içe iki Dialog (ör. Ayarlar açıkken
    // içinde Görünüm de açık) AYNI ANDA yazı boyutu değişince recompose
    // olduğunda Compose'un dıştaki Dialog'un içerik View'ını sıfırlayıp
    // yeniden kurmasına (ve SettingsScreen'in "hangi alt ekrandayım"
    // durumunun kaybolup Ayarlar listesine fırlatılmasına) yol açtı. Artık
    // Yazı Boyutu, MainActivity.attachBaseContext'te Configuration.
    // fontScale üzerinden uygulanıyor: bu HER Window'u (her Dialog dahil)
    // native olarak doğru ölçekliyor, burada CompositionLocalProvider'a
    // hiç gerek kalmıyor.
    MaterialTheme(
        // Faz 10b: 23 tema arasında (ya da açık/koyu arasında) geçiş artık
        // anlık "çat" diye değil, her rengin kendi başına yumuşakça
        // kayması ile oluyor, bkz. rememberAnimatedColorScheme.
        colorScheme = rememberAnimatedColorScheme(colorScheme, rememberReducedMotionEnabled()),
        typography = Typography,
        shapes = IvirShapes,
        content = content,
    )
}

// Bir ColorScheme'in TÜMÜNÜ (33 rengin hepsini) anında yeni değerlere
// atlamak yerine, her birini kendi animateColorAsState'i ile yumuşakça
// kaydırıyoruz. Crossfade ile TÜM ekran ağacını (bütün TaskListScreen,
// ViewModel'ler dahil) iki kere composable etmek çok daha pahalı olurdu:
// bu yaklaşım içeriği SADECE BİR KERE composable ediyor, ona sadece
// kare kare değişen bir renk paleti besliyoruz. reducedMotion açıksa
// hiç animasyon yapmadan doğrudan hedef rengi döndürüyoruz.
// target.copy(...) kullanıyoruz, ColorScheme(...) constructor'ını DEĞİL:
// yeni Material3 sürümleri zaman zaman ColorScheme'e ek "fixed" tonal
// roller ekliyor ve doğrudan constructor'ı çağırmak o zaman deprecated
// uyarısı veriyor. copy() ise elle saymadığımız her alanı (bu yeni
// roller dahil) target'tan olduğu gibi devralıyor, sadece burada
// listelediğimiz 33 alanı animasyonlu değerle değiştiriyor.
@Composable
private fun rememberAnimatedColorScheme(target: ColorScheme, reducedMotion: Boolean): ColorScheme = target.copy(
    primary = animatedColor(target.primary, reducedMotion),
    onPrimary = animatedColor(target.onPrimary, reducedMotion),
    primaryContainer = animatedColor(target.primaryContainer, reducedMotion),
    onPrimaryContainer = animatedColor(target.onPrimaryContainer, reducedMotion),
    inversePrimary = animatedColor(target.inversePrimary, reducedMotion),
    secondary = animatedColor(target.secondary, reducedMotion),
    onSecondary = animatedColor(target.onSecondary, reducedMotion),
    secondaryContainer = animatedColor(target.secondaryContainer, reducedMotion),
    onSecondaryContainer = animatedColor(target.onSecondaryContainer, reducedMotion),
    tertiary = animatedColor(target.tertiary, reducedMotion),
    onTertiary = animatedColor(target.onTertiary, reducedMotion),
    tertiaryContainer = animatedColor(target.tertiaryContainer, reducedMotion),
    onTertiaryContainer = animatedColor(target.onTertiaryContainer, reducedMotion),
    background = animatedColor(target.background, reducedMotion),
    onBackground = animatedColor(target.onBackground, reducedMotion),
    surface = animatedColor(target.surface, reducedMotion),
    onSurface = animatedColor(target.onSurface, reducedMotion),
    surfaceVariant = animatedColor(target.surfaceVariant, reducedMotion),
    onSurfaceVariant = animatedColor(target.onSurfaceVariant, reducedMotion),
    surfaceTint = animatedColor(target.surfaceTint, reducedMotion),
    inverseSurface = animatedColor(target.inverseSurface, reducedMotion),
    inverseOnSurface = animatedColor(target.inverseOnSurface, reducedMotion),
    error = animatedColor(target.error, reducedMotion),
    onError = animatedColor(target.onError, reducedMotion),
    errorContainer = animatedColor(target.errorContainer, reducedMotion),
    onErrorContainer = animatedColor(target.onErrorContainer, reducedMotion),
    outline = animatedColor(target.outline, reducedMotion),
    outlineVariant = animatedColor(target.outlineVariant, reducedMotion),
    scrim = animatedColor(target.scrim, reducedMotion),
    surfaceBright = animatedColor(target.surfaceBright, reducedMotion),
    surfaceDim = animatedColor(target.surfaceDim, reducedMotion),
    surfaceContainer = animatedColor(target.surfaceContainer, reducedMotion),
    surfaceContainerHigh = animatedColor(target.surfaceContainerHigh, reducedMotion),
    surfaceContainerHighest = animatedColor(target.surfaceContainerHighest, reducedMotion),
    surfaceContainerLow = animatedColor(target.surfaceContainerLow, reducedMotion),
    surfaceContainerLowest = animatedColor(target.surfaceContainerLowest, reducedMotion),
)

@Composable
private fun animatedColor(target: Color, reducedMotion: Boolean): Color =
    if (reducedMotion) target else animateColorAsState(target, label = "theme-color").value
