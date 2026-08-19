package com.yusufemre.ivirbyyuinn.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yusufemre.ivirbyyuinn.domain.GetTaskListsUseCase
import com.yusufemre.ivirbyyuinn.domain.GetTasksUseCase

class HomeViewModelFactory(
    private val getTaskListsUseCase: GetTaskListsUseCase,
    private val getTasksUseCase: GetTasksUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(getTaskListsUseCase, getTasksUseCase) as T
    }
}
