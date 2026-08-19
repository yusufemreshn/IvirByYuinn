// Bu, projenin en tepesindeki build dosyası. Buraya gerçek bir kod yazmıyoruz,
// sadece app modülünün kullanacağı Gradle eklentilerini burada "tanıtıyoruz"
// (apply false demek: "bu eklentiyi burada uygulama, sadece versiyonunu bildir").
//
// Not: AGP 9.0'dan beri Kotlin desteği AGP'nin içine gömülü geldiği için
// artık ayrıca "org.jetbrains.kotlin.android" eklentisine ihtiyaç yok
// (bunu build sırasında bir hatadan öğrendik). Compose derleyicisi hâlâ
// ayrı bir eklenti olarak kalıyor.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // Room'un derleme zamanında DAO implementasyonlarını üretmesi için gerekli
    // (Google/JetBrains'in resmi annotation-processing aracı, bkz. plan/faz1-cekirdek.md).
    alias(libs.plugins.ksp) apply false
}
