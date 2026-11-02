package com.yusufemre.ivirbyyuinn.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yusufemre.ivirbyyuinn.domain.AddNoteUseCase
import com.yusufemre.ivirbyyuinn.domain.DeleteNoteUseCase
import com.yusufemre.ivirbyyuinn.domain.GetNotesUseCase
import com.yusufemre.ivirbyyuinn.domain.UpdateNoteUseCase

class NoteViewModelFactory(
    private val getNotesUseCase: GetNotesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NoteViewModel(
            getNotesUseCase = getNotesUseCase,
            addNoteUseCase = addNoteUseCase,
            updateNoteUseCase = updateNoteUseCase,
            deleteNoteUseCase = deleteNoteUseCase,
        ) as T
    }
}
