package com.yusufemre.ivirbyyuinn.ui.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.data.TaskListEntity
import com.yusufemre.ivirbyyuinn.ui.util.ConfirmDeleteDialog
import kotlinx.coroutines.launch

// TrashScreen'in AYNI deseni: Navigation kütüphanesi yok, TaskListScreen
// tarafından tam ekran bir Dialog içinde açılıyor. Arşivlenmiş bir liste
// SİLİNMİŞ değil, görevleri/geçmişi korunuyor, sadece drawer'da
// görünmüyor; buradaki tek aksiyon "Aktif Et" (unarchive).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedListsScreen(
    archivedTaskLists: List<TaskListEntity>,
    onUnarchive: (TaskListEntity, onUnarchived: () -> Unit) -> Unit,
    onDelete: (TaskListEntity, onResult: (deleted: Boolean) -> Unit) -> Unit,
    onClose: () -> Unit,
    confirmOnDelete: Boolean = true,
) {
    var taskListPendingDelete by remember { mutableStateOf<TaskListEntity?>(null) }

    // TrashScreen/CategoryManagementScreen'de olduğu gibi: bu ekran da bir
    // AnimatedDialog penceresi içinde açılıyor, o yüzden kendi SnackbarHostState'i.
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val unarchivedTemplate = stringResource(R.string.task_list_unarchived_snackbar)
    val deletedTemplate = stringResource(R.string.task_list_deleted_snackbar)
    val cannotDeleteLastListMessage = stringResource(R.string.cannot_delete_last_list)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.archived_lists_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_close_cd),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (archivedTaskLists.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.archived_lists_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                items(items = archivedTaskLists, key = { it.id }) { taskList ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = taskList.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = {
                                onUnarchive(taskList) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(String.format(unarchivedTemplate, taskList.name))
                                    }
                                }
                            }) {
                                Text(stringResource(R.string.action_unarchive))
                            }
                            IconButton(onClick = {
                                if (confirmOnDelete) {
                                    taskListPendingDelete = taskList
                                } else {
                                    onDelete(taskList) { deleted ->
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                if (deleted) String.format(deletedTemplate, taskList.name) else cannotDeleteLastListMessage,
                                            )
                                        }
                                    }
                                }
                            }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete))
                            }
                        }
                    }
                }
            }
        }
    }

    taskListPendingDelete?.let { taskList ->
        ConfirmDeleteDialog(
            title = stringResource(R.string.confirm_delete_list_title),
            message = stringResource(R.string.confirm_delete_list_message, taskList.name),
            onConfirm = {
                onDelete(taskList) { deleted ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            if (deleted) String.format(deletedTemplate, taskList.name) else cannotDeleteLastListMessage,
                        )
                    }
                }
            },
            onDismiss = { taskListPendingDelete = null },
        )
    }
}
