package com.yusufemre.ivirbyyuinn.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufemre.ivirbyyuinn.data.LanguagePreference
import com.yusufemre.ivirbyyuinn.domain.GetLanguagePreferenceUseCase
import com.yusufemre.ivirbyyuinn.domain.SetLanguagePreferenceUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// ThemeViewModel'in birebir aynı deseni, bkz. oradaki yorumlar. Dil
// tercihini DEĞİŞTİRMEK burada, ama tercihin gerçekten UYGULANMASI
// (Activity'nin Locale'ini değiştirmesi) MainActivity.attachBaseContext'te
// oluyor, o yüzden SettingsScreen, kullanıcı yeni bir dil seçtiğinde
// setLanguagePreference'tan SONRA activity.recreate() çağırmak zorunda.
class LanguageViewModel(
    getLanguagePreferenceUseCase: GetLanguagePreferenceUseCase,
    private val setLanguagePreferenceUseCase: SetLanguagePreferenceUseCase,
) : ViewModel() {

    val languagePreference: StateFlow<LanguagePreference> = getLanguagePreferenceUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LanguagePreference.SYSTEM)

    fun setLanguagePreference(preference: LanguagePreference) {
        setLanguagePreferenceUseCase(preference)
    }
}
