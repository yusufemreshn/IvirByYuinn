package com.yusufemre.ivirbyyuinn.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yusufemre.ivirbyyuinn.IvirApplication
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.data.ThemePreference

// TopAppBar'ın "actions" bölümüne konan küçük bir buton. viewModel()
// çağrısı burada da MainActivity'dekiyle AYNI ThemeViewModel örneğini
// döndürüyor (ikisi de aynı Activity'ye bağlı), yani burada tema
// değiştirdiğimizde, MainActivity'nin IvirTheme'i otomatik güncelleniyor,
// elle bir "geri bildirim" mekanizması kurmamıza gerek kalmadı.
@Composable
fun ThemeToggleButton() {
    val application = LocalContext.current.applicationContext as IvirApplication
    val themeViewModel: ThemeViewModel = viewModel(
        factory = ThemeViewModelFactory(
            getThemePreferenceUseCase = application.getThemePreferenceUseCase,
            setThemePreferenceUseCase = application.setThemePreferenceUseCase,
        ),
    )
    val themePreference by themeViewModel.themePreference.collectAsStateWithLifecycle()
    var menuExpanded by remember { mutableStateOf(false) }

    // Faz 8: buton "Sistem"/"Açık"/"Koyu" YAZMIYOR, tercihi temsil eden
    // küçük bir simge gösteriyor: menüdeki 3 seçenek hiç değişmedi, sadece
    // tetikleyici butonun görünümü. Faz 10a'ya kadar bu emoji glifiydi
    // ("☀️"/"🌙"/"🌓"); artık IvirIcons'taki elden çizilmiş güneş/ay/otomatik
    // ikonları kullanılıyor, markanın kendi çizim diliyle tutarlı.
    val themePreferenceDescription = themePreference.label()

    Box {
        IconButton(
            onClick = { menuExpanded = true },
            modifier = Modifier.semantics { contentDescription = themePreferenceDescription },
        ) {
            Icon(imageVector = themePreference.icon(), contentDescription = null)
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            ThemePreference.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label()) },
                    onClick = {
                        themeViewModel.setThemePreference(option)
                        menuExpanded = false
                    },
                )
            }
        }
    }
}

private fun ThemePreference.icon() = when (this) {
    ThemePreference.SYSTEM -> IvirIcons.Auto
    ThemePreference.LIGHT -> IvirIcons.Sun
    ThemePreference.DARK -> IvirIcons.Moon
}

@Composable
private fun ThemePreference.label(): String = when (this) {
    ThemePreference.SYSTEM -> stringResource(R.string.theme_system)
    ThemePreference.LIGHT -> stringResource(R.string.theme_light)
    ThemePreference.DARK -> stringResource(R.string.theme_dark)
}
