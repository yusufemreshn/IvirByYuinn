package com.yusufemre.ivirbyyuinn.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Task.note (data/Task.kt) tek bir göreve bağlı bir not alanı; bu entity
// ONDAN TAMAMEN BAĞIMSIZ, hiçbir göreve/listeye bağlı olmayan, kullanıcının
// drawer'daki "Notlar" girdisinden ulaştığı serbest metin not defteri.
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
