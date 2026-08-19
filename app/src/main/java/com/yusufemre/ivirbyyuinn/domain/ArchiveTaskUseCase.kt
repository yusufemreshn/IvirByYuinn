package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Task
import com.yusufemre.ivirbyyuinn.data.TaskRepository

// DeleteTaskUseCase'in isDeleted için yaptığının birebir aynısı, sadece
// isArchived alanı için. completedAt'e burada DOKUNMUYORUZ, o zaten
// ToggleTaskDoneUseCase'de, gerçek tamamlanma anında yazıldı.
class ArchiveTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task): Task {
        val archivedTask = task.copy(isArchived = true)
        repository.updateTask(archivedTask)
        return archivedTask
    }
}
