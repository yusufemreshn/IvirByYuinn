package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.FontScalePreference
import com.yusufemre.ivirbyyuinn.data.FontScalePreferencesRepository

class SetFontScalePreferenceUseCase(private val repository: FontScalePreferencesRepository) {
    operator fun invoke(preference: FontScalePreference) {
        repository.setFontScalePreference(preference)
    }
}
