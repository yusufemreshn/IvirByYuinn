package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Task
import com.yusufemre.ivirbyyuinn.data.TaskRepository
import kotlinx.coroutines.flow.Flow

class GetDeletedTasksUseCase(private val repository: TaskRepository) {
    operator fun invoke(taskListId: Long): Flow<List<Task>> = repository.getDeletedTasks(taskListId)
}
