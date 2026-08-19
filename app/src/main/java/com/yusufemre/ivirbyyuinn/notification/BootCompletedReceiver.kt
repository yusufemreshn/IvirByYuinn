package com.yusufemre.ivirbyyuinn.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yusufemre.ivirbyyuinn.IvirApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Cihaz yeniden başladığında Android, AlarmManager'daki TÜM alarmları
// otomatik siliyor (pil/güvenlik nedeniyle böyle tasarlanmış), bu
// yüzden hatırlatmaları unutulmuş gibi bırakmamak için BOOT_COMPLETED
// yayınını yakalayıp henüz tamamlanmamış, son tarihi olan görevlerin
// hatırlatmalarını yeniden kuruyoruz.
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        // onReceive çok kısa sürede bitmesi gereken bir fonksiyon ama
        // bizim Room'dan veri okumamız gerekiyor. goAsync() ile sisteme
        // "işim bitmedi, biraz daha bekle" diyoruz, iş bitince finish()
        // ile haber veriyoruz.
        val pendingResult = goAsync()
        val application = context.applicationContext as IvirApplication

        CoroutineScope(Dispatchers.IO).launch {
            try {
                application.rescheduleAllRemindersUseCase()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
