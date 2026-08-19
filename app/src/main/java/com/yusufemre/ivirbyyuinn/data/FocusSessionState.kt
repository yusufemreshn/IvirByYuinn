package com.yusufemre.ivirbyyuinn.data

enum class FocusPhase { IDLE, WORK, BREAK }

// Odak modu sayacının TAMAMI, kullanıcı isteğiyle artık SharedPreferences'a
// kaydediliyor (bkz. FocusSessionPreferencesRepository), sadece Compose'un
// hafızasında yaşamıyor. Böylece kullanıcı başka bir menüye geçse, hatta
// uygulamayı komple kapatıp açsa bile sayaç kaldığı yerden devam ediyor.
//
// lastTickEpochMillis: isRunning=true iken remainingSeconds'ın hangi ANDA
// doğru olduğunu tutuyor: ekran yeniden açıldığında (ya da uygulama
// yeniden başladığında) System.currentTimeMillis() ile arasındaki farkı
// hesaplayıp aradan geçen GERÇEK süreyi remainingSeconds'tan düşüyoruz.
data class FocusSessionState(
    val phase: FocusPhase = FocusPhase.IDLE,
    val isRunning: Boolean = false,
    val remainingSeconds: Int = DEFAULT_WORK_DURATION_SECONDS,
    val workDurationSeconds: Int = DEFAULT_WORK_DURATION_SECONDS,
    val breakDurationSeconds: Int = DEFAULT_BREAK_DURATION_SECONDS,
    val lastTickEpochMillis: Long = 0L,
) {
    companion object {
        const val DEFAULT_WORK_DURATION_SECONDS = 25 * 60
        const val DEFAULT_BREAK_DURATION_SECONDS = 5 * 60
    }
}
