package com.yusufemre.ivirbyyuinn.data

import kotlinx.coroutines.flow.Flow

class TaskCompletionRepository(private val dao: TaskCompletionDao) {

    suspend fun recordCompletion(taskId: Long) {
        dao.insert(TaskCompletionEntity(taskId = taskId, completedAt = System.currentTimeMillis()))
    }

    fun getCompletionsForList(taskListId: Long): Flow<List<Long>> = dao.getCompletionsForList(taskListId)

    // Faz 6: yedekleme.
    suspend fun getAllCompletionsOnce(): List<TaskCompletionEntity> = dao.getAllCompletionsOnce()

    suspend fun clearAll() = dao.clearAll()

    suspend fun insertAll(completions: List<TaskCompletionEntity>) = dao.insertAll(completions)
}
