package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Note
import com.yusufemre.ivirbyyuinn.data.NoteRepository

class AddNoteUseCase(private val noteRepository: NoteRepository) {
    suspend operator fun invoke(content: String) {
        noteRepository.insert(Note(content = content))
    }
}
