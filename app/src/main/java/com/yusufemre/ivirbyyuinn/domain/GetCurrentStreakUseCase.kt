package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.TaskCompletionRepository
import java.util.Calendar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// "Kaç gündür üst üste görev tamamlanıyor": bugünden (ya da bugün henüz
// hiç tamamlama yoksa dünden) geriye doğru, aralıksız tamamlama olan
// gün sayısını sayar. Bugün henüz bir şey tamamlanmamışsa streak hemen
// sıfırlanmıyor: gün henüz bitmedi, dün tamamlama varsa "hâlâ canlı"
// sayılıyor; ama dün de yoksa streak gerçekten kırılmış demektir.
class GetCurrentStreakUseCase(private val repository: TaskCompletionRepository) {
    operator fun invoke(taskListId: Long): Flow<Int> {
        return repository.getCompletionsForList(taskListId).map { completions -> calculateStreak(completions) }
    }

    private fun calculateStreak(completions: List<Long>): Int {
        if (completions.isEmpty()) return 0

        val completedDayStarts = completions.map { dayStartMillis(it) }.toSet()
        val cursor = Calendar.getInstance().apply { timeInMillis = dayStartMillis(System.currentTimeMillis()) }

        if (!completedDayStarts.contains(cursor.timeInMillis)) {
            cursor.add(Calendar.DAY_OF_MONTH, -1)
            if (!completedDayStarts.contains(cursor.timeInMillis)) return 0
        }

        var streak = 0
        while (completedDayStarts.contains(cursor.timeInMillis)) {
            streak++
            cursor.add(Calendar.DAY_OF_MONTH, -1)
        }
        return streak
    }

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
