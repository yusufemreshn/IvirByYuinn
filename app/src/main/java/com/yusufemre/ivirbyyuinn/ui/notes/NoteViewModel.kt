package com.yusufemre.ivirbyyuinn.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufemre.ivirbyyuinn.data.Note
import com.yusufemre.ivirbyyuinn.domain.AddNoteUseCase
import com.yusufemre.ivirbyyuinn.domain.DeleteNoteUseCase
import com.yusufemre.ivirbyyuinn.domain.GetNotesUseCase
import com.yusufemre.ivirbyyuinn.domain.UpdateNoteUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(
    getNotesUseCase: GetNotesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
) : ViewModel() {

    val notes: StateFlow<List<Note>> = getNotesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addNote(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch { addNoteUseCase(content) }
    }

    fun updateNote(note: Note, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch { updateNoteUseCase(note, content) }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { deleteNoteUseCase(note) }
    }
}
