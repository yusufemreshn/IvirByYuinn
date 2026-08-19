package com.yusufemre.ivirbyyuinn.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Faz 12a: sık tekrar eden görevler için ("Süt al" gibi) tek dokunuşluk
// şablonlar. Task'tan TÜRETMEDİK çünkü bir şablonun kendi dueAt'ı/isDone'ı/
// taskListId'si olmaz: her kullanıldığında YENİ bir Task'a "tohum" olur.
// categoryId'ye bilerek foreign key KOYMADIK: kategori silme özelliği zaten
// yok (bkz. Faz 9 notu, "silme/düzenleme
// kapsam dışı"), yani sarkan bir categoryId pratikte hiç oluşamaz; FK
// eklemek MIGRATION_6_7'yi (tabloyu yeniden kurmak gerekirdi) gereksiz yere
// karmaşıklaştırırdı.
@Entity(tableName = "task_templates")
data class TaskTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val priority: Priority = Priority.MEDIUM,
    val categoryId: Long? = null,
    val note: String? = null,
)
