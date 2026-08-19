package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.AppLockRepository

// AddTaskUseCase'deki "boş başlık kabul etme" kuralına benzer bir iş
// kuralı burada da var: PIN tam 4 haneli, sadece rakam olmalı. Sabit
// uzunluk bilerek seçildi (bankacılık uygulamaları/Signal gibi çoğu PIN
// kilidinin yaptığı gibi), 4. hane girilir girilmez otomatik doğrulama
// yapılabiliyor, "bitirdim" demek için ayrı bir onay butonuna gerek
// kalmıyor. Bu kural ekrana değil domain katmanına yazıldı, nereden
// çağrılırsa çağrılsın hep aynı şekilde uygulansın diye.
class SetAppLockPinUseCase(private val repository: AppLockRepository) {
    operator fun invoke(pin: String): Boolean {
        if (pin.length != PIN_LENGTH || !pin.all { it.isDigit() }) return false
        repository.setPin(pin)
        return true
    }

    companion object {
        const val PIN_LENGTH = 4
    }
}
