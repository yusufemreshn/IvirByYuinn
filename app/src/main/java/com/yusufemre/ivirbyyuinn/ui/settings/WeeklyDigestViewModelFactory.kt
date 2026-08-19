package com.yusufemre.ivirbyyuinn.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yusufemre.ivirbyyuinn.domain.GetWeeklyDigestOptInUseCase
import com.yusufemre.ivirbyyuinn.domain.SetWeeklyDigestOptInUseCase

class WeeklyDigestViewModelFactory(
    private val getWeeklyDigestOptInUseCase: GetWeeklyDigestOptInUseCase,
    private val setWeeklyDigestOptInUseCase: SetWeeklyDigestOptInUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WeeklyDigestViewModel(getWeeklyDigestOptInUseCase, setWeeklyDigestOptInUseCase) as T
    }
}
