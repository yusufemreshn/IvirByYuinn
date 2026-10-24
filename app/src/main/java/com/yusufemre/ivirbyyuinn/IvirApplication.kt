package com.yusufemre.ivirbyyuinn

import android.app.Application
import com.yusufemre.ivirbyyuinn.data.AppColorThemePreferencesRepository
import com.yusufemre.ivirbyyuinn.data.AppDatabase
import com.yusufemre.ivirbyyuinn.data.AppLockRepository
import com.yusufemre.ivirbyyuinn.data.BackupRepository
import com.yusufemre.ivirbyyuinn.data.CategoryRepository
import com.yusufemre.ivirbyyuinn.data.ConfirmationPreferencesRepository
import com.yusufemre.ivirbyyuinn.data.FocusSessionPreferencesRepository
import com.yusufemre.ivirbyyuinn.data.FontScalePreferencesRepository
import com.yusufemre.ivirbyyuinn.data.GoogleDriveBackupRepository
import com.yusufemre.ivirbyyuinn.data.GoogleDrivePreferencesRepository
import com.yusufemre.ivirbyyuinn.data.LanguagePreferencesRepository
import com.yusufemre.ivirbyyuinn.data.QuietHoursPreferencesRepository
import com.yusufemre.ivirbyyuinn.data.ShowHomeScreenPreferencesRepository
import com.yusufemre.ivirbyyuinn.data.SnoozeDurationPreferencesRepository
import com.yusufemre.ivirbyyuinn.data.SubtaskRepository
import com.yusufemre.ivirbyyuinn.data.TaskCompletionRepository
import com.yusufemre.ivirbyyuinn.data.TaskListRepository
import com.yusufemre.ivirbyyuinn.data.TaskRepository
import com.yusufemre.ivirbyyuinn.data.TaskTemplateRepository
import com.yusufemre.ivirbyyuinn.data.ThemePreferencesRepository
import com.yusufemre.ivirbyyuinn.data.WeeklyDigestPreferencesRepository
import com.yusufemre.ivirbyyuinn.domain.AddCategoryUseCase
import com.yusufemre.ivirbyyuinn.domain.AddSubtaskUseCase
import com.yusufemre.ivirbyyuinn.domain.AddTaskListUseCase
import com.yusufemre.ivirbyyuinn.domain.AddTaskUseCase
import com.yusufemre.ivirbyyuinn.domain.ArchiveTaskUseCase
import com.yusufemre.ivirbyyuinn.domain.AutoArchiveCompletedTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.CancelTaskReminderUseCase
import com.yusufemre.ivirbyyuinn.domain.CompleteGoogleDriveSignInUseCase
import com.yusufemre.ivirbyyuinn.domain.DeleteCategoryUseCase
import com.yusufemre.ivirbyyuinn.domain.DeleteSubtaskUseCase
import com.yusufemre.ivirbyyuinn.domain.DeleteTaskListUseCase
import com.yusufemre.ivirbyyuinn.domain.DeleteTaskTemplateUseCase
import com.yusufemre.ivirbyyuinn.domain.DeleteTaskUseCase
import com.yusufemre.ivirbyyuinn.domain.DisableAppLockUseCase
import com.yusufemre.ivirbyyuinn.domain.ExportBackupUseCase
import com.yusufemre.ivirbyyuinn.domain.FilterAndSortTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.GetAllSubtasksUseCase
import com.yusufemre.ivirbyyuinn.domain.GetAppColorThemeUseCase
import com.yusufemre.ivirbyyuinn.domain.GetConfirmationPreferencesUseCase
import com.yusufemre.ivirbyyuinn.domain.GetArchivedTaskListsUseCase
import com.yusufemre.ivirbyyuinn.domain.GetArchivedTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.GetCategoriesUseCase
import com.yusufemre.ivirbyyuinn.domain.GetCurrentStreakUseCase
import com.yusufemre.ivirbyyuinn.domain.GetDeletedTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.GetFocusSessionStateUseCase
import com.yusufemre.ivirbyyuinn.domain.GetFontScalePreferenceUseCase
import com.yusufemre.ivirbyyuinn.domain.GetGoogleDriveAccountEmailUseCase
import com.yusufemre.ivirbyyuinn.domain.GetGoogleDriveBackupPreferencesUseCase
import com.yusufemre.ivirbyyuinn.domain.GetGoogleDriveSignInIntentUseCase
import com.yusufemre.ivirbyyuinn.domain.GetLanguagePreferenceUseCase
import com.yusufemre.ivirbyyuinn.domain.GetQuietHoursUseCase
import com.yusufemre.ivirbyyuinn.domain.GetShowHomeScreenPreferenceUseCase
import com.yusufemre.ivirbyyuinn.domain.GetSnoozeDurationUseCase
import com.yusufemre.ivirbyyuinn.domain.GetSmartViewTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.GetSubtasksUseCase
import com.yusufemre.ivirbyyuinn.domain.GetTaskByIdUseCase
import com.yusufemre.ivirbyyuinn.domain.GetTaskListsUseCase
import com.yusufemre.ivirbyyuinn.domain.GetTaskTemplatesUseCase
import com.yusufemre.ivirbyyuinn.domain.GetTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.GetThemePreferenceUseCase
import com.yusufemre.ivirbyyuinn.domain.GetUpcomingTasksForWidgetUseCase
import com.yusufemre.ivirbyyuinn.domain.GetWeeklyCompletionStatsUseCase
import com.yusufemre.ivirbyyuinn.domain.GetWeeklyDigestOptInUseCase
import com.yusufemre.ivirbyyuinn.domain.GetWeeklyDigestSummaryUseCase
import com.yusufemre.ivirbyyuinn.domain.ImportBackupUseCase
import com.yusufemre.ivirbyyuinn.domain.IsAppLockEnabledUseCase
import com.yusufemre.ivirbyyuinn.domain.ParseNaturalLanguageDateUseCase
import com.yusufemre.ivirbyyuinn.domain.PermanentlyDeleteAllTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.PermanentlyDeleteTaskUseCase
import com.yusufemre.ivirbyyuinn.domain.ReorderTaskListsUseCase
import com.yusufemre.ivirbyyuinn.domain.RescheduleAllRemindersUseCase
import com.yusufemre.ivirbyyuinn.domain.ReorderTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.RefreshWidgetUseCase
import com.yusufemre.ivirbyyuinn.domain.RestoreBackupFromDriveUseCase
import com.yusufemre.ivirbyyuinn.domain.RestoreTaskUseCase
import com.yusufemre.ivirbyyuinn.domain.SaveFocusSessionStateUseCase
import com.yusufemre.ivirbyyuinn.domain.SaveTaskTemplateUseCase
import com.yusufemre.ivirbyyuinn.domain.ScheduleTaskReminderUseCase
import com.yusufemre.ivirbyyuinn.domain.SetAppColorThemeUseCase
import com.yusufemre.ivirbyyuinn.domain.SetAppLockPinUseCase
import com.yusufemre.ivirbyyuinn.domain.SetConfirmationPreferencesUseCase
import com.yusufemre.ivirbyyuinn.domain.SetFontScalePreferenceUseCase
import com.yusufemre.ivirbyyuinn.domain.SetGoogleDriveAutoBackupEnabledUseCase
import com.yusufemre.ivirbyyuinn.domain.SetLanguagePreferenceUseCase
import com.yusufemre.ivirbyyuinn.domain.SetQuietHoursUseCase
import com.yusufemre.ivirbyyuinn.domain.SetShowHomeScreenPreferenceUseCase
import com.yusufemre.ivirbyyuinn.domain.SetSnoozeDurationUseCase
import com.yusufemre.ivirbyyuinn.domain.SetTaskListArchivedUseCase
import com.yusufemre.ivirbyyuinn.domain.SetThemePreferenceUseCase
import com.yusufemre.ivirbyyuinn.domain.SetWeeklyDigestOptInUseCase
import com.yusufemre.ivirbyyuinn.domain.SignOutGoogleDriveUseCase
import com.yusufemre.ivirbyyuinn.domain.ToggleSubtaskDoneUseCase
import com.yusufemre.ivirbyyuinn.domain.ToggleTaskDoneUseCase
import com.yusufemre.ivirbyyuinn.domain.UnarchiveTaskUseCase
import com.yusufemre.ivirbyyuinn.domain.UpdateTaskUseCase
import com.yusufemre.ivirbyyuinn.domain.UploadBackupToDriveUseCase
import com.yusufemre.ivirbyyuinn.domain.VerifyAppLockPinUseCase
import com.yusufemre.ivirbyyuinn.notification.AutoArchiveScheduler
import com.yusufemre.ivirbyyuinn.notification.GoogleDriveBackupScheduler
import com.yusufemre.ivirbyyuinn.notification.NotificationChannels
import com.yusufemre.ivirbyyuinn.notification.ReminderScheduler
import com.yusufemre.ivirbyyuinn.notification.WeeklyDigestScheduler
import com.yusufemre.ivirbyyuinn.widget.WidgetRefresher

// Hilt kullanmadığımız için "kim neye ihtiyaç duyuyor" ilişkisini elle
// burada kuruyoruz. Uygulama açıldığı anda bir kere oluşuyor ve tüm
// ekranlar boyunca yaşıyor: .NET'teki bir DI container'a Singleton
// olarak kayıt yapmanın elle karşılığı gibi düşünebilirsin.
//
// Faz 3'te dördüncü bir katman (notification/) ve onunla ilgili
// UseCase'ler eklendi; liste daha da uzadı ama her satır hâlâ tek bir
// şeyin nasıl kurulacağını söylüyor.
class IvirApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Bildirim kanalı, bildirim göstermeden ÖNCE var olmalı: en
        // güvenli yer uygulama açılışı.
        NotificationChannels.createAll(this)
        // Faz 12b: kullanıcı daha önce haftalık özete opt-in olduysa,
        // WorkManager kaydının ayakta olduğundan emin oluyoruz:
        // schedule() KEEP politikasıyla çağrıldığı için zaten kurulu bir
        // işe DOKUNMUYOR, sadece (ör. bir sistem güncellemesi WorkManager
        // veritabanını sıfırlarsa) kayıp bir işi geri kuruyor.
        if (weeklyDigestPreferencesRepository.isOptedInNow()) {
            weeklyDigestScheduler.schedule()
        }
        // 2026-08-11: otomatik arşivleme haftalık özetin AKSİNE bir tercihe
        // bağlı DEĞİL, koşulsuz kuruluyor. schedule() KEEP politikasıyla
        // çağrıldığı için zaten ayakta olan bir işe dokunmuyor.
        autoArchiveScheduler.schedule()
        // weeklyDigestPreferencesRepository ile AYNI gerekçe: kullanıcı
        // daha önce Drive otomatik yedeklemeyi açtıysa WorkManager kaydının
        // ayakta olduğundan emin oluyoruz.
        if (googleDrivePreferencesRepository.isAutoBackupEnabledNow()) {
            googleDriveBackupScheduler.schedule()
        }
    }

    // by lazy sayesinde bunlar gerçekten ilk kullanıldığı anda kuruluyor,
    // uygulama açılışında boş yere hepsini bir anda oluşturmuyoruz.
    private val database by lazy { AppDatabase.getInstance(this) }

    private val taskRepository by lazy { TaskRepository(database.taskDao()) }
    private val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }
    private val taskListRepository by lazy { TaskListRepository(database.taskListDao()) }
    private val subtaskRepository by lazy { SubtaskRepository(database.subtaskDao()) }
    private val taskCompletionRepository by lazy { TaskCompletionRepository(database.taskCompletionDao()) }
    private val quietHoursPreferencesRepository by lazy { QuietHoursPreferencesRepository(this) }
    private val reminderScheduler by lazy { ReminderScheduler(this, quietHoursPreferencesRepository) }
    private val widgetRefresher by lazy { WidgetRefresher(this) }
    private val themePreferencesRepository by lazy { ThemePreferencesRepository(this) }
    private val fontScalePreferencesRepository by lazy { FontScalePreferencesRepository(this) }
    private val showHomeScreenPreferencesRepository by lazy { ShowHomeScreenPreferencesRepository(this) }
    private val appLockRepository by lazy { AppLockRepository(this) }
    private val languagePreferencesRepository by lazy { LanguagePreferencesRepository(this) }
    private val appColorThemePreferencesRepository by lazy { AppColorThemePreferencesRepository(this) }
    private val taskTemplateRepository by lazy { TaskTemplateRepository(database.taskTemplateDao()) }
    private val weeklyDigestPreferencesRepository by lazy { WeeklyDigestPreferencesRepository(this) }
    private val weeklyDigestScheduler by lazy { WeeklyDigestScheduler(this) }
    private val autoArchiveScheduler by lazy { AutoArchiveScheduler(this) }
    private val focusSessionPreferencesRepository by lazy { FocusSessionPreferencesRepository(this) }
    private val confirmationPreferencesRepository by lazy { ConfirmationPreferencesRepository(this) }
    private val snoozeDurationPreferencesRepository by lazy { SnoozeDurationPreferencesRepository(this) }
    private val googleDriveBackupRepository by lazy { GoogleDriveBackupRepository(this) }
    private val googleDrivePreferencesRepository by lazy { GoogleDrivePreferencesRepository(this) }
    private val googleDriveBackupScheduler by lazy { GoogleDriveBackupScheduler(this) }
    // Faz 6: beş tabloyu birden ilgilendirdiği için diğer repository'lere
    // ek olarak kuruluyor, onların yerine geçmiyor.
    private val backupRepository by lazy {
        BackupRepository(
            database = database,
            taskListRepository = taskListRepository,
            categoryRepository = categoryRepository,
            taskRepository = taskRepository,
            subtaskRepository = subtaskRepository,
            taskCompletionRepository = taskCompletionRepository,
        )
    }

    val getTasksUseCase by lazy { GetTasksUseCase(taskRepository) }
    val filterAndSortTasksUseCase by lazy { FilterAndSortTasksUseCase() }
    val addTaskUseCase by lazy { AddTaskUseCase(taskRepository) }
    val updateTaskUseCase by lazy { UpdateTaskUseCase(taskRepository) }
    val toggleTaskDoneUseCase by lazy { ToggleTaskDoneUseCase(taskRepository, taskCompletionRepository) }
    val deleteTaskUseCase by lazy { DeleteTaskUseCase(taskRepository) }
    val restoreTaskUseCase by lazy { RestoreTaskUseCase(taskRepository) }
    val permanentlyDeleteTaskUseCase by lazy { PermanentlyDeleteTaskUseCase(taskRepository) }
    val permanentlyDeleteAllTasksUseCase by lazy { PermanentlyDeleteAllTasksUseCase(taskRepository) }
    val getDeletedTasksUseCase by lazy { GetDeletedTasksUseCase(taskRepository) }
    val archiveTaskUseCase by lazy { ArchiveTaskUseCase(taskRepository) }
    val unarchiveTaskUseCase by lazy { UnarchiveTaskUseCase(taskRepository) }
    val getArchivedTasksUseCase by lazy { GetArchivedTasksUseCase(taskRepository) }
    val autoArchiveCompletedTasksUseCase by lazy { AutoArchiveCompletedTasksUseCase(taskRepository) }
    val reorderTasksUseCase by lazy { ReorderTasksUseCase(taskRepository) }
    val getSmartViewTasksUseCase by lazy { GetSmartViewTasksUseCase(taskRepository) }
    val getTaskByIdUseCase by lazy { GetTaskByIdUseCase(taskRepository) }

    val getSubtasksUseCase by lazy { GetSubtasksUseCase(subtaskRepository) }
    val getAllSubtasksUseCase by lazy { GetAllSubtasksUseCase(subtaskRepository) }
    val addSubtaskUseCase by lazy { AddSubtaskUseCase(subtaskRepository) }
    val toggleSubtaskDoneUseCase by lazy { ToggleSubtaskDoneUseCase(subtaskRepository) }
    val deleteSubtaskUseCase by lazy { DeleteSubtaskUseCase(subtaskRepository) }

    val getCategoriesUseCase by lazy { GetCategoriesUseCase(categoryRepository) }
    val addCategoryUseCase by lazy { AddCategoryUseCase(categoryRepository) }
    val deleteCategoryUseCase by lazy { DeleteCategoryUseCase(categoryRepository) }

    val getTaskListsUseCase by lazy { GetTaskListsUseCase(taskListRepository) }
    val addTaskListUseCase by lazy { AddTaskListUseCase(taskListRepository) }
    val deleteTaskListUseCase by lazy { DeleteTaskListUseCase(taskListRepository, taskRepository, cancelTaskReminderUseCase) }
    val getArchivedTaskListsUseCase by lazy { GetArchivedTaskListsUseCase(taskListRepository) }
    val reorderTaskListsUseCase by lazy { ReorderTaskListsUseCase(taskListRepository) }
    val setTaskListArchivedUseCase by lazy { SetTaskListArchivedUseCase(taskListRepository) }

    val scheduleTaskReminderUseCase by lazy { ScheduleTaskReminderUseCase(reminderScheduler) }
    val cancelTaskReminderUseCase by lazy { CancelTaskReminderUseCase(reminderScheduler) }
    val rescheduleAllRemindersUseCase by lazy { RescheduleAllRemindersUseCase(taskRepository, reminderScheduler) }

    val getQuietHoursUseCase by lazy { GetQuietHoursUseCase(quietHoursPreferencesRepository) }
    val setQuietHoursUseCase by lazy { SetQuietHoursUseCase(quietHoursPreferencesRepository) }

    val getSnoozeDurationUseCase by lazy { GetSnoozeDurationUseCase(snoozeDurationPreferencesRepository) }
    val setSnoozeDurationUseCase by lazy { SetSnoozeDurationUseCase(snoozeDurationPreferencesRepository) }

    val getConfirmationPreferencesUseCase by lazy { GetConfirmationPreferencesUseCase(confirmationPreferencesRepository) }
    val setConfirmationPreferencesUseCase by lazy { SetConfirmationPreferencesUseCase(confirmationPreferencesRepository) }

    val getWeeklyDigestSummaryUseCase by lazy { GetWeeklyDigestSummaryUseCase(taskCompletionRepository) }
    val getWeeklyDigestOptInUseCase by lazy { GetWeeklyDigestOptInUseCase(weeklyDigestPreferencesRepository) }
    val setWeeklyDigestOptInUseCase by lazy {
        SetWeeklyDigestOptInUseCase(weeklyDigestPreferencesRepository, weeklyDigestScheduler)
    }

    val getUpcomingTasksForWidgetUseCase by lazy { GetUpcomingTasksForWidgetUseCase(taskRepository) }
    val refreshWidgetUseCase by lazy { RefreshWidgetUseCase(widgetRefresher) }

    val getFocusSessionStateUseCase by lazy { GetFocusSessionStateUseCase(focusSessionPreferencesRepository) }
    val saveFocusSessionStateUseCase by lazy { SaveFocusSessionStateUseCase(focusSessionPreferencesRepository) }

    val getThemePreferenceUseCase by lazy { GetThemePreferenceUseCase(themePreferencesRepository) }
    val setThemePreferenceUseCase by lazy { SetThemePreferenceUseCase(themePreferencesRepository) }

    val getFontScalePreferenceUseCase by lazy { GetFontScalePreferenceUseCase(fontScalePreferencesRepository) }
    val setFontScalePreferenceUseCase by lazy { SetFontScalePreferenceUseCase(fontScalePreferencesRepository) }

    val getShowHomeScreenPreferenceUseCase by lazy { GetShowHomeScreenPreferenceUseCase(showHomeScreenPreferencesRepository) }
    val setShowHomeScreenPreferenceUseCase by lazy { SetShowHomeScreenPreferenceUseCase(showHomeScreenPreferencesRepository) }

    val getWeeklyCompletionStatsUseCase by lazy { GetWeeklyCompletionStatsUseCase(taskCompletionRepository) }
    val getCurrentStreakUseCase by lazy { GetCurrentStreakUseCase(taskCompletionRepository) }

    val exportBackupUseCase by lazy { ExportBackupUseCase(backupRepository) }
    val importBackupUseCase by lazy { ImportBackupUseCase(backupRepository) }

    val getGoogleDriveSignInIntentUseCase by lazy { GetGoogleDriveSignInIntentUseCase(googleDriveBackupRepository) }
    val completeGoogleDriveSignInUseCase by lazy { CompleteGoogleDriveSignInUseCase(googleDriveBackupRepository) }
    val getGoogleDriveAccountEmailUseCase by lazy { GetGoogleDriveAccountEmailUseCase(googleDriveBackupRepository) }
    val signOutGoogleDriveUseCase by lazy { SignOutGoogleDriveUseCase(googleDriveBackupRepository) }
    val uploadBackupToDriveUseCase by lazy {
        UploadBackupToDriveUseCase(backupRepository, googleDriveBackupRepository, googleDrivePreferencesRepository)
    }
    val restoreBackupFromDriveUseCase by lazy {
        RestoreBackupFromDriveUseCase(backupRepository, googleDriveBackupRepository)
    }
    val getGoogleDriveBackupPreferencesUseCase by lazy {
        GetGoogleDriveBackupPreferencesUseCase(googleDrivePreferencesRepository)
    }
    val setGoogleDriveAutoBackupEnabledUseCase by lazy {
        SetGoogleDriveAutoBackupEnabledUseCase(googleDrivePreferencesRepository, googleDriveBackupScheduler)
    }

    val isAppLockEnabledUseCase by lazy { IsAppLockEnabledUseCase(appLockRepository) }
    val setAppLockPinUseCase by lazy { SetAppLockPinUseCase(appLockRepository) }
    val verifyAppLockPinUseCase by lazy { VerifyAppLockPinUseCase(appLockRepository) }
    val disableAppLockUseCase by lazy { DisableAppLockUseCase(appLockRepository) }

    val getLanguagePreferenceUseCase by lazy { GetLanguagePreferenceUseCase(languagePreferencesRepository) }
    val setLanguagePreferenceUseCase by lazy { SetLanguagePreferenceUseCase(languagePreferencesRepository) }

    val getAppColorThemeUseCase by lazy { GetAppColorThemeUseCase(appColorThemePreferencesRepository) }
    val setAppColorThemeUseCase by lazy { SetAppColorThemeUseCase(appColorThemePreferencesRepository) }

    val getTaskTemplatesUseCase by lazy { GetTaskTemplatesUseCase(taskTemplateRepository) }
    val saveTaskTemplateUseCase by lazy { SaveTaskTemplateUseCase(taskTemplateRepository) }
    val deleteTaskTemplateUseCase by lazy { DeleteTaskTemplateUseCase(taskTemplateRepository) }

    // Repository/veritabanı bağımlılığı olmayan, saf bir UseCase, yine de
    // diğerleriyle AYNI "elle DI" deseniyle burada kuruluyor, tutarlılık için.
    val parseNaturalLanguageDateUseCase by lazy { ParseNaturalLanguageDateUseCase() }

    // MainActivity.attachBaseContext ÇOK erken çalışıyor (Compose/ViewModel
    // dünyası henüz kurulmadan önce), bu yüzden UseCase/Flow zinciri yerine
    // doğrudan senkron bir okumaya ihtiyacı var, bkz.
    // LanguagePreferencesRepository.getLanguagePreferenceNow.
    fun currentLanguagePreference() = languagePreferencesRepository.getLanguagePreferenceNow()

    // Aynı gerekçe: MainActivity'nin "giriş ekranını göster mi" kararı da
    // tek seferlik, composition başına verilen bir karar, bkz.
    // ShowHomeScreenPreferencesRepository.getShowHomeScreenPreferenceNow.
    fun currentShowHomeScreenPreference() = showHomeScreenPreferencesRepository.getShowHomeScreenPreferenceNow()

    // Aynı gerekçe: Yazı Boyutu artık Configuration.fontScale üzerinden
    // (attachBaseContext) uygulanıyor, bu da Activity oluşmadan ÖNCE
    // senkron bir okuma gerektiriyor, bkz. oradaki yorum.
    fun currentFontScalePreference() = fontScalePreferencesRepository.getFontScalePreferenceNow()

    // TaskReminderActionReceiver bir BroadcastReceiver, Flow toplamak için
    // uygun bir yaşam döngüsü yok, bu yüzden AYNI gerekçeyle senkron okuma.
    fun currentSnoozeMinutes() = snoozeDurationPreferencesRepository.getSnoozeMinutesNow()
}
