package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Subtask
import com.yusufemre.ivirbyyuinn.data.SubtaskRepository
import kotlinx.coroutines.flow.Flow

class GetSubtasksUseCase(private val repository: SubtaskRepository) {
    operator fun invoke(taskId: Long): Flow<List<Subtask>> = repository.getSubtasksForTask(taskId)
}
