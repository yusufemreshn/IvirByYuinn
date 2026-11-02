package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Note
import com.yusufemre.ivirbyyuinn.data.NoteRepository
import kotlinx.coroutines.flow.Flow

class GetNotesUseCase(private val noteRepository: NoteRepository) {
    operator fun invoke(): Flow<List<Note>> = noteRepository.getAll()
}
