package com.yusufemre.ivirbyyuinn.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// onDelete = CASCADE: bir görevi sildiğimizde, ona bağlı alt görevlerin
// ortada "sahipsiz" kalmasını istemiyoruz, onlar da otomatik silinsin.
// index koymamızın sebebi Room'un kendisi: foreign key sütunlarında index
// olmazsa build sırasında uyarı veriyor (sorgular yavaşlar diye).
@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("taskId")],
)
data class Subtask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val taskId: Long,
    val title: String,
    val isDone: Boolean = false,
)
