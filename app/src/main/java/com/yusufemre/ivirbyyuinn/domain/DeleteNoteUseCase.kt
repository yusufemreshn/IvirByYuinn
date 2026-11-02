package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Note
import com.yusufemre.ivirbyyuinn.data.NoteRepository

class DeleteNoteUseCase(private val noteRepository: NoteRepository) {
    suspend operator fun invoke(note: Note) {
        noteRepository.delete(note)
    }
}
