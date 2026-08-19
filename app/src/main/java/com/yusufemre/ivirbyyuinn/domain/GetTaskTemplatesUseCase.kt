package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.TaskTemplateEntity
import com.yusufemre.ivirbyyuinn.data.TaskTemplateRepository
import kotlinx.coroutines.flow.Flow

class GetTaskTemplatesUseCase(private val repository: TaskTemplateRepository) {
    operator fun invoke(): Flow<List<TaskTemplateEntity>> = repository.getAll()
}
