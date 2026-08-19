package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Recurrence
import com.yusufemre.ivirbyyuinn.data.Task
import com.yusufemre.ivirbyyuinn.data.TaskCompletionRepository
import com.yusufemre.ivirbyyuinn.data.TaskRepository
import java.util.Calendar

// Task bir data class olduğu için "copy" ile istediğimiz alanı değiştirip
// geri kalanını olduğu gibi koruyan yeni bir kopya üretiyoruz.
//
// Faz 3'te davranış ikiye ayrıldı: tekrarsız bir görevi tamamlamak
// klasik "isDone = true" yapıyor. Ama tekrarlı bir görevi (recurrence
// != NONE, dueAt dolu) tamamlamak satırı KAPATMIYOR: dueAt'i bir
// sonraki tekrara ilerletip isDone'ı false'ta bırakıyoruz. Yani
// tekrarlı bir görevde checkbox "tamamlandı/tamamlanmadı" değil, "bu
// seferkini bitir, sıradakine geç" anlamına geliyor.
//
// Faz 5: "tamamlama anı" tam olarak burada belli oluyor, o yüzden
// istatistik/streak için tamamlanma kaydını da (task.isDone false iken
// çağrıldıysa, yani bu bir "tamamlama" ise) burada düşüyoruz. Görevi
// GERİ ALMAK (isDone true -> false) bir tamamlama sayılmıyor, kayıt
// silinmiyor de eklenmiyor de, sadece "tamamlandı" anları loglanıyor.
//
// 2026-08-11: aynı "tamamlama anı" artık Task.completedAt'e de yazılıyor
// (task_completions'daki olay günlüğünden AYRI, görevin kendi üzerinde
// duran son tamamlanma bilgisi): Arşiv ekranı ve Takvim'in "bu görev ne
// zaman tamamlandı" sorusuna task_completions'a JOIN atmadan, doğrudan
// tek bir satırdan cevap verebilmesi için. Geri alınırsa null'a dönüyor.
class ToggleTaskDoneUseCase(
    private val repository: TaskRepository,
    private val completionRepository: TaskCompletionRepository,
) {
    suspend operator fun invoke(task: Task): Task {
        val isCompleting = !task.isDone
        val isCompletingRecurringTask = isCompleting && task.recurrence != Recurrence.NONE && task.dueAt != null

        val updatedTask = if (isCompletingRecurringTask) {
            task.copy(dueAt = nextDueDate(task.dueAt, task.recurrence))
        } else {
            task.copy(
                isDone = !task.isDone,
                completedAt = if (isCompleting) System.currentTimeMillis() else null,
            )
        }

        repository.updateTask(updatedTask)
        if (isCompleting) {
            completionRepository.recordCompletion(task.id)
        }
        // Güncel task'ı geri döndürüyoruz ki ViewModel, hatırlatmayı iptal
        // mi edecek yoksa yeni tarihe mi yeniden kuracak buna karar
        // verebilsin (bkz. ui/task/TaskViewModel.toggleDone).
        return updatedTask
    }

    private fun nextDueDate(currentDueAt: Long, recurrence: Recurrence): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentDueAt }
        when (recurrence) {
            Recurrence.DAILY -> calendar.add(Calendar.DAY_OF_MONTH, 1)
            Recurrence.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            Recurrence.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            Recurrence.NONE -> Unit // buraya hiç girilmiyor, when'i exhaustive yapmak için
        }
        return calendar.timeInMillis
    }
}
