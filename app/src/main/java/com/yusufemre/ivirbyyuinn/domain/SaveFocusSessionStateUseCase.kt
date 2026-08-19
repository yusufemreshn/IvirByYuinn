package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.FocusSessionPreferencesRepository
import com.yusufemre.ivirbyyuinn.data.FocusSessionState

class SaveFocusSessionStateUseCase(private val repository: FocusSessionPreferencesRepository) {
    operator fun invoke(state: FocusSessionState) = repository.saveState(state)
}
