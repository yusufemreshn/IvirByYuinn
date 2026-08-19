package com.yusufemre.ivirbyyuinn.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufemre.ivirbyyuinn.data.AppColorTheme
import com.yusufemre.ivirbyyuinn.domain.GetAppColorThemeUseCase
import com.yusufemre.ivirbyyuinn.domain.SetAppColorThemeUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// ThemeViewModel'in (Açık/Koyu) birebir aynı deseni, ayrı bir ViewModel
// çünkü "hangi renk teması" ve "açık mı koyu mu", birbirinden bağımsız
// iki farklı tercih.
class ColorThemeViewModel(
    getAppColorThemeUseCase: GetAppColorThemeUseCase,
    private val setAppColorThemeUseCase: SetAppColorThemeUseCase,
) : ViewModel() {

    val colorTheme: StateFlow<AppColorTheme> = getAppColorThemeUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppColorTheme.TEAL)

    fun setColorTheme(theme: AppColorTheme) {
        setAppColorThemeUseCase(theme)
    }
}
