package com.yusufemre.ivirbyyuinn.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yusufemre.ivirbyyuinn.domain.GetAppColorThemeUseCase
import com.yusufemre.ivirbyyuinn.domain.SetAppColorThemeUseCase

class ColorThemeViewModelFactory(
    private val getAppColorThemeUseCase: GetAppColorThemeUseCase,
    private val setAppColorThemeUseCase: SetAppColorThemeUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ColorThemeViewModel(getAppColorThemeUseCase, setAppColorThemeUseCase) as T
    }
}
