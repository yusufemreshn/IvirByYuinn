package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.TaskRepository

// Çöp kutusundaki "tümünü sil": PermanentlyDeleteTaskUseCase'in tek
// görev yerine, aktif listenin çöp kutusundaki TÜM görevlere uygulanan
// hali. Aynı sebeple (subtasks tablosundaki ON DELETE CASCADE) alt
// görevleri elle silmeye gerek yok.
class PermanentlyDeleteAllTasksUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(taskListId: Long) {
        repository.permanentlyDeleteAllForList(taskListId)
    }
}
