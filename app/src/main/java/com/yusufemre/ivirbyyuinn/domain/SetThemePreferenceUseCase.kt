package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.ThemePreference
import com.yusufemre.ivirbyyuinn.data.ThemePreferencesRepository

class SetThemePreferenceUseCase(private val repository: ThemePreferencesRepository) {
    operator fun invoke(preference: ThemePreference) {
        repository.setThemePreference(preference)
    }
}
