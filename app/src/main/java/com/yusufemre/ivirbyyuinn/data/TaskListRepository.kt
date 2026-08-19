package com.yusufemre.ivirbyyuinn.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TaskListRepository(private val taskListDao: TaskListDao) {

    fun getAllTaskLists(): Flow<List<TaskListEntity>> = taskListDao.getAllTaskLists()

    fun getArchivedTaskLists(): Flow<List<TaskListEntity>> = taskListDao.getArchivedTaskLists()

    suspend fun addTaskList(taskList: TaskListEntity): Long = taskListDao.insert(taskList)

    suspend fun deleteTaskList(taskList: TaskListEntity) = taskListDao.delete(taskList)

    suspend fun getNextSortOrder(): Int = taskListDao.getNextSortOrder()

    suspend fun updateTaskLists(taskLists: List<TaskListEntity>) = taskListDao.updateAll(taskLists)

    suspend fun updateTaskList(taskList: TaskListEntity) = taskListDao.update(taskList)

    // Faz 6, yedekleme. Ayrı bir DAO sorgusu açmak yerine zaten var olan
    // Flow'un ilk (ve o an güncel) değerini alıyoruz, Room'un Flow'ları
    // her zaman en az bir kere hemen emit ettiği için bu güvenli.
    suspend fun getAllTaskListsOnce(): List<TaskListEntity> = taskListDao.getAllTaskLists().first()

    suspend fun clearAll() = taskListDao.clearAll()

    suspend fun insertAll(taskLists: List<TaskListEntity>) = taskListDao.insertAll(taskLists)
}
