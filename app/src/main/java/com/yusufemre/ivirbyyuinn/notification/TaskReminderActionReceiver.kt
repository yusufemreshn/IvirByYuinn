package com.yusufemre.ivirbyyuinn.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.yusufemre.ivirbyyuinn.IvirApplication
import com.yusufemre.ivirbyyuinn.domain.TaskSaveOutcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TaskReminderReceiver'ın kurduğu bildirimdeki "Ertele"/"Tamamla"
// butonlarına basılınca burası çalışıyor. BootCompletedReceiver'la AYNI
// desen: Room'a erişmemiz gerekiyor ama onReceive senkron kalmalı, o
// yüzden goAsync() + Dispatchers.IO coroutine'i.
class TaskReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return
        val action = intent.action
        if (action != ACTION_SNOOZE && action != ACTION_COMPLETE) return

        val pendingResult = goAsync()
        val application = context.applicationContext as IvirApplication

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = application.getTaskByIdUseCase(taskId) ?: return@launch
                when (action) {
                    ACTION_SNOOZE -> {
                        // "1 saat sonra tekrar düşüneceğim": dueAt'in
                        // KENDİSİNİ 1 saat ileri alıyoruz (ayrı bir
                        // "hatırlatma zamanı" alanı açmak bu aşamada
                        // gereksiz karmaşıklık olurdu), sonra
                        // TaskViewModel.rescheduleReminder'la AYNI iki
                        // adımla alarmı yeniden kuruyoruz.
                        val newDueAt = System.currentTimeMillis() + SNOOZE_DURATION_MILLIS
                        val outcome = application.updateTaskUseCase(task.copy(dueAt = newDueAt))
                        // Burada sadece dueAt değişiyor, başlık aynı kalıyor,
                        // BlankTitle/DuplicateTitle pratikte hiç
                        // gerçekleşmez (görevin kendi başlığıyla çakışması
                        // kendi id'si hariç tutularak zaten engelleniyor,
                        // bkz. UpdateTaskUseCase), ama sonuç tipi Saved
                        // dışında da bir şey döndürebildiği için exhaustive
                        // when gerekiyor.
                        if (outcome is TaskSaveOutcome.Saved) {
                            val updatedTask = outcome.task
                            application.cancelTaskReminderUseCase(updatedTask.id)
                            application.scheduleTaskReminderUseCase(updatedTask)
                        }
                    }
                    ACTION_COMPLETE -> {
                        val updatedTask = application.toggleTaskDoneUseCase(task)
                        application.cancelTaskReminderUseCase(updatedTask.id)
                        // Tekrarsız bir görevde bu çağrı hiçbir şey
                        // kurmuyor: dueAt hâlâ GEÇMİŞTE (bildirim zaten o
                        // yüzden tetiklendi), ReminderScheduler kendi
                        // "geçmiş bir tarihe alarm kurma" kuralıyla bunu
                        // otomatik atlıyor. Tekrarlı bir görevde ise
                        // toggleTaskDoneUseCase satırı kapatmıyor, dueAt'i
                        // GELECEĞE ilerletiyor, o durumda burası yeni
                        // tarih için hatırlatmayı gerçekten kuruyor.
                        application.scheduleTaskReminderUseCase(updatedTask)
                    }
                }
                NotificationManagerCompat.from(context).cancel(taskId.toInt())
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val ACTION_SNOOZE = "com.yusufemre.ivirbyyuinn.action.SNOOZE_TASK"
        const val ACTION_COMPLETE = "com.yusufemre.ivirbyyuinn.action.COMPLETE_TASK"
        private const val SNOOZE_DURATION_MILLIS = 60 * 60 * 1000L
    }
}
