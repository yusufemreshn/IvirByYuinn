package com.yusufemre.ivirbyyuinn.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskTemplateDao {

    @Insert
    suspend fun insert(template: TaskTemplateEntity): Long

    @Delete
    suspend fun delete(template: TaskTemplateEntity)

    // Alfabetik: şablon sayısı azken "en son eklenen üstte" gibi bir
    // sıralamanın hiçbir anlamı yok, kullanıcı ismine göre arasın.
    @Query("SELECT * FROM task_templates ORDER BY title ASC")
    fun getAll(): Flow<List<TaskTemplateEntity>>
}
