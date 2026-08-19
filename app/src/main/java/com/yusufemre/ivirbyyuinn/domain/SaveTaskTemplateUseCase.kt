package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Priority
import com.yusufemre.ivirbyyuinn.data.TaskTemplateEntity
import com.yusufemre.ivirbyyuinn.data.TaskTemplateRepository

// Boş başlıklı bir şablon kaydetmenin hiçbir anlamı yok, AddCategoryUseCase
// ile aynı desen, boşsa sessizce reddediyoruz (null dönüyor).
class SaveTaskTemplateUseCase(private val repository: TaskTemplateRepository) {
    suspend operator fun invoke(
        title: String,
        priority: Priority,
        categoryId: Long?,
        note: String?,
    ): Long? {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return null
        return repository.save(
            TaskTemplateEntity(
                title = trimmedTitle,
                priority = priority,
                categoryId = categoryId,
                note = note?.trim()?.ifEmpty { null },
            ),
        )
    }
}
