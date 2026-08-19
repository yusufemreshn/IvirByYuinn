package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.WeeklyDigestPreferencesRepository
import com.yusufemre.ivirbyyuinn.notification.WeeklyDigestScheduler

// Sadece tercihi kaydetmiyor, WorkManager kaydını da AYNI anda
// güncelliyor: UI tarafı (Ayarlar'daki switch) bu ikisinin birlikte
// yürümesi gerektiğini bilmek zorunda kalmasın diye koordinasyon burada.
class SetWeeklyDigestOptInUseCase(
    private val repository: WeeklyDigestPreferencesRepository,
    private val scheduler: WeeklyDigestScheduler,
) {
    operator fun invoke(enabled: Boolean) {
        repository.setOptIn(enabled)
        if (enabled) scheduler.schedule() else scheduler.cancel()
    }
}
