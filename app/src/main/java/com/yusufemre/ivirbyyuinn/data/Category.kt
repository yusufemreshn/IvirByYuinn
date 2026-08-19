package com.yusufemre.ivirbyyuinn.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// colorHex'i "#E8735C" gibi hazır bir metin olarak saklıyoruz, Compose'un
// Color(...) fonksiyonu bu formatı doğrudan anlıyor, ayrıca bir renk
// paketi/kütüphanesi gerekmiyor.
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val colorHex: String,
)
