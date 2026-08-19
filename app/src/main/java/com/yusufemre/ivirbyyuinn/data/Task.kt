package com.yusufemre.ivirbyyuinn.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Room bu data class'ı görünce arkada otomatik olarak bir "tasks" tablosu
// kurar; alanlarımız da o tablonun sütunları oluyor. .NET tarafındaki EF
// Core entity sınıflarına birebir karşılık geliyor.
//
// Faz 2'de iki foreign key eklendi: taskListId ve categoryId. Liste
// silinince (CASCADE) o listedeki görevlerin de gitmesi mantıklı, ama
// kategori silinince (SET_NULL) görevin kendisi kalsın, sadece
// kategorisiz kalsın istiyoruz. İkisi farklı davranışlar bu yüzden.
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskListEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskListId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("taskListId"), Index("categoryId")],
)
data class Task(
    // autoGenerate = true dediğimiz için id'yi biz vermiyoruz, SQLite kendi
    // artırıyor: yeni bir görev eklerken 0 gönderiyoruz, Room gerçek id'yi
    // kendisi atıyor.
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    // Varsayılan olarak henüz tamamlanmamış bir görev ekliyoruz.
    val isDone: Boolean = false,
    // Listeyi en yeni görev üstte olacak şekilde sıralayabilmek için
    // oluşturulma zamanını epoch milisaniye cinsinden saklıyoruz.
    val createdAt: Long = System.currentTimeMillis(),
    // Her görev mutlaka bir listeye ait (nullable değil), kullanıcı hiç
    // liste oluşturmasa bile migration sırasında otomatik açtığımız
    // "Genel" listesine düşüyor.
    val taskListId: Long,
    // Kategori opsiyonel: bir göreve kategori atamak zorunda değilsin.
    val categoryId: Long? = null,
    val priority: Priority = Priority.MEDIUM,
    // Son tarih/saat de opsiyonel, epoch milisaniye cinsinden.
    val dueAt: Long? = null,
    val note: String? = null,
    // Faz 3'te eklendi: bu görev tekrarlıysa (NONE değilse), tamamlandı
    // işaretlendiğinde satır kapanmıyor: dueAt bir sonraki tarihe
    // ilerliyor (bkz. domain/ToggleTaskDoneUseCase).
    val recurrence: Recurrence = Recurrence.NONE,
    // Faz 4a'da eklendi: "silme" artık satırı gerçekten silmiyor, bunu
    // true yapıyor, çöp kutusu bu sayede mümkün oluyor. Gerçek/kalıcı
    // silme sadece çöp kutusu ekranından, ayrı bir DAO fonksiyonuyla
    // yapılıyor (bkz. domain/PermanentlyDeleteTaskUseCase).
    val isDeleted: Boolean = false,
    // Faz 4c'de eklendi: SortOption.MANUAL seçiliyken kullanıcının elle
    // sürükle-bırakla verdiği sırayı tutuyor. Küçük değer üstte demek.
    val manualOrder: Int = 0,
    // 2026-08-11'de eklendi: görev arşivi. true olunca normal listeden
    // kaybolur (getTasksForList artık isArchived=0 şartı da arıyor),
    // Arşiv ekranında görünür: isDeleted'in çöp kutusu için yaptığının
    // birebir aynısı, ayrı bir "durum" kavramı.
    val isArchived: Boolean = false,
    // Tamamlanma anı, isDone=true olduğu anda (bkz. ToggleTaskDoneUseCase)
    // yazılıyor; arşivlendiği an DEĞİL, çünkü Takvim'in "bu görev hangi
    // günde tamamlandı" sorusuna doğru cevap verebilmesi için gerçek
    // tamamlanma anı lazım, arşivleme çok sonra (elle ya da 24 saat
    // sonra otomatik) olabiliyor. Geri alınırsa (isDone false'a dönerse)
    // null'a sıfırlanıyor.
    val completedAt: Long? = null,
)
