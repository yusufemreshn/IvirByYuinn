package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.TaskListEntity
import com.yusufemre.ivirbyyuinn.data.TaskListRepository
import com.yusufemre.ivirbyyuinn.data.TaskRepository
import kotlinx.coroutines.flow.first

// Kullanıcı isteğiyle eklendi. Arşivleme'den (bkz. SetTaskListArchivedUseCase)
// FARKLI olarak burası GERÇEKTEN, KALICI olarak siliyor: Task.taskListId'nin
// foreign key'i CASCADE olduğu için (bkz. Task.kt) listedeki tüm görevler
// (çöp kutusundakiler dahil) de birlikte gidiyor, soft-delete/undo yolu YOK.
//
// İki güvenlik önlemi:
// 1) Aktif (arşivlenmemiş) son listeyi silmeye izin vermiyoruz: uygulama
//    her zaman en az bir seçilebilir listenin var olmasını varsayıyor
//    (TaskViewModel'in "ilk listeyi otomatik seç" mantığı).
// 2) CASCADE, Room'un FK'sı üzerinden veritabanı seviyesinde olduğu için
//    silinen görevlerin olası hatırlatma alarmlarından (AlarmManager) HABERİ
//    YOK: silmeden ÖNCE görevleri tek tek çekip alarmlarını iptal ediyoruz,
//    yoksa artık var olmayan bir görev için "hayalet" bir bildirim gelebilirdi.
class DeleteTaskListUseCase(
    private val taskListRepository: TaskListRepository,
    private val taskRepository: TaskRepository,
    private val cancelTaskReminderUseCase: CancelTaskReminderUseCase,
) {
    suspend operator fun invoke(taskList: TaskListEntity): Boolean {
        if (!taskList.isArchived) {
            val activeLists = taskListRepository.getAllTaskListsOnce()
            if (activeLists.size <= 1) return false
        }

        taskRepository.getTasksForList(taskList.id).first().forEach { task ->
            cancelTaskReminderUseCase(task.id)
        }
        taskListRepository.deleteTaskList(taskList)
        return true
    }
}
