package com.yusufemre.ivirbyyuinn.ui.lock

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufemre.ivirbyyuinn.domain.DisableAppLockUseCase
import com.yusufemre.ivirbyyuinn.domain.IsAppLockEnabledUseCase
import com.yusufemre.ivirbyyuinn.domain.SetAppLockPinUseCase
import com.yusufemre.ivirbyyuinn.domain.VerifyAppLockPinUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

// ProcessLifecycleOwner, TEK BİR Activity'nin değil, TÜM UYGULAMA
// SÜRECİNİN (process) ön planda mı arka planda mı olduğunu izliyor.
// Kullanıcı görev değiştirici (recent apps) ile başka bir uygulamaya
// geçtiğinde, ya da ekranı kilitlediğinde `onStop` tetikleniyor. Bunu
// dinleyerek "uygulama arka plana her düştüğünde tekrar kilitle" kuralını
// uyguluyoruz, sadece uygulama ilk açıldığında değil, HER ön plana
// gelişte. DefaultLifecycleObserver'ın .NET'te doğrudan bir karşılığı yok;
// en yakın benzetme, bir WinForms/WPF uygulamasının `Application.
// Deactivated`/`Activated` olaylarını dinlemek gibi düşünülebilir.
class AppLockViewModel(
    isAppLockEnabledUseCase: IsAppLockEnabledUseCase,
    private val verifyAppLockPinUseCase: VerifyAppLockPinUseCase,
    private val setAppLockPinUseCase: SetAppLockPinUseCase,
    private val disableAppLockUseCase: DisableAppLockUseCase,
) : ViewModel(), DefaultLifecycleObserver {

    val isLockEnabled: StateFlow<Boolean> = isAppLockEnabledUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // Kilit açıksa bile, kullanıcı bu oturumda (uygulama en son ön plana
    // geldiğinden beri) doğru PIN'i/biyometrikle zaten girdiyse tekrar
    // sormuyoruz, her ekran değişiminde değil, sadece "arka plandan
    // dönüşte" kilitli görünmesi gerekiyor.
    private val _isUnlockedForThisSession = MutableStateFlow(false)
    val isUnlockedForThisSession: StateFlow<Boolean> = _isUnlockedForThisSession.asStateFlow()

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        _isUnlockedForThisSession.value = false
    }

    override fun onCleared() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        super.onCleared()
    }

    fun verifyPin(pin: String): Boolean {
        val correct = verifyAppLockPinUseCase(pin)
        if (correct) _isUnlockedForThisSession.value = true
        return correct
    }

    fun unlockWithBiometric() {
        _isUnlockedForThisSession.value = true
    }

    // Ayarlar ekranından yeni bir PIN belirlenirken çağrılıyor. Yeni PIN
    // belirlemek doğal olarak "kilidi zaten açmış" demek: kullanıcıyı
    // hemen ardından tekrar PIN sormuyoruz.
    fun setNewPin(pin: String): Boolean {
        val success = setAppLockPinUseCase(pin)
        if (success) _isUnlockedForThisSession.value = true
        return success
    }

    // Kilidi kapatmak için mevcut PIN'i doğrulamak gerekiyor: bkz.
    // DisableAppLockUseCase'in yorumu.
    fun disableLock(currentPin: String): Boolean = disableAppLockUseCase(currentPin)
}
