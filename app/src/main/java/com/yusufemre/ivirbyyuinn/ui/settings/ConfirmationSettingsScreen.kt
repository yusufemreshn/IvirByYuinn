package com.yusufemre.ivirbyyuinn.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yusufemre.ivirbyyuinn.IvirApplication
import com.yusufemre.ivirbyyuinn.R

// SettingsScreen'in diğer alt bölümleri gibi (Çöp kutusu, Takvim vb.)
// TaskListScreen değil, doğrudan SettingsScreen tarafından tam ekran bir
// Dialog içinde açılıyor; kullanıcı isteğiyle uygulamadaki TÜM "emin misin"
// onay diyalogları (görev/alt görev silme-tamamlama-arşivleme-ekleme,
// kategori/liste/arşiv/çöp kutusu silme) tek tek açılıp kapatılabiliyor.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationSettingsScreen(onClose: () -> Unit) {
    val application = LocalContext.current.applicationContext as IvirApplication
    val viewModel: ConfirmationSettingsViewModel = viewModel(
        factory = ConfirmationSettingsViewModelFactory(
            getConfirmationPreferencesUseCase = application.getConfirmationPreferencesUseCase,
            setConfirmationPreferencesUseCase = application.setConfirmationPreferencesUseCase,
        ),
    )
    val preferences by viewModel.confirmationPreferences.collectAsStateWithLifecycle()

    val (snackbarHostState, notifySettingChanged) = rememberSettingChangedSnackbar()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.confirmation_settings_title)) },
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
        snackbarHost = { SettingSnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = stringResource(R.string.confirmation_settings_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ConfirmationToggleRow(
                title = stringResource(R.string.confirm_task_delete_setting_title),
                checked = preferences.confirmTaskDelete,
                onCheckedChange = {
                    viewModel.setConfirmationPreferences(preferences.copy(confirmTaskDelete = it))
                    notifySettingChanged()
                },
            )
            ConfirmationToggleRow(
                title = stringResource(R.string.confirm_task_complete_setting_title),
                checked = preferences.confirmTaskComplete,
                onCheckedChange = {
                    viewModel.setConfirmationPreferences(preferences.copy(confirmTaskComplete = it))
                    notifySettingChanged()
                },
            )
            ConfirmationToggleRow(
                title = stringResource(R.string.confirm_task_archive_setting_title),
                checked = preferences.confirmTaskArchive,
                onCheckedChange = {
                    viewModel.setConfirmationPreferences(preferences.copy(confirmTaskArchive = it))
                    notifySettingChanged()
                },
            )
            ConfirmationToggleRow(
                title = stringResource(R.string.confirm_subtask_delete_setting_title),
                checked = preferences.confirmSubtaskDelete,
                onCheckedChange = {
                    viewModel.setConfirmationPreferences(preferences.copy(confirmSubtaskDelete = it))
                    notifySettingChanged()
                },
            )
            ConfirmationToggleRow(
                title = stringResource(R.string.confirm_subtask_complete_setting_title),
                checked = preferences.confirmSubtaskComplete,
                onCheckedChange = {
                    viewModel.setConfirmationPreferences(preferences.copy(confirmSubtaskComplete = it))
                    notifySettingChanged()
                },
            )
            ConfirmationToggleRow(
                title = stringResource(R.string.confirm_subtask_add_setting_title),
                checked = preferences.confirmSubtaskAdd,
                onCheckedChange = {
                    viewModel.setConfirmationPreferences(preferences.copy(confirmSubtaskAdd = it))
                    notifySettingChanged()
                },
            )
            ConfirmationToggleRow(
                title = stringResource(R.string.confirm_category_delete_setting_title),
                checked = preferences.confirmCategoryDelete,
                onCheckedChange = {
                    viewModel.setConfirmationPreferences(preferences.copy(confirmCategoryDelete = it))
                    notifySettingChanged()
                },
            )
            ConfirmationToggleRow(
                title = stringResource(R.string.confirm_list_delete_setting_title),
                checked = preferences.confirmListDelete,
                onCheckedChange = {
                    viewModel.setConfirmationPreferences(preferences.copy(confirmListDelete = it))
                    notifySettingChanged()
                },
            )
            ConfirmationToggleRow(
                title = stringResource(R.string.confirm_archived_task_delete_setting_title),
                checked = preferences.confirmArchivedTaskDelete,
                onCheckedChange = {
                    viewModel.setConfirmationPreferences(preferences.copy(confirmArchivedTaskDelete = it))
                    notifySettingChanged()
                },
            )
            ConfirmationToggleRow(
                title = stringResource(R.string.confirm_trash_delete_setting_title),
                checked = preferences.confirmTrashDelete,
                onCheckedChange = {
                    viewModel.setConfirmationPreferences(preferences.copy(confirmTrashDelete = it))
                    notifySettingChanged()
                },
            )
        }
    }
}

@Composable
private fun ConfirmationToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
