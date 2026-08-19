package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Task
import com.yusufemre.ivirbyyuinn.data.TaskRepository

// Arşiv ekranındaki "Arşivden çıkar" butonu için, RestoreTaskUseCase'in
// isDeleted için yaptığının birebir aynısı. Görev normal listeye geri
// döner ama isDone/completedAt aynen kalır, "tamamlanmamış" hale
// dönmüyor, sadece arşivden çıkıyor.
class UnarchiveTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task): Task {
        val unarchivedTask = task.copy(isArchived = false)
        repository.updateTask(unarchivedTask)
        return unarchivedTask
    }
}
