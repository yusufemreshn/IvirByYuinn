package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.TaskCompletionRepository
import java.util.Calendar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Son 7 günün (bugün dahil) her biri için kaç görev tamamlandığını
// döndürür: StatsScreen'deki çubuk grafiğin veri kaynağı. Liste en
// eski günden bugüne doğru sıralı geliyor, grafik soldan sağa "geçmişten
// bugüne" akış hissi versin diye.
class GetWeeklyCompletionStatsUseCase(private val repository: TaskCompletionRepository) {
    operator fun invoke(taskListId: Long): Flow<List<DailyCompletionCount>> {
        return repository.getCompletionsForList(taskListId).map { completions ->
            val countsByDay = completions.groupingBy { dayStartMillis(it) }.eachCount()

            val cursor = Calendar.getInstance().apply {
                timeInMillis = dayStartMillis(System.currentTimeMillis())
                add(Calendar.DAY_OF_MONTH, -6)
            }
            (0..6).map {
                val dayStart = cursor.timeInMillis
                val count = countsByDay[dayStart] ?: 0
                cursor.add(Calendar.DAY_OF_MONTH, 1)
                DailyCompletionCount(dayStartMillis = dayStart, count = count)
            }
        }
    }

    // Bir zaman damgasını, ait olduğu günün gece yarısına (00:00) yuvarlar,
    // "aynı güne mi ait" sorusunu saat/dakika farkı olmadan cevaplamak
    // için tamamlanma anlarını gün başlangıcına göre grupluyoruz.
    private fun dayStartMillis(millis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
