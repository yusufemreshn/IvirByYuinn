package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.TaskListEntity
import com.yusufemre.ivirbyyuinn.data.TaskListRepository

// Drawer'da tam bir sürükle-bırak yerine "Yukarı taşı"/"Aşağı taşı"
// seçildi (bkz. plan), bu yüzden burada da tüm listeyi yeniden
// numaralandırmak yerine sadece İKİ KOMŞU listenin sortOrder'ını
// takas ediyoruz, en basit yeterli çözüm.
class ReorderTaskListsUseCase(private val repository: TaskListRepository) {
    suspend operator fun invoke(currentLists: List<TaskListEntity>, taskListId: Long, moveUp: Boolean) {
        val index = currentLists.indexOfFirst { it.id == taskListId }
        if (index == -1) return
        val swapIndex = if (moveUp) index - 1 else index + 1
        if (swapIndex !in currentLists.indices) return

        val current = currentLists[index]
        val swapWith = currentLists[swapIndex]
        repository.updateTaskLists(
            listOf(
                current.copy(sortOrder = swapWith.sortOrder),
                swapWith.copy(sortOrder = current.sortOrder),
            ),
        )
    }
}
