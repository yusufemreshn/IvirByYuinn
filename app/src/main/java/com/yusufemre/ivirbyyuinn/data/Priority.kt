package com.yusufemre.ivirbyyuinn.data

// level'ı bilerek elle veriyoruz (enum'un sırasına -ordinal'ına- güvenmek
// yerine). Çünkü ileride araya yeni bir öncelik eklersek ya da sırayı
// değiştirirsek, ordinal kayar ve veritabanındaki eski kayıtlar yanlış
// önceliğe dönüşür; level burada sabit kalıyor, tabloya hep bu yazılıyor.
enum class Priority(val level: Int) {
    LOW(0),
    MEDIUM(1),
    HIGH(2),
}
