package com.yusufemre.ivirbyyuinn.domain

// Haftalık grafiğin tek bir çubuğu: bir günün başlangıcı (etiket üretmek
// için, ör. "Sal") ve o gün tamamlanan görev sayısı.
data class DailyCompletionCount(
    val dayStartMillis: Long,
    val count: Int,
)
