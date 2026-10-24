package com.yusufemre.ivirbyyuinn.ui.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.yusufemre.ivirbyyuinn.data.Task
import com.yusufemre.ivirbyyuinn.ui.theme.IvirIcons
import com.yusufemre.ivirbyyuinn.ui.util.ConfirmDeleteDialog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TrashScreen ile AYNI iskelet: Navigation kütüphanesi olmadığı için
// TaskListScreen bu ekranı da tam ekran bir AnimatedDialog içinde açıyor,
// bu yüzden Trash/CategoryManagement/ArchivedLists ile aynı gerekçeyle
// kendi SnackbarHostState'imiz var, dışarıdaki Scaffold'un Snackbar'ı bu
// pencerenin ARKASINDA kalıp görünmeyebilirdi.
private val completedAtFormatter get() = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    archivedTasks: List<Task>,
    onUnarchive: (Task, onUnarchived: () -> Unit) -> Unit,
    onDelete: (Task, onDeleted: () -> Unit) -> Unit,
    onClose: () -> Unit,
    confirmOnDelete: Boolean = true,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val unarchivedTemplate = stringResource(R.string.task_unarchived_snackbar)
    val deletedTemplate = stringResource(R.string.task_deleted_snackbar)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.archive_title)) },
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
        if (archivedTasks.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = IvirIcons.Archive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(64.dp),
                )
                Text(
                    text = stringResource(R.string.archive_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        } else {
            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                items(items = archivedTasks, key = { it.id }) { task ->
                    ArchiveRow(
                        task = task,
                        onUnarchive = {
                            onUnarchive(task) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(String.format(unarchivedTemplate, task.title))
                                }
                            }
                        },
                        onDelete = {
                            onDelete(task) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(String.format(deletedTemplate, task.title))
                                }
                            }
                        },
                        confirmOnDelete = confirmOnDelete,
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchiveRow(
    task: Task,
    onUnarchive: () -> Unit,
    onDelete: () -> Unit,
    confirmOnDelete: Boolean = true,
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    // TrashRow ile AYNI sade kart deseni: burada da TaskRow'un tam
    // karmaşıklığı (öncelik şeridi, swipe, checkbox) gerekmiyor.
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.small,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(text = task.title, style = MaterialTheme.typography.bodyLarge)
            // Eski görevlerde (bu özellikten önce tamamlanmış) completedAt
            // null olabilir, o zaman tarih satırını hiç göstermiyoruz.
            task.completedAt?.let {
                Text(
                    text = completedAtFormatter.format(Date(it)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onUnarchive) {
                    Text(stringResource(R.string.action_unarchive_task))
                }
                IconButton(onClick = {
                    if (confirmOnDelete) showConfirmDialog = true else onDelete()
                }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.delete_task_cd))
                }
            }
        }
    }

    if (showConfirmDialog) {
        ConfirmDeleteDialog(
            title = stringResource(R.string.confirm_delete_task_title),
            message = stringResource(R.string.confirm_delete_task_message, task.title),
            onConfirm = {
                showConfirmDialog = false
                onDelete()
            },
            onDismiss = { showConfirmDialog = false },
        )
    }
}
