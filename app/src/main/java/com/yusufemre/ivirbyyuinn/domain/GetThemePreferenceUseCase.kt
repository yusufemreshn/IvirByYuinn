package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.ThemePreference
import com.yusufemre.ivirbyyuinn.data.ThemePreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetThemePreferenceUseCase(private val repository: ThemePreferencesRepository) {
    operator fun invoke(): Flow<ThemePreference> = repository.getThemePreference()
}
