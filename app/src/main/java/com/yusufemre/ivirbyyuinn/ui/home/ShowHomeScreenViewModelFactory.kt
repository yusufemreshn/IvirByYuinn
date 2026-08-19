package com.yusufemre.ivirbyyuinn.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yusufemre.ivirbyyuinn.domain.GetShowHomeScreenPreferenceUseCase
import com.yusufemre.ivirbyyuinn.domain.SetShowHomeScreenPreferenceUseCase

class ShowHomeScreenViewModelFactory(
    private val getShowHomeScreenPreferenceUseCase: GetShowHomeScreenPreferenceUseCase,
    private val setShowHomeScreenPreferenceUseCase: SetShowHomeScreenPreferenceUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ShowHomeScreenViewModel(getShowHomeScreenPreferenceUseCase, setShowHomeScreenPreferenceUseCase) as T
    }
}
