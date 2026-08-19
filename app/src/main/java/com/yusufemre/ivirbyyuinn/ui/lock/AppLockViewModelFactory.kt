package com.yusufemre.ivirbyyuinn.ui.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yusufemre.ivirbyyuinn.domain.DisableAppLockUseCase
import com.yusufemre.ivirbyyuinn.domain.IsAppLockEnabledUseCase
import com.yusufemre.ivirbyyuinn.domain.SetAppLockPinUseCase
import com.yusufemre.ivirbyyuinn.domain.VerifyAppLockPinUseCase

class AppLockViewModelFactory(
    private val isAppLockEnabledUseCase: IsAppLockEnabledUseCase,
    private val verifyAppLockPinUseCase: VerifyAppLockPinUseCase,
    private val setAppLockPinUseCase: SetAppLockPinUseCase,
    private val disableAppLockUseCase: DisableAppLockUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppLockViewModel(
            isAppLockEnabledUseCase,
            verifyAppLockPinUseCase,
            setAppLockPinUseCase,
            disableAppLockUseCase,
        ) as T
    }
}
