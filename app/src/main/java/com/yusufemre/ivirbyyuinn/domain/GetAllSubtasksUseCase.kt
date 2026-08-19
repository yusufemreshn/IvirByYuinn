package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Subtask
import com.yusufemre.ivirbyyuinn.data.SubtaskRepository
import kotlinx.coroutines.flow.Flow

// TaskRow'larda "x/y tamamlandı" sayacını göstermek için kullanılıyor:
// GetSubtasksUseCase'den farklı olarak tek bir görevle sınırlı değil,
// tüm alt görevleri getiriyor (ViewModel bunları taskId'ye göre gruplayıp
// sayaç haline getiriyor).
class GetAllSubtasksUseCase(private val repository: SubtaskRepository) {
    operator fun invoke(): Flow<List<Subtask>> = repository.getAllSubtasks()
}
