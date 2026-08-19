package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.AppLockRepository

class VerifyAppLockPinUseCase(private val repository: AppLockRepository) {
    operator fun invoke(pin: String): Boolean = repository.verifyPin(pin)
}
