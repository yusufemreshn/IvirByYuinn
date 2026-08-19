package com.yusufemre.ivirbyyuinn.ui.task

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.data.TaskListEntity
import com.yusufemre.ivirbyyuinn.domain.SmartView
import com.yusufemre.ivirbyyuinn.ui.theme.IvirIcons
import com.yusufemre.ivirbyyuinn.ui.util.ConfirmDeleteDialog

// Faz 8'de TaskListSwitcher.kt'in (TopAppBar başlığındaki dropdown)
// yerini alıyor, aynı işlevler (liste seçme/oluşturma, çöp kutusu/
// takvim/istatistik/ayarlar) artık soldan açılan bir ModalNavigationDrawer
// içinde. TaskListScreen'in kendisi drawer'ı sarmalıyor, bu dosya sadece
// drawer'ın İÇERİĞİni çiziyor.
// Faz 12a: akıllı liste enum'unun ekranda görünecek adı, SearchAndFilterBar
// içindeki SortOption.label() ile aynı desen.
@Composable
private fun SmartView.label(): String = when (this) {
    SmartView.TODAY -> stringResource(R.string.smart_view_today)
    SmartView.UPCOMING -> stringResource(R.string.smart_view_upcoming)
    SmartView.OVERDUE -> stringResource(R.string.smart_view_overdue)
}

@Composable
fun AppDrawerContent(
    taskLists: List<TaskListEntity>,
    selectedTaskListId: Long?,
    selectedSmartView: SmartView?,
    onSelect: (Long) -> Unit,
    onSelectSmartView: (SmartView) -> Unit,
    onCreateNew: (String) -> Unit,
    // Faz 12b: liste sıralama/arşivleme.
    onMoveTaskList: (taskListId: Long, moveUp: Boolean) -> Unit,
    onArchiveTaskList: (TaskListEntity) -> Unit,
    onDeleteTaskList: (TaskListEntity) -> Unit,
    hasArchivedLists: Boolean,
    onOpenArchivedLists: () -> Unit,
    onOpenTrash: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenArchive: () -> Unit,
    onOpenSettings: () -> Unit,
    // Faz 12c: odak modu.
    onOpenFocusMode: () -> Unit,
    onCloseDrawer: () -> Unit,
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var taskListPendingDelete by remember { mutableStateOf<TaskListEntity?>(null) }

    ModalDrawerSheet {
        // Liste sayısı arttıkça (ya da küçük ekranlarda) içerik ekran
        // yüksekliğini aşabiliyordu, en alttaki "Ayarlar" gibi öğelere hiç
        // dokunulamıyordu. Tüm içeriği dikey kaydırılabilir bir Column'a
        // alıyoruz; liste sayısı ne olursa olsun her öğeye kaydırarak
        // ulaşılabiliyor. Başlığın dikey dolgusu da 20dp'den 16dp'ye
        // indirildi: kaydırmaya ihtiyaç duyulma sıklığını biraz azaltmak için.
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )

        // Faz 12a: kullanıcının kendi listelerinin ÜSTÜNDE sabit üç akıllı
        // liste girdisi: bunlar TEK bir listeye değil, TÜM listelerdeki
        // görevlere dueAt'e göre bakıyor (bkz. GetSmartViewTasksUseCase).
        NavigationDrawerItem(
            label = { Text(SmartView.TODAY.label()) },
            icon = { Icon(imageVector = IvirIcons.Today, contentDescription = null) },
            selected = selectedSmartView == SmartView.TODAY,
            onClick = {
                onSelectSmartView(SmartView.TODAY)
                onCloseDrawer()
            },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        NavigationDrawerItem(
            label = { Text(SmartView.UPCOMING.label()) },
            icon = { Icon(imageVector = IvirIcons.Upcoming, contentDescription = null) },
            selected = selectedSmartView == SmartView.UPCOMING,
            onClick = {
                onSelectSmartView(SmartView.UPCOMING)
                onCloseDrawer()
            },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        NavigationDrawerItem(
            label = { Text(SmartView.OVERDUE.label()) },
            icon = { Icon(imageVector = Icons.Default.Warning, contentDescription = null) },
            selected = selectedSmartView == SmartView.OVERDUE,
            onClick = {
                onSelectSmartView(SmartView.OVERDUE)
                onCloseDrawer()
            },
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Kullanıcı geri bildirimi: akıllı görünümlerden sonra gelen liste
        // isimleri ("Genel" vb.) hiçbir başlık olmadan öylece duruyordu,
        // neyin ne olduğu belli değildi.
        Text(
            text = stringResource(R.string.drawer_lists_section_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 4.dp),
        )

        taskLists.forEachIndexed { index, list ->
            NavigationDrawerItem(
                label = { Text(list.name) },
                selected = selectedSmartView == null && list.id == selectedTaskListId,
                onClick = {
                    onSelect(list.id)
                    onCloseDrawer()
                },
                // Faz 12b: her listenin sırasını/arşiv durumunu değiştirmek
                // için küçük bir "⋮" menüsü, badge slot'u aslında bir
                // sayaç göstermek için düşünülmüş ama burada bir
                // IconButton+DropdownMenu koymak için de gayet uygun.
                badge = {
                    var menuExpanded by remember { mutableStateOf(false) }
                    Box {
                        // Önceden 32dp'ye sabitlenmişti, önerilen 48dp
                        // dokunma hedefinin altındaydı. IconButton'ın
                        // varsayılan boyutunu kullanmak dokunma alanını
                        // büyütüyor, ikonun kendi görsel boyutu değişmiyor.
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = stringResource(R.string.list_options_cd))
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_move_up)) },
                                enabled = index > 0,
                                onClick = {
                                    onMoveTaskList(list.id, true)
                                    menuExpanded = false
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_move_down)) },
                                enabled = index < taskLists.lastIndex,
                                onClick = {
                                    onMoveTaskList(list.id, false)
                                    menuExpanded = false
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_archive)) },
                                onClick = {
                                    onArchiveTaskList(list)
                                    menuExpanded = false
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_delete)) },
                                onClick = {
                                    taskListPendingDelete = list
                                    menuExpanded = false
                                },
                            )
                        }
                    }
                },
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.switcher_new_list)) },
            icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
            selected = false,
            onClick = { showCreateDialog = true },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        if (hasArchivedLists) {
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.switcher_archived_lists)) },
                selected = false,
                onClick = {
                    onOpenArchivedLists()
                    onCloseDrawer()
                },
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.switcher_trash)) },
            icon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null) },
            selected = false,
            onClick = {
                onOpenTrash()
                onCloseDrawer()
            },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.switcher_calendar)) },
            icon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = null) },
            selected = false,
            onClick = {
                onOpenCalendar()
                onCloseDrawer()
            },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.switcher_stats)) },
            icon = { Icon(imageVector = Icons.Default.Star, contentDescription = null) },
            selected = false,
            onClick = {
                onOpenStats()
                onCloseDrawer()
            },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.switcher_focus_mode)) },
            icon = { Icon(imageVector = IvirIcons.Timer, contentDescription = null) },
            selected = false,
            onClick = {
                onOpenFocusMode()
                onCloseDrawer()
            },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.switcher_categories)) },
            icon = { Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = null) },
            selected = false,
            onClick = {
                onOpenCategories()
                onCloseDrawer()
            },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        // 2026-08-11: görev arşivi, bilerek Kategoriler ile Ayarlar
        // arasında, kullanıcının istediği yerde.
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.switcher_task_archive)) },
            icon = { Icon(imageVector = IvirIcons.Archive, contentDescription = null) },
            selected = false,
            onClick = {
                onOpenArchive()
                onCloseDrawer()
            },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.switcher_settings)) },
            icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = null) },
            selected = false,
            onClick = {
                onOpenSettings()
                onCloseDrawer()
            },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        }
    }

    if (showCreateDialog) {
        var newListName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(stringResource(R.string.new_list_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text(stringResource(R.string.new_list_name_label)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onCreateNew(newListName)
                    showCreateDialog = false
                    onCloseDrawer()
                }) { Text(stringResource(R.string.action_create)) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }

    taskListPendingDelete?.let { list ->
        ConfirmDeleteDialog(
            title = stringResource(R.string.confirm_delete_list_title),
            message = stringResource(R.string.confirm_delete_list_message, list.name),
            onConfirm = { onDeleteTaskList(list) },
            onDismiss = { taskListPendingDelete = null },
        )
    }
}
