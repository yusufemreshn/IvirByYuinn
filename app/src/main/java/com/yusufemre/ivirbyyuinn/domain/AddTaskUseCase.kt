package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Priority
import com.yusufemre.ivirbyyuinn.data.Recurrence
import com.yusufemre.ivirbyyuinn.data.Task
import com.yusufemre.ivirbyyuinn.data.TaskRepository
import kotlinx.coroutines.flow.first

// İlk gerçek "iş kurallarımız" burada yaşıyor: boş ya da sadece boşluktan
// oluşan bir görev eklemenin hiçbir anlamı yok, aynı listede aynı isimde
// iki görev olması da kullanıcıyı karıştırır. Bu yüzden ikisini de
// ViewModel'e veya ekrana değil, doğrudan buraya, domain katmanına
// yazdık, kural nereden çağrılırsa çağrılsın hep aynı şekilde uygulansın
// diye.
//
// Faz 3'te dönüş tipi Unit'ten Task?'e değişmişti: eklenen görevin gerçek
// id'sini bilmeden onun için bir hatırlatma alarmı kuramayız (bkz.
// ui/task/TaskViewModel.addTask). 2026-08-11'de Task?'ten TaskSaveOutcome'a
// geçti: null dönmesi tek bir "reddedildi" anlamına geliyordu, ama artık
// reddin iki farklı sebebi olabiliyor (boş başlık / isim çakışması) ve
// ViewModel bu ikisine farklı tepki vermek zorunda (bkz. TaskSaveOutcome).
class AddTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(
        title: String,
        taskListId: Long,
        categoryId: Long? = null,
        priority: Priority = Priority.MEDIUM,
        dueAt: Long? = null,
        note: String? = null,
        recurrence: Recurrence = Recurrence.NONE,
    ): TaskSaveOutcome {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return TaskSaveOutcome.BlankTitle

        // Büyük/küçük harf farkı gözetmiyoruz: "Market" ve "market"
        // kullanıcı için aynı görev, iki ayrı satır olarak eklenmesin.
        val existingTasksInList = repository.getTasksForList(taskListId).first()
        if (existingTasksInList.any { it.title.equals(trimmedTitle, ignoreCase = true) }) {
            return TaskSaveOutcome.DuplicateTitle
        }

        val task = Task(
            title = trimmedTitle,
            taskListId = taskListId,
            categoryId = categoryId,
            priority = priority,
            dueAt = dueAt,
            note = note?.trim()?.ifEmpty { null },
            recurrence = recurrence,
        )
        val newId = repository.addTask(task)
        return TaskSaveOutcome.Saved(task.copy(id = newId))
    }
}
