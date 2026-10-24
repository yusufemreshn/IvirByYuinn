package com.yusufemre.ivirbyyuinn.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yusufemre.ivirbyyuinn.IvirApplication
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.ui.settings.SettingsScreen
import com.yusufemre.ivirbyyuinn.ui.util.AnimatedDialog
import com.yusufemre.ivirbyyuinn.ui.util.findActivity

// Uygulama süreç ilk açıldığında (soğuk başlangıç) gösterilen giriş
// ekranı: MainActivity bunu sadece BİR KERE gösteriyor, arka plandan
// her dönüşte değil (o zaten AppLockScreen'in işi, bkz. Faz 7a).
// Widget'ın "+" butonundan geldiysek bu ekran tamamen atlanıyor.
//
// Kullanıcı geri bildirimi: eski ekran her şeyi dikeyde ortalıyordu, bol
// boş alan vardı, görev sayısı küçük bir satırdı, ortadaki tik rozeti de
// "saçma" bulundu. Yeniden tasarım: rozet tamamen kalktı; bekleyen görev
// sayısı artık ekranın BAŞLIĞI, "Ivır" küçük bir üst işarete (wordmark)
// geri çekildi, üç aksiyon ekranın altına yaslanan yükseltilmiş bir
// "sayfaya" toplandı, ortada kalan boşluğa da rastgele bir söz geliyor.
@Composable
fun HomeScreen(onEnterApp: () -> Unit) {
    // rememberSaveable: Ayarlar > Görünüm'de Yazı Boyutu/Dil değişince
    // Activity recreate() oluyor, bkz. AppearanceSettingsScreen. Bu
    // sayede recreate sonrası kullanıcı Ayarlar açıkken HomeScreen'e değil
    // yine Ayarlar'a dönüyor.
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    // Faz 10b: HomeScreen artık kendi (küçük, sadece bunun için var olan)
    // ViewModel'ini kuruyor, TaskListScreen'deki dev TaskViewModel'i
    // buraya taşımıyoruz, sadece "kaç görev seni bekliyor" bilgisine
    // ihtiyacımız var.
    val application = LocalContext.current.applicationContext as IvirApplication
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            getTaskListsUseCase = application.getTaskListsUseCase,
            getTasksUseCase = application.getTasksUseCase,
        ),
    )
    val activeTaskCount by homeViewModel.activeTaskCount.collectAsStateWithLifecycle()

    // remember (activeTaskCount'a bağlı DEĞİL): ekran süreç başına bir kez
    // görüldüğü için söz de süreç başına bir kez seçiliyor, görev sayısı
    // güncellendikçe (ör. widget'tan gelen bir değişiklik) rastgele rastgele
    // değişip dikkat dağıtmıyor.
    val quotes = stringArrayResource(R.array.home_quotes)
    val quote = remember { quotes.random() }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(modifier = Modifier.padding(horizontal = 28.dp, vertical = 28.dp)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                )
                Text(
                    text = homeHeadline(activeTaskCount),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 18.dp),
                )
                Text(
                    text = stringResource(R.string.home_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Uygulamanın geri kalanı (TopAppBar, butonlar, metinler)
                // hep sans-serif; burada BİLEREK FontFamily.Serif + italik
                // kullanıyoruz, ekranın tek "ses" anı bir arayüz etiketi
                // gibi değil, biri sana bir şey söylüyormuş gibi okunsun.
                Text(
                    text = "“$quote”",
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Button(onClick = onEnterApp, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.home_enter_app))
                    }
                    OutlinedButton(
                        onClick = { showSettings = true },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    ) {
                        Text(stringResource(R.string.switcher_settings))
                    }
                    TextButton(
                        onClick = { context.findActivity()?.finish() },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    ) {
                        Text(stringResource(R.string.home_exit))
                    }
                }
            }
        }
    }

    if (showSettings) {
        AnimatedDialog(onDismissRequest = { showSettings = false }) { dismiss ->
            SettingsScreen(onClose = dismiss)
        }
    }
}

// Üç duruma göre farklı başlık: sayı yüklenmediyse (null) sessizce uygulama
// adını, sıfırsa "bekleyen yok" cümlesini, doluysa sayıyı (vurgulanmış
// renkte) içeren cümleyi gösteriyor. "0 görev seni bekliyor" gibi tuhaf/
// anlamsız bir cümle kurmaktan bilerek kaçınıyoruz.
@Composable
private fun homeHeadline(count: Int?) = when {
    count == null -> buildAnnotatedString { append(stringResource(R.string.app_name)) }
    count == 0 -> buildAnnotatedString { append(stringResource(R.string.home_headline_empty)) }
    else -> {
        val text = pluralStringResource(R.plurals.home_active_tasks_summary, count, count)
        val numberText = count.toString()
        buildAnnotatedString {
            // Her iki dilde de ("%d görev..."/"%d task(s)...") sayı
            // CÜMLENİN BAŞINDA, bu yüzden sadece baştaki rakamları
            // vurguluyoruz; beklenmedik bir biçimle karşılaşırsak (ör.
            // ileride farklı bir dil eklenirse) hiç vurgu yapmadan tüm
            // metni olduğu gibi yazıyoruz, kırılmaya karşı güvenli.
            if (text.startsWith(numberText)) {
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append(numberText) }
                append(text.substring(numberText.length))
            } else {
                append(text)
            }
        }
    }
}
