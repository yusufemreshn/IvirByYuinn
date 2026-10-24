package com.yusufemre.ivirbyyuinn.ui.task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.data.Category
import com.yusufemre.ivirbyyuinn.ui.util.ConfirmDeleteDialog
import kotlinx.coroutines.launch

// Kullanıcının isteğiyle (2026-08-07) eklendi: kategori eklemek eskiden
// SADECE görev düzenleme ekranından ("+Yeni" chip'i) yapılabiliyordu, artık
// drawer'daki "Kategoriler" seçeneğiyle buraya da gelinip bağımsız olarak
// kategori eklenebiliyor. AddCategoryDialog (Faz 9'da TaskEditorSheet'ten
// buraya taşınıp public yapıldı) iki yerde de AYNI bileşen.
//
// Silme kullanıcı isteğiyle eklendi: bir kategoriyi silmek onu kullanan
// görevleri SİLMİYOR, sadece kategorisiz bırakıyor (bkz. Task.categoryId'nin
// SET_NULL foreign key'i, data/Task.kt). Düzenleme (isim/renk değiştirme)
// hâlâ yok, o ayrı bir istek.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    categories: List<Category>,
    onAddCategory: (name: String, colorHex: String, onCreated: () -> Unit) -> Unit,
    onDeleteCategory: (Category, onDeleted: () -> Unit) -> Unit,
    onClose: () -> Unit,
    confirmOnDelete: Boolean = true,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryPendingDelete by remember { mutableStateOf<Category?>(null) }

    // Bu ekran TaskListScreen tarafından bir AnimatedDialog (kendi ayrı
    // penceresi) içinde açılıyor, TaskListScreen'in kendi Snackbar'ı bu
    // pencerenin ARKASINDA kalıp görünmeyebileceği için (SettingsScreen'in
    // yaptığı gibi) kendi SnackbarHostState'imizi tutuyoruz.
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val categoryAddedTemplate = stringResource(R.string.category_added_snackbar)
    val categoryDeletedTemplate = stringResource(R.string.category_deleted_snackbar)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.categories_title)) },
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
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add_category_cd))
            }
        },
    ) { innerPadding ->
        if (categories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.categories_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                items(items = categories, key = { it.id }) { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(category.colorHex))),
                        )
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp),
                        )
                        IconButton(onClick = {
                            if (confirmOnDelete) {
                                categoryPendingDelete = category
                            } else {
                                onDeleteCategory(category) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(String.format(categoryDeletedTemplate, category.name))
                                    }
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete_category_cd),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCategoryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, colorHex ->
                onAddCategory(name, colorHex) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(String.format(categoryAddedTemplate, name))
                    }
                }
                showAddDialog = false
            },
        )
    }

    categoryPendingDelete?.let { category ->
        ConfirmDeleteDialog(
            title = stringResource(R.string.confirm_delete_category_title),
            message = stringResource(R.string.confirm_delete_category_message, category.name),
            onConfirm = {
                onDeleteCategory(category) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(String.format(categoryDeletedTemplate, category.name))
                    }
                }
            },
            onDismiss = { categoryPendingDelete = null },
        )
    }
}
