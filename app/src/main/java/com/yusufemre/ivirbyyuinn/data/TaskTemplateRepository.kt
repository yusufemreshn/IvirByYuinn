package com.yusufemre.ivirbyyuinn.data

import kotlinx.coroutines.flow.Flow

class TaskTemplateRepository(private val taskTemplateDao: TaskTemplateDao) {

    fun getAll(): Flow<List<TaskTemplateEntity>> = taskTemplateDao.getAll()

    suspend fun save(template: TaskTemplateEntity): Long = taskTemplateDao.insert(template)

    suspend fun delete(template: TaskTemplateEntity) = taskTemplateDao.delete(template)
}
