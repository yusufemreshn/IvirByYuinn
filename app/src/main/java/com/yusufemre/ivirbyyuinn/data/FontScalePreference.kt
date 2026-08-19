package com.yusufemre.ivirbyyuinn.data

// Uygulama İÇİNDEKİ yazı boyutu tercihi, telefonun sistem geneli
// erişilebilirlik yazı tipi ayarından (Ayarlar > Ekran > Yazı boyutu)
// BAĞIMSIZ, sadece bu uygulamaya özel bir çarpan. İkisi çakışmıyor,
// üst üste biniyor (çarpanın hesaplandığı yer: ui/theme/Theme.kt).
enum class FontScalePreference {
    SMALL,
    MEDIUM,
    LARGE,
}
