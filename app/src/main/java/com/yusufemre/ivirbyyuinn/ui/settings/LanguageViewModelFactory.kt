package com.yusufemre.ivirbyyuinn.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yusufemre.ivirbyyuinn.domain.GetLanguagePreferenceUseCase
import com.yusufemre.ivirbyyuinn.domain.SetLanguagePreferenceUseCase

class LanguageViewModelFactory(
    private val getLanguagePreferenceUseCase: GetLanguagePreferenceUseCase,
    private val setLanguagePreferenceUseCase: SetLanguagePreferenceUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LanguageViewModel(getLanguagePreferenceUseCase, setLanguagePreferenceUseCase) as T
    }
}
