package com.yusufemre.ivirbyyuinn.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SubtaskRepository(private val subtaskDao: SubtaskDao) {

    fun getSubtasksForTask(taskId: Long): Flow<List<Subtask>> = subtaskDao.getSubtasksForTask(taskId)

    fun getAllSubtasks(): Flow<List<Subtask>> = subtaskDao.getAllSubtasks()

    suspend fun addSubtask(subtask: Subtask) = subtaskDao.insert(subtask)

    suspend fun updateSubtask(subtask: Subtask) = subtaskDao.update(subtask)

    suspend fun deleteSubtask(subtask: Subtask) = subtaskDao.delete(subtask)

    // Faz 6 (yedekleme), bkz. TaskListRepository.getAllTaskListsOnce().
    suspend fun getAllSubtasksOnce(): List<Subtask> = subtaskDao.getAllSubtasks().first()

    suspend fun clearAll() = subtaskDao.clearAll()

    suspend fun insertAll(subtasks: List<Subtask>) = subtaskDao.insertAll(subtasks)
}
