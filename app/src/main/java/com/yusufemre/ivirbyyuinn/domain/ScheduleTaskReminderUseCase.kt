package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Task
import com.yusufemre.ivirbyyuinn.notification.ReminderScheduler

// ReminderScheduler zaten "dueAt null mu / geçmişte mi" kontrolünü kendi
// içinde yapıyor (bkz. notification/ReminderScheduler): burada eklediğimiz
// tek iş kuralı: tamamlanmış bir görev için hatırlatma kurmanın anlamı yok.
class ScheduleTaskReminderUseCase(private val reminderScheduler: ReminderScheduler) {
    operator fun invoke(task: Task) {
        if (task.isDone) return
        reminderScheduler.scheduleReminder(task)
    }
}
