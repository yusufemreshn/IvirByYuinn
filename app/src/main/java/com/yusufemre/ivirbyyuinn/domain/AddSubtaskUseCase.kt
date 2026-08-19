package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Subtask
import com.yusufemre.ivirbyyuinn.data.SubtaskRepository

// AddTaskUseCase'deki gibi burada da boş başlığı reddediyoruz, aynı kural,
// bu sefer alt görevler için.
class AddSubtaskUseCase(private val repository: SubtaskRepository) {
    suspend operator fun invoke(taskId: Long, title: String) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return
        repository.addSubtask(Subtask(taskId = taskId, title = trimmedTitle))
    }
}
