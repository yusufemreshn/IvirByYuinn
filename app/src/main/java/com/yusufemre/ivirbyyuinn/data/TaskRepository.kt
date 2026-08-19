package com.yusufemre.ivirbyyuinn.data

import kotlinx.coroutines.flow.Flow

// Şu an DAO'dan neredeyse hiç farksız görünüyor, kabul ediyoruz. Ama
// UseCase'lerin (domain katmanı) Room'u hiç tanımaması, sadece bu
// arayüzü tanıması önemli: ileride verinin kaynağı değişse (örn. bir
// kısmı ağdan gelse) domain katmanına hiç dokunmadan burayı güncelleriz.
// .NET'teki DAL'ın (Data Access Layer) rolü tam olarak bu.
class TaskRepository(private val taskDao: TaskDao) {

    fun getTasksForList(taskListId: Long): Flow<List<Task>> = taskDao.getTasksForList(taskListId)

    suspend fun getTaskById(taskId: Long): Task? = taskDao.getTaskById(taskId)

    fun getDeletedTasks(taskListId: Long): Flow<List<Task>> = taskDao.getDeletedTasks(taskListId)

    // 2026-08-11: görev arşivi.
    fun getArchivedTasks(taskListId: Long): Flow<List<Task>> = taskDao.getArchivedTasks(taskListId)

    suspend fun getTasksPendingAutoArchive(cutoffMillis: Long): List<Task> =
        taskDao.getTasksPendingAutoArchive(cutoffMillis)

    // Faz 12a: akıllı listeler.
    fun getTasksDueToday(startMillis: Long, endMillis: Long): Flow<List<Task>> =
        taskDao.getTasksDueToday(startMillis, endMillis)

    fun getTasksDueThisWeek(startMillis: Long, endMillis: Long): Flow<List<Task>> =
        taskDao.getTasksDueThisWeek(startMillis, endMillis)

    fun getOverdueTasks(nowMillis: Long): Flow<List<Task>> = taskDao.getOverdueTasks(nowMillis)

    suspend fun addTask(task: Task) = taskDao.insert(task)

    suspend fun updateTask(task: Task) = taskDao.update(task)

    suspend fun updateTasks(tasks: List<Task>) = taskDao.updateAll(tasks)

    // "Silme" artık burada değil, bkz. domain/DeleteTaskUseCase, artık
    // updateTask(task.copy(isDeleted = true)) çağırıyor. Bu fonksiyon
    // sadece çöp kutusundan kalıcı silme için var.
    suspend fun permanentlyDeleteTask(task: Task) = taskDao.hardDelete(task)

    suspend fun permanentlyDeleteAllForList(taskListId: Long) = taskDao.hardDeleteAllForList(taskListId)

    suspend fun getTasksWithPendingReminders(): List<Task> = taskDao.getTasksWithPendingReminders()

    suspend fun getUpcomingTasksForWidget(): List<Task> = taskDao.getUpcomingTasksForWidget()

    // Faz 6, yedekleme.
    suspend fun getAllTasksOnce(): List<Task> = taskDao.getAllTasksOnce()

    suspend fun clearAll() = taskDao.clearAll()

    suspend fun insertAll(tasks: List<Task>) = taskDao.insertAll(tasks)
}
