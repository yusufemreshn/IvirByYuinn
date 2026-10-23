package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.ConfirmationPreferences
import com.yusufemre.ivirbyyuinn.data.ConfirmationPreferencesRepository

class SetConfirmationPreferencesUseCase(private val repository: ConfirmationPreferencesRepository) {
    operator fun invoke(preferences: ConfirmationPreferences) {
        repository.setConfirmationPreferences(preferences)
    }
}
