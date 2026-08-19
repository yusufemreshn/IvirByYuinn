package com.yusufemre.ivirbyyuinn.data

import androidx.room.TypeConverter

// Room, enum'ları tabloya doğrudan yazamıyor. SQLite'ın anladığı tek şey
// sayı/metin gibi temel tipler. Bu yüzden Priority'yi saklarken Int'e,
// okurken tekrar Priority'ye çeviren küçük bir "tercüman" yazıyoruz.
// .NET'te EF Core'a bir enum için ValueConverter tanımlamaya çok benziyor.
class Converters {
    @TypeConverter
    fun fromPriority(priority: Priority): Int = priority.level

    @TypeConverter
    fun toPriority(value: Int): Priority = Priority.entries.first { it.level == value }

    @TypeConverter
    fun fromRecurrence(recurrence: Recurrence): Int = recurrence.level

    @TypeConverter
    fun toRecurrence(value: Int): Recurrence = Recurrence.entries.first { it.level == value }
}
