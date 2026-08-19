package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.WeeklyDigestPreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetWeeklyDigestOptInUseCase(private val repository: WeeklyDigestPreferencesRepository) {
    operator fun invoke(): Flow<Boolean> = repository.isOptedIn()
}
