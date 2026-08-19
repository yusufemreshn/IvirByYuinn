package com.yusufemre.ivirbyyuinn.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.data.AppColorTheme

// Her tema için elden seçilmiş SADECE iki tohum renk (açık moddaki ve koyu
// moddaki "primary"): geri kalan bütün renk rollerini (surface, outline,
// container'lar, error vb.) Material3'ün kendi `lightColorScheme(primary=
// ...)`/`darkColorScheme(primary=...)` fabrika fonksiyonları otomatik
// dolduruyor. Bu, kütüphane kullanmama kararımızın "elden yaz" ruhuna
// uyuyor ama 22 tema × ~12 renk rolü × 2 mod'u tek tek elle boyamak
// (500'den fazla renk) gerçekçi olmadığı için, Material3'ün RESMİ ve
// belgelenmiş "sadece ana rengi ver, gerisini M3 tamamlasın" desenine
// güveniyoruz: bu bir kısayol değil, Google'ın kendi önerdiği yöntem.
//
// TEAL teması İSTİSNA: o, projenin ilk gününden beri olan, Color.kt'deki
// tam elden ayarlanmış (container'lar, surface, outline dahil) marka
// paleti: hiç dokunulmadı, aynen kaldı.
private data class ThemeSeed(val lightPrimary: Color, val darkPrimary: Color)

private val themeSeeds: Map<AppColorTheme, ThemeSeed> = mapOf(
    AppColorTheme.OCEAN to ThemeSeed(Color(0xFF1B6CA8), Color(0xFF7FC4EE)),
    AppColorTheme.SUNSET to ThemeSeed(Color(0xFFD65A31), Color(0xFFFFB37B)),
    AppColorTheme.FOREST to ThemeSeed(Color(0xFF3B6E3B), Color(0xFFA8D8A8)),
    AppColorTheme.LAVENDER to ThemeSeed(Color(0xFF7C5CBF), Color(0xFFC9B3F5)),
    AppColorTheme.ROSE to ThemeSeed(Color(0xFFC2427A), Color(0xFFF5A3C7)),
    AppColorTheme.MINT to ThemeSeed(Color(0xFF2E8B74), Color(0xFF8FE3CB)),
    AppColorTheme.AMBER to ThemeSeed(Color(0xFFB8860B), Color(0xFFFFD680)),
    AppColorTheme.CORAL to ThemeSeed(Color(0xFFE8735C), Color(0xFFFFB4A2)),
    AppColorTheme.INDIGO to ThemeSeed(Color(0xFF3F51B5), Color(0xFFAEB8F0)),
    AppColorTheme.EMERALD to ThemeSeed(Color(0xFF1F8A5E), Color(0xFF7EE6B8)),
    AppColorTheme.PLUM to ThemeSeed(Color(0xFF8E4585), Color(0xFFE0A8DA)),
    AppColorTheme.SLATE to ThemeSeed(Color(0xFF46607A), Color(0xFFA9C1D9)),
    AppColorTheme.PEACH to ThemeSeed(Color(0xFFD98756), Color(0xFFFFCBA4)),
    AppColorTheme.SKY to ThemeSeed(Color(0xFF2E93C7), Color(0xFFA8DDF7)),
    AppColorTheme.RUBY to ThemeSeed(Color(0xFFA32638), Color(0xFFF49AA6)),
    AppColorTheme.OLIVE to ThemeSeed(Color(0xFF6B7A2E), Color(0xFFC6D68A)),
    AppColorTheme.MOCHA to ThemeSeed(Color(0xFF7A5230), Color(0xFFD9B58C)),
    AppColorTheme.LILAC to ThemeSeed(Color(0xFF9A6FC2), Color(0xFFD8BFF0)),
    AppColorTheme.CHERRY_BLOSSOM to ThemeSeed(Color(0xFFD65E8E), Color(0xFFFBC1DA)),
    AppColorTheme.CHARCOAL to ThemeSeed(Color(0xFF45494D), Color(0xFFC4CACF)),
    AppColorTheme.TURQUOISE to ThemeSeed(Color(0xFF159C9C), Color(0xFF83E5E5)),
)

// Ayarlar ekranındaki renk seçicide her temanın küçük bir yuvarlak
// örneğini (swatch) göstermek için, her zaman AÇIK moddaki tohum
// rengi kullanıyoruz, koyu/açık ne olursa olsun tema tanınabilir kalsın
// diye.
fun AppColorTheme.swatchColor(): Color = when (this) {
    AppColorTheme.DYNAMIC -> Color(0xFF9E9E9E) // Ayarlar ekranında gerçek swatch ayrıca çiziliyor, bu sadece yedek.
    AppColorTheme.TEAL -> IvirDeepTeal
    else -> themeSeeds.getValue(this).lightPrimary
}

// dynamicColor: Android 12+ olmayan bir cihazda AppColorTheme.DYNAMIC
// seçiliyse, çağıran taraf (IvirTheme) bunu TEAL'e düşürüyor, burası
// sadece "seçiliyse gerçekten uygulanabilir mi" sorusuna bakmıyor.
fun AppColorTheme.toColorScheme(darkTheme: Boolean): ColorScheme {
    if (this == AppColorTheme.TEAL || this == AppColorTheme.DYNAMIC) {
        return if (darkTheme) IvirDarkColorScheme else IvirLightColorScheme
    }
    val seed = themeSeeds.getValue(this)
    return if (darkTheme) {
        darkColorScheme(primary = seed.darkPrimary)
    } else {
        lightColorScheme(primary = seed.lightPrimary)
    }
}

@Composable
fun AppColorTheme.label(): String = when (this) {
    AppColorTheme.DYNAMIC -> stringResource(R.string.color_theme_dynamic)
    AppColorTheme.TEAL -> stringResource(R.string.color_theme_teal)
    AppColorTheme.OCEAN -> stringResource(R.string.color_theme_ocean)
    AppColorTheme.SUNSET -> stringResource(R.string.color_theme_sunset)
    AppColorTheme.FOREST -> stringResource(R.string.color_theme_forest)
    AppColorTheme.LAVENDER -> stringResource(R.string.color_theme_lavender)
    AppColorTheme.ROSE -> stringResource(R.string.color_theme_rose)
    AppColorTheme.MINT -> stringResource(R.string.color_theme_mint)
    AppColorTheme.AMBER -> stringResource(R.string.color_theme_amber)
    AppColorTheme.CORAL -> stringResource(R.string.color_theme_coral)
    AppColorTheme.INDIGO -> stringResource(R.string.color_theme_indigo)
    AppColorTheme.EMERALD -> stringResource(R.string.color_theme_emerald)
    AppColorTheme.PLUM -> stringResource(R.string.color_theme_plum)
    AppColorTheme.SLATE -> stringResource(R.string.color_theme_slate)
    AppColorTheme.PEACH -> stringResource(R.string.color_theme_peach)
    AppColorTheme.SKY -> stringResource(R.string.color_theme_sky)
    AppColorTheme.RUBY -> stringResource(R.string.color_theme_ruby)
    AppColorTheme.OLIVE -> stringResource(R.string.color_theme_olive)
    AppColorTheme.MOCHA -> stringResource(R.string.color_theme_mocha)
    AppColorTheme.LILAC -> stringResource(R.string.color_theme_lilac)
    AppColorTheme.CHERRY_BLOSSOM -> stringResource(R.string.color_theme_cherry_blossom)
    AppColorTheme.CHARCOAL -> stringResource(R.string.color_theme_charcoal)
    AppColorTheme.TURQUOISE -> stringResource(R.string.color_theme_turquoise)
}
