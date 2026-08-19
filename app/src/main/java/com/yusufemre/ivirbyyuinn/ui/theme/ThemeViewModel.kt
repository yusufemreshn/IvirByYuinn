package com.yusufemre.ivirbyyuinn.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufemre.ivirbyyuinn.data.ThemePreference
import com.yusufemre.ivirbyyuinn.domain.GetThemePreferenceUseCase
import com.yusufemre.ivirbyyuinn.domain.SetThemePreferenceUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// TaskViewModel'den bilerek ayrı tuttuk: tema tercihi görev listesiyle
// ilgili bir şey değil, tüm uygulamayı kapsayan, bambaşka bir sorumluluk.
// MainActivity ve TaskListScreen aynı Activity içinde olduğu için
// ikisi de bu ViewModel'i istediğinde, Android ikisine de AYNI (paylaşılan)
// örneği veriyor, elle bir "paylaşılan state" mekanizması kurmamıza
// gerek kalmadı.
class ThemeViewModel(
    getThemePreferenceUseCase: GetThemePreferenceUseCase,
    private val setThemePreferenceUseCase: SetThemePreferenceUseCase,
) : ViewModel() {

    val themePreference: StateFlow<ThemePreference> = getThemePreferenceUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemePreference.SYSTEM)

    fun setThemePreference(preference: ThemePreference) {
        setThemePreferenceUseCase(preference)
    }
}
