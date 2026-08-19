package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.QuietHours
import com.yusufemre.ivirbyyuinn.data.QuietHoursPreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetQuietHoursUseCase(private val repository: QuietHoursPreferencesRepository) {
    operator fun invoke(): Flow<QuietHours> = repository.getQuietHours()
}
