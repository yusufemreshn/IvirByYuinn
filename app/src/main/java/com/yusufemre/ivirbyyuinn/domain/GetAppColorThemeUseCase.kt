package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.AppColorTheme
import com.yusufemre.ivirbyyuinn.data.AppColorThemePreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetAppColorThemeUseCase(private val repository: AppColorThemePreferencesRepository) {
    operator fun invoke(): Flow<AppColorTheme> = repository.getColorTheme()
}
