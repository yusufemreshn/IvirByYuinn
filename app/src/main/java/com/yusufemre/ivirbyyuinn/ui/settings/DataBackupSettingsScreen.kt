package com.yusufemre.ivirbyyuinn.ui.settings

import android.content.ContentResolver
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yusufemre.ivirbyyuinn.IvirApplication
import com.yusufemre.ivirbyyuinn.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Ayarlar > Veri & Yedekleme: yerel dosya yedeği (Faz 6) ile Google Drive
// yedeklemesi (appDataFolder) AYNI ekranda, çünkü ikisi de aynı soruya
// ("verim nerede saklı/nasıl geri getiririm") cevap veriyor.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataBackupSettingsScreen(onClose: () -> Unit) {
    val application = LocalContext.current.applicationContext as IvirApplication
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            exportBackupUseCase = application.exportBackupUseCase,
            importBackupUseCase = application.importBackupUseCase,
        ),
    )

    val googleDriveViewModel: GoogleDriveViewModel = viewModel(
        factory = GoogleDriveViewModelFactory(
            getGoogleDriveSignInIntentUseCase = application.getGoogleDriveSignInIntentUseCase,
            completeGoogleDriveSignInUseCase = application.completeGoogleDriveSignInUseCase,
            getGoogleDriveAccountEmailUseCase = application.getGoogleDriveAccountEmailUseCase,
            signOutGoogleDriveUseCase = application.signOutGoogleDriveUseCase,
            uploadBackupToDriveUseCase = application.uploadBackupToDriveUseCase,
            restoreBackupFromDriveUseCase = application.restoreBackupFromDriveUseCase,
            getGoogleDriveBackupPreferencesUseCase = application.getGoogleDriveBackupPreferencesUseCase,
            setGoogleDriveAutoBackupEnabledUseCase = application.setGoogleDriveAutoBackupEnabledUseCase,
        ),
    )
    val driveAccountEmail by googleDriveViewModel.accountEmail.collectAsStateWithLifecycle()
    val driveBackupPreferences by googleDriveViewModel.backupPreferences.collectAsStateWithLifecycle()
    val lastBackupFormatter = remember { SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showRestoreWarning by remember { mutableStateOf(false) }
    var showDriveRestoreWarning by remember { mutableStateOf(false) }

    val backupSavedMessage = stringResource(R.string.backup_saved)
    val backupSaveFailedMessage = stringResource(R.string.backup_save_failed)
    val fileReadFailedMessage = stringResource(R.string.file_read_failed)
    val backupRestoredMessage = stringResource(R.string.backup_restored)
    val backupInvalidMessage = stringResource(R.string.backup_invalid)
    val driveSignInFailedMessage = stringResource(R.string.google_drive_sign_in_failed)
    val driveBackupSuccessMessage = stringResource(R.string.google_drive_backup_success)
    val driveBackupFailedMessage = stringResource(R.string.google_drive_backup_failed)
    val driveRestoreSuccessMessage = stringResource(R.string.google_drive_restore_success)
    val driveRestoreFailedMessage = stringResource(R.string.google_drive_restore_failed)
    val settingChangedMessage = stringResource(R.string.setting_updated_snackbar)

    val driveSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        googleDriveViewModel.onSignInResult(result.data)
        if (googleDriveViewModel.accountEmail.value == null) {
            coroutineScope.launch { snackbarHostState.showSnackbar(driveSignInFailedMessage) }
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            val json = viewModel.exportBackup()
            val success = writeTextToUri(context.contentResolver, uri, json)
            snackbarHostState.showSnackbar(if (success) backupSavedMessage else backupSaveFailedMessage)
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            val json = readTextFromUri(context.contentResolver, uri)
            if (json == null) {
                snackbarHostState.showSnackbar(fileReadFailedMessage)
                return@launch
            }
            val result = viewModel.importBackup(json)
            val message = if (result.isSuccess) backupRestoredMessage else backupInvalidMessage
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Long)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_category_data_title)) },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsSectionCard {
                Text(text = stringResource(R.string.data_management_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.data_management_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                )

                Button(
                    onClick = { createDocumentLauncher.launch(defaultBackupFileName()) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.export_backup))
                }

                OutlinedButton(
                    onClick = { showRestoreWarning = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.import_backup))
                }
            }

            SettingsSectionCard {
                Text(text = stringResource(R.string.google_drive_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.google_drive_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                )

                if (driveAccountEmail == null) {
                    Button(
                        onClick = { driveSignInLauncher.launch(googleDriveViewModel.signInIntent()) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.google_drive_connect))
                    }
                } else {
                    Text(
                        text = stringResource(R.string.google_drive_connected_as, driveAccountEmail ?: ""),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val result = googleDriveViewModel.uploadBackup()
                                val message = if (result.isSuccess) driveBackupSuccessMessage else driveBackupFailedMessage
                                snackbarHostState.showSnackbar(message)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.google_drive_backup_now))
                    }

                    OutlinedButton(
                        onClick = { showDriveRestoreWarning = true },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    ) {
                        Text(stringResource(R.string.google_drive_restore))
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = stringResource(R.string.google_drive_auto_backup_title), style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = stringResource(R.string.google_drive_auto_backup_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = driveBackupPreferences.autoBackupEnabled,
                            onCheckedChange = { checked ->
                                googleDriveViewModel.setAutoBackupEnabled(checked)
                                coroutineScope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    snackbarHostState.showSnackbar(settingChangedMessage)
                                }
                            },
                        )
                    }

                    Text(
                        text = driveBackupPreferences.lastBackupAtMillis?.let {
                            stringResource(R.string.google_drive_last_backup, lastBackupFormatter.format(Date(it)))
                        } ?: stringResource(R.string.google_drive_never_backed_up),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )

                    TextButton(
                        onClick = {
                            googleDriveViewModel.signOut()
                            googleDriveViewModel.setAutoBackupEnabled(false)
                        },
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Text(stringResource(R.string.google_drive_disconnect))
                    }
                }
            }
        }
    }

    if (showRestoreWarning) {
        AlertDialog(
            onDismissRequest = { showRestoreWarning = false },
            title = { Text(stringResource(R.string.restore_warning_title)) },
            text = { Text(stringResource(R.string.restore_warning_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreWarning = false
                    openDocumentLauncher.launch(arrayOf("application/json"))
                }) { Text(stringResource(R.string.action_proceed)) }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreWarning = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }

    if (showDriveRestoreWarning) {
        // Metin olarak yerel geri yükleme uyarısıyla (restore_warning_title/
        // text) AYNI: ikisi de "TÜM veri yerine yazılacak" riskini anlatıyor,
        // ayrı bir string çifti açmaya gerek yok.
        AlertDialog(
            onDismissRequest = { showDriveRestoreWarning = false },
            title = { Text(stringResource(R.string.restore_warning_title)) },
            text = { Text(stringResource(R.string.restore_warning_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showDriveRestoreWarning = false
                    coroutineScope.launch {
                        val result = googleDriveViewModel.restoreBackup()
                        val message = if (result.isSuccess) driveRestoreSuccessMessage else driveRestoreFailedMessage
                        snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Long)
                    }
                }) { Text(stringResource(R.string.action_proceed)) }
            },
            dismissButton = {
                TextButton(onClick = { showDriveRestoreWarning = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

private fun defaultBackupFileName(): String {
    val timestamp = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.getDefault()).format(System.currentTimeMillis())
    return "ivirbyyuinn_yedek_$timestamp.json"
}

// contentResolver ile SAF Uri'lerine yazma/okuma her zaman diskle
// konuşuyor, o yüzden Dispatchers.IO'ya geçiyoruz. Compose'un ana
// thread'ini (UI'ı) bloklamamak için.
private suspend fun writeTextToUri(resolver: ContentResolver, uri: Uri, text: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            resolver.openOutputStream(uri)?.use { stream ->
                stream.write(text.toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: java.io.IOException) {
            false
        }
    }
}

private suspend fun readTextFromUri(resolver: ContentResolver, uri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            resolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
        } catch (e: java.io.IOException) {
            null
        }
    }
}
