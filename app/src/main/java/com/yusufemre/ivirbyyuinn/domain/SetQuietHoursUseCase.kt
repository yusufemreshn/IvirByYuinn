package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.QuietHours
import com.yusufemre.ivirbyyuinn.data.QuietHoursPreferencesRepository

class SetQuietHoursUseCase(private val repository: QuietHoursPreferencesRepository) {
    operator fun invoke(quietHours: QuietHours) {
        repository.setQuietHours(quietHours)
    }
}
