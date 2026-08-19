package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Task

// Diğer UseCase'lerden farklı olarak bu bir repository'ye bağlı değil:
// Room'dan hiç haberi yok, sadece elindeki listeyi süzüp sıralayan SAF bir
// fonksiyon. Reaktif tarafı (arama kutusu değiştikçe StateFlow'ları
// birleştirmek) bilerek ViewModel'e bıraktık; "bir listeyi nasıl
// filtreleyip sıralarım" iş kuralı burada, "ekranın state'i ne zaman
// yeniden hesaplanır" ise ViewModel'in sorumluluğu.
class FilterAndSortTasksUseCase {
    operator fun invoke(
        tasks: List<Task>,
        searchQuery: String,
        categoryFilter: Long?,
        sortOption: SortOption,
    ): List<Task> {
        val filtered = tasks
            .filter { categoryFilter == null || it.categoryId == categoryFilter }
            .filter { searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) }

        return when (sortOption) {
            SortOption.DATE_NEWEST_FIRST -> filtered.sortedByDescending { it.createdAt }
            SortOption.PRIORITY_HIGH_FIRST -> filtered.sortedByDescending { it.priority.level }
            SortOption.ALPHABETICAL -> filtered.sortedBy { it.title.lowercase() }
            // Kullanıcının sürükle-bırakla verdiği sıra.
            SortOption.MANUAL -> filtered.sortedBy { it.manualOrder }
        }
    }
}
