package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.TaskListEntity
import com.yusufemre.ivirbyyuinn.data.TaskListRepository

class AddTaskListUseCase(private val repository: TaskListRepository) {
    suspend operator fun invoke(name: String): Long? {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return null
        // sortOrder'ı ELLE veriyoruz (Kotlin varsayılanı olan 0'a
        // güvenmiyoruz): aksi halde her yeni liste "Genel"le (ya da
        // birbiriyle) aynı sıra değerine düşer, "Yukarı/Aşağı taşı" eşit
        // iki değeri takas edip görünürde hiçbir şey değiştirmemiş olur.
        val nextSortOrder = repository.getNextSortOrder()
        return repository.addTaskList(TaskListEntity(name = trimmedName, sortOrder = nextSortOrder))
    }
}
