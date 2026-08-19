package com.yusufemre.ivirbyyuinn.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Adına bilerek "TaskListEntity" dedik, sade "TaskList" değil. Kotlin'in
// kendi stdlib'inde zaten bir "List" tipi var, karışıklık olmasın istedik.
@Entity(tableName = "task_lists")
data class TaskListEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    // Faz 12b: kullanıcının elle verdiği sıra (küçük değer üstte),
    // Task.manualOrder'la AYNI fikir, sadece listeler için.
    val sortOrder: Int = 0,
    // Faz 12b: arşivlenmiş bir liste normal drawer'da hiç görünmüyor,
    // ayrı bir "Arşivlenmiş listeler" ekranında duruyor: silme DEĞİL,
    // görevleri/geçmişi korunuyor, sadece göz önünden kalkıyor.
    val isArchived: Boolean = false,
)
