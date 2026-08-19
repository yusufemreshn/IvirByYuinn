package com.yusufemre.ivirbyyuinn.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Task.isDone sadece "şu an tamamlanmış mı" bilgisini tutuyor, NE ZAMAN
// tamamlandığını değil: tekrarlayan görevlerde de aynı satır tekrar
// tekrar kullanıldığı için tek bir "completedAt" alanı geçmişi kaybederdi.
// Bu tablo, her tamamlanma anını ayrı bir satır olarak biriktiren küçük
// bir "olay günlüğü" (event log), istatistik grafiği ve streak hesabı
// buradan besleniyor.
@Entity(
    tableName = "task_completions",
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
data class TaskCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val taskId: Long,
    val completedAt: Long,
)
