package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.SnoozeDurationPreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetSnoozeDurationUseCase(private val repository: SnoozeDurationPreferencesRepository) {
    operator fun invoke(): Flow<Int> = repository.getSnoozeMinutes()
}
