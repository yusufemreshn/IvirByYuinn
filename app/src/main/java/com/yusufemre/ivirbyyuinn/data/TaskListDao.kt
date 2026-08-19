package com.yusufemre.ivirbyyuinn.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskListDao {

    // insert'ün Long dönmesini istedik: yeni bir liste eklendiğinde,
    // kullanıcıyı otomatik olarak o listeye geçirebilmek için yeni
    // satırın id'sine ihtiyacımız var.
    @Insert
    suspend fun insert(taskList: TaskListEntity): Long

    // Task.taskListId'nin foreign key'i CASCADE ile kurulu (bkz. Task.kt),
    // bir liste silinince İÇİNDEKİ TÜM GÖREVLER de (çöp kutusundakiler
    // dahil) veritabanından KALICI olarak siliniyor, soft-delete/undo YOK.
    // Bu yüzden domain/DeleteTaskListUseCase, silmeden önce görevlerin
    // hatırlatma alarmlarını iptal ediyor ve kullanıcı arayüzü net bir
    // uyarı gösteriyor.
    @Delete
    suspend fun delete(taskList: TaskListEntity)

    // Faz 12b: arşivlenmiş listeler normal drawer'da hiç görünmüyor,
    // sortOrder'a göre sıralanıyor (id sırası değil, kullanıcı elle
    // yeniden sıralayabiliyor).
    @Query("SELECT * FROM task_lists WHERE isArchived = 0 ORDER BY sortOrder ASC")
    fun getAllTaskLists(): Flow<List<TaskListEntity>>

    @Query("SELECT * FROM task_lists WHERE isArchived = 1 ORDER BY sortOrder ASC")
    fun getArchivedTaskLists(): Flow<List<TaskListEntity>>

    // Yeni bir liste eklerken TaskListEntity'nin Kotlin varsayılanı
    // (sortOrder = 0) kullanılırsa, "Genel" gibi zaten sortOrder=0 olan
    // bir listeyle eşit değere düşer: ORDER BY eşit değerleri id sırasına
    // göre keyfi çözer, "Yukarı/Aşağı taşı" da eşit iki değeri takas edip
    // hiçbir şeyi değiştirmemiş gibi görünürdü. Yeni listeyi her zaman
    // MEVCUT en büyük sortOrder'ın bir fazlasına, yani listenin EN SONUNA
    // koyuyoruz.
    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM task_lists")
    suspend fun getNextSortOrder(): Int

    // TaskDao.updateAll() ile AYNI amaç: sıralama değiştiğinde etkilenen
    // listelerin hepsini tek bir transaction'da kaydetmek.
    @Update
    suspend fun updateAll(taskLists: List<TaskListEntity>)

    // Tek bir listenin arşiv durumunu değiştirmek için.
    @Update
    suspend fun update(taskList: TaskListEntity)

    // Faz 6 (yedekleme): tabloyu boşalt, sonra id'leri korunarak geri
    // yazılacak liste kayıtlarını ekle. Bkz. TaskDao.clearAll()/insertAll()
    // için yazdığımız yorum, aynı mantık burada da geçerli.
    @Query("DELETE FROM task_lists")
    suspend fun clearAll()

    @Insert
    suspend fun insertAll(taskLists: List<TaskListEntity>)
}
