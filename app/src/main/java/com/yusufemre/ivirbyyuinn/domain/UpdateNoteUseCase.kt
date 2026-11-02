package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Note
import com.yusufemre.ivirbyyuinn.data.NoteRepository

class UpdateNoteUseCase(private val noteRepository: NoteRepository) {
    suspend operator fun invoke(note: Note, content: String) {
        noteRepository.update(note.copy(content = content, updatedAt = System.currentTimeMillis()))
    }
}
