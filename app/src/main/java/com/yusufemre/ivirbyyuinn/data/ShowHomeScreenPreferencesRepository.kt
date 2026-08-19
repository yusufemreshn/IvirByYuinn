package com.yusufemre.ivirbyyuinn.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// ThemePreferencesRepository/FontScalePreferencesRepository ile birebir
// aynı desen (bkz. oradaki yorumlar). Tek fark: enum yerine düz bir
// Boolean, "uygulama ilk açıldığında giriş ekranı (Enter App/Ayarlar/
// Çıkış butonları) gösterilsin mi" tercihi. Varsayılan AÇIK (true),
// kullanıcı açıkça kapatmadıkça eski davranış aynen sürüyor.
class ShowHomeScreenPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getShowHomeScreenPreference(): Flow<Boolean> = callbackFlow {
        trySend(readCurrentValue())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_SHOW_HOME_SCREEN) {
                trySend(readCurrentValue())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun setShowHomeScreenPreference(show: Boolean) {
        prefs.edit { putBoolean(KEY_SHOW_HOME_SCREEN, show) }
    }

    // MainActivity'nin "bu process'te giriş ekranını göster mi" kararı
    // sadece İLK composition'da bir kere veriliyor (openAddTaskOnLaunch
    // gibi düz bir val, StateFlow'la CANLI takip edilen bir şey değil),
    // bkz. LanguagePreferencesRepository.getLanguagePreferenceNow ile aynı
    // gerekçe. Bilerek Flow/StateFlow YOLUNU kullanmıyoruz: kullanıcı
    // Ayarlar'ı giriş ekranının ÜSTÜNDE açıp anahtarı kapatırsa, canlı bir
    // StateFlow o anda showHomeScreen'i false yapıp kullanıcıyı görev
    // listesine fırlatırdı: istenmeyen bir yan etki. Senkron okuma bunu
    // engelliyor, değişiklik ancak BİR SONRAKİ soğuk başlangıçta etkili olur.
    fun getShowHomeScreenPreferenceNow(): Boolean = readCurrentValue()

    private fun readCurrentValue(): Boolean = prefs.getBoolean(KEY_SHOW_HOME_SCREEN, true)

    private companion object {
        const val PREFS_NAME = "ivir_prefs"
        const val KEY_SHOW_HOME_SCREEN = "show_home_screen_preference"
    }
}
