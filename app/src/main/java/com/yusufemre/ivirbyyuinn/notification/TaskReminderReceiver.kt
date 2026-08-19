package com.yusufemre.ivirbyyuinn.notification

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.yusufemre.ivirbyyuinn.MainActivity
import com.yusufemre.ivirbyyuinn.R

// AlarmManager'ın tetiklediği an burası çalışıyor. Room'a hiç dokunmuyor,
// veritabanına gitmiyor: sadece Intent'in içinde taşınan başlığı alıp
// bir bildirim gösteriyor. "Hatırlatma zamanı geldi" bilgisinin son
// durağı burası.
class TaskReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TASK_TITLE)
        if (taskId == -1L || title.isNullOrBlank()) return

        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            // ic_launcher_foreground zaten şeffaf zeminde beyaz bir
            // çizim (onay işareti), bildirim küçük ikonu için tam
            // istenen format bu, ayrı bir ikon çizmemize gerek kalmadı.
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.reminder_notification_title))
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            // Faz 12b: bildirimi açmadan, doğrudan bildirimin üzerinden
            // "Ertele"/"Tamamla", ikisi de aynı taskId'yi taşıyan ama
            // farklı action string'leriyle ayrışan TaskReminderActionReceiver'a
            // gidiyor.
            .addAction(0, context.getString(R.string.reminder_action_snooze), buildActionPendingIntent(context, taskId, TaskReminderActionReceiver.ACTION_SNOOZE))
            .addAction(0, context.getString(R.string.reminder_action_complete), buildActionPendingIntent(context, taskId, TaskReminderActionReceiver.ACTION_COMPLETE))
            .build()

        // Android 13+'ta POST_NOTIFICATIONS runtime izni yoksa bu çağrı
        // SecurityException fırlatır: önce kontrol ediyoruz. API 33'ten
        // eski cihazlarda bu kontrol her zaman "izinli" döner (o
        // sürümlerde böyle bir runtime izin hiç yok).
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            NotificationManagerCompat.from(context).notify(taskId.toInt(), notification)
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
    }
}

// TaskReminderActionReceiver'a (Ertele/Tamamla) giden PendingIntent'i
// kuruyor: requestCode'u ReminderScheduler'ın alarm PendingIntent'iyle
// AYNI (taskId.toInt()) olsa da sorun değil, çünkü Intent'in action'ı
// (SNOOZE/COMPLETE) ve hedef component'i (TaskReminderActionReceiver,
// ReminderScheduler'ın hedeflediği TaskReminderReceiver'dan FARKLI)
// birlikte kimliği zaten ayrıştırıyor.
private fun buildActionPendingIntent(context: Context, taskId: Long, action: String): PendingIntent {
    val intent = Intent(context, TaskReminderActionReceiver::class.java).apply {
        this.action = action
        putExtra(TaskReminderActionReceiver.EXTRA_TASK_ID, taskId)
    }
    return PendingIntent.getBroadcast(
        context,
        taskId.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
