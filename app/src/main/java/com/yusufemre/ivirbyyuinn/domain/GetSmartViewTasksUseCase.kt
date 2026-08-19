package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Task
import com.yusufemre.ivirbyyuinn.data.TaskRepository
import java.util.Calendar
import kotlinx.coroutines.flow.Flow

// Akıllı listeler: TEK bir taskListId'ye değil, TÜM listelerdeki görevlere
// dueAt'e göre bakan hazır görünümler. AppDrawerContent'te kullanıcının
// kendi listelerinin ÜSTÜNDE sabit üç girdi olarak duruyor.
enum class SmartView {
    TODAY,
    UPCOMING,
    OVERDUE,
}

// Gün/hafta sınırları burada (Kotlin'de) hesaplanıyor, DAO sadece hazır bir
// [start, end) aralığı alıyor: GetWeeklyCompletionStatsUseCase'deki "gün
// başlangıcı" deseniyle aynı yaklaşım, iş kuralı SQL'e değil buraya yazılı.
class GetSmartViewTasksUseCase(private val repository: TaskRepository) {
    operator fun invoke(view: SmartView, now: Calendar = Calendar.getInstance()): Flow<List<Task>> {
        val todayStart = dayStartMillis(now)
        return when (view) {
            SmartView.TODAY -> repository.getTasksDueToday(todayStart, todayStart + DAY_MILLIS)
            // "Yaklaşan" BUGÜNÜ kapsamıyor, o zaten ayrı bir sekme, ikisini
            // çakıştırmak kafa karıştırır. Yarından başlayıp 7 gün ileriye
            // bakıyor.
            SmartView.UPCOMING -> {
                val start = todayStart + DAY_MILLIS
                repository.getTasksDueThisWeek(start, start + 7 * DAY_MILLIS)
            }
            SmartView.OVERDUE -> repository.getOverdueTasks(now.timeInMillis)
        }
    }

    private fun dayStartMillis(now: Calendar): Long {
        val calendar = now.clone() as Calendar
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private companion object {
        const val DAY_MILLIS = 24L * 60 * 60 * 1000
    }
}
