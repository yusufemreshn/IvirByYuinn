package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.ConfirmationPreferences
import com.yusufemre.ivirbyyuinn.data.ConfirmationPreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetConfirmationPreferencesUseCase(private val repository: ConfirmationPreferencesRepository) {
    operator fun invoke(): Flow<ConfirmationPreferences> = repository.getConfirmationPreferences()
}
