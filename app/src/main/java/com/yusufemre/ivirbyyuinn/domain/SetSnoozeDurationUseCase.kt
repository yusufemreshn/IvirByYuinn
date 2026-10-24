package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.SnoozeDurationPreferencesRepository

class SetSnoozeDurationUseCase(private val repository: SnoozeDurationPreferencesRepository) {
    operator fun invoke(minutes: Int) {
        repository.setSnoozeMinutes(minutes)
    }
}
