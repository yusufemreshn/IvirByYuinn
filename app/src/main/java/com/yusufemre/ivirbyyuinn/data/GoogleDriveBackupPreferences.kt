package com.yusufemre.ivirbyyuinn.data

// autoBackupEnabled: GoogleDriveBackupScheduler'ın periyodik işi kurulu mu.
// lastBackupAtMillis: hem otomatik hem manuel yedeklemeden sonra güncellenen,
// Ayarlar'da "son yedek: ..." satırında gösterilen zaman damgası.
// 2026-10-25: kullanıcı isteğiyle otomatik yedekleme artık hep gece 03:00'te
// günde bir değil, saat ve sıklık da seçilebiliyor. autoBackupMinuteOfDay
// QuietHours'taki startMinute/endMinute İLE AYNI birim (gece yarısından
// geçen dakika, 0-1439), sadece İLK çalışmanın ne zaman tetikleneceğini
// belirliyor. autoBackupIntervalHours WorkManager'ın PeriodicWorkRequestBuilder'ına
// doğrudan verilen saat sayısı (3/6/12 saatte bir gibi gün altı sıklıklar da
// seçilebilsin diye gün yerine SAAT birimine geçildi; 24=günde bir,
// 168=haftada bir).
data class GoogleDriveBackupPreferences(
    val autoBackupEnabled: Boolean = false,
    val lastBackupAtMillis: Long? = null,
    val autoBackupMinuteOfDay: Int = 180,
    val autoBackupIntervalHours: Int = 24,
)
