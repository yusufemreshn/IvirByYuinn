package com.yusufemre.ivirbyyuinn.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yusufemre.ivirbyyuinn.domain.GetThemePreferenceUseCase
import com.yusufemre.ivirbyyuinn.domain.SetThemePreferenceUseCase

class ThemeViewModelFactory(
    private val getThemePreferenceUseCase: GetThemePreferenceUseCase,
    private val setThemePreferenceUseCase: SetThemePreferenceUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ThemeViewModel(getThemePreferenceUseCase, setThemePreferenceUseCase) as T
    }
}
