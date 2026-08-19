package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.ShowHomeScreenPreferencesRepository

class SetShowHomeScreenPreferenceUseCase(private val repository: ShowHomeScreenPreferencesRepository) {
    operator fun invoke(show: Boolean) {
        repository.setShowHomeScreenPreference(show)
    }
}
