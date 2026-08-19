package com.yusufemre.ivirbyyuinn.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yusufemre.ivirbyyuinn.domain.GetFontScalePreferenceUseCase
import com.yusufemre.ivirbyyuinn.domain.SetFontScalePreferenceUseCase

class FontScaleViewModelFactory(
    private val getFontScalePreferenceUseCase: GetFontScalePreferenceUseCase,
    private val setFontScalePreferenceUseCase: SetFontScalePreferenceUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FontScaleViewModel(getFontScalePreferenceUseCase, setFontScalePreferenceUseCase) as T
    }
}
