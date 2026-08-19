package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Task
import com.yusufemre.ivirbyyuinn.data.TaskRepository

// Sadece çöp kutusu ekranından çağrılıyor: burası geri dönüşü olmayan
// GERÇEK silme. subtasks tablosundaki FOREIGN KEY(taskId) ON DELETE
// CASCADE tanımı sayesinde bu görevin alt görevleri de otomatik gidiyor,
// elle silmemize gerek yok.
class PermanentlyDeleteTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task) {
        repository.permanentlyDeleteTask(task)
    }
}
