package com.yusufemre.ivirbyyuinn.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufemre.ivirbyyuinn.data.SnoozeDurationPreferencesRepository
import com.yusufemre.ivirbyyuinn.domain.GetSnoozeDurationUseCase
import com.yusufemre.ivirbyyuinn.domain.SetSnoozeDurationUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// QuietHoursViewModel ile AYNI desen: küçük, tek bir tercihe bakan,
// ayrı bir ViewModel.
class SnoozeDurationViewModel(
    getSnoozeDurationUseCase: GetSnoozeDurationUseCase,
    private val setSnoozeDurationUseCase: SetSnoozeDurationUseCase,
) : ViewModel() {

    val snoozeMinutes: StateFlow<Int> = getSnoozeDurationUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SnoozeDurationPreferencesRepository.DEFAULT_SNOOZE_MINUTES)

    fun setSnoozeMinutes(minutes: Int) {
        setSnoozeDurationUseCase(minutes)
    }
}
