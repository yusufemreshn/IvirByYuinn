package com.yusufemre.ivirbyyuinn.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.yusufemre.ivirbyyuinn.R

// Android 8 (API 26) ve üstünde her bildirimin bir kanala ait olması
// zorunlu: kullanıcı kanalı ayarlardan tek tek açıp kapatabiliyor.
// Bizim minSdk'ımız zaten 26 olduğu için "eski cihazlarda kanal
// gerekmiyor" gibi bir ayrım yapmamıza gerek yok.
const val REMINDER_CHANNEL_ID = "task_reminders"

// Faz 12b: haftalık özet/yeniden-etkileşim bildirimi zaman-kritik değil
// (bir görevin son tarihi gibi acil değil), ayrı, daha düşük öncelikli
// bir kanalda, kullanıcı isterse hatırlatmalardan bağımsız kapatabilsin.
const val WEEKLY_DIGEST_CHANNEL_ID = "weekly_digest"

// Faz 12c: odak modu çalışma/mola geçişleri, ekran kapalıyken/uygulama
// kısa süreliğine arkadayken bile fark edilsin diye, ama hatırlatmalar
// kadar acil değil.
const val FOCUS_CHANNEL_ID = "focus_mode"

object NotificationChannels {
    // Uygulama her açıldığında çağrılıyor (IvirApplication.onCreate).
    // Kanal zaten varsa bu çağrı hiçbir şeyi bozmuyor: Android aynı id
    // ile ikinci kez oluşturma isteğini sessizce yok sayıyor.
    fun createAll(context: Context) {
        val reminderChannel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.reminder_channel_description)
        }
        val digestChannel = NotificationChannel(
            WEEKLY_DIGEST_CHANNEL_ID,
            context.getString(R.string.weekly_digest_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.weekly_digest_channel_description)
        }
        val focusChannel = NotificationChannel(
            FOCUS_CHANNEL_ID,
            context.getString(R.string.focus_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.focus_channel_description)
        }
        val notificationManager = context.getSystemService<NotificationManager>()
        notificationManager?.createNotificationChannel(reminderChannel)
        notificationManager?.createNotificationChannel(digestChannel)
        notificationManager?.createNotificationChannel(focusChannel)
    }
}
