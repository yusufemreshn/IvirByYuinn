package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.TaskTemplateEntity
import com.yusufemre.ivirbyyuinn.data.TaskTemplateRepository

class DeleteTaskTemplateUseCase(private val repository: TaskTemplateRepository) {
    suspend operator fun invoke(template: TaskTemplateEntity) = repository.delete(template)
}
