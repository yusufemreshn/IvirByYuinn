package com.yusufemre.ivirbyyuinn.notification

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.yusufemre.ivirbyyuinn.IvirApplication
import com.yusufemre.ivirbyyuinn.R
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
                        // Kullanıcı isteğiyle artık sabit 1 saat değil,
                        // Ayarlar > Bildirimler'de seçilen süre (varsayılan
                        // yine 60 dakika, eski davranışla AYNI): dueAt'in
                        // KENDİSİNİ o kadar ileri alıyoruz (ayrı bir
                        // "hatırlatma zamanı" alanı açmak bu aşamada
                        // gereksiz karmaşıklık olurdu), sonra
                        // TaskViewModel.rescheduleReminder'la AYNI iki
                        // adımla alarmı yeniden kuruyoruz.
                        val snoozeMinutes = application.currentSnoozeMinutes()
                        val newDueAt = System.currentTimeMillis() + snoozeMinutes * 60_000L
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
                            // Kullanıcı isteği: bildirimdeki Ertele'ye
                            // basınca, telefonun altında (ayrı bir toast/
                            // bildirim olarak) "kaç dakika ertelendi"
                            // teyidi görünsün, orijinal hatırlatma
                            // bildirimi sessizce kapanıp gitmesin.
                            showConfirmation(
                                context = context,
                                notificationId = taskId.toInt() + CONFIRMATION_ID_OFFSET,
                                title = context.getString(R.string.snooze_confirmation_title),
                                text = context.getString(
                                    R.string.snooze_confirmation_text,
                                    task.title,
                                    snoozeMinutes,
                                ),
                            )
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
                        showConfirmation(
                            context = context,
                            notificationId = taskId.toInt() + CONFIRMATION_ID_OFFSET,
                            title = context.getString(R.string.complete_confirmation_title),
                            text = context.getString(R.string.complete_confirmation_text, task.title),
                        )
                    }
                }
                NotificationManagerCompat.from(context).cancel(taskId.toInt())
            } finally {
                pendingResult.finish()
            }
        }
    }

    // TaskReminderReceiver'daki izin kontrolüyle AYNI: Android 13+'ta
    // POST_NOTIFICATIONS yoksa notify() SecurityException fırlatır.
    private fun showConfirmation(context: Context, notificationId: Int, title: String, text: String) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return

        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val ACTION_SNOOZE = "com.yusufemre.ivirbyyuinn.action.SNOOZE_TASK"
        const val ACTION_COMPLETE = "com.yusufemre.ivirbyyuinn.action.COMPLETE_TASK"
        // Orijinal hatırlatma bildirimi taskId.toInt() kimliğini kullanıyor;
        // teyit bildirimi AYNI id'yi kullanırsa onu (henüz cancel
        // çağrılmadan önce) üzerine yazabilirdi, o yüzden ayrı bir kimlik
        // alanına kaydırıyoruz.
        private const val CONFIRMATION_ID_OFFSET = 1_000_000
    }
}
