package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Task
import com.yusufemre.ivirbyyuinn.data.TaskRepository

// Çöp kutusundan (ya da Snackbar'daki "Geri Al"dan) çağrılıyor.
// isDeleted'i false yapmak, silmeden önceki hale dönmenin tamamı:
// alt görevler, kategori, tarih gibi hiçbir şey kaybolmuyor çünkü satır
// zaten hiç silinmemişti.
class RestoreTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task): Task {
        val restoredTask = task.copy(isDeleted = false)
        repository.updateTask(restoredTask)
        return restoredTask
    }
}
