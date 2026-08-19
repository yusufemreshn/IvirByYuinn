package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Subtask
import com.yusufemre.ivirbyyuinn.data.SubtaskRepository

class ToggleSubtaskDoneUseCase(private val repository: SubtaskRepository) {
    suspend operator fun invoke(subtask: Subtask) {
        repository.updateSubtask(subtask.copy(isDone = !subtask.isDone))
    }
}
