package com.yusufemre.ivirbyyuinn.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubtaskDao {

    @Insert
    suspend fun insert(subtask: Subtask)

    @Update
    suspend fun update(subtask: Subtask)

    @Delete
    suspend fun delete(subtask: Subtask)

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY id ASC")
    fun getSubtasksForTask(taskId: Long): Flow<List<Subtask>>

    // TaskRow'da "2/5 tamamlandı" gibi bir sayaç gösterebilmek için tüm alt
    // görevleri tek seferde çekip taskId'ye göre gruplamayı ViewModel'e
    // bırakıyoruz: her satır için ayrı ayrı sorgu atmaktansa (N+1 sorun)
    // tek sorguyla hepsini alıp bellekte gruplamak bu ölçekte çok daha basit.
    @Query("SELECT * FROM subtasks")
    fun getAllSubtasks(): Flow<List<Subtask>>

    // Faz 6 (yedekleme): bkz. TaskListDao'daki aynı isimli fonksiyonlar.
    @Query("DELETE FROM subtasks")
    suspend fun clearAll()

    @Insert
    suspend fun insertAll(subtasks: List<Subtask>)
}
