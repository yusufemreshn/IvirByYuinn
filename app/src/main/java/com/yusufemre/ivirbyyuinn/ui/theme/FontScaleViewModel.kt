package com.yusufemre.ivirbyyuinn.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufemre.ivirbyyuinn.data.FontScalePreference
import com.yusufemre.ivirbyyuinn.domain.GetFontScalePreferenceUseCase
import com.yusufemre.ivirbyyuinn.domain.SetFontScalePreferenceUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// ThemeViewModel'in birebir aynı deseni, bkz. oradaki yorumlar.
class FontScaleViewModel(
    getFontScalePreferenceUseCase: GetFontScalePreferenceUseCase,
    private val setFontScalePreferenceUseCase: SetFontScalePreferenceUseCase,
) : ViewModel() {

    val fontScalePreference: StateFlow<FontScalePreference> = getFontScalePreferenceUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FontScalePreference.MEDIUM)

    fun setFontScalePreference(preference: FontScalePreference) {
        setFontScalePreferenceUseCase(preference)
    }
}
