package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.TaskListEntity
import com.yusufemre.ivirbyyuinn.data.TaskListRepository

class SetTaskListArchivedUseCase(private val repository: TaskListRepository) {
    suspend operator fun invoke(taskList: TaskListEntity, archived: Boolean) {
        repository.updateTaskList(taskList.copy(isArchived = archived))
    }
}
