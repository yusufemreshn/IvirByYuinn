package com.yusufemre.ivirbyyuinn.data

// ThemePreference (SYSTEM/LIGHT/DARK, AÇIK/KOYU seçimi) ile KARIŞTIRMA:
// bu enum "hangi RENK paleti" sorusuna cevap veriyor, o ise "açık mı koyu
// mu" sorusuna. İkisi birbirinden bağımsız, aynı anda ikisi de geçerli
// (ör. "Okyanus" teması + "Koyu" mod bir arada olabiliyor).
//
// Bilerek burada (data/ katmanında) sadece İSİMLER var, gerçek Color
// değerleri yok. Katmanlama kuralımız gereği Compose'un
// Color tipi bir UI kavramı, data/ katmanı UI'ı hiç tanımıyor. Gerçek
// renk paletleri ui/theme/AppColorThemePalettes.kt'de tanımlı.
enum class AppColorTheme {
    // Android 12+ cihazlarda duvar kağıdından türetilen renkler. Eski
    // cihazlarda (API 31 altı) kullanılamıyor, o durumda TEAL'e düşülüyor.
    DYNAMIC,
    TEAL,
    OCEAN,
    SUNSET,
    FOREST,
    LAVENDER,
    ROSE,
    MINT,
    AMBER,
    CORAL,
    INDIGO,
    EMERALD,
    PLUM,
    SLATE,
    PEACH,
    SKY,
    RUBY,
    OLIVE,
    MOCHA,
    LILAC,
    CHERRY_BLOSSOM,
    CHARCOAL,
    TURQUOISE,
}
