package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.ShowHomeScreenPreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetShowHomeScreenPreferenceUseCase(private val repository: ShowHomeScreenPreferencesRepository) {
    operator fun invoke(): Flow<Boolean> = repository.getShowHomeScreenPreference()
}
