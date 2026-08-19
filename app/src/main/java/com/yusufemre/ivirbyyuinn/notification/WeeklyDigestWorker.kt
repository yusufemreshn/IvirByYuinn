package com.yusufemre.ivirbyyuinn.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yusufemre.ivirbyyuinn.IvirApplication
import com.yusufemre.ivirbyyuinn.MainActivity
import com.yusufemre.ivirbyyuinn.R

// WorkManager, WeeklyDigestScheduler'ın kurduğu 7 günlük periyotta bu
// sınıfı çalıştırıyor. TaskReminderReceiver'dan farklı olarak burası zaten
// suspend bir bağlamda (CoroutineWorker kendi coroutine scope'unu
// sağlıyor), goAsync() gibi bir el şakasına gerek yok.
class WeeklyDigestWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val application = applicationContext as IvirApplication
        val completedCount = application.getWeeklyDigestSummaryUseCase()

        val hasPermission = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return Result.success()

        val contentIntent = Intent(applicationContext, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            applicationContext,
            DIGEST_NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        // Sayı > 0 ise bir ÖZET, 0 ise (kullanıcı bu hafta hiç görev
        // tamamlamamış) daha yumuşak bir YENİDEN-ETKİLEŞİM çerçevesi:
        // planın iki ayrı fikri (özet + yeniden-etkileşim), gürültü
        // yapmamak için TEK bir haftalık bildirimde birleşiyor.
        val contentText = if (completedCount > 0) {
            applicationContext.getString(R.string.weekly_digest_summary, completedCount)
        } else {
            applicationContext.getString(R.string.weekly_digest_reengagement)
        }

        val notification = NotificationCompat.Builder(applicationContext, WEEKLY_DIGEST_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(applicationContext.getString(R.string.weekly_digest_title))
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(DIGEST_NOTIFICATION_ID, notification)
        return Result.success()
    }

    private companion object {
        const val DIGEST_NOTIFICATION_ID = -1
    }
}
