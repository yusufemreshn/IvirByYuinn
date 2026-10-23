package com.yusufemre.ivirbyyuinn.data

// autoBackupEnabled: GoogleDriveBackupScheduler'ın periyodik işi kurulu mu.
// lastBackupAtMillis: hem otomatik hem manuel yedeklemeden sonra güncellenen,
// Ayarlar'da "son yedek: ..." satırında gösterilen zaman damgası.
data class GoogleDriveBackupPreferences(
    val autoBackupEnabled: Boolean = false,
    val lastBackupAtMillis: Long? = null,
)
