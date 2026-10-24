package com.yusufemre.ivirbyyuinn.data

// Uygulama İÇİNDEKİ yazı boyutu tercihi, telefonun sistem geneli
// erişilebilirlik yazı tipi ayarından (Ayarlar > Ekran > Yazı boyutu)
// BAĞIMSIZ, sadece bu uygulamaya özel bir çarpan. İkisi çakışmıyor,
// üst üste biniyor (çarpanın hesaplandığı yer: MainActivity.attachBaseContext).
enum class FontScalePreference {
    SMALL,
    MEDIUM,
    LARGE,
}

// Telefonun kendi erişilebilirlik yazı tipi ayarının (Configuration.fontScale)
// ÜZERİNE binen çarpan, bkz. MainActivity.attachBaseContext.
fun FontScalePreference.scaleFactor(): Float = when (this) {
    FontScalePreference.SMALL -> 0.9f
    FontScalePreference.MEDIUM -> 1f
    FontScalePreference.LARGE -> 1.15f
}
