package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Task
import com.yusufemre.ivirbyyuinn.data.TaskRepository

// Faz 4a'da söz verdiğimiz gibi: artık gerçek silme değil, isDeleted=true
// işaretlemek. Görev çöp kutusuna düşüyor, "Geri Al"la ya da çöp
// kutusundan geri yüklenebiliyor. Güncellenmiş task'ı döndürüyoruz ki
// ViewModel hatırlatmasını iptal edebilsin (bkz. ToggleTaskDoneUseCase'in
// aynı deseni).
class DeleteTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task): Task {
        val deletedTask = task.copy(isDeleted = true)
        repository.updateTask(deletedTask)
        return deletedTask
    }
}
