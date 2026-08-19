package com.yusufemre.ivirbyyuinn.data

// Priority'deki gibi level'ı elle veriyoruz, ordinal'a güvenmiyoruz.
// aynı sebep: ileride araya yeni bir tekrar sıklığı eklersek eski
// kayıtların anlamı kaymasın.
enum class Recurrence(val level: Int) {
    NONE(0),
    DAILY(1),
    WEEKLY(2),
    MONTHLY(3),
}
