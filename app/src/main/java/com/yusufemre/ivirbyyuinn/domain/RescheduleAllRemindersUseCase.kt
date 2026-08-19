package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.TaskRepository
import com.yusufemre.ivirbyyuinn.notification.ReminderScheduler

// Sadece cihaz yeniden başladığında (bkz. notification/BootCompletedReceiver)
// çağrılıyor: AlarmManager'daki alarmlar reboot'ta silindiği için, hâlâ
// son tarihi olan ve tamamlanmamış tüm görevlerin hatırlatmalarını
// tek tek yeniden kuruyoruz.
class RescheduleAllRemindersUseCase(
    private val repository: TaskRepository,
    private val reminderScheduler: ReminderScheduler,
) {
    suspend operator fun invoke() {
        repository.getTasksWithPendingReminders().forEach { task ->
            reminderScheduler.scheduleReminder(task)
        }
    }
}
