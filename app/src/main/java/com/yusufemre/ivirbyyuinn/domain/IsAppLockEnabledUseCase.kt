package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.AppLockRepository
import kotlinx.coroutines.flow.Flow

class IsAppLockEnabledUseCase(private val repository: AppLockRepository) {
    operator fun invoke(): Flow<Boolean> = repository.isLockEnabled()
}
