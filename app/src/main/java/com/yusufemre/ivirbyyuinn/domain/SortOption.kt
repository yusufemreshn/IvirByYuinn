package com.yusufemre.ivirbyyuinn.domain

// Kullanıcının seçebileceği sıralama seçenekleri. MANUAL, Faz 4c'de
// eklendi, sürükle-bırakla verilen sıra, Task.manualOrder alanına
// yazılıyor.
enum class SortOption {
    DATE_NEWEST_FIRST,
    PRIORITY_HIGH_FIRST,
    ALPHABETICAL,
    MANUAL,
}
