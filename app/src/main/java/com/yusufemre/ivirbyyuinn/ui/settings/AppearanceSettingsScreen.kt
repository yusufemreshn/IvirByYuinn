package com.yusufemre.ivirbyyuinn.ui.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yusufemre.ivirbyyuinn.IvirApplication
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.data.AppColorTheme
import com.yusufemre.ivirbyyuinn.data.FontScalePreference
import com.yusufemre.ivirbyyuinn.data.LanguagePreference
import com.yusufemre.ivirbyyuinn.data.ThemePreference
import com.yusufemre.ivirbyyuinn.ui.theme.ColorThemeViewModel
import com.yusufemre.ivirbyyuinn.ui.theme.ColorThemeViewModelFactory
import com.yusufemre.ivirbyyuinn.ui.theme.FontScaleViewModel
import com.yusufemre.ivirbyyuinn.ui.theme.FontScaleViewModelFactory
import com.yusufemre.ivirbyyuinn.ui.theme.ThemeViewModel
import com.yusufemre.ivirbyyuinn.ui.theme.ThemeViewModelFactory
import com.yusufemre.ivirbyyuinn.ui.theme.label
import com.yusufemre.ivirbyyuinn.ui.theme.swatchColor
import com.yusufemre.ivirbyyuinn.ui.util.findActivity

// Ayarlar > Görünüm: Dil, Yazı Boyutu ve Renk Teması (23 seçenek), üçü de
// "uygulama ekranda nasıl görünüyor" sorusuna cevap veriyor.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(onClose: () -> Unit) {
    val application = LocalContext.current.applicationContext as IvirApplication

    val languageViewModel: LanguageViewModel = viewModel(
        factory = LanguageViewModelFactory(
            getLanguagePreferenceUseCase = application.getLanguagePreferenceUseCase,
            setLanguagePreferenceUseCase = application.setLanguagePreferenceUseCase,
        ),
    )
    val languagePreference by languageViewModel.languagePreference.collectAsStateWithLifecycle()

    val fontScaleViewModel: FontScaleViewModel = viewModel(
        factory = FontScaleViewModelFactory(
            getFontScalePreferenceUseCase = application.getFontScalePreferenceUseCase,
            setFontScalePreferenceUseCase = application.setFontScalePreferenceUseCase,
        ),
    )
    val fontScalePreference by fontScaleViewModel.fontScalePreference.collectAsStateWithLifecycle()

    val colorThemeViewModel: ColorThemeViewModel = viewModel(
        factory = ColorThemeViewModelFactory(
            getAppColorThemeUseCase = application.getAppColorThemeUseCase,
            setAppColorThemeUseCase = application.setAppColorThemeUseCase,
        ),
    )
    val colorTheme by colorThemeViewModel.colorTheme.collectAsStateWithLifecycle()

    // ThemeToggleButton'daki (TopAppBar'daki hızlı erişim) ile AYNI örnek:
    // MainActivity'yle Activity paylaşılı olduğu için burada seçilen Açık/
    // Koyu/Sistem anında IvirTheme'e yansıyor, recreate GEREKMİYOR.
    val themeViewModel: ThemeViewModel = viewModel(
        factory = ThemeViewModelFactory(
            getThemePreferenceUseCase = application.getThemePreferenceUseCase,
            setThemePreferenceUseCase = application.setThemePreferenceUseCase,
        ),
    )
    val themePreference by themeViewModel.themePreference.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val (snackbarHostState, notifySettingChanged) = rememberSettingChangedSnackbar()

    // Dil/Yazı Boyutu recreate() istediği için o anki composition'ı anında
    // yok ediyor, showSnackbar çağrısı hiç başlamadan kayboluyordu (bkz.
    // önceki yorum). rememberSaveable olduğu için recreate'i AŞIYOR: değişikliği
    // burada not düşüp, YENİDEN kurulan ekran ilk composition'a geldiğinde
    // (LaunchedEffect(Unit)) teyidi gösteriyoruz.
    var pendingConfirmation by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (pendingConfirmation) {
            pendingConfirmation = false
            notifySettingChanged()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_category_appearance_title)) },
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
                Text(text = stringResource(R.string.language_title), style = MaterialTheme.typography.titleMedium)
                LanguagePicker(
                    selected = languagePreference,
                    onSelect = { preference ->
                        languageViewModel.setLanguagePreference(preference)
                        // attachBaseContext sadece Activity İLK kurulurken
                        // çalışıyor, yeni dilin gerçekten uygulanması için
                        // Activity'yi baştan kurdurmamız (recreate) gerekiyor.
                        pendingConfirmation = true
                        context.findActivity()?.recreate()
                    },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            SettingsSectionCard {
                Text(text = stringResource(R.string.font_size_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.font_size_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                )
                FontSizePicker(
                    selected = fontScalePreference,
                    onSelect = {
                        fontScaleViewModel.setFontScalePreference(it)
                        // LanguagePicker'daki ile AYNI sebep: Yazı Boyutu artık
                        // Configuration.fontScale üzerinden (attachBaseContext)
                        // uygulanıyor ki Ayarlar dahil TÜM Dialog'lar da
                        // etkilensin, bu da ancak recreate() ile devreye giriyor.
                        // Hangi alt ekranda olduğumuz (showAppearance vb.)
                        // rememberSaveable olduğu için recreate SONRASI AYNI
                        // ekranda kalıyoruz, "aynı yerde kalsın" isteği korunuyor.
                        // Teyidi burada GÖSTERMİYORUZ (recreate composition'ı
                        // anında yok eder): pendingConfirmation ile not düşüp
                        // yeniden kurulan ekranda gösteriyoruz.
                        pendingConfirmation = true
                        context.findActivity()?.recreate()
                    },
                )
            }

            SettingsSectionCard {
                Text(text = stringResource(R.string.theme_mode_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.theme_mode_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                )
                ThemeModePicker(
                    selected = themePreference,
                    onSelect = { preference ->
                        themeViewModel.setThemePreference(preference)
                        notifySettingChanged()
                    },
                )
            }

            SettingsSectionCard {
                Text(text = stringResource(R.string.color_theme_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.color_theme_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                // Kullanıcı geri bildirimi: 23 daireyi taramadan şu anki
                // temayı hatırlatan küçük bir satır.
                Text(
                    text = stringResource(R.string.color_theme_selected_format, colorTheme.label()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                )
                // Eskiden FlowRow'du: sabit 68dp genişlikli daireleri soldan
                // sağa dizip sığmayınca alt satıra geçiriyordu, ama satırı
                // kartın tam genişliğine hiç YAYMIYORDU. Şimdi 3'erli
                // satırlara elle bölüp her hücreye weight(1f) veriyoruz.
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AppColorTheme.entries.chunked(3).forEach { rowThemes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            rowThemes.forEach { theme ->
                                ColorThemeSwatch(
                                    theme = theme,
                                    selected = theme == colorTheme,
                                    onClick = {
                                        colorThemeViewModel.setColorTheme(theme)
                                        notifySettingChanged()
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            repeat(3 - rowThemes.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Daha önce sadece TopAppBar'daki ThemeToggleButton'ın dropdown'ında vardı;
// kullanıcı isteğiyle Ayarlar > Görünüm'e de eklendi. Dil/Yazı Boyutu
// picker'larıyla AYNI FilterChip deseni.
@Composable
private fun ThemeModePicker(
    selected: ThemePreference,
    onSelect: (ThemePreference) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items = ThemePreference.entries, key = { it.name }) { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(option.label()) },
            )
        }
    }
}

@Composable
private fun ThemePreference.label(): String = when (this) {
    ThemePreference.SYSTEM -> stringResource(R.string.theme_system)
    ThemePreference.LIGHT -> stringResource(R.string.theme_light)
    ThemePreference.DARK -> stringResource(R.string.theme_dark)
}

// Kullanıcı geri bildirimi: eskiden bir dropdown menüydü, Yazı Boyutu'yla
// (zaten FilterChip kullanan) aynı seçim dilinden sapıyordu. Şimdi AYNI
// desen: tüm seçenekler açmadan tek bakışta görünüyor.
@Composable
private fun LanguagePicker(
    selected: LanguagePreference,
    onSelect: (LanguagePreference) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items = LanguagePreference.entries, key = { it.name }) { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(option.label()) },
            )
        }
    }
}

// Öncelik seçiciyle (TaskEditorSheet) aynı desen, dropdown yerine 3
// seçeneğin tamamı tek bakışta görünsün diye FilterChip satırı. LazyRow
// (düz Row DEĞİL): dar ekranda/büyük yazı tipinde metnin bölündüğü bir
// hata bulunmuştu, LazyRow chip'leri asla sıkıştırmadan yatay kaydırmaya
// izin veriyor.
//
// Kullanıcı geri bildirimi: her hapın etiketi artık KENDİ boyutunda bir
// "Aa" önizlemesi taşıyor (Küçük'te küçük, Büyük'te büyük yazılı) —
// hangisini seçtiğini metinden değil, gözle de anlıyorsun.
@Composable
private fun FontSizePicker(
    selected: FontScalePreference,
    onSelect: (FontScalePreference) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items = FontScalePreference.entries, key = { it.name }) { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Aa", fontSize = option.previewFontSize(), fontWeight = FontWeight.Bold)
                        Text(text = option.label(), style = MaterialTheme.typography.labelSmall)
                    }
                },
            )
        }
    }
}

private fun FontScalePreference.previewFontSize() = when (this) {
    FontScalePreference.SMALL -> 13.sp
    FontScalePreference.MEDIUM -> 16.sp
    FontScalePreference.LARGE -> 19.sp
}

@Composable
private fun FontScalePreference.label(): String = when (this) {
    FontScalePreference.SMALL -> stringResource(R.string.font_size_small)
    FontScalePreference.MEDIUM -> stringResource(R.string.font_size_medium)
    FontScalePreference.LARGE -> stringResource(R.string.font_size_large)
}

// Ayarlar > Renk Teması ızgarasındaki tek bir yuvarlak örnek. DYNAMIC
// tema tek bir sabit rengi temsil edemiyor (duvar kağıdına göre değişken)
// onun için sweep gradient kullanıyoruz, "otomatik/karma" hissi versin diye.
@Composable
private fun ColorThemeSwatch(theme: AppColorTheme, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        val background = if (theme == AppColorTheme.DYNAMIC) {
            Brush.sweepGradient(listOf(Color(0xFFE8735C), Color(0xFF7C5CBF), Color(0xFF2E93C7), Color(0xFFE8735C)))
        } else {
            Brush.linearGradient(listOf(theme.swatchColor(), theme.swatchColor()))
        }
        val borderWidth by animateDpAsState(targetValue = if (selected) 3.dp else 1.dp, label = "swatch-border")
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(background)
                .border(
                    width = borderWidth,
                    color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape,
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
            }
        }
        Text(
            text = theme.label(),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun LanguagePreference.label(): String = when (this) {
    LanguagePreference.SYSTEM -> stringResource(R.string.language_system)
    LanguagePreference.TURKISH -> stringResource(R.string.language_turkish)
    LanguagePreference.ENGLISH -> stringResource(R.string.language_english)
}
