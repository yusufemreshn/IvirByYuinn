package com.yusufemre.ivirbyyuinn.ui.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.yusufemre.ivirbyyuinn.ui.home.ShowHomeScreenViewModel
import com.yusufemre.ivirbyyuinn.ui.home.ShowHomeScreenViewModelFactory
import com.yusufemre.ivirbyyuinn.ui.util.AnimatedDialog

// Ayarlar > Davranış: Onay Mesajları (kendi alt ekranı) ve Giriş Ekranı,
// ikisi de "uygulama belirli anlarda nasıl tepki veriyor" sorusuna cevap
// veriyor.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BehaviorSettingsScreen(onClose: () -> Unit) {
    val application = LocalContext.current.applicationContext as IvirApplication

    val showHomeScreenViewModel: ShowHomeScreenViewModel = viewModel(
        factory = ShowHomeScreenViewModelFactory(
            getShowHomeScreenPreferenceUseCase = application.getShowHomeScreenPreferenceUseCase,
            setShowHomeScreenPreferenceUseCase = application.setShowHomeScreenPreferenceUseCase,
        ),
    )
    val showHomeScreenPreference by showHomeScreenViewModel.showHomeScreen.collectAsStateWithLifecycle()

    var showConfirmationSettings by remember { mutableStateOf(false) }

    val (snackbarHostState, notifySettingChanged) = rememberSettingChangedSnackbar()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_category_behavior_title)) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsSectionCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showConfirmationSettings = true },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.confirmation_settings_row_title), style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = stringResource(R.string.confirmation_settings_row_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }

            SettingsSectionCard {
                Text(text = stringResource(R.string.home_screen_setting_title), style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.home_screen_setting_row_title), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = stringResource(R.string.home_screen_setting_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = showHomeScreenPreference,
                        onCheckedChange = { checked ->
                            showHomeScreenViewModel.setShowHomeScreen(checked)
                            notifySettingChanged()
                        },
                    )
                }
            }
        }
    }

    if (showConfirmationSettings) {
        AnimatedDialog(onDismissRequest = { showConfirmationSettings = false }) { dismiss ->
            ConfirmationSettingsScreen(onClose = dismiss)
        }
    }
}
