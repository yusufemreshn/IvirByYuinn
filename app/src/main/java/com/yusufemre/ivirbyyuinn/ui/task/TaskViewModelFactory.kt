package com.yusufemre.ivirbyyuinn.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yusufemre.ivirbyyuinn.domain.AddCategoryUseCase
import com.yusufemre.ivirbyyuinn.domain.AddSubtaskUseCase
import com.yusufemre.ivirbyyuinn.domain.AddTaskListUseCase
import com.yusufemre.ivirbyyuinn.domain.AddTaskUseCase
import com.yusufemre.ivirbyyuinn.domain.ArchiveTaskUseCase
import com.yusufemre.ivirbyyuinn.domain.AutoArchiveCompletedTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.CancelTaskReminderUseCase
import com.yusufemre.ivirbyyuinn.domain.DeleteCategoryUseCase
import com.yusufemre.ivirbyyuinn.domain.DeleteSubtaskUseCase
import com.yusufemre.ivirbyyuinn.domain.DeleteTaskListUseCase
import com.yusufemre.ivirbyyuinn.domain.DeleteTaskTemplateUseCase
import com.yusufemre.ivirbyyuinn.domain.DeleteTaskUseCase
import com.yusufemre.ivirbyyuinn.domain.FilterAndSortTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.GetAllSubtasksUseCase
import com.yusufemre.ivirbyyuinn.domain.GetArchivedTaskListsUseCase
import com.yusufemre.ivirbyyuinn.domain.GetArchivedTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.GetCategoriesUseCase
import com.yusufemre.ivirbyyuinn.domain.GetCurrentStreakUseCase
import com.yusufemre.ivirbyyuinn.domain.GetDeletedTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.GetSmartViewTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.GetSubtasksUseCase
import com.yusufemre.ivirbyyuinn.domain.GetTaskListsUseCase
import com.yusufemre.ivirbyyuinn.domain.GetTaskTemplatesUseCase
import com.yusufemre.ivirbyyuinn.domain.GetTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.GetWeeklyCompletionStatsUseCase
import com.yusufemre.ivirbyyuinn.domain.ParseNaturalLanguageDateUseCase
import com.yusufemre.ivirbyyuinn.domain.PermanentlyDeleteAllTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.PermanentlyDeleteTaskUseCase
import com.yusufemre.ivirbyyuinn.domain.RefreshWidgetUseCase
import com.yusufemre.ivirbyyuinn.domain.ReorderTaskListsUseCase
import com.yusufemre.ivirbyyuinn.domain.ReorderTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.RestoreTaskUseCase
import com.yusufemre.ivirbyyuinn.domain.SaveTaskTemplateUseCase
import com.yusufemre.ivirbyyuinn.domain.ScheduleTaskReminderUseCase
import com.yusufemre.ivirbyyuinn.domain.SetTaskListArchivedUseCase
import com.yusufemre.ivirbyyuinn.domain.ToggleSubtaskDoneUseCase
import com.yusufemre.ivirbyyuinn.domain.ToggleTaskDoneUseCase
import com.yusufemre.ivirbyyuinn.domain.UnarchiveTaskUseCase
import com.yusufemre.ivirbyyuinn.domain.UpdateTaskUseCase

// TaskViewModel'in boş bir constructor'ı yok (bir sürü UseCase'e ihtiyacı
// var), bu yüzden Android'e "bunu nasıl kuracağını" bu factory üzerinden
// öğretiyoruz. .NET'te bir sınıfı elle "new" ile constructor injection
// yaparak oluşturmaya çok benziyor.
class TaskViewModelFactory(
    private val getTasksUseCase: GetTasksUseCase,
    private val filterAndSortTasksUseCase: FilterAndSortTasksUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val toggleTaskDoneUseCase: ToggleTaskDoneUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val restoreTaskUseCase: RestoreTaskUseCase,
    private val permanentlyDeleteTaskUseCase: PermanentlyDeleteTaskUseCase,
    private val permanentlyDeleteAllTasksUseCase: PermanentlyDeleteAllTasksUseCase,
    private val getDeletedTasksUseCase: GetDeletedTasksUseCase,
    private val archiveTaskUseCase: ArchiveTaskUseCase,
    private val unarchiveTaskUseCase: UnarchiveTaskUseCase,
    private val getArchivedTasksUseCase: GetArchivedTasksUseCase,
    private val autoArchiveCompletedTasksUseCase: AutoArchiveCompletedTasksUseCase,
    private val reorderTasksUseCase: ReorderTasksUseCase,
    private val getSmartViewTasksUseCase: GetSmartViewTasksUseCase,
    private val getSubtasksUseCase: GetSubtasksUseCase,
    private val getAllSubtasksUseCase: GetAllSubtasksUseCase,
    private val addSubtaskUseCase: AddSubtaskUseCase,
    private val toggleSubtaskDoneUseCase: ToggleSubtaskDoneUseCase,
    private val deleteSubtaskUseCase: DeleteSubtaskUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val getTaskListsUseCase: GetTaskListsUseCase,
    private val addTaskListUseCase: AddTaskListUseCase,
    private val deleteTaskListUseCase: DeleteTaskListUseCase,
    private val getArchivedTaskListsUseCase: GetArchivedTaskListsUseCase,
    private val reorderTaskListsUseCase: ReorderTaskListsUseCase,
    private val setTaskListArchivedUseCase: SetTaskListArchivedUseCase,
    private val scheduleTaskReminderUseCase: ScheduleTaskReminderUseCase,
    private val cancelTaskReminderUseCase: CancelTaskReminderUseCase,
    private val getWeeklyCompletionStatsUseCase: GetWeeklyCompletionStatsUseCase,
    private val getCurrentStreakUseCase: GetCurrentStreakUseCase,
    private val getTaskTemplatesUseCase: GetTaskTemplatesUseCase,
    private val saveTaskTemplateUseCase: SaveTaskTemplateUseCase,
    private val deleteTaskTemplateUseCase: DeleteTaskTemplateUseCase,
    private val parseNaturalLanguageDateUseCase: ParseNaturalLanguageDateUseCase,
    private val refreshWidgetUseCase: RefreshWidgetUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TaskViewModel(
            getTasksUseCase = getTasksUseCase,
            filterAndSortTasksUseCase = filterAndSortTasksUseCase,
            addTaskUseCase = addTaskUseCase,
            updateTaskUseCase = updateTaskUseCase,
            toggleTaskDoneUseCase = toggleTaskDoneUseCase,
            deleteTaskUseCase = deleteTaskUseCase,
            restoreTaskUseCase = restoreTaskUseCase,
            permanentlyDeleteTaskUseCase = permanentlyDeleteTaskUseCase,
            permanentlyDeleteAllTasksUseCase = permanentlyDeleteAllTasksUseCase,
            getDeletedTasksUseCase = getDeletedTasksUseCase,
            archiveTaskUseCase = archiveTaskUseCase,
            unarchiveTaskUseCase = unarchiveTaskUseCase,
            getArchivedTasksUseCase = getArchivedTasksUseCase,
            autoArchiveCompletedTasksUseCase = autoArchiveCompletedTasksUseCase,
            reorderTasksUseCase = reorderTasksUseCase,
            getSmartViewTasksUseCase = getSmartViewTasksUseCase,
            getSubtasksUseCase = getSubtasksUseCase,
            getAllSubtasksUseCase = getAllSubtasksUseCase,
            addSubtaskUseCase = addSubtaskUseCase,
            toggleSubtaskDoneUseCase = toggleSubtaskDoneUseCase,
            deleteSubtaskUseCase = deleteSubtaskUseCase,
            getCategoriesUseCase = getCategoriesUseCase,
            addCategoryUseCase = addCategoryUseCase,
            deleteCategoryUseCase = deleteCategoryUseCase,
            getTaskListsUseCase = getTaskListsUseCase,
            addTaskListUseCase = addTaskListUseCase,
            deleteTaskListUseCase = deleteTaskListUseCase,
            getArchivedTaskListsUseCase = getArchivedTaskListsUseCase,
            reorderTaskListsUseCase = reorderTaskListsUseCase,
            setTaskListArchivedUseCase = setTaskListArchivedUseCase,
            scheduleTaskReminderUseCase = scheduleTaskReminderUseCase,
            cancelTaskReminderUseCase = cancelTaskReminderUseCase,
            getWeeklyCompletionStatsUseCase = getWeeklyCompletionStatsUseCase,
            getCurrentStreakUseCase = getCurrentStreakUseCase,
            getTaskTemplatesUseCase = getTaskTemplatesUseCase,
            saveTaskTemplateUseCase = saveTaskTemplateUseCase,
            deleteTaskTemplateUseCase = deleteTaskTemplateUseCase,
            parseNaturalLanguageDateUseCase = parseNaturalLanguageDateUseCase,
            refreshWidgetUseCase = refreshWidgetUseCase,
        ) as T
    }
}
