package com.yusufemre.ivirbyyuinn.widget

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import com.yusufemre.ivirbyyuinn.MainActivity

// Bildirim çekmecesindeki Quick Settings panelinden ("hızlı ayarlar")
// dokunulunca uygulamayı doğrudan görev ekleme moduyla açan tile.
// Kullanıcı bunu panele elle eklemeli (Android'in kendi "düzenle" akışı
// üzerinden), biz sadece dokununca ne olacağını tanımlıyoruz.
class QuickAddTileService : TileService() {

    override fun onClick() {
        super.onClick()

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(MainActivity.EXTRA_OPEN_ADD_TASK, true)
        }

        // startActivityAndCollapse(Intent) Android 14'te deprecated oldu,
        // yerine PendingIntent alan bir sürümü geldi. minSdk'ımız 26
        // olduğu için ikisini de desteklememiz gerekiyor.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
