package com.yusufemre.ivirbyyuinn.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufemre.ivirbyyuinn.data.QuietHours
import com.yusufemre.ivirbyyuinn.domain.GetQuietHoursUseCase
import com.yusufemre.ivirbyyuinn.domain.SetQuietHoursUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// ColorThemeViewModel'in birebir aynı deseni: küçük, tek bir tercihe
// (bu sefer üç alanlı bir veri sınıfına) bakan, ayrı bir ViewModel.
class QuietHoursViewModel(
    getQuietHoursUseCase: GetQuietHoursUseCase,
    private val setQuietHoursUseCase: SetQuietHoursUseCase,
) : ViewModel() {

    val quietHours: StateFlow<QuietHours> = getQuietHoursUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuietHours())

    fun setQuietHours(quietHours: QuietHours) {
        setQuietHoursUseCase(quietHours)
    }
}
