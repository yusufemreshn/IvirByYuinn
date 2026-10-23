package com.yusufemre.ivirbyyuinn.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufemre.ivirbyyuinn.data.Category
import com.yusufemre.ivirbyyuinn.data.ConfirmationPreferences
import com.yusufemre.ivirbyyuinn.data.Priority
import com.yusufemre.ivirbyyuinn.data.Recurrence
import com.yusufemre.ivirbyyuinn.data.Subtask
import com.yusufemre.ivirbyyuinn.data.Task
import com.yusufemre.ivirbyyuinn.data.TaskListEntity
import com.yusufemre.ivirbyyuinn.data.TaskTemplateEntity
import com.yusufemre.ivirbyyuinn.domain.AddCategoryUseCase
import com.yusufemre.ivirbyyuinn.domain.AddSubtaskUseCase
import com.yusufemre.ivirbyyuinn.domain.AddTaskListUseCase
import com.yusufemre.ivirbyyuinn.domain.AddTaskUseCase
import com.yusufemre.ivirbyyuinn.domain.ArchiveTaskUseCase
import com.yusufemre.ivirbyyuinn.domain.AutoArchiveCompletedTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.CancelTaskReminderUseCase
import com.yusufemre.ivirbyyuinn.domain.DailyCompletionCount
import com.yusufemre.ivirbyyuinn.domain.GetConfirmationPreferencesUseCase
import com.yusufemre.ivirbyyuinn.domain.DeleteCategoryUseCase
import com.yusufemre.ivirbyyuinn.domain.DeleteSubtaskUseCase
import com.yusufemre.ivirbyyuinn.domain.DeleteTaskListUseCase
import com.yusufemre.ivirbyyuinn.domain.DeleteTaskUseCase
import com.yusufemre.ivirbyyuinn.domain.FilterAndSortTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.GetAllSubtasksUseCase
import com.yusufemre.ivirbyyuinn.domain.GetCategoriesUseCase
import com.yusufemre.ivirbyyuinn.domain.GetCurrentStreakUseCase
import com.yusufemre.ivirbyyuinn.domain.GetDeletedTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.GetArchivedTaskListsUseCase
import com.yusufemre.ivirbyyuinn.domain.GetArchivedTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.GetSmartViewTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.GetSubtasksUseCase
import com.yusufemre.ivirbyyuinn.domain.DeleteTaskTemplateUseCase
import com.yusufemre.ivirbyyuinn.domain.GetTaskListsUseCase
import com.yusufemre.ivirbyyuinn.domain.GetTaskTemplatesUseCase
import com.yusufemre.ivirbyyuinn.domain.GetTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.GetWeeklyCompletionStatsUseCase
import com.yusufemre.ivirbyyuinn.domain.ParseNaturalLanguageDateUseCase
import com.yusufemre.ivirbyyuinn.domain.ParsedDate
import com.yusufemre.ivirbyyuinn.domain.PermanentlyDeleteAllTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.PermanentlyDeleteTaskUseCase
import com.yusufemre.ivirbyyuinn.domain.RefreshWidgetUseCase
import com.yusufemre.ivirbyyuinn.domain.ReorderTaskListsUseCase
import com.yusufemre.ivirbyyuinn.domain.ReorderTasksUseCase
import com.yusufemre.ivirbyyuinn.domain.RestoreTaskUseCase
import com.yusufemre.ivirbyyuinn.domain.SaveTaskTemplateUseCase
import com.yusufemre.ivirbyyuinn.domain.ScheduleTaskReminderUseCase
import com.yusufemre.ivirbyyuinn.domain.SetTaskListArchivedUseCase
import com.yusufemre.ivirbyyuinn.domain.SmartView
import com.yusufemre.ivirbyyuinn.domain.SortOption
import com.yusufemre.ivirbyyuinn.domain.TaskSaveOutcome
import com.yusufemre.ivirbyyuinn.domain.ToggleSubtaskDoneUseCase
import com.yusufemre.ivirbyyuinn.domain.ToggleTaskDoneUseCase
import com.yusufemre.ivirbyyuinn.domain.UnarchiveTaskUseCase
import com.yusufemre.ivirbyyuinn.domain.UpdateTaskUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Bu sınıf ekranın "beyni": Composable'lar hiçbir iş kuralı bilmeden
// sadece burada tutulan durumu (state) okuyup ekrana çiziyor, kullanıcı
// etkileşimlerini de buradaki fonksiyonlara yönlendiriyor. .NET MVC'deki
// Controller'a benziyor ama burada UI ile çok daha sıkı bağlı yaşıyor.
//
// Faz 2'de bu sınıf epey büyüdü çünkü artık tek bir liste değil; aktif
// liste, arama metni, kategori filtresi ve sıralama seçeneği gibi birden
// fazla "hareketli parça" var. Bunları ayrı ayrı StateFlow'larda tutup
// combine() ile birleştiriyoruz, Excel'deki birden fazla hücreye bağlı
// bir formülün, herhangi biri değişince otomatik yeniden hesaplanmasına
// benzer bir mantık.
//
// flatMapLatest hâlâ "deneysel" (ExperimentalCoroutinesApi) işaretli ama
// coroutines dünyasında yıllardır standart, yaygın kullanılan bir işlev,
// @OptIn ile "bunu bilerek kullanıyorum" diyoruz.
@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(
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
    getArchivedTasksUseCase: GetArchivedTasksUseCase,
    private val autoArchiveCompletedTasksUseCase: AutoArchiveCompletedTasksUseCase,
    private val reorderTasksUseCase: ReorderTasksUseCase,
    private val getSmartViewTasksUseCase: GetSmartViewTasksUseCase,
    private val getSubtasksUseCase: GetSubtasksUseCase,
    getAllSubtasksUseCase: GetAllSubtasksUseCase,
    private val addSubtaskUseCase: AddSubtaskUseCase,
    private val toggleSubtaskDoneUseCase: ToggleSubtaskDoneUseCase,
    private val deleteSubtaskUseCase: DeleteSubtaskUseCase,
    getCategoriesUseCase: GetCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    getTaskListsUseCase: GetTaskListsUseCase,
    private val addTaskListUseCase: AddTaskListUseCase,
    private val deleteTaskListUseCase: DeleteTaskListUseCase,
    getArchivedTaskListsUseCase: GetArchivedTaskListsUseCase,
    private val reorderTaskListsUseCase: ReorderTaskListsUseCase,
    private val setTaskListArchivedUseCase: SetTaskListArchivedUseCase,
    private val scheduleTaskReminderUseCase: ScheduleTaskReminderUseCase,
    private val cancelTaskReminderUseCase: CancelTaskReminderUseCase,
    getWeeklyCompletionStatsUseCase: GetWeeklyCompletionStatsUseCase,
    getCurrentStreakUseCase: GetCurrentStreakUseCase,
    getTaskTemplatesUseCase: GetTaskTemplatesUseCase,
    private val saveTaskTemplateUseCase: SaveTaskTemplateUseCase,
    private val deleteTaskTemplateUseCase: DeleteTaskTemplateUseCase,
    private val parseNaturalLanguageDateUseCase: ParseNaturalLanguageDateUseCase,
    private val refreshWidgetUseCase: RefreshWidgetUseCase,
    getConfirmationPreferencesUseCase: GetConfirmationPreferencesUseCase,
) : ViewModel() {

    val taskLists: StateFlow<List<TaskListEntity>> = getTaskListsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Faz 12b: arşivlenmiş listeler ayrı bir ekranda (ArchivedListsScreen)
    // gösteriliyor, normal drawer'da hiç yer almıyor.
    val archivedTaskLists: StateFlow<List<TaskListEntity>> = getArchivedTaskListsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories: StateFlow<List<Category>> = getCategoriesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Faz 12a: sık kullanılan görevler için tek dokunuşluk şablonlar.
    val templates: StateFlow<List<TaskTemplateEntity>> = getTaskTemplatesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Tüm alt görevleri tek seferde çekip taskId'ye göre grupluyoruz,
    // TaskRow bunu satırın altında ufak ufak listelemek için kullanıyor.
    // Hiç alt görevi olmayan bir görev bu haritada hiç görünmez.
    val subtasksByTask: StateFlow<Map<Long, List<Subtask>>> = getAllSubtasksUseCase()
        .map { allSubtasks -> allSubtasks.groupBy { it.taskId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    // Ayarlar > Onay Mesajları'ndaki 4 anahtar; TaskListScreen bunu
    // TaskRow/TaskEditorSheet'e geçirip hangi onay dialoglarının
    // açılacağını belirliyor.
    val confirmationPreferences: StateFlow<ConfirmationPreferences> = getConfirmationPreferencesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConfirmationPreferences())

    // null demek "henüz hangi listenin gösterileceği belli değil",
    // task_lists tablosu yüklenince aşağıdaki init bloğu ilk listeyi
    // otomatik seçiyor.
    private val _selectedTaskListId = MutableStateFlow<Long?>(null)
    val selectedTaskListId: StateFlow<Long?> = _selectedTaskListId.asStateFlow()

    // Faz 12a: normal liste modu ile akıllı liste modu birbirini dışlıyor,
    // null demek "normal, selectedTaskListId'ye göre" göster; dolu demek
    // TÜM listelerden bu akıllı görünüme göre göster. selectedTaskListId'yi
    // BOŞALTMIYORUZ ki kullanıcı akıllı listeden çıkınca kaldığı listeye
    // dönsün.
    private val _selectedSmartView = MutableStateFlow<SmartView?>(null)
    val selectedSmartView: StateFlow<SmartView?> = _selectedSmartView.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categoryFilter = MutableStateFlow<Long?>(null)
    val categoryFilter: StateFlow<Long?> = _categoryFilter.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.DATE_NEWEST_FIRST)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    // Faz 12c: toplu seçim. Boş küme = seçim modu KAPALI, ayrı bir
    // "isSelectionMode" boole'una gerek yok, TaskListScreen bunu
    // selectedTaskIds.isEmpty() ile zaten anlayabiliyor.
    private val _selectedTaskIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedTaskIds: StateFlow<Set<Long>> = _selectedTaskIds.asStateFlow()

    init {
        // Kullanıcı henüz elle bir liste seçmediyse (uygulama yeni açıldı),
        // listeler yüklenir yüklenmez ilkini otomatik aktif liste yapıyoruz.
        viewModelScope.launch {
            taskLists.collect { lists ->
                if (_selectedTaskListId.value == null && lists.isNotEmpty()) {
                    _selectedTaskListId.value = lists.first().id
                }
            }
        }
        // 2026-08-11: WorkManager'ın 30 dakikalık periyoduna ek olarak,
        // uygulama her açıldığında bir kerelik anlık kontrol. Kullanıcı
        // uygulamayı açtığı an, 24 saati zaten dolmuş bekleyen görevler
        // WorkManager'ın bir sonraki turunu beklemeden arşivlensin diye.
        viewModelScope.launch {
            autoArchiveCompletedTasksUseCase()
        }
    }

    // selectedTaskListId her değiştiğinde flatMapLatest, Room'dan o listeye
    // ait GÜNCEL Flow'a otomatik geçiyor (öncekini bırakıp yenisini
    // dinlemeye başlıyor), elle "eski dinlemeyi durdur, yenisini başlat"
    // yazmamıza gerek kalmıyor.
    private val rawTasksInSelectedList: Flow<List<Task>> = selectedTaskListId
        .filterNotNull()
        .flatMapLatest { taskListId -> getTasksUseCase(taskListId) }

    // selectedSmartView null iken normal listeye, dolu iken TÜM listeler
    // üzerinden hesaplanan akıllı görünüme geçiyor.
    private val rawTasksForActiveView: Flow<List<Task>> = selectedSmartView
        .flatMapLatest { smartView ->
            if (smartView != null) getSmartViewTasksUseCase(smartView) else rawTasksInSelectedList
        }

    val uiState: StateFlow<List<Task>> = combine(
        rawTasksForActiveView,
        searchQuery,
        categoryFilter,
        sortOption,
    ) { tasks, query, categoryFilterValue, sort ->
        filterAndSortTasksUseCase(tasks, query, categoryFilterValue, sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Çöp kutusu ekranı için, normal listeyle aynı flatMapLatest deseni,
    // sadece silinmiş görevlere bakıyor.
    val deletedTasks: StateFlow<List<Task>> = selectedTaskListId
        .filterNotNull()
        .flatMapLatest { taskListId -> getDeletedTasksUseCase(taskListId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Arşiv ekranı için, deletedTasks ile BİREBİR aynı desen, sadece
    // arşivlenmiş görevlere bakıyor.
    val archivedTasks: StateFlow<List<Task>> = selectedTaskListId
        .filterNotNull()
        .flatMapLatest { taskListId -> getArchivedTasksUseCase(taskListId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Takvim ekranı için: arama/kategori filtresi/sıralamadan ETKİLENMEMİŞ,
    // aktif listenin tüm (silinmemiş) görevleri, takvimde "ayın tamamını"
    // göstermek istiyoruz, o an ekranda uygulanan filtreleri değil.
    //
    // 2026-08-11: artık SADECE aktif (arşivlenmemiş) görevler değil,
    // arşivlenmiş görevler de dahil, kullanıcı takvimde geçmiş günlerde
    // tamamladığı (ve belki de arşivlenmiş) görevleri de görebilsin diye.
    // rawTasksInSelectedList zaten isArchived=0 şartlı olduğu için,
    // arşivdekileri AYRICA combine ile ekliyoruz.
    val tasksForCalendar: StateFlow<List<Task>> = combine(
        rawTasksInSelectedList,
        archivedTasks,
    ) { active, archived -> active + archived }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // İstatistik ekranı için: hangi liste seçiliyse onun tamamlanma
    // geçmişine göre hesaplanıyor, Takvim'de olduğu gibi listeler
    // arasında geçiş yapınca istatistik de o listeninkine dönüyor.
    val weeklyStats: StateFlow<List<DailyCompletionCount>> = selectedTaskListId
        .filterNotNull()
        .flatMapLatest { taskListId -> getWeeklyCompletionStatsUseCase(taskListId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentStreak: StateFlow<Int> = selectedTaskListId
        .filterNotNull()
        .flatMapLatest { taskListId -> getCurrentStreakUseCase(taskListId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun selectTaskList(taskListId: Long) {
        _selectedTaskListId.value = taskListId
        _selectedSmartView.value = null
    }

    // Manuel sıralama (sürükle-bırak) tek bir listeye özel, akıllı liste
    // birden fazla listeden görev topladığı için MANUAL anlamsız kalır, o
    // yüzden akıllı listeye geçerken üzerinde kalmış olabilecek MANUAL
    // seçimini de sıfırlıyoruz (SearchAndFilterBar zaten bu seçeneği akıllı
    // listede menüden gizliyor, ama ALREADY seçiliyken geçilirse diye).
    fun selectSmartView(view: SmartView) {
        _selectedSmartView.value = view
        if (_sortOption.value == SortOption.MANUAL) {
            _sortOption.value = SortOption.DATE_NEWEST_FIRST
        }
    }

    fun addTaskList(name: String, onCreated: () -> Unit = {}) {
        viewModelScope.launch {
            val newListId = addTaskListUseCase(name)
            // Yeni oluşturulan listeye otomatik geçiyoruz, kullanıcı boş bir
            // listeye bakıp "nereye gitti?" diye sormasın diye.
            if (newListId != null) {
                _selectedTaskListId.value = newListId
                onCreated()
            }
        }
    }

    fun moveTaskList(taskListId: Long, moveUp: Boolean) {
        viewModelScope.launch {
            reorderTaskListsUseCase(taskLists.value, taskListId, moveUp)
        }
    }

    fun archiveTaskList(taskList: TaskListEntity, onArchived: () -> Unit = {}) {
        viewModelScope.launch {
            setTaskListArchivedUseCase(taskList, archived = true)
            // Arşivlenen liste o an seçiliyse, artık drawer'da hiç
            // görünmeyecek, null'a çekiyoruz ki init'teki "ilk listeyi
            // otomatik seç" mantığı devreye girip yerine geçerli bir
            // liste seçsin.
            if (_selectedTaskListId.value == taskList.id) {
                _selectedTaskListId.value = null
            }
            onArchived()
        }
    }

    fun unarchiveTaskList(taskList: TaskListEntity, onUnarchived: () -> Unit = {}) {
        viewModelScope.launch {
            setTaskListArchivedUseCase(taskList, archived = false)
            onUnarchived()
        }
    }

    // deleted=false dönerse (son aktif liste silinmeye çalışılmış demek),
    // bkz. DeleteTaskListUseCase'in "en az bir liste kalmalı" kuralı.
    // onResult'ı hem drawer'daki hem arşiv ekranındaki çağıran KENDİ
    // Snackbar'ında göstermek için kullanıyor, bu ekranlar birer Dialog
    // (ayrı pencere) içinde açıldığından mesajı burada değil, çağıranın
    // kendi penceresinde göstermek gerekiyor.
    fun deleteTaskList(taskList: TaskListEntity, onResult: (deleted: Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val deleted = deleteTaskListUseCase(taskList)
            if (deleted && _selectedTaskListId.value == taskList.id) {
                // archiveTaskList ile AYNI mantık: null'a çekince init
                // bloğundaki "ilk listeyi otomatik seç" devreye giriyor.
                _selectedTaskListId.value = null
            }
            onResult(deleted)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateCategoryFilter(categoryId: Long?) {
        _categoryFilter.value = categoryId
    }

    fun updateSortOption(option: SortOption) {
        _sortOption.value = option
    }

    // onFinished: Saved VE BlankTitle'da çağrılıyor, eski "her durumda
    // editörü kapat" davranışını aynen koruyor (boş başlık sessizce
    // reddediliyor, bu davranışa dokunmadık). onDuplicateTitle: sadece
    // aynı listede aynı isimde başka bir görev varsa çağrılıyor, çağıran
    // ekran (TaskListScreen) bu durumda editörü AÇIK bırakıp kullanıcıya
    // haber veriyor, aksi halde girilen bilgiler sessizce kaybolurdu.
    fun addTask(
        title: String,
        categoryId: Long?,
        priority: Priority,
        dueAt: Long?,
        note: String?,
        recurrence: Recurrence,
        onFinished: () -> Unit,
        onDuplicateTitle: () -> Unit,
        onSaved: (Task) -> Unit = {},
    ) {
        val taskListId = selectedTaskListId.value ?: return
        viewModelScope.launch {
            when (val outcome = addTaskUseCase(title, taskListId, categoryId, priority, dueAt, note, recurrence)) {
                is TaskSaveOutcome.Saved -> {
                    // onFinished() (editörü kapatan çağrı) BİLEREK burada,
                    // asıl kayıt biter bitmez tetikleniyor. Eskiden hatırlatma
                    // kurma + widget yenileme onFinished'tan ÖNCE bekleniyordu
                    // widget yenileme özellikle (Glance'ın RemoteViews'i
                    // yeniden çizip AppWidgetManager'a göndermesi) gözle
                    // görülür sürebiliyor, bu da "Kaydet'e bastım ama ekran
                    // bir süre tepki vermiyor" hissine sebep oluyordu. Bu
                    // ikisi kullanıcının editörü kapatabilmesi için gerekli
                    // DEĞİL, viewModelScope, sheet kapandıktan sonra da
                    // (Compose'un kendi ömründen bağımsız) çalışmaya devam
                    // ediyor, o yüzden arkada bitirmeleri yeterli.
                    onFinished()
                    onSaved(outcome.task)
                    scheduleTaskReminderUseCase(outcome.task)
                    refreshWidgetUseCase()
                }
                TaskSaveOutcome.BlankTitle -> onFinished()
                TaskSaveOutcome.DuplicateTitle -> onDuplicateTitle()
            }
        }
    }

    fun updateTask(
        task: Task,
        onFinished: () -> Unit,
        onDuplicateTitle: () -> Unit,
        onSaved: (Task) -> Unit = {},
    ) {
        viewModelScope.launch {
            when (val outcome = updateTaskUseCase(task)) {
                is TaskSaveOutcome.Saved -> {
                    // bkz. addTask'taki aynı yorum: onFinished() önce.
                    onFinished()
                    onSaved(outcome.task)
                    rescheduleReminder(outcome.task)
                    refreshWidgetUseCase()
                }
                TaskSaveOutcome.BlankTitle -> onFinished()
                TaskSaveOutcome.DuplicateTitle -> onDuplicateTitle()
            }
        }
    }

    fun toggleDone(task: Task) {
        viewModelScope.launch {
            val updatedTask = toggleTaskDoneUseCase(task)
            rescheduleReminder(updatedTask)
            refreshWidgetUseCase()
        }
    }

    // onDeleted: TaskListScreen'in normal satırlarda kullandığı KENDİ
    // Snackbar+Geri Al akışı için varsayılan {} yeterli (o zaten ayrı bir
    // Snackbar gösteriyor), ama ArchiveScreen gibi bir Dialog penceresi
    // içinden çağrılan yerlerde, o ekranın KENDİ SnackbarHostState'inde
    // mesaj göstermesi için bu geri çağrıya ihtiyaç var (bkz.
    // restoreTask/archiveTask'taki AYNI desen).
    fun deleteTask(task: Task, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            // Artık gerçek silme değil, çöp kutusuna taşıma. Ama hâlâ
            // bir hatırlatması varsa, çöpteki bir görev için bildirim
            // gelmesin diye onu iptal ediyoruz.
            deleteTaskUseCase(task)
            cancelTaskReminderUseCase(task.id)
            refreshWidgetUseCase()
            onDeleted()
        }
    }

    // Long-press bir satıra ilk bastığında da, seçim modundayken başka bir
    // satıra bastığında da AYNI fonksiyon, "seç/seçimi kaldır" tek bir
    // toggle, seçim modunun kendisi başka bir state değil.
    fun toggleTaskSelection(taskId: Long) {
        _selectedTaskIds.value = if (taskId in _selectedTaskIds.value) {
            _selectedTaskIds.value - taskId
        } else {
            _selectedTaskIds.value + taskId
        }
    }

    fun clearSelection() {
        _selectedTaskIds.value = emptySet()
    }

    // "Tamamla" bir TOGGLE değil, zaten tamamlanmış seçili görevleri
    // atlıyoruz, sadece tamamlanmamış olanları işaretliyoruz. Kullanıcı
    // birkaç görevi seçip "bitir" derken "hepsini işaretle" bekliyor,
    // yanlışlıkla zaten bitmiş birini geri açmak istemiyor.
    fun bulkComplete(onResult: (completedCount: Int) -> Unit = {}) {
        viewModelScope.launch {
            val affected = uiState.value
                .filter { it.id in _selectedTaskIds.value && !it.isDone }
            affected.forEach { task ->
                val updatedTask = toggleTaskDoneUseCase(task)
                rescheduleReminder(updatedTask)
            }
            clearSelection()
            refreshWidgetUseCase()
            onResult(affected.size)
        }
    }

    fun bulkDelete(onResult: (deletedCount: Int) -> Unit = {}) {
        viewModelScope.launch {
            val affected = uiState.value.filter { it.id in _selectedTaskIds.value }
            affected.forEach { task ->
                deleteTaskUseCase(task)
                cancelTaskReminderUseCase(task.id)
            }
            clearSelection()
            refreshWidgetUseCase()
            onResult(affected.size)
        }
    }

    fun restoreTask(task: Task, onRestored: () -> Unit = {}) {
        viewModelScope.launch {
            val restoredTask = restoreTaskUseCase(task)
            scheduleTaskReminderUseCase(restoredTask)
            refreshWidgetUseCase()
            onRestored()
        }
    }

    fun archiveTask(task: Task, onArchived: () -> Unit = {}) {
        viewModelScope.launch {
            archiveTaskUseCase(task)
            onArchived()
        }
    }

    fun unarchiveTask(task: Task, onUnarchived: () -> Unit = {}) {
        viewModelScope.launch {
            unarchiveTaskUseCase(task)
            onUnarchived()
        }
    }

    fun permanentlyDeleteTask(task: Task, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            permanentlyDeleteTaskUseCase(task)
            onDeleted()
        }
    }

    // Çöp kutusundaki "tümünü sil", sadece o an ekranda gösterilen
    // (yani aktif listenin) silinmiş görevlerini etkiliyor, diğer
    // listelerin çöp kutusuna dokunmuyor.
    fun permanentlyDeleteAllTasks(onDeleted: () -> Unit = {}) {
        val taskListId = selectedTaskListId.value ?: return
        viewModelScope.launch {
            permanentlyDeleteAllTasksUseCase(taskListId)
            onDeleted()
        }
    }

    // Sürükle-bırak sırasında UI kendi geçici sırasını tutuyor (bkz.
    // TaskListScreen'deki DragDropListState); kullanıcı parmağını
    // kaldırdığında son hali burada, tek seferde kaydediliyor.
    fun reorderTasks(newOrder: List<Task>) {
        viewModelScope.launch {
            reorderTasksUseCase(newOrder)
        }
    }

    // Var olan bir hatırlatmayı iptal edip, hâlâ geçerliyse (dueAt dolu,
    // görev tamamlanmamış, tarih gelecekte) yeniden kuruyoruz. "Önce
    // iptal, sonra tekrar kur" deseni; her senaryoyu (tarih değişti,
    // tarih kaldırıldı, görev tamamlandı) ayrı ayrı düşünmek yerine
    // hepsini aynı iki adıma indirgiyor.
    private fun rescheduleReminder(task: Task) {
        cancelTaskReminderUseCase(task.id)
        scheduleTaskReminderUseCase(task)
    }

    fun addCategory(name: String, colorHex: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val newCategoryId = addCategoryUseCase(name, colorHex)
            if (newCategoryId != null) {
                onCreated(newCategoryId)
            }
        }
    }

    fun deleteCategory(category: Category, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            deleteCategoryUseCase(category)
            onDeleted()
        }
    }

    fun saveAsTemplate(title: String, priority: Priority, categoryId: Long?, note: String?, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            saveTaskTemplateUseCase(title, priority, categoryId, note)
            onSaved()
        }
    }

    fun deleteTemplate(template: TaskTemplateEntity, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            deleteTaskTemplateUseCase(template)
            onDeleted()
        }
    }

    // Saf/senkron bir hesaplama (repository yok), o yüzden coroutine
    // gerekmiyor, TaskEditorSheet her tuş vuruşunda doğrudan çağırabilir.
    fun parseNaturalLanguageDate(text: String): ParsedDate? = parseNaturalLanguageDateUseCase(text)

    // Alt görevler her göreve özel olduğu için, tek bir ortak StateFlow
    // yerine bu fonksiyonu çağıran ekran (TaskEditorSheet) kendi
    // collectAsStateWithLifecycle'ını yapıyor.
    fun getSubtasks(taskId: Long): Flow<List<Subtask>> = getSubtasksUseCase(taskId)

    fun addSubtask(taskId: Long, title: String) {
        viewModelScope.launch {
            addSubtaskUseCase(taskId, title)
        }
    }

    fun toggleSubtaskDone(subtask: Subtask) {
        viewModelScope.launch {
            toggleSubtaskDoneUseCase(subtask)
        }
    }

    fun deleteSubtask(subtask: Subtask) {
        viewModelScope.launch {
            deleteSubtaskUseCase(subtask)
        }
    }
}
