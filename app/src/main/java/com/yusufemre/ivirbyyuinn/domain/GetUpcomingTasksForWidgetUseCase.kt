package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Task
import com.yusufemre.ivirbyyuinn.data.TaskRepository

// Widget, Compose'un normal Flow/StateFlow dünyasının dışında yaşıyor:
// GlanceAppWidget her güncellemesinde bu tek seferlik sorguyu çağırıp
// ekranını yeniden çiziyor, sürekli bir dinleme yapmıyor.
class GetUpcomingTasksForWidgetUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(): List<Task> = repository.getUpcomingTasksForWidget()
}
