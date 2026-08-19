package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.TaskRepository
import java.util.concurrent.TimeUnit

// Kullanıcı isteği: bir görev tamamlandıktan sonra 24 saat içinde elle
// arşivlenmez ya da silinmezse, otomatik olarak arşive düşsün. Bu
// UseCase hem AutoArchiveWorker'ın (WorkManager, 30 dakikada bir, uygulama
// kapalıyken de) hem TaskViewModel.init'in (uygulama her açıldığında bir
// kerelik anlık kontrol için) ÇAĞIRDIĞI tek, paylaşılan kural.
//
// ReorderTasksUseCase'in updateAll ile TOPLU kaydetmesiyle aynı desen:
// tek tek update yerine bulunan görevleri tek seferde yazıyoruz.
class AutoArchiveCompletedTasksUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(): Int {
        val cutoff = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24)
        val pendingTasks = repository.getTasksPendingAutoArchive(cutoff)
        if (pendingTasks.isEmpty()) return 0
        repository.updateTasks(pendingTasks.map { it.copy(isArchived = true) })
        return pendingTasks.size
    }
}
