package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.LanguagePreference
import com.yusufemre.ivirbyyuinn.data.LanguagePreferencesRepository

class SetLanguagePreferenceUseCase(private val repository: LanguagePreferencesRepository) {
    operator fun invoke(preference: LanguagePreference) {
        repository.setLanguagePreference(preference)
    }
}
