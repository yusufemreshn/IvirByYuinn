package com.yusufemre.ivirbyyuinn.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufemre.ivirbyyuinn.domain.GetWeeklyDigestOptInUseCase
import com.yusufemre.ivirbyyuinn.domain.SetWeeklyDigestOptInUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// QuietHoursViewModel/ColorThemeViewModel'le AYNI küçük desen.
class WeeklyDigestViewModel(
    getWeeklyDigestOptInUseCase: GetWeeklyDigestOptInUseCase,
    private val setWeeklyDigestOptInUseCase: SetWeeklyDigestOptInUseCase,
) : ViewModel() {

    val optedIn: StateFlow<Boolean> = getWeeklyDigestOptInUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setOptIn(enabled: Boolean) {
        setWeeklyDigestOptInUseCase(enabled)
    }
}
