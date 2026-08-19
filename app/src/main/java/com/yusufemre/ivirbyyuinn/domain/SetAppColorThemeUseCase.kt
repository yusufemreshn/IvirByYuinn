package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.AppColorTheme
import com.yusufemre.ivirbyyuinn.data.AppColorThemePreferencesRepository

class SetAppColorThemeUseCase(private val repository: AppColorThemePreferencesRepository) {
    operator fun invoke(theme: AppColorTheme) {
        repository.setColorTheme(theme)
    }
}
