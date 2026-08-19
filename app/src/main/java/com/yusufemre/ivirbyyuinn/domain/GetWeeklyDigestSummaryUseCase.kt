package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.TaskCompletionRepository
import java.util.Calendar

// Haftalık özet bildirimi TEK bir listeye değil TÜM listelere bakıyor
// (GetWeeklyCompletionStatsUseCase'in aksine, o belirli bir taskListId
// alıyordu), bu yüzden liste ayrımı yapmayan getAllCompletionsOnce()'u
// kullanıp pencereyi (son 7 gün) burada, bellekte süzüyoruz. Aynı "iş
// kuralı Kotlin'de, sorgu SQL'de sade" deseni GetSmartViewTasksUseCase'de
// de var.
class GetWeeklyDigestSummaryUseCase(private val repository: TaskCompletionRepository) {
    suspend operator fun invoke(now: Calendar = Calendar.getInstance()): Int {
        val weekAgoMillis = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -7) }.timeInMillis
        return repository.getAllCompletionsOnce().count { it.completedAt >= weekAgoMillis }
    }
}
