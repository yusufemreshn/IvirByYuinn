package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Task
import com.yusufemre.ivirbyyuinn.data.TaskRepository
import kotlinx.coroutines.flow.Flow

// "operator fun invoke" sayesinde bu sınıfı çağırırken getTasksUseCase(id)
// yazabiliyoruz, sanki bir fonksiyonmuş gibi. Kotlin'in UseCase'ler için
// yaygın bir alışkanlığı. Faz 2'den itibaren artık "tüm görevler" değil,
// belirli bir listenin görevleri isteniyor.
class GetTasksUseCase(private val repository: TaskRepository) {
    operator fun invoke(taskListId: Long): Flow<List<Task>> = repository.getTasksForList(taskListId)
}
