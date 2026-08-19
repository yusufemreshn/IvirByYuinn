package com.yusufemre.ivirbyyuinn

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yusufemre.ivirbyyuinn.data.LanguagePreference
import com.yusufemre.ivirbyyuinn.data.ThemePreference
import com.yusufemre.ivirbyyuinn.ui.home.HomeScreen
import com.yusufemre.ivirbyyuinn.ui.lock.AppLockScreen
import com.yusufemre.ivirbyyuinn.ui.lock.AppLockViewModel
import com.yusufemre.ivirbyyuinn.ui.lock.AppLockViewModelFactory
import com.yusufemre.ivirbyyuinn.ui.task.TaskListScreen
import com.yusufemre.ivirbyyuinn.ui.theme.ColorThemeViewModel
import com.yusufemre.ivirbyyuinn.ui.theme.ColorThemeViewModelFactory
import com.yusufemre.ivirbyyuinn.ui.theme.FontScaleViewModel
import com.yusufemre.ivirbyyuinn.ui.theme.FontScaleViewModelFactory
import com.yusufemre.ivirbyyuinn.ui.theme.IvirTheme
import com.yusufemre.ivirbyyuinn.ui.theme.ThemeViewModel
import com.yusufemre.ivirbyyuinn.ui.theme.ThemeViewModelFactory
import java.util.Locale

// Faz 7a'dan itibaren ComponentActivity değil FragmentActivity'den
// türüyor: BiometricPrompt (androidx.biometric) bunu istiyor.
// FragmentActivity zaten ComponentActivity'nin bir alt sınıfı olduğu
// için Compose'un setContent'i ve her şey aynı şekilde çalışmaya devam
// ediyor, bu değişiklik başka hiçbir şeyi bozmuyor.
class MainActivity : FragmentActivity() {

    // Faz 7c: kullanıcı dili elle sabitlediyse (SYSTEM değilse), Activity
    // gerçekten oluşmadan ÖNCE, en erken kancada devreye giriyoruz.
    // attachBaseContext, onCreate'ten bile önce çalışıyor: Compose/
    // ViewModel dünyası henüz yok, bu yüzden IvirApplication'dan senkron
    // bir okuma yapıyoruz (bkz. IvirApplication.currentLanguagePreference).
    // .NET'te bir ASP.NET Core middleware'inin, asıl controller/action
    // çalışmadan önce request'e müdahale etmesine benzer bir zamanlama:
    // burada da "asıl ekran çizilmeden önce, hangi dilde çizileceğine"
    // karar veriyoruz.
    override fun attachBaseContext(newBase: Context) {
        val application = newBase.applicationContext as IvirApplication
        val preference = application.currentLanguagePreference()
        val localizedContext = when (preference) {
            LanguagePreference.SYSTEM -> newBase
            LanguagePreference.TURKISH -> newBase.withLocale(Locale.forLanguageTag("tr"))
            LanguagePreference.ENGLISH -> newBase.withLocale(Locale.forLanguageTag("en"))
        }
        super.attachBaseContext(localizedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // İçerik status bar'ın/navigation bar'ın altına kadar uzanabilsin diye
        // edge-to-edge'i açıyoruz; modern Android'de artık varsayılan beklenti bu.
        enableEdgeToEdge()

        // Faz 12b: başka bir uygulamadan "Paylaş" ile buraya düşen düz
        // metin: EXTRA_TEXT'i başlık olarak kullanmaya çalışıyoruz.
        // Paylaşılan metin koca bir makale de olabilir, o yüzden ilk
        // satırla ve makul bir uzunlukla sınırlıyoruz (bir görev başlığı
        // hiçbir zaman bu kadar uzun olmamalı).
        val sharedTaskTitle = if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)
                ?.trim()
                ?.lineSequence()?.firstOrNull()
                ?.take(MAX_SHARED_TITLE_LENGTH)
                ?.takeIf { it.isNotBlank() }
        } else {
            null
        }

        // Widget'taki "+" butonu ya da Quick Settings tile'ı bu extra'yı
        // true yaparak açıyor: uygulama açılır açılmaz görev ekleme
        // ekranının otomatik açılmasını istiyoruz. Faz 12a'dan itibaren
        // launcher ikonuna uzun basınca çıkan App Shortcuts'tan gelen
        // ACTION_ADD_TASK de AYNI davranışı tetikliyor: üç farklı giriş
        // noktası (widget/tile/kısayol), tek bir ortak sonuç. Faz 12b'de
        // paylaşım sayfasından gelmek de (dördüncü giriş noktası) aynı
        // "editörü hemen aç" sonucuna bağlanıyor.
        val openAddTaskOnLaunch = intent.getBooleanExtra(EXTRA_OPEN_ADD_TASK, false) ||
            intent.action == ACTION_ADD_TASK ||
            sharedTaskTitle != null

        setContent {
            val application = LocalContext.current.applicationContext as IvirApplication
            val themeViewModel: ThemeViewModel = viewModel(
                factory = ThemeViewModelFactory(
                    getThemePreferenceUseCase = application.getThemePreferenceUseCase,
                    setThemePreferenceUseCase = application.setThemePreferenceUseCase,
                ),
            )
            val themePreference by themeViewModel.themePreference.collectAsStateWithLifecycle()

            // Faz 4b'de eklendi: artık sadece sistemi takip etmiyoruz,
            // kullanıcı "Açık" ya da "Koyu"yu sabitleyebiliyor.
            val darkTheme = when (themePreference) {
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
            }

            // Faz 9: hangi RENK teması (Teal/Okyanus/Gün Batımı/... 20+
            // seçenek), Açık/Koyu'dan bağımsız, ayrı bir tercih.
            val colorThemeViewModel: ColorThemeViewModel = viewModel(
                factory = ColorThemeViewModelFactory(
                    getAppColorThemeUseCase = application.getAppColorThemeUseCase,
                    setAppColorThemeUseCase = application.setAppColorThemeUseCase,
                ),
            )
            val colorTheme by colorThemeViewModel.colorTheme.collectAsStateWithLifecycle()

            // 2026-08-09: Ayarlar > Yazı Boyutu, ThemeViewModel/ColorThemeViewModel
            // ile aynı desen, MainActivity ve SettingsScreen aynı Activity'yi
            // paylaştığı için ikisi de AYNI örneği görüyor.
            val fontScaleViewModel: FontScaleViewModel = viewModel(
                factory = FontScaleViewModelFactory(
                    getFontScalePreferenceUseCase = application.getFontScalePreferenceUseCase,
                    setFontScalePreferenceUseCase = application.setFontScalePreferenceUseCase,
                ),
            )
            val fontScalePreference by fontScaleViewModel.fontScalePreference.collectAsStateWithLifecycle()

            // Faz 7a: uygulama kilidi. ThemeViewModel gibi, MainActivity ve
            // TaskListScreen aynı Activity'yi (ViewModelStoreOwner) paylaştığı
            // için burada kurulan AppLockViewModel örneği ihtiyaç duyan her
            // yerde AYNI örnek: elle bir "paylaşılan kilit durumu" mekanizması
            // kurmaya gerek kalmadı.
            val appLockViewModel: AppLockViewModel = viewModel(
                factory = AppLockViewModelFactory(
                    isAppLockEnabledUseCase = application.isAppLockEnabledUseCase,
                    verifyAppLockPinUseCase = application.verifyAppLockPinUseCase,
                    setAppLockPinUseCase = application.setAppLockPinUseCase,
                    disableAppLockUseCase = application.disableAppLockUseCase,
                ),
            )
            val isLockEnabled by appLockViewModel.isLockEnabled.collectAsStateWithLifecycle()
            val isUnlockedForThisSession by appLockViewModel.isUnlockedForThisSession.collectAsStateWithLifecycle()
            val isLocked = isLockEnabled && !isUnlockedForThisSession

            // Faz 8: giriş ekranı sadece process İLK açıldığında gösteriliyor
            // (remember, Activity arka plana atılıp geri gelince SIFIRLANMIYOR,
            // o zaten AppLockScreen'in işi). Widget'ın "+" butonundan
            // geldiysek bu ekranı tamamen atlıyoruz, kullanıcı zaten "hemen
            // görev ekle" niyetiyle geldi.
            //
            // 2026-08-09: Ayarlar > Giriş Ekranı, kullanıcı bu ekranı hiç
            // görmek istemeyebilir. Bilerek CANLI bir StateFlow/ViewModel
            // KULLANMIYORUZ: application.currentShowHomeScreenPreference()
            // senkron, TEK SEFERLİK bir okuma (openAddTaskOnLaunch gibi
            // düz bir val). Eğer bunun yerine reaktif bir StateFlow'la "VE"
            // yapsaydık, kullanıcı Ayarlar'ı giriş ekranının ÜSTÜNDE açıp
            // anahtarı kapattığı anda showHomeScreen false olur, HomeScreen
            // (ve üzerindeki Ayarlar Dialog'u) aniden kaybolup kullanıcıyı
            // görev listesine fırlatırdı: bu gerçekten denenip görülen bir
            // hataydı. Senkron okuma sayesinde değişiklik ancak BİR SONRAKİ
            // soğuk başlangıçta etkili oluyor, tıpkı dil değişikliğinin
            // recreate() istemesi gibi.
            var showHomeScreen by remember {
                mutableStateOf(!openAddTaskOnLaunch && application.currentShowHomeScreenPreference())
            }

            IvirTheme(darkTheme = darkTheme, colorTheme = colorTheme, fontScale = fontScalePreference) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Kullanıcı isteğiyle (2026-08-07) sıra değişti: kilit
                    // AÇIKSA giriş ekranından bile ÖNCE soruluyor: yani
                    // "Uygulamaya Gir" butonuna basmadan, uygulamayı her
                    // açtığında/arka plandan döndüğünde önce PIN isteniyor.
                    // Giriş ekranı, kilit açıldıktan SONRA (ya da kilit hiç
                    // yoksa direkt) gösteriliyor.
                    if (isLocked) {
                        // Kilitliyken ne giriş ekranını ne TaskListScreen'i
                        // oluşturmuyoruz: sadece görsel olarak gizli değil,
                        // arkada hiç çalışmıyor, veriler ekrana hiç gelmiyor.
                        AppLockScreen(
                            viewModel = appLockViewModel,
                            onUnlocked = {},
                        )
                    } else if (showHomeScreen) {
                        HomeScreen(onEnterApp = { showHomeScreen = false })
                    } else {
                        // Bildirim gösterebilmemiz için gereken tek runtime izni
                        // burada, uygulama ilk açıldığında bir kere isteniyor.
                        RequestNotificationPermissionIfNeeded()
                        TaskListScreen(openAddTaskOnLaunch = openAddTaskOnLaunch, initialTaskTitle = sharedTaskTitle)
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_OPEN_ADD_TASK = "extra_open_add_task"
        const val ACTION_ADD_TASK = "com.yusufemre.ivirbyyuinn.action.ADD_TASK"
        private const val MAX_SHARED_TITLE_LENGTH = 200
    }
}

// Configuration'ı KOPYALAYIP (orijinalini bozmadan) üzerine yeni Locale'i
// yazıp createConfigurationContext ile SADECE bu Context için geçerli
// yeni bir Context üretiyoruz: sistem genelini değil, sadece bizim
// Activity'mizin gördüğü kaynakları (strings.xml seçimini) etkiliyor.
// Locale.setDefault de çağırıyoruz ki SimpleDateFormat gibi
// Locale.getDefault() kullanan yerler de (bkz. TaskRow, CalendarScreen,
// WeeklyBarChart) tutarlı davransın.
private fun Context.withLocale(locale: Locale): Context {
    Locale.setDefault(locale)
    val configuration = Configuration(resources.configuration)
    configuration.setLocale(locale)
    return createConfigurationContext(configuration)
}

// Android 13 (API 33) öncesinde bildirim göstermek için runtime izin
// gerekmiyordu, kullanıcı sadece ayarlardan kapatabiliyordu. 13'ten
// itibaren POST_NOTIFICATIONS artık diğer tehlikeli izinler gibi (kamera,
// konum...) elle onaylanması gereken bir izin, bu yüzden SDK_INT
// kontrolü şart, eski cihazlarda bu fonksiyon hiçbir şey yapmıyor.
@Composable
private fun RequestNotificationPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}, // Kullanıcı reddederse de uygulama çalışmaya devam ediyor,
        // sadece hatırlatma bildirimleri görünmüyor, bunu zorlamıyoruz.
    )

    LaunchedEffect(Unit) {
        val alreadyGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

        if (!alreadyGranted) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
