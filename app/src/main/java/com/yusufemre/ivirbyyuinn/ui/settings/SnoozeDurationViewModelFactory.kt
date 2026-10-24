package com.yusufemre.ivirbyyuinn.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yusufemre.ivirbyyuinn.domain.GetSnoozeDurationUseCase
import com.yusufemre.ivirbyyuinn.domain.SetSnoozeDurationUseCase

class SnoozeDurationViewModelFactory(
    private val getSnoozeDurationUseCase: GetSnoozeDurationUseCase,
    private val setSnoozeDurationUseCase: SetSnoozeDurationUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SnoozeDurationViewModel(getSnoozeDurationUseCase, setSnoozeDurationUseCase) as T
    }
}
