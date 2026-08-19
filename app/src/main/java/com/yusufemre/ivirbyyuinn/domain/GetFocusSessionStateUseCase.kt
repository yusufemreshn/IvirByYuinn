package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.FocusSessionPreferencesRepository
import com.yusufemre.ivirbyyuinn.data.FocusSessionState

class GetFocusSessionStateUseCase(private val repository: FocusSessionPreferencesRepository) {
    operator fun invoke(): FocusSessionState = repository.getState()
}
