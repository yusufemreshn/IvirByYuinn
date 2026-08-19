package com.yusufemre.ivirbyyuinn.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CategoryRepository(private val categoryDao: CategoryDao) {

    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    // Long dönmesi, ekledikten hemen sonra "yeni kategoriyi seçili yap"
    // gibi bir şey yapmak istersek id'yi elimizde bulundurmamızı sağlıyor.
    suspend fun addCategory(category: Category): Long = categoryDao.insert(category)

    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)

    // Faz 6: yedekleme, bkz. TaskListRepository.getAllTaskListsOnce().
    suspend fun getAllCategoriesOnce(): List<Category> = categoryDao.getAllCategories().first()

    suspend fun clearAll() = categoryDao.clearAll()

    suspend fun insertAll(categories: List<Category>) = categoryDao.insertAll(categories)
}
