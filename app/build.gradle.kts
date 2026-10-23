plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    // namespace: Kotlin/Java tarafında R sınıfının ve kodun hangi paket altında
    // üretileceğini belirler. applicationId ise Play Store'daki (ya da telefondaki)
    // gerçek kimliğimiz, ikisi aynı olmak zorunda değil ama basitlik için aynı tuttuk.
    namespace = "com.yusufemre.ivirbyyuinn"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.yusufemre.ivirbyyuinn"
        minSdk = 26
        targetSdk = 37
        versionCode = 2
        // Ayarlar > Hakkında bölümündeki sürüm numarasının TEK kaynağı
        // burası, PackageManager.getPackageInfo() üzerinden orada
        // otomatik okunuyor (bkz. SettingsScreen.kt AboutSection). Yeni
        // bir derleme alıp "ıvırVX.Y.Z.apk" olarak isimlendirdiğimde, bu
        // satırı "X.Y.Z" ile güncellemem yeterli, başka hiçbir yeri
        // elle değiştirmeme gerek yok.
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Şimdilik küçültme/obfuscation kapalı; ileride release almaya
            // yaklaşınca bunu açıp proguard-rules.pro dosyasını dolduracağız.
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Play Store'a gitmiyoruz, kendi telefonumuza kuruyoruz, imzasız
            // APK Android tarafından "bozuk dosya" diye reddediliyor, o yüzden
            // debug anahtarıyla imzalıyoruz ki kurulabilsin.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        // Kotlin desteği artık AGP'nin kendi içinde geldiği için jvmTarget
        // ayrı bir "kotlinOptions" bloğuna değil, buradaki Java uyumluluk
        // ayarına bakıyor.
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    // Google Drive istemcisinin (google-api-client-android üzerinden gelen
    // google-auth-library) birkaç bağımlılığı AYNI META-INF dosya adını
    // (ör. INDEX.LIST) taşıyor, APK paketleme bu yüzden hangisini
    // kullanacağını bilemeyip hata veriyordu. Bu dosyalar çalışma zamanında
    // hiç kullanılmıyor (sadece derleme zamanı meta verisi), güvenle
    // dışlıyoruz.
    packaging {
        resources {
            excludes += setOf(
                "META-INF/INDEX.LIST",
                "META-INF/DEPENDENCIES",
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // viewModelScope için viewmodel-ktx, Compose'ta viewModel()/State
    // toplama yardımcıları için de compose eklentileri lazım.
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Compose BOM (Bill of Materials): tek bir versiyon buradan belirleniyor,
    // altındaki compose kütüphanelerinin hepsi bu BOM'la uyumlu versiyonu
    // otomatik seçiyor, tek tek versiyon numarası uğraşmamıza gerek kalmıyor.
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // Silme/ekleme gibi standart ikonlar için Google'ın kendi Material
    // ikon setini kullanıyoruz, bu Jetpack'in resmi bir parçası, hazır
    // bir 3. parti ikon paketi değil.
    implementation(libs.androidx.material.icons.core)

    // Room: kalıcı veri katmanımız. room-compiler normal implementation değil,
    // ksp(...) ile ekleniyor çünkü derleme zamanında DAO implementasyonlarını
    // KSP üretiyor, uygulamanın kendisinde çalışan bir kod değil bu.
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Ana ekran widget'ı için Jetpack Glance, RemoteViews'in modern,
    // Compose tabanlı yerine geçeni, resmi Jetpack parçası.
    implementation(libs.androidx.glance.appwidget)

    // Faz 7a: uygulama kilidi. biometric, resmi Jetpack parmak izi/yüz
    // tanıma API'si, BiometricPrompt bir FragmentActivity istediği için
    // fragment-ktx de gerekiyor (MainActivity artık FragmentActivity'den
    // türüyor). lifecycle-process, uygulama arka plana düşünce/geri
    // gelince haberdar olabilmemiz için (ProcessLifecycleOwner).
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.process)

    // Faz 12b: haftalık özet/yeniden-etkileşim bildirimi için periyodik
    // arka plan işi. Resmi Jetpack, AlarmManager (ReminderScheduler'ın
    // kullandığı) zaman-kritik/tam saatli işler için uygun, WorkManager
    // ise "yaklaşık bir zamanda, cihaz uygunken çalışsın yeter" türü
    // periyodik işler için (burada tam olarak bu). 2.11'den itibaren
    // CoroutineWorker work-runtime-ktx'te değil, doğrudan bu pakette.
    implementation(libs.androidx.work.runtime)

    // Google Drive yedekleme: hesap girişi (play-services-auth) + Drive v3
    // REST istemcisi (google-api-client-android, httpclient'sız Android
    // varyantı; google-api-services-drive, Drive'ın model sınıfları;
    // google-http-client-gson, JSON (de)serileştirme).
    implementation(libs.play.services.auth)
    implementation(libs.google.api.client.android) {
        exclude(group = "org.apache.httpcomponents")
    }
    implementation(libs.google.api.services.drive) {
        exclude(group = "org.apache.httpcomponents")
    }
    implementation(libs.google.http.client.gson)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
