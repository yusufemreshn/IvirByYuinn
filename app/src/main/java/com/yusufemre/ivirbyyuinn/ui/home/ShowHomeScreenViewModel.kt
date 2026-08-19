package com.yusufemre.ivirbyyuinn.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufemre.ivirbyyuinn.domain.GetShowHomeScreenPreferenceUseCase
import com.yusufemre.ivirbyyuinn.domain.SetShowHomeScreenPreferenceUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// ThemeViewModel/FontScaleViewModel'in birebir aynı deseni, bkz.
// oradaki yorumlar. MainActivity ile SettingsScreen aynı Activity'yi
// (ViewModelStoreOwner) paylaştığı için ikisi de AYNI örneği görüyor,
// Ayarlar'daki anahtar değişince MainActivity'nin okuduğu değer de
// (bir sonraki soğuk başlangıçta) otomatik güncel oluyor.
class ShowHomeScreenViewModel(
    getShowHomeScreenPreferenceUseCase: GetShowHomeScreenPreferenceUseCase,
    private val setShowHomeScreenPreferenceUseCase: SetShowHomeScreenPreferenceUseCase,
) : ViewModel() {

    val showHomeScreen: StateFlow<Boolean> = getShowHomeScreenPreferenceUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setShowHomeScreen(show: Boolean) {
        setShowHomeScreenPreferenceUseCase(show)
    }
}
