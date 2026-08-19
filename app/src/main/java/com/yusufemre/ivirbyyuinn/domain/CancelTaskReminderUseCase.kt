package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.notification.ReminderScheduler

class CancelTaskReminderUseCase(private val reminderScheduler: ReminderScheduler) {
    operator fun invoke(taskId: Long) {
        reminderScheduler.cancelReminder(taskId)
    }
}
