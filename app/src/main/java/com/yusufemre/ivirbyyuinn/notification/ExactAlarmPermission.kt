package com.yusufemre.ivirbyyuinn.notification

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService

// Android 12 (API 31) öncesinde bu izin diye bir şey yoktu, tam zamanlı
// alarm kurmak herkese açıktı, bu yüzden eski cihazlarda hep "izinli"
// dönüyoruz. 12+'ta kullanıcı bunu sistem ayarlarından açık açık
// vermeli, biz sadece durumu soruyoruz.
fun hasExactAlarmPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    val alarmManager = context.getSystemService<AlarmManager>() ?: return false
    return alarmManager.canScheduleExactAlarms()
}
