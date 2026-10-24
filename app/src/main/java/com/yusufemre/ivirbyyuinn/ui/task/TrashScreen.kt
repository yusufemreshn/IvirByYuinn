package com.yusufemre.ivirbyyuinn.ui.task

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.data.Task
import com.yusufemre.ivirbyyuinn.ui.util.ConfirmDeleteDialog
import kotlinx.coroutines.launch

// Navigation kütüphanesi kullanmadığımız için bu ekran, TaskListScreen
// tarafından tam ekran bir Dialog içinde açılıyor, ayrı bir "sayfa"
// gibi hissettiriyor ama aslında hâlâ aynı Composable ağacındayız.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    deletedTasks: List<Task>,
    onRestore: (Task, onRestored: () -> Unit) -> Unit,
    onPermanentlyDelete: (Task, onDeleted: () -> Unit) -> Unit,
    onDeleteAll: (onDeleted: () -> Unit) -> Unit,
    onClose: () -> Unit,
    confirmOnDelete: Boolean = true,
) {
    var showDeleteAllConfirm by remember { mutableStateOf(false) }

    // CategoryManagementScreen/ArchivedListsScreen'deki AYNI gerekçe: bu
    // ekran da bir AnimatedDialog penceresi içinde açılıyor, kendi
    // SnackbarHostState'imiz olmadan mesajlar görünmeyebilirdi.
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val restoredTemplate = stringResource(R.string.task_restored_snackbar)
    val permanentlyDeletedTemplate = stringResource(R.string.task_permanently_deleted_snackbar)
    val trashEmptiedMessage = stringResource(R.string.trash_emptied_snackbar)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.trash_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_close_cd),
                        )
                    }
                },
                actions = {
                    // Çöp kutusu boşken bu butonun bir anlamı yok, o yüzden
                    // sadece silinecek bir şey varken gösteriyoruz.
                    if (deletedTasks.isNotEmpty()) {
                        IconButton(onClick = {
                            if (confirmOnDelete) {
                                showDeleteAllConfirm = true
                            } else {
                                onDeleteAll {
                                    coroutineScope.launch { snackbarHostState.showSnackbar(trashEmptiedMessage) }
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete_all_cd),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (deletedTasks.isEmpty()) {
            // Faz 11: düz bir metin yerine, EmptyTaskList.kt'deki desenle
            // (elde çizilmiş, DrawScope extension fonksiyonu) aynı dilde
            // bir "boş çöp kutusu" illüstrasyonu.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                EmptyBinIllustration()
                Text(
                    text = stringResource(R.string.trash_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        } else {
            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                items(items = deletedTasks, key = { it.id }) { task ->
                    TrashRow(
                        task = task,
                        onRestore = {
                            onRestore(task) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(String.format(restoredTemplate, task.title))
                                }
                            }
                        },
                        onPermanentlyDelete = {
                            onPermanentlyDelete(task) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(String.format(permanentlyDeletedTemplate, task.title))
                                }
                            }
                        },
                        confirmOnDelete = confirmOnDelete,
                    )
                }
            }
        }
    }

    if (showDeleteAllConfirm) {
        ConfirmDeleteDialog(
            title = stringResource(R.string.confirm_delete_all_title),
            message = stringResource(R.string.confirm_delete_all_message),
            onConfirm = {
                onDeleteAll {
                    coroutineScope.launch { snackbarHostState.showSnackbar(trashEmptiedMessage) }
                }
            },
            onDismiss = { showDeleteAllConfirm = false },
        )
    }
}

@Composable
private fun EmptyBinIllustration() {
    val outlineColor = MaterialTheme.colorScheme.outline
    Canvas(modifier = Modifier.size(96.dp)) {
        val strokeWidth = 4.dp.toPx()
        val style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Kapak + tutamaç
        drawLine(
            color = outlineColor,
            start = Offset(size.width * 0.18f, size.height * 0.28f),
            end = Offset(size.width * 0.82f, size.height * 0.28f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = outlineColor,
            start = Offset(size.width * 0.4f, size.height * 0.16f),
            end = Offset(size.width * 0.6f, size.height * 0.16f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )

        // Gövde: yamuk (üstü geniş, altı dar), klasik çöp kutusu silueti.
        val bodyPath = Path().apply {
            moveTo(size.width * 0.26f, size.height * 0.3f)
            lineTo(size.width * 0.74f, size.height * 0.3f)
            lineTo(size.width * 0.66f, size.height * 0.86f)
            lineTo(size.width * 0.34f, size.height * 0.86f)
            close()
        }
        drawPath(path = bodyPath, color = outlineColor, style = style)

        // İç çizgiler (kutunun "yivleri"): boş olduğunu vurgulayan iki
        // ince dikey çizgi.
        listOf(0.42f, 0.58f).forEach { xFraction ->
            drawLine(
                color = outlineColor,
                start = Offset(size.width * xFraction, size.height * 0.4f),
                end = Offset(size.width * xFraction, size.height * 0.74f),
                strokeWidth = strokeWidth / 2,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun TrashRow(
    task: Task,
    onRestore: () -> Unit,
    onPermanentlyDelete: () -> Unit,
    confirmOnDelete: Boolean = true,
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Faz 11: düz bir Row yerine, TaskRow'un kart diliyle tutarlı, sade bir
    // Card: tam TaskRow karmaşıklığı (öncelik şeridi, swipe) burada
    // gerekmiyor, sadece "her satır bir kart" tutarlılığı için.
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
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onRestore) {
                Text(stringResource(R.string.action_restore))
            }
            IconButton(onClick = {
                if (confirmOnDelete) showConfirmDialog = true else onPermanentlyDelete()
            }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.permanently_delete_cd))
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(stringResource(R.string.permanently_delete_cd)) },
            text = { Text(String.format(stringResource(R.string.permanently_delete_confirm), task.title)) },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    onPermanentlyDelete()
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}
