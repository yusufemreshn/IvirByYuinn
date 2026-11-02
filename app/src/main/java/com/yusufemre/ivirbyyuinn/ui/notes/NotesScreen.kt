package com.yusufemre.ivirbyyuinn.ui.notes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yusufemre.ivirbyyuinn.IvirApplication
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.data.Note
import com.yusufemre.ivirbyyuinn.ui.util.ConfirmDeleteDialog

// Kullanıcının isteğiyle eklendi: görevlerden bağımsız, sadece serbest metin
// not alınabilen bir alan. CategoryManagementScreen'le AYNI iskelet
// (Scaffold + FAB + LazyColumn + silme onayı), ama düzenleme burada ayrı bir
// ekrana değil, tek bir çok satırlı metin alanına sahip NoteEditorDialog'a
// açılıyor: not SADECE serbest metin, görev ekleme kadar alana (öncelik,
// tarih, kategori...) ihtiyacı yok.
//
// SettingsScreen'in AYNI deseni: kendi ViewModel'ini burada, LocalContext'teki
// IvirApplication'dan kuruyor, TaskListScreen'den hiçbir use case/VM
// enjekte edilmesine gerek kalmıyor.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(onClose: () -> Unit) {
    val application = LocalContext.current.applicationContext as IvirApplication
    val viewModel: NoteViewModel = viewModel(
        factory = NoteViewModelFactory(
            getNotesUseCase = application.getNotesUseCase,
            addNoteUseCase = application.addNoteUseCase,
            updateNoteUseCase = application.updateNoteUseCase,
            deleteNoteUseCase = application.deleteNoteUseCase,
        ),
    )
    val notes by viewModel.notes.collectAsStateWithLifecycle()

    var showEditorFor by remember { mutableStateOf<Note?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var notePendingDelete by remember { mutableStateOf<Note?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notes_title)) },
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
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add_note_cd))
            }
        },
    ) { innerPadding ->
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.notes_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                items(items = notes, key = { it.id }) { note ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        onClick = { showEditorFor = note },
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
                        ) {
                            Text(
                                text = note.content,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 40.dp),
                            )
                            IconButton(
                                onClick = { notePendingDelete = note },
                                modifier = Modifier.align(Alignment.TopEnd),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete_note_cd),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        NoteEditorDialog(
            initialContent = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { content ->
                viewModel.addNote(content)
                showAddDialog = false
            },
        )
    }

    showEditorFor?.let { note ->
        NoteEditorDialog(
            initialContent = note.content,
            onDismiss = { showEditorFor = null },
            onConfirm = { content ->
                viewModel.updateNote(note, content)
                showEditorFor = null
            },
        )
    }

    notePendingDelete?.let { note ->
        ConfirmDeleteDialog(
            title = stringResource(R.string.confirm_delete_note_title),
            message = stringResource(R.string.confirm_delete_note_message),
            onConfirm = { viewModel.deleteNote(note) },
            onDismiss = { notePendingDelete = null },
        )
    }
}

@Composable
private fun NoteEditorDialog(
    initialContent: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var content by remember { mutableStateOf(initialContent) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.notes_title)) },
        text = {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text(stringResource(R.string.note_content_label)) },
                minLines = 4,
                maxLines = 10,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                enabled = content.isNotBlank(),
                onClick = { onConfirm(content) },
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
