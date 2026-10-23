package com.yusufemre.ivirbyyuinn.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yusufemre.ivirbyyuinn.domain.GetConfirmationPreferencesUseCase
import com.yusufemre.ivirbyyuinn.domain.SetConfirmationPreferencesUseCase

class ConfirmationSettingsViewModelFactory(
    private val getConfirmationPreferencesUseCase: GetConfirmationPreferencesUseCase,
    private val setConfirmationPreferencesUseCase: SetConfirmationPreferencesUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ConfirmationSettingsViewModel(getConfirmationPreferencesUseCase, setConfirmationPreferencesUseCase) as T
    }
}
