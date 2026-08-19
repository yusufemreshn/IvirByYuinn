package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Task
import com.yusufemre.ivirbyyuinn.data.TaskRepository
import kotlinx.coroutines.flow.first

// Faz 1'de eksik kalan "görev düzenleme" burada tamamlanıyor. AddTaskUseCase
// gibi burada da boş başlık ve isim çakışması kurallarını uyguluyoruz:
// düzenlerken de başlığı boşaltmak ya da aynı listedeki başka bir görevle
// aynı isme çevirmek anlamsız olurdu.
//
// Faz 3'te dönüş tipi Task?'e çevrilmişti: ViewModel, güncellenen görevin
// yeni dueAt'ine göre hatırlatmayı yeniden kurup kurmayacağına karar
// vermek için sonucu bilmek zorunda. 2026-08-11'de Task?'ten
// TaskSaveOutcome'a geçti, bkz. AddTaskUseCase'deki aynı değişikliğin
// gerekçesi.
class UpdateTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task): TaskSaveOutcome {
        val trimmedTitle = task.title.trim()
        if (trimmedTitle.isEmpty()) return TaskSaveOutcome.BlankTitle

        // Kendi id'sini karşılaştırmadan hariç tutuyoruz: yoksa görev
        // hiç başlığı değişmeden kaydedilirken bile "kendisiyle çakışıyor"
        // görünürdü.
        val existingTasksInList = repository.getTasksForList(task.taskListId).first()
        val hasDuplicate = existingTasksInList.any {
            it.id != task.id && it.title.equals(trimmedTitle, ignoreCase = true)
        }
        if (hasDuplicate) return TaskSaveOutcome.DuplicateTitle

        val updatedTask = task.copy(title = trimmedTitle)
        repository.updateTask(updatedTask)
        return TaskSaveOutcome.Saved(updatedTask)
    }
}
