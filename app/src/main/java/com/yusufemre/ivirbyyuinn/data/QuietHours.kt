package com.yusufemre.ivirbyyuinn.data

// startMinute/endMinute: gece yarısından (00:00) beri geçen dakika sayısı
// (0-1439), TimePickerState'in ayrı hour/minute çiftini tek bir Int'e
// sıkıştırıyoruz, karşılaştırma (ReminderScheduler'daki "şu an pencerenin
// içinde mi") bu sayede tek bir sayı aralığı kontrolüne indirgeniyor.
// startMinute > endMinute ise pencere gece yarısını AŞIYOR demektir
// (ör. 22:00 -> 08:00).
data class QuietHours(
    val enabled: Boolean = false,
    val startMinute: Int = DEFAULT_START_MINUTE,
    val endMinute: Int = DEFAULT_END_MINUTE,
) {
    companion object {
        const val DEFAULT_START_MINUTE = 22 * 60 // 22:00
        const val DEFAULT_END_MINUTE = 8 * 60 // 08:00
    }
}
