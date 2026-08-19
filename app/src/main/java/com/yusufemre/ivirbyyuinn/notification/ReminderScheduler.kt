package com.yusufemre.ivirbyyuinn.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import com.yusufemre.ivirbyyuinn.data.QuietHours
import com.yusufemre.ivirbyyuinn.data.QuietHoursPreferencesRepository
import com.yusufemre.ivirbyyuinn.data.Task
import java.util.Calendar

// AlarmManager'ı sarmalayan sınıf: .NET'te bir arka plan zamanlayıcıyı
// (Hangfire, Windows Task Scheduler) elle kullanmaya benzer: "zamanı
// geldiğinde şu Intent'i tetikle" diyoruz, gerisini işletim sistemi
// hallediyor. UseCase'ler Room'u tanımadığı gibi, AlarmManager'ı da
// doğrudan tanımıyor: sadece bu sınıfı tanıyor.
class ReminderScheduler(
    private val context: Context,
    private val quietHoursRepository: QuietHoursPreferencesRepository,
) {

    private val alarmManager = context.getSystemService<AlarmManager>()

    fun scheduleReminder(task: Task) {
        val dueAt = task.dueAt ?: return
        // Geçmişte kalmış bir tarih için alarm kurmanın anlamı yok.
        if (dueAt <= System.currentTimeMillis()) return

        // setExactAndAllowWhileIdle: cihaz Doze modunda (uykuda) olsa
        // bile tam zamanında tetikleniyor. Bunun için Android 12+'ta
        // SCHEDULE_EXACT_ALARM iznine ihtiyaç var: izin kontrolünü
        // burada değil, çağıran UseCase/ViewModel tarafında yapıyoruz;
        // buraya zaten izin varsa geliniyor.
        alarmManager?.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            adjustForQuietHours(dueAt),
            buildPendingIntent(task.id, task.title),
        )
    }

    // Faz 12b: hatırlatma sessiz saatler penceresine düşüyorsa SESSİZCE
    // İPTAL ETMİYORUZ: bir hatırlatmayı hiç göstermemek "unutulmuş"
    // hissi verir. Bunun yerine pencerenin BİTİŞ anına erteliyoruz,
    // kullanıcı uyandığında/telefonuna baktığında zaten orada duruyor.
    private fun adjustForQuietHours(triggerAt: Long): Long {
        val quietHours = quietHoursRepository.getQuietHoursNow()
        if (!quietHours.enabled) return triggerAt

        val calendar = Calendar.getInstance().apply { timeInMillis = triggerAt }
        val minuteOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        if (!isWithinQuietHours(minuteOfDay, quietHours)) return triggerAt

        val adjusted = (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, quietHours.endMinute / 60)
            set(Calendar.MINUTE, quietHours.endMinute % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // Pencere gece yarısını aşıyorsa (ör. 22:00->08:00) VE tetikleme
        // anı gece yarısından ÖNCEKİ yarıdaysa (ör. 23:00), bitiş ertesi
        // güne düşüyor, aksi halde "bitiş" geriye, geçmişe düşerdi.
        val windowCrossesMidnight = quietHours.startMinute > quietHours.endMinute
        if (windowCrossesMidnight && minuteOfDay >= quietHours.startMinute) {
            adjusted.add(Calendar.DAY_OF_YEAR, 1)
        }
        return adjusted.timeInMillis
    }

    private fun isWithinQuietHours(minuteOfDay: Int, quietHours: QuietHours): Boolean {
        return if (quietHours.startMinute <= quietHours.endMinute) {
            minuteOfDay in quietHours.startMinute until quietHours.endMinute
        } else {
            // Gece yarısını aşan pencere: ya başlangıçtan sonra (aynı gün
            // gece yarısına kadar) ya da bitişten önce (ertesi gün sabah).
            minuteOfDay >= quietHours.startMinute || minuteOfDay < quietHours.endMinute
        }
    }

    fun cancelReminder(taskId: Long) {
        // PendingIntent eşleşmesi extra'lara değil, requestCode + hedef
        // component'e bakıyor, bu yüzden burada başlığı doğru bilmemize
        // gerek yok, boş string de aynı alarmı bulup iptal etmeye yetiyor.
        alarmManager?.cancel(buildPendingIntent(taskId, title = ""))
    }

    private fun buildPendingIntent(taskId: Long, title: String): PendingIntent {
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, title)
        }
        // requestCode'u taskId'den türetiyoruz ki her görevin kendine ait
        // bir PendingIntent'i olsun, aksi halde farklı görevlerin
        // alarmları birbirinin üstüne yazardı. (taskId.toInt() ile Long'u
        // Int'e sıkıştırıyoruz; kişisel bir to-do uygulamasında görev
        // sayısının Int sınırını aşması gerçekçi değil.)
        return PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
