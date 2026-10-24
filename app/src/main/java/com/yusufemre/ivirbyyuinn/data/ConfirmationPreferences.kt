package com.yusufemre.ivirbyyuinn.data

// Kullanıcı isteği: görev/alt görev silme ve tamamlama onay diyalogları
// Ayarlar'dan ayrı ayrı kapatılabilsin. Hepsinin varsayılanı true, yani
// bu ayar hiç dokunulmamışsa uygulamanın ÖNCEKİ (her zaman soran) davranışı
// değişmiyor.
//
// 2026-10-25: kapsam, görev/alt görev dışındaki diğer "emin misin"
// diyaloglarını (arşivleme, alt görev ekleme, kategori/liste/çöp kutusu
// silme) da kapsayacak şekilde genişletildi.
data class ConfirmationPreferences(
    val confirmTaskDelete: Boolean = true,
    val confirmTaskComplete: Boolean = true,
    val confirmTaskArchive: Boolean = true,
    val confirmSubtaskDelete: Boolean = true,
    val confirmSubtaskComplete: Boolean = true,
    val confirmSubtaskAdd: Boolean = true,
    val confirmCategoryDelete: Boolean = true,
    val confirmListDelete: Boolean = true,
    val confirmArchivedTaskDelete: Boolean = true,
    val confirmTrashDelete: Boolean = true,
)
