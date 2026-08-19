package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.TaskListEntity
import com.yusufemre.ivirbyyuinn.data.TaskListRepository
import kotlinx.coroutines.flow.Flow

class GetArchivedTaskListsUseCase(private val repository: TaskListRepository) {
    operator fun invoke(): Flow<List<TaskListEntity>> = repository.getArchivedTaskLists()
}
