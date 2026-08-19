package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Task
import com.yusufemre.ivirbyyuinn.data.TaskRepository

// Faz 12b: bildirimden gelen Ertele/Tamamla aksiyonları için, Intent
// sadece bir taskId taşıyor, işlem yapmadan önce görevin güncel halini
// tek seferlik yüklüyoruz.
class GetTaskByIdUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: Long): Task? = repository.getTaskById(taskId)
}
