package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Task
import com.yusufemre.ivirbyyuinn.data.TaskDao
import com.yusufemre.ivirbyyuinn.data.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// AddTaskUseCase projenin "ilk gerçek iş kuralı" (bkz. sınıfın kendi
// yorumu): boş/sadece boşluktan oluşan bir başlık asla eklenmemeli.
// TaskDao interface'inin gerçek Room implementasyonuna ihtiyacımız yok:
// insert() dışındaki her şeyi bu testte kullanılmıyor sayıp error()
// ile işaretliyoruz, böylece testin GERÇEKTEN dokunduğu davranış
// (insert çağrıldı mı, çağrılmadı mı, hangi Task ile) açıkça görünüyor.
private class FakeTaskDao : TaskDao {
    var insertedTask: Task? = null
    private var nextId = 1L

    override suspend fun insert(task: Task): Long {
        insertedTask = task
        return nextId++
    }

    override suspend fun update(task: Task) = error("bu testte kullanılmıyor")
    override suspend fun updateAll(tasks: List<Task>) = error("bu testte kullanılmıyor")
    override suspend fun hardDelete(task: Task) = error("bu testte kullanılmıyor")
    // AddTaskUseCase artık eklemeden ÖNCE aynı isimde görev var mı diye
    // burayı çağırıyor (isim çakışması kontrolü), testlerde hiç önceden
    // eklenmiş görev olmadığı için boş liste dönmesi yeterli.
    override fun getTasksForList(taskListId: Long): Flow<List<Task>> = flowOf(emptyList())
    override suspend fun getTaskById(taskId: Long): Task? = error("bu testte kullanılmıyor")
    override fun getDeletedTasks(taskListId: Long): Flow<List<Task>> = error("bu testte kullanılmıyor")
    override fun getArchivedTasks(taskListId: Long): Flow<List<Task>> = error("bu testte kullanılmıyor")
    override suspend fun getTasksPendingAutoArchive(cutoffMillis: Long): List<Task> = error("bu testte kullanılmıyor")
    override suspend fun getTasksWithPendingReminders(): List<Task> = error("bu testte kullanılmıyor")
    override suspend fun getUpcomingTasksForWidget(): List<Task> = error("bu testte kullanılmıyor")
    override fun getTasksDueToday(startMillis: Long, endMillis: Long): Flow<List<Task>> = error("bu testte kullanılmıyor")
    override fun getTasksDueThisWeek(startMillis: Long, endMillis: Long): Flow<List<Task>> = error("bu testte kullanılmıyor")
    override fun getOverdueTasks(nowMillis: Long): Flow<List<Task>> = error("bu testte kullanılmıyor")
    override suspend fun getAllTasksOnce(): List<Task> = error("bu testte kullanılmıyor")
    override suspend fun clearAll() = error("bu testte kullanılmıyor")
    override suspend fun insertAll(tasks: List<Task>) = error("bu testte kullanılmıyor")
    override suspend fun hardDeleteAllForList(taskListId: Long) = error("bu testte kullanılmıyor")
}

class AddTaskUseCaseTest {

    private fun useCaseWithDao(): Pair<AddTaskUseCase, FakeTaskDao> {
        val dao = FakeTaskDao()
        return AddTaskUseCase(TaskRepository(dao)) to dao
    }

    @Test
    fun `bos baslik reddedilir ve repository ye hic dokunulmaz`() = runBlocking {
        val (useCase, dao) = useCaseWithDao()

        val result = useCase(title = "", taskListId = 1L)

        assertEquals(TaskSaveOutcome.BlankTitle, result)
        assertNull(dao.insertedTask)
    }

    @Test
    fun `sadece bosluktan olusan baslik da reddedilir`() = runBlocking {
        val (useCase, dao) = useCaseWithDao()

        val result = useCase(title = "   ", taskListId = 1L)

        assertEquals(TaskSaveOutcome.BlankTitle, result)
        assertNull(dao.insertedTask)
    }

    @Test
    fun `gecerli baslik trim edilerek eklenir ve gercek id ile doner`() = runBlocking {
        val (useCase, dao) = useCaseWithDao()

        val result = useCase(title = "  Ekmek al  ", taskListId = 1L) as TaskSaveOutcome.Saved

        assertEquals("Ekmek al", dao.insertedTask?.title)
        assertEquals("Ekmek al", result.task.title)
        assertTrue(result.task.id > 0L)
    }

    @Test
    fun `bos not null a cevrilir`() = runBlocking {
        val (useCase, dao) = useCaseWithDao()

        useCase(title = "Görev", taskListId = 1L, note = "   ")

        assertNull(dao.insertedTask?.note)
    }

    @Test
    fun `dolu not trim edilerek saklanir`() = runBlocking {
        val (useCase, dao) = useCaseWithDao()

        useCase(title = "Görev", taskListId = 1L, note = "  detay  ")

        assertEquals("detay", dao.insertedTask?.note)
    }
}
