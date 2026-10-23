package com.yusufemre.ivirbyyuinn.data

// Kullanıcı isteği: görev/alt görev silme ve tamamlama onay diyalogları
// Ayarlar'dan ayrı ayrı kapatılabilsin. Hepsinin varsayılanı true, yani
// bu ayar hiç dokunulmamışsa uygulamanın ÖNCEKİ (her zaman soran) davranışı
// değişmiyor.
data class ConfirmationPreferences(
    val confirmTaskDelete: Boolean = true,
    val confirmTaskComplete: Boolean = true,
    val confirmSubtaskDelete: Boolean = true,
    val confirmSubtaskComplete: Boolean = true,
)
