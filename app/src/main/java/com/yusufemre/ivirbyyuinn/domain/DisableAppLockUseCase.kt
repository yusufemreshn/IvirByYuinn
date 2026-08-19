package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.AppLockRepository

// Kilidi kapatmak için de doğru PIN'i bilmek gerekiyor: yoksa telefonu
// eline alan biri Ayarlar'a girip kilidi tek dokunuşla kapatabilirdi,
// bu da kilidin anlamını tamamen ortadan kaldırırdı.
class DisableAppLockUseCase(private val repository: AppLockRepository) {
    operator fun invoke(currentPin: String): Boolean {
        if (!repository.verifyPin(currentPin)) return false
        repository.disableLock()
        return true
    }
}
