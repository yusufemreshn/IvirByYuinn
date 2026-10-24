package com.yusufemre.ivirbyyuinn.ui.task

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yusufemre.ivirbyyuinn.IvirApplication
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.data.Task
import com.yusufemre.ivirbyyuinn.domain.SmartView
import com.yusufemre.ivirbyyuinn.domain.SortOption
import com.yusufemre.ivirbyyuinn.ui.focus.FocusModeScreen
import com.yusufemre.ivirbyyuinn.ui.settings.SettingsScreen
import com.yusufemre.ivirbyyuinn.ui.stats.StatsScreen
import com.yusufemre.ivirbyyuinn.ui.theme.IvirIcons
import com.yusufemre.ivirbyyuinn.ui.theme.ThemeToggleButton
import com.yusufemre.ivirbyyuinn.ui.theme.rememberReducedMotionEnabled
import com.yusufemre.ivirbyyuinn.ui.util.AnimatedDialog
import com.yusufemre.ivirbyyuinn.ui.util.ConfirmDeleteDialog
import com.yusufemre.ivirbyyuinn.ui.util.ConfirmDialog
import com.yusufemre.ivirbyyuinn.ui.util.findActivity
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Faz 1'in ana ekranı, Faz 2'de belirgin şekilde büyüdü. ViewModel'i
// burada, Application'daki hazır UseCase'leri Factory'ye geçirerek
// kuruyoruz, Hilt olmadığı için bu "elle bağlama" adımını her ekranın
// başında kendimiz yapıyoruz.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(openAddTaskOnLaunch: Boolean = false, initialTaskTitle: String? = null) {
    val application = LocalContext.current.applicationContext as IvirApplication
    val viewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(
            getTasksUseCase = application.getTasksUseCase,
            filterAndSortTasksUseCase = application.filterAndSortTasksUseCase,
            addTaskUseCase = application.addTaskUseCase,
            updateTaskUseCase = application.updateTaskUseCase,
            toggleTaskDoneUseCase = application.toggleTaskDoneUseCase,
            deleteTaskUseCase = application.deleteTaskUseCase,
            restoreTaskUseCase = application.restoreTaskUseCase,
            permanentlyDeleteTaskUseCase = application.permanentlyDeleteTaskUseCase,
            permanentlyDeleteAllTasksUseCase = application.permanentlyDeleteAllTasksUseCase,
            getDeletedTasksUseCase = application.getDeletedTasksUseCase,
            archiveTaskUseCase = application.archiveTaskUseCase,
            unarchiveTaskUseCase = application.unarchiveTaskUseCase,
            getArchivedTasksUseCase = application.getArchivedTasksUseCase,
            autoArchiveCompletedTasksUseCase = application.autoArchiveCompletedTasksUseCase,
            reorderTasksUseCase = application.reorderTasksUseCase,
            getSmartViewTasksUseCase = application.getSmartViewTasksUseCase,
            getSubtasksUseCase = application.getSubtasksUseCase,
            getAllSubtasksUseCase = application.getAllSubtasksUseCase,
            addSubtaskUseCase = application.addSubtaskUseCase,
            toggleSubtaskDoneUseCase = application.toggleSubtaskDoneUseCase,
            deleteSubtaskUseCase = application.deleteSubtaskUseCase,
            getCategoriesUseCase = application.getCategoriesUseCase,
            addCategoryUseCase = application.addCategoryUseCase,
            deleteCategoryUseCase = application.deleteCategoryUseCase,
            getTaskListsUseCase = application.getTaskListsUseCase,
            addTaskListUseCase = application.addTaskListUseCase,
            deleteTaskListUseCase = application.deleteTaskListUseCase,
            getArchivedTaskListsUseCase = application.getArchivedTaskListsUseCase,
            reorderTaskListsUseCase = application.reorderTaskListsUseCase,
            setTaskListArchivedUseCase = application.setTaskListArchivedUseCase,
            scheduleTaskReminderUseCase = application.scheduleTaskReminderUseCase,
            cancelTaskReminderUseCase = application.cancelTaskReminderUseCase,
            getWeeklyCompletionStatsUseCase = application.getWeeklyCompletionStatsUseCase,
            getCurrentStreakUseCase = application.getCurrentStreakUseCase,
            getTaskTemplatesUseCase = application.getTaskTemplatesUseCase,
            saveTaskTemplateUseCase = application.saveTaskTemplateUseCase,
            deleteTaskTemplateUseCase = application.deleteTaskTemplateUseCase,
            parseNaturalLanguageDateUseCase = application.parseNaturalLanguageDateUseCase,
            refreshWidgetUseCase = application.refreshWidgetUseCase,
            getConfirmationPreferencesUseCase = application.getConfirmationPreferencesUseCase,
        ),
    )

    val tasks by viewModel.uiState.collectAsStateWithLifecycle()
    val deletedTasks by viewModel.deletedTasks.collectAsStateWithLifecycle()
    val archivedTasks by viewModel.archivedTasks.collectAsStateWithLifecycle()
    val tasksForCalendar by viewModel.tasksForCalendar.collectAsStateWithLifecycle()
    val weeklyStats by viewModel.weeklyStats.collectAsStateWithLifecycle()
    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val taskLists by viewModel.taskLists.collectAsStateWithLifecycle()
    val archivedTaskLists by viewModel.archivedTaskLists.collectAsStateWithLifecycle()
    val selectedTaskListId by viewModel.selectedTaskListId.collectAsStateWithLifecycle()
    val selectedSmartView by viewModel.selectedSmartView.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val subtasksByTask by viewModel.subtasksByTask.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val selectedTaskIds by viewModel.selectedTaskIds.collectAsStateWithLifecycle()
    val selectionMode = selectedTaskIds.isNotEmpty()
    val confirmationPreferences by viewModel.confirmationPreferences.collectAsStateWithLifecycle()

    // showEditor + taskBeingEdited ikilisi editörün "hangi modda" açık
    // olduğunu anlatıyor: showEditor=false -> kapalı, showEditor=true ve
    // taskBeingEdited=null -> yeni görev ekleniyor, ikisi de doluysa
    // var olan bir görev düzenleniyor.
    var showEditor by remember { mutableStateOf(false) }
    var taskBeingEdited by remember { mutableStateOf<Task?>(null) }
    var showTrash by remember { mutableStateOf(false) }
    var showArchive by remember { mutableStateOf(false) }
    var showCalendar by remember { mutableStateOf(false) }
    var showStats by remember { mutableStateOf(false) }
    var showCategories by remember { mutableStateOf(false) }
    // rememberSaveable: Ayarlar > Görünüm'de Yazı Boyutu/Dil değişince
    // Activity recreate() oluyor, bkz. AppearanceSettingsScreen. Bu
    // sayede recreate sonrası kullanıcı Ayarlar açıkken görev listesine
    // değil yine Ayarlar'a dönüyor.
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showArchivedLists by remember { mutableStateOf(false) }
    var showFocusMode by remember { mutableStateOf(false) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }
    var showExitConfirm by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val reducedMotion = rememberReducedMotionEnabled()
    val context = LocalContext.current

    // stringResource() bir @Composable fonksiyon, aşağıdaki onDeleteTask
    // lambda'sı bir event handler (composition dışında, sonradan
    // çağrılıyor), o yüzden metinleri burada, composition sırasında,
    // önceden okuyup değişkende tutuyoruz.
    val taskDeletedTemplate = stringResource(R.string.task_deleted_snackbar)
    val undoLabel = stringResource(R.string.action_undo)
    val cannotDeleteLastListMessage = stringResource(R.string.cannot_delete_last_list)
    val duplicateTaskTitleMessage = stringResource(R.string.duplicate_task_title_message)
    val taskAddedTemplate = stringResource(R.string.task_added_snackbar)
    val taskUpdatedTemplate = stringResource(R.string.task_updated_snackbar)
    val taskListAddedTemplate = stringResource(R.string.task_list_added_snackbar)
    val taskListDeletedTemplate = stringResource(R.string.task_list_deleted_snackbar)
    val taskListArchivedTemplate = stringResource(R.string.task_list_archived_snackbar)
    val taskArchivedTemplate = stringResource(R.string.task_archived_snackbar)

    // Widget'taki "+" ya da Quick Settings tile'ından geldiysek, ekran
    // ilk kez çizilir çizilmez editörü otomatik açıyoruz.
    LaunchedEffect(openAddTaskOnLaunch) {
        if (openAddTaskOnLaunch) {
            taskBeingEdited = null
            showEditor = true
        }
    }

    // Bir görev silinince (swipe ya da sil butonuyla) hem gerçekten
    // sildiriyoruz hem de birkaç saniyelik bir "Geri Al" penceresi
    // açıyoruz, çöp kutusu zaten var ama anlık bir hata yapıldığında
    // oraya gitmeye gerek kalmasın diye.
    val onDeleteTask: (Task) -> Unit = { task ->
        viewModel.deleteTask(task)
        coroutineScope.launch {
            val result = snackbarHostState.showSnackbar(
                message = String.format(taskDeletedTemplate, task.title),
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.restoreTask(task)
            }
        }
    }

    // ConfirmDeleteDialog'un onConfirm'i İLE confirmTaskDelete tercihi
    // kapalıyken doğrudan çağrılan yol AYNI işi yapıyor, tek bir yerde
    // tanımlı tutuyoruz ki ikisi birbirinden sapmasın.
    val performBulkDelete = {
        viewModel.bulkDelete { deletedCount ->
            if (deletedCount > 0) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        context.resources.getQuantityString(R.plurals.bulk_deleted_snackbar, deletedCount, deletedCount),
                    )
                }
            }
        }
    }

    // Arşivleme, ana ekranın kendi Row'undan tetikleniyor (bir Dialog
    // penceresinin içinden değil), bu yüzden Trash/Kategori/Ayarlar'daki
    // gibi ayrı bir SnackbarHostState'e gerek yok, dışarıdaki normal
    // snackbarHostState zaten görünür.
    val onArchiveTask: (Task) -> Unit = { task ->
        viewModel.archiveTask(task) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(String.format(taskArchivedTemplate, task.title))
            }
        }
    }

    // Sürükle-bırak sıralaması sadece SortOption.MANUAL'da anlamlı.
    // Listenin GEÇİCİ halini burada tutuyoruz, her piksel hareketinde
    // Room'a yazmıyoruz, parmak kalkınca tek seferde
    // viewModel.reorderTasks ile kaydediyoruz.
    val lazyListState = rememberLazyListState()
    var manualOrderTasks by remember { mutableStateOf(tasks) }
    LaunchedEffect(tasks) { manualOrderTasks = tasks }
    val dragDropState = rememberDragDropListState(lazyListState) { fromIndex, toIndex ->
        manualOrderTasks = manualOrderTasks.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
    }

    // Faz 8: eskiden TopAppBar başlığındaki bir dropdown olan liste
    // seçici artık soldan açılan bir ModalNavigationDrawer, DrawerState
    // açık/kapalı olup olmadığını tutuyor, coroutineScope de open()/
    // close() gibi suspend fonksiyonları çağırabilmemiz için (yukarıda
    // zaten Snackbar için kurulmuştu, aynısını burada da kullanıyoruz).
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Kullanıcı geri bildirimi: geri tuşuna basınca uygulama hiç sormadan
    // direkt kapanıyordu. Şimdi üç ayrı BackHandler var, her biri sadece
    // KENDİ durumu geçerliyken devrede (enabled), drawer açıksa önce onu
    // kapat, toplu seçim modundaysak önce seçimi temizle, hiçbiri değilse
    // (yani kullanıcı gerçekten ana ekrandaysa) çıkış onayı sor. Alt
    // ekranlar (Ayarlar, Çöp kutusu, görev editörü vs.) kendi Dialog
    // pencerelerinde açık olduğu için geri tuşunu ÖNCE onlar yakalıyor,
    // bu handler'lar sadece hiçbir alt ekran açık değilken devreye giriyor.
    BackHandler(enabled = drawerState.isOpen) {
        coroutineScope.launch { drawerState.close() }
    }
    BackHandler(enabled = drawerState.isClosed && selectionMode) {
        viewModel.clearSelection()
    }
    BackHandler(enabled = drawerState.isClosed && !selectionMode) {
        showExitConfirm = true
    }

    val menuDescription = stringResource(R.string.menu_cd)
    // Akıllı liste aktifken başlıkta liste adı yerine akıllı listenin adı
    // gösteriliyor. "Hangi görünümdeyim" her zaman net olsun diye.
    val screenTitle = when (selectedSmartView) {
        SmartView.TODAY -> stringResource(R.string.smart_view_today)
        SmartView.UPCOMING -> stringResource(R.string.smart_view_upcoming)
        SmartView.OVERDUE -> stringResource(R.string.smart_view_overdue)
        null -> taskLists.firstOrNull { it.id == selectedTaskListId }?.name
            ?: stringResource(R.string.app_name)
    }

    // Bu Box, ModalNavigationDrawer'ın ÜSTÜNE (aynı z-sırasında SONRA
    // eklenen her şey Compose'da bir öncekinin üzerine çizilir) kendi
    // "dışına dokununca kapat" katmanımızı eklemek için var, aşağıdaki
    // yorumda neden gerektiği anlatılıyor.
    Box(modifier = Modifier.fillMaxSize()) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        // Kullanıcı geri bildirimi: drawer açıldıktan/kaydıktan sonra bile
        // kısa bir süre (~yarım saniye) hiçbir öğeye tıklanamıyordu.
        // `gesturesEnabled = true` (varsayılan) parmakla kenardan sürükleyip
        // drawer'ı AÇIP/KAPATABİLMEK için kendi sürükleme algılayıcısını
        // (Modifier.anchoredDraggable) kuruyor. Bu algılayıcı, HER
        // dokunuşun "bir sürükleme mi, yoksa bir öğeye tıklama mı" olduğuna
        // karar vermek zorunda, ve bu karar (özellikle drawer'ın kendi
        // açılma animasyonu/settle mantığıyla aynı anda) NavigationDrawerItem
        // gibi çocuk bileşenlerin `clickable`'ına giden dokunuşu geciktirip
        // "yutabiliyor". Bu uygulamada drawer zaten SADECE hamburger
        // ikonuna basarak açılıyor (kenardan sürükleyerek açan bir kullanıcı
        // akışı yok), yani sürükleme jestine gerçekten ihtiyacımız yok.
        // Kapatınca bu rekabet ortadan kalkıyor, tıklamalar anında işliyor;
        // AÇILIŞ animasyonuna (drawerState.open() ile programatik olarak
        // tetiklenen kayma) hiç dokunmuyor, o aynen kalıyor.
        //
        // Kütüphanenin KENDİ "dışına dokununca kapat" davranışı da
        // (Material3 kaynağında Scrim'in onClick'i) YAZIK Kİ bu AYNI
        // gesturesEnabled bayrağına bağlı, false yapınca o da devre dışı
        // kalıyor (Android kaynağından doğrulandı: NavigationDrawer.kt'de
        // onDismissRequest'in içinde `if (gesturesEnabled && ...)` şartı
        // var). Bu yüzden "dışına dokununca kapansın" isteğini aşağıdaki
        // elle yazılmış overlay ile ayrıca kendimiz sağlıyoruz.
        gesturesEnabled = false,
        drawerContent = {
            // Kullanıcı isteği: drawer'ı kapatmanın bir yolu da parmağı
            // sağdan sola kaydırmak olsun. gesturesEnabled = false zaten
            // kütüphanenin KENDİ sürükleme algılayıcısını kapattığı için
            // (yukarıdaki yorumda anlatıldığı gibi, hem kenardan AÇMA hem
            // sheet'i sürükleyerek KAPATMA aynı bayrağa bağlı, ayrı ayrı
            // açılıp kapatılamıyor), bunu kendi elle yazdığımız basit bir
            // `detectHorizontalDragGestures` ile SADECE bu Box'ta (drawer
            // sheet'in içeriğinde) yeniden kuruyoruz. Kütüphanenin
            // AnchoredDraggableState tabanlı mekanizması (tıklama
            // gecikmesine sebep olan asıl şeydi) burada YOK, o yüzden
            // NavigationDrawerItem'ların tıklanabilirliğini etkilemiyor:
            // düz bir dokunuş (yatay hareket olmadan) hiç sürükleme
            // olarak algılanmıyor, touch-slop aşılmadan onDragStart bile
            // tetiklenmiyor.
            val density = LocalDensity.current
            val closeSwipeThresholdPx = with(density) { 72.dp.toPx() }
            Box(
                modifier = Modifier.pointerInput(drawerState) {
                    var accumulatedDrag = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { accumulatedDrag = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            accumulatedDrag += dragAmount
                        },
                        onDragEnd = {
                            if (accumulatedDrag < -closeSwipeThresholdPx) {
                                coroutineScope.launch { drawerState.close() }
                            }
                        },
                    )
                },
            ) {
                AppDrawerContent(
                    taskLists = taskLists,
                    selectedTaskListId = selectedTaskListId,
                    selectedSmartView = selectedSmartView,
                    onSelect = viewModel::selectTaskList,
                    onSelectSmartView = viewModel::selectSmartView,
                    onCreateNew = { name ->
                        viewModel.addTaskList(name) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(String.format(taskListAddedTemplate, name.trim()))
                            }
                        }
                    },
                    onMoveTaskList = viewModel::moveTaskList,
                    onArchiveTaskList = { list ->
                        viewModel.archiveTaskList(list) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(String.format(taskListArchivedTemplate, list.name))
                            }
                        }
                    },
                    onDeleteTaskList = { list ->
                        viewModel.deleteTaskList(list) { deleted ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (deleted) String.format(taskListDeletedTemplate, list.name) else cannotDeleteLastListMessage,
                                )
                            }
                        }
                    },
                    hasArchivedLists = archivedTaskLists.isNotEmpty(),
                    onOpenArchivedLists = { showArchivedLists = true },
                    onOpenTrash = { showTrash = true },
                    onOpenCalendar = { showCalendar = true },
                    onOpenStats = { showStats = true },
                    onOpenCategories = { showCategories = true },
                    onOpenArchive = { showArchive = true },
                    onOpenSettings = { showSettings = true },
                    onOpenFocusMode = { showFocusMode = true },
                    onCloseDrawer = { coroutineScope.launch { drawerState.close() } },
                    confirmOnDeleteList = confirmationPreferences.confirmListDelete,
                )
            }
        },
    ) {
        Scaffold(
            topBar = {
                // Faz 12c: bir satıra uzun basınca normal TopAppBar,
                // "N seçili" + toplu aksiyon çubuğuna dönüşüyor, Gmail/
                // Dosyalar gibi uygulamalardaki tanıdık desen.
                if (selectionMode) {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = viewModel::clearSelection) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.exit_selection_cd))
                            }
                        },
                        title = { Text(pluralStringResource(R.plurals.selection_count, selectedTaskIds.size, selectedTaskIds.size)) },
                        actions = {
                            IconButton(onClick = {
                                viewModel.bulkComplete { completedCount ->
                                    if (completedCount > 0) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                context.resources.getQuantityString(R.plurals.bulk_completed_snackbar, completedCount, completedCount),
                                            )
                                        }
                                    }
                                }
                            }) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = stringResource(R.string.bulk_complete_cd))
                            }
                            IconButton(onClick = {
                                if (confirmationPreferences.confirmTaskDelete) showBulkDeleteConfirm = true else performBulkDelete()
                            }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.bulk_delete_cd))
                            }
                        },
                    )
                } else {
                    TopAppBar(
                        navigationIcon = {
                            // Faz 10a'ya kadar burada düz bir metin glifi ("☰")
                            // vardı; artık IvirIcons.Menu, mascot'la aynı
                            // çizgide, elden çizilmiş bir hamburger ikonu.
                            IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = IvirIcons.Menu,
                                    contentDescription = null,
                                    modifier = Modifier.semantics { contentDescription = menuDescription },
                                )
                            }
                        },
                        title = { Text(screenTitle) },
                        actions = { ThemeToggleButton() },
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    taskBeingEdited = null
                    showEditor = true
                }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add_task_fab_cd))
                }
            },
        ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            SearchAndFilterBar(
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::updateSearchQuery,
                categories = categories,
                selectedCategoryId = categoryFilter,
                onCategorySelected = viewModel::updateCategoryFilter,
                sortOption = sortOption,
                onSortOptionChange = viewModel::updateSortOption,
                hideManualSort = selectedSmartView != null,
            )

            // Boş liste <-> dolu liste geçişi AnimatedContent ile yumuşak
            // bir çapraz geçiş (crossfade + hafif kayma) oluyor. Faz 1'den
            // beri burası anında, sert bir şekilde değişiyordu.
            AnimatedContent(
                targetState = tasks.isEmpty(),
                label = "task-list-empty-state",
            ) { isEmpty ->
                if (isEmpty) {
                    // Liste boşken FAB'ı gizlemiyoruz, çünkü kullanıcının
                    // ilk görevini eklemesi gereken yer zaten orası. Faz 10b:
                    // boşluğun SEBEBİ arama mı, kategori filtresi mi, yoksa
                    // liste gerçekten boş mu, her biri kendi mesajını/
                    // aksiyonunu görüyor. İkisi de aktifse arama önce
                    // gösteriliyor (daha yeni/spesifik eylem olduğu için).
                    val emptyReason = when {
                        searchQuery.isNotBlank() -> EmptyListReason.SEARCH
                        categoryFilter != null -> EmptyListReason.FILTER
                        else -> EmptyListReason.NO_TASKS
                    }
                    EmptyTaskList(
                        reason = emptyReason,
                        onClearAction = {
                            if (emptyReason == EmptyListReason.SEARCH) {
                                viewModel.updateSearchQuery("")
                            } else {
                                viewModel.updateCategoryFilter(null)
                            }
                        },
                    )
                } else if (sortOption == SortOption.MANUAL) {
                    // Manuel sıralamada ViewModel'in verdiği "tasks" değil,
                    // sürüklerken canlı güncellediğimiz manualOrderTasks
                    // gösteriliyor, aksi halde her piksel hareketinde
                    // Room'dan gelen yeni liste ile çakışırdı.
                    LazyColumn(state = lazyListState) {
                        itemsIndexed(items = manualOrderTasks, key = { _, task -> task.id }) { index, task ->
                            val isDragging = dragDropState.draggingItemIndex == index
                            TaskRow(
                                task = task,
                                category = categories.firstOrNull { it.id == task.categoryId },
                                subtasks = subtasksByTask[task.id] ?: emptyList(),
                                onToggleSubtaskDone = viewModel::toggleSubtaskDone,
                                onToggleDone = viewModel::toggleDone,
                                onDelete = onDeleteTask,
                                onArchive = onArchiveTask,
                                confirmOnDelete = confirmationPreferences.confirmTaskDelete,
                                confirmOnComplete = confirmationPreferences.confirmTaskComplete,
                                confirmOnArchive = confirmationPreferences.confirmTaskArchive,
                                confirmOnSubtaskComplete = confirmationPreferences.confirmSubtaskComplete,
                                modifier = Modifier
                                    .zIndex(if (isDragging) 1f else 0f)
                                    .offset {
                                        IntOffset(
                                            0,
                                            if (isDragging) dragDropState.draggingItemOffset.roundToInt() else 0,
                                        )
                                    }
                                    // Faz 10b: sürüklenen satır gerçekten
                                    // "kalkmış" hissettirsin diye bir gölge
                                    // ekliyoruz, bıraktığında (isDragging
                                    // false olunca) gölge de anında iniyor.
                                    .shadow(
                                        elevation = if (isDragging) 8.dp else 0.dp,
                                        shape = MaterialTheme.shapes.small,
                                    ),
                                dragHandleModifier = Modifier.pointerInput(task.id) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { offset ->
                                            val itemOffset = lazyListState.layoutInfo.visibleItemsInfo
                                                .firstOrNull { it.index == index }?.offset ?: 0
                                            dragDropState.onDragStart(index, itemOffset)
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragDropState.onDrag(dragAmount)
                                        },
                                        onDragEnd = {
                                            dragDropState.onDragEnd()
                                            viewModel.reorderTasks(manualOrderTasks)
                                        },
                                        onDragCancel = { dragDropState.onDragEnd() },
                                    )
                                },
                                onClick = {
                                    taskBeingEdited = it
                                    showEditor = true
                                },
                                selectionMode = selectionMode,
                                isSelected = task.id in selectedTaskIds,
                                onToggleSelect = { viewModel.toggleTaskSelection(it.id) },
                                onLongClick = { viewModel.toggleTaskSelection(it.id) },
                            )
                        }
                    }
                } else {
                    LazyColumn {
                        itemsIndexed(items = tasks, key = { _, task -> task.id }) { index, task ->
                          // Faz 10b: satır ilk kez listeye girerken kısa bir
                          // solup-kayarak-belirme animasyonu, sadece YENİ
                          // satırlar için, bkz. AnimatedListEntry'nin
                          // yorum satırı.
                          AnimatedListEntry(index = index, reducedMotion = reducedMotion) {
                            TaskRow(
                                task = task,
                                category = categories.firstOrNull { it.id == task.categoryId },
                                subtasks = subtasksByTask[task.id] ?: emptyList(),
                                onToggleSubtaskDone = viewModel::toggleSubtaskDone,
                                onToggleDone = viewModel::toggleDone,
                                onDelete = onDeleteTask,
                                onArchive = onArchiveTask,
                                confirmOnDelete = confirmationPreferences.confirmTaskDelete,
                                confirmOnComplete = confirmationPreferences.confirmTaskComplete,
                                confirmOnArchive = confirmationPreferences.confirmTaskArchive,
                                confirmOnSubtaskComplete = confirmationPreferences.confirmSubtaskComplete,
                                // Arama/filtre/sıralama değiştikçe satırlar
                                // yer değiştiriyor ya da listeden çıkıp
                                // giriyor, animateItem() olmadan bu
                                // değişiklikler animasyonsuz, aniden
                                // "zıplayarak" oluyordu. Bu modifier,
                                // Compose'a "bir satır yer değiştirirse/
                                // kaybolursa/belirirse bunu yumuşak bir
                                // geçişle göster" diyor.
                                modifier = Modifier.animateItem(),
                                onClick = {
                                    taskBeingEdited = it
                                    showEditor = true
                                },
                                selectionMode = selectionMode,
                                isSelected = task.id in selectedTaskIds,
                                onToggleSelect = { viewModel.toggleTaskSelection(it.id) },
                                onLongClick = { viewModel.toggleTaskSelection(it.id) },
                            )
                          }
                        }
                    }
                }
            }
        }
        }
    }

    // Yukarıdaki yorumda anlatıldığı gibi: gesturesEnabled = false
    // kütüphanenin kendi "dışına dokununca kapat" davranışını da
    // (istemeden) kapatıyor. Burada, drawer sheet'in KAPLADIĞI genişliği
    // (DrawerDefaults.MaximumDrawerWidth, ModalDrawerSheet'in de
    // kullandığı AYNI resmi sabit) hesaba katıp, SADECE onun SAĞINDA
    // kalan (görünürde zaten koyulaşmış/scrim'li) alanı görünmez bir
    // dokunma yakalayıcıyla kaplıyoruz. Drawer kapalıyken bu Box hiç
    // var olmuyor (composition'a girmiyor), kapalıyken görev
    // listesindeki normal dokunuşları hiç etkilemiyor.
    if (drawerState.isOpen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = DrawerDefaults.MaximumDrawerWidth)
                .pointerInput(drawerState) {
                    detectTapGestures {
                        coroutineScope.launch { drawerState.close() }
                    }
                },
        )
    }
    }

    if (showEditor) {
        val editingTask = taskBeingEdited
        TaskEditorSheet(
            existingTask = editingTask,
            categories = categories,
            subtasksFlow = editingTask?.let { viewModel.getSubtasks(it.id) },
            onDismiss = { showEditor = false },
            confirmOnSubtaskDelete = confirmationPreferences.confirmSubtaskDelete,
            confirmOnSubtaskComplete = confirmationPreferences.confirmSubtaskComplete,
            confirmOnSubtaskAdd = confirmationPreferences.confirmSubtaskAdd,
            onSave = { title, categoryId, priority, dueAt, note, recurrence ->
                // Kullanıcı isteği: aynı listede aynı isimde ikinci bir görev
                // eklenemesin/böyle bir isme değiştirilemesin. Bu durumda
                // editörü KAPATMIYORUZ (girdiği bilgiler kaybolmasın, ismini
                // değiştirip tekrar denesin), sadece kısa bir Toast ile
                // haber veriyoruz. Snackbar değil Toast kullanıyoruz çünkü
                // ModalBottomSheet kendi penceresinde açılıyor, Scaffold'un
                // Snackbar'ı sheet'in arkasında kalıp görünmeyebilirdi.
                val onDuplicateTitle = {
                    Toast.makeText(context, duplicateTaskTitleMessage, Toast.LENGTH_SHORT).show()
                }
                // Ekleme/güncelleme başarı mesajı da TaskEditorSheet'in
                // duplicate-title uyarısıyla AYNI sebeple Toast, sheet
                // kapanma animasyonu sürerken Scaffold'un Snackbar'ı
                // birkaç yüz milisaniye arkada/görünmez kalabiliyordu.
                if (editingTask == null) {
                    viewModel.addTask(
                        title, categoryId, priority, dueAt, note, recurrence,
                        onFinished = { showEditor = false },
                        onDuplicateTitle = onDuplicateTitle,
                        onSaved = { savedTask ->
                            Toast.makeText(context, String.format(taskAddedTemplate, savedTask.title), Toast.LENGTH_SHORT).show()
                        },
                    )
                } else {
                    viewModel.updateTask(
                        editingTask.copy(
                            title = title,
                            categoryId = categoryId,
                            priority = priority,
                            dueAt = dueAt,
                            note = note?.trim()?.ifEmpty { null },
                            recurrence = recurrence,
                        ),
                        onFinished = { showEditor = false },
                        onDuplicateTitle = onDuplicateTitle,
                        onSaved = { savedTask ->
                            Toast.makeText(context, String.format(taskUpdatedTemplate, savedTask.title), Toast.LENGTH_SHORT).show()
                        },
                    )
                }
            },
            onAddCategory = { name, colorHex, onCreated -> viewModel.addCategory(name, colorHex, onCreated) },
            onAddSubtask = { title -> editingTask?.let { viewModel.addSubtask(it.id, title) } },
            onToggleSubtaskDone = viewModel::toggleSubtaskDone,
            onDeleteSubtask = viewModel::deleteSubtask,
            templates = templates,
            onSaveAsTemplate = viewModel::saveAsTemplate,
            onDeleteTemplate = viewModel::deleteTemplate,
            onParseDate = viewModel::parseNaturalLanguageDate,
            initialTitle = if (editingTask == null) initialTaskTitle.orEmpty() else "",
        )
    }

    if (showTrash) {
        // Navigation kütüphanesi kullanmadığımız için (bkz. Faz 10 toplantı
        // notları) "yeni bir ekrana git" hissi vermek için tam ekran bir
        // Dialog kullanmaya devam ediyoruz, ama Faz 10a'dan beri çıplak
        // değil, AnimatedDialog sağdan kayan gerçek bir geçiş ekliyor.
        AnimatedDialog(onDismissRequest = { showTrash = false }) { dismiss ->
            // TrashScreen kendi Snackbar'ını gösteriyor (bkz. ArchivedListsScreen'in
            // yukarısındaki not), ham ViewModel çağrılarını olduğu gibi geçiriyoruz.
            TrashScreen(
                deletedTasks = deletedTasks,
                onRestore = viewModel::restoreTask,
                onPermanentlyDelete = viewModel::permanentlyDeleteTask,
                onDeleteAll = viewModel::permanentlyDeleteAllTasks,
                onClose = dismiss,
                confirmOnDelete = confirmationPreferences.confirmTrashDelete,
            )
        }
    }

    if (showArchive) {
        // TrashScreen'in AynI gerekçesi: kendi Snackbar'ını gösteriyor, ham
        // ViewModel çağrılarını olduğu gibi geçiriyoruz.
        AnimatedDialog(onDismissRequest = { showArchive = false }) { dismiss ->
            ArchiveScreen(
                archivedTasks = archivedTasks,
                onUnarchive = viewModel::unarchiveTask,
                onDelete = viewModel::deleteTask,
                onClose = dismiss,
                confirmOnDelete = confirmationPreferences.confirmArchivedTaskDelete,
            )
        }
    }

    if (showCalendar) {
        AnimatedDialog(onDismissRequest = { showCalendar = false }) { dismiss ->
            CalendarScreen(
                tasks = tasksForCalendar,
                onClose = dismiss,
                onTaskClick = { task ->
                    showCalendar = false
                    taskBeingEdited = task
                    showEditor = true
                },
            )
        }
    }

    if (showStats) {
        AnimatedDialog(onDismissRequest = { showStats = false }) { dismiss ->
            StatsScreen(
                weeklyStats = weeklyStats,
                currentStreak = currentStreak,
                onClose = dismiss,
            )
        }
    }

    if (showCategories) {
        AnimatedDialog(onDismissRequest = { showCategories = false }) { dismiss ->
            // Aynı gerekçe: CategoryManagementScreen kendi Snackbar'ını gösteriyor.
            CategoryManagementScreen(
                categories = categories,
                onAddCategory = { name, colorHex, onCreated -> viewModel.addCategory(name, colorHex) { onCreated() } },
                onDeleteCategory = viewModel::deleteCategory,
                onClose = dismiss,
                confirmOnDelete = confirmationPreferences.confirmCategoryDelete,
            )
        }
    }

    if (showSettings) {
        AnimatedDialog(onDismissRequest = { showSettings = false }) { dismiss ->
            SettingsScreen(onClose = dismiss)
        }
    }

    if (showArchivedLists) {
        AnimatedDialog(onDismissRequest = { showArchivedLists = false }) { dismiss ->
            // Bu dialog kendi penceresinde açıldığı için mesajları ARTIK
            // burada değil, ArchivedListsScreen'in kendi Snackbar'ında
            // gösteriyoruz (bkz. plan/2026-08-11-islem-basari-bildirimleri.md),
            // bu yüzden ham ViewModel çağrılarını olduğu gibi geçiriyoruz.
            ArchivedListsScreen(
                archivedTaskLists = archivedTaskLists,
                onUnarchive = viewModel::unarchiveTaskList,
                onDelete = viewModel::deleteTaskList,
                onClose = dismiss,
                confirmOnDelete = confirmationPreferences.confirmListDelete,
            )
        }
    }

    if (showFocusMode) {
        AnimatedDialog(onDismissRequest = { showFocusMode = false }) { dismiss ->
            FocusModeScreen(onClose = dismiss)
        }
    }

    if (showBulkDeleteConfirm) {
        ConfirmDeleteDialog(
            title = stringResource(R.string.confirm_bulk_delete_title),
            message = stringResource(R.string.confirm_bulk_delete_message, selectedTaskIds.size),
            onConfirm = performBulkDelete,
            onDismiss = { showBulkDeleteConfirm = false },
        )
    }

    if (showExitConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.exit_confirm_title),
            message = stringResource(R.string.exit_confirm_message),
            onConfirm = { context.findActivity()?.finish() },
            onDismiss = { showExitConfirm = false },
        )
    }
}
