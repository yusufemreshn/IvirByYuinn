package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Category
import com.yusufemre.ivirbyyuinn.data.CategoryRepository

// Bir kategoriyi silmek onu kullanan görevleri SİLMİYOR: foreign key
// SET_NULL sayesinde (bkz. data/Task.kt) o görevler sadece kategorisiz
// kalıyor. Ayrı bir onay/uyarı ekranı UI tarafında (CategoryManagementScreen).
class DeleteCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(category: Category) = repository.deleteCategory(category)
}
