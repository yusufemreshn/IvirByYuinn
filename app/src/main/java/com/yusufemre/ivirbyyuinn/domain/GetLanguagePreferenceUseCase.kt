package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.LanguagePreference
import com.yusufemre.ivirbyyuinn.data.LanguagePreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetLanguagePreferenceUseCase(private val repository: LanguagePreferencesRepository) {
    operator fun invoke(): Flow<LanguagePreference> = repository.getLanguagePreference()
}
