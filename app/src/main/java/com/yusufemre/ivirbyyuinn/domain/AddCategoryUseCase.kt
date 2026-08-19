package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Category
import com.yusufemre.ivirbyyuinn.data.CategoryRepository

// Long? dönüyor: isim boşsa null döner (reddedildi demek), doluysa yeni
// kategorinin id'sini veriyor, ekran bu id'yi hemen "seçili kategori"
// yapabilsin diye.
class AddCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(name: String, colorHex: String): Long? {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return null
        return repository.addCategory(Category(name = trimmedName, colorHex = colorHex))
    }
}
