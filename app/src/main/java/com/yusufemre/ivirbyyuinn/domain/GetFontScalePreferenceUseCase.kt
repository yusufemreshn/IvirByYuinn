package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.FontScalePreference
import com.yusufemre.ivirbyyuinn.data.FontScalePreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetFontScalePreferenceUseCase(private val repository: FontScalePreferencesRepository) {
    operator fun invoke(): Flow<FontScalePreference> = repository.getFontScalePreference()
}
