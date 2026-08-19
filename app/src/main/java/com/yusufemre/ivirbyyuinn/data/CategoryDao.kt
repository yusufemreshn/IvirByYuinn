package com.yusufemre.ivirbyyuinn.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert
    suspend fun insert(category: Category): Long

    // Task.categoryId'nin foreign key'i SET_NULL ile kurulu (bkz. Task.kt),
    // bir kategori silinince onu kullanan görevler silinmiyor, sadece
    // kategorisiz kalıyor. Bunu biz elle yapmıyoruz, SQLite kendisi hallediyor.
    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    // Faz 6 (yedekleme), bkz. TaskListDao'daki aynı isimli fonksiyonlar.
    @Query("DELETE FROM categories")
    suspend fun clearAll()

    @Insert
    suspend fun insertAll(categories: List<Category>)
}
