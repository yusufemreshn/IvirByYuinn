package com.yusufemre.ivirbyyuinn.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yusufemre.ivirbyyuinn.domain.GetQuietHoursUseCase
import com.yusufemre.ivirbyyuinn.domain.SetQuietHoursUseCase

class QuietHoursViewModelFactory(
    private val getQuietHoursUseCase: GetQuietHoursUseCase,
    private val setQuietHoursUseCase: SetQuietHoursUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return QuietHoursViewModel(getQuietHoursUseCase, setQuietHoursUseCase) as T
    }
}
