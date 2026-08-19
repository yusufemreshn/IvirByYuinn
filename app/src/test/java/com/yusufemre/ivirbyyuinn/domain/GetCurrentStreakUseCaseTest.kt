package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.TaskCompletionDao
import com.yusufemre.ivirbyyuinn.data.TaskCompletionEntity
import com.yusufemre.ivirbyyuinn.data.TaskCompletionRepository
import java.util.Calendar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

// Gerçek Room'a ihtiyaç yok: TaskCompletionDao küçük bir interface
// olduğu için elle bir "fake" yazıp GetCurrentStreakUseCase'i gerçek
// TaskCompletionRepository üzerinden, uçtan uca (ama veritabanısız) test
// edebiliyoruz. Zaman `System.currentTimeMillis()`'e göre RELATİF
// hesaplandığı için (ör. "dün" = şimdi - 1 gün), testler hangi gün
// çalıştırılırsa çalıştırılsın deterministik kalıyor.
private class FakeTaskCompletionDao(private val completions: List<Long>) : TaskCompletionDao {
    override suspend fun insert(completion: TaskCompletionEntity) = error("bu testte kullanılmıyor")
    override fun getCompletionsForList(taskListId: Long): Flow<List<Long>> = flowOf(completions)
    override suspend fun getAllCompletionsOnce(): List<TaskCompletionEntity> = error("bu testte kullanılmıyor")
    override suspend fun clearAll() = error("bu testte kullanılmıyor")
    override suspend fun insertAll(completions: List<TaskCompletionEntity>) = error("bu testte kullanılmıyor")
}

class GetCurrentStreakUseCaseTest {

    private fun daysAgoMillis(days: Int): Long {
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -days) }
        return calendar.timeInMillis
    }

    private fun streakFor(completions: List<Long>): Int {
        val repository = TaskCompletionRepository(FakeTaskCompletionDao(completions))
        val useCase = GetCurrentStreakUseCase(repository)
        return runBlocking { useCase(taskListId = 1L).first() }
    }

    @Test
    fun `hic tamamlama yoksa streak sifirdir`() {
        assertEquals(0, streakFor(emptyList()))
    }

    @Test
    fun `sadece bugun tamamlanmissa streak birdir`() {
        assertEquals(1, streakFor(listOf(daysAgoMillis(0))))
    }

    @Test
    fun `bugun ve dun art arda ise streak ikidir`() {
        assertEquals(2, streakFor(listOf(daysAgoMillis(0), daysAgoMillis(1))))
    }

    @Test
    fun `bugun henuz tamamlama yoksa ama dun varsa streak kirilmis sayilmaz`() {
        // Gün henüz bitmedi: dünkü tamamlama "hâlâ canlı" kabul ediliyor.
        assertEquals(2, streakFor(listOf(daysAgoMillis(1), daysAgoMillis(2))))
    }

    @Test
    fun `bugun ve dun ikisi de bossa streak sifira duser`() {
        // 3 gün önce tamamlama var ama dün/bugün yok: zincir kopmuş demektir.
        assertEquals(0, streakFor(listOf(daysAgoMillis(3))))
    }

    @Test
    fun `aradaki bir gun bosluk streak i orada durdurur`() {
        // Bugün, dün, sonra 2 gün önce BOŞLUK, 3 gün önce tekrar var,
        // zincir 2 gün önceki boşlukta durmalı, 3 gün önceki sayılmamalı.
        val result = streakFor(listOf(daysAgoMillis(0), daysAgoMillis(1), daysAgoMillis(3)))

        assertEquals(2, result)
    }

    @Test
    fun `ayni gun icindeki birden fazla tamamlama tek gun sayilir`() {
        val today = daysAgoMillis(0)
        val alsoToday = today + 60_000L // aynı gün, birkaç dakika sonra

        assertEquals(1, streakFor(listOf(today, alsoToday)))
    }
}
