package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Priority
import com.yusufemre.ivirbyyuinn.data.Task
import org.junit.Assert.assertEquals
import org.junit.Test

// FilterAndSortTasksUseCase repository'ye hiç bağımlı olmayan saf bir
// fonksiyon (bkz. sınıfın kendi yorumu), bu yüzden gerçek/fake bir Room
// kurmadan, doğrudan çağırarak test edilebiliyor.
class FilterAndSortTasksUseCaseTest {

    private val useCase = FilterAndSortTasksUseCase()

    private fun task(
        id: Long,
        title: String,
        categoryId: Long? = null,
        priority: Priority = Priority.MEDIUM,
        createdAt: Long = id,
        manualOrder: Int = id.toInt(),
    ) = Task(
        id = id,
        title = title,
        taskListId = 1L,
        categoryId = categoryId,
        priority = priority,
        createdAt = createdAt,
        manualOrder = manualOrder,
    )

    @Test
    fun `searchQuery bos ise tum gorevleri dondurur`() {
        val tasks = listOf(task(1, "Ekmek al"), task(2, "Ders çalış"))

        val result = useCase(tasks, searchQuery = "", categoryFilter = null, sortOption = SortOption.DATE_NEWEST_FIRST)

        assertEquals(2, result.size)
    }

    @Test
    fun `searchQuery baslikta buyuk kucuk harf duyarsiz eslesiyor`() {
        val tasks = listOf(task(1, "Ekmek Al"), task(2, "Ders çalış"))

        val result = useCase(tasks, searchQuery = "ekmek", categoryFilter = null, sortOption = SortOption.DATE_NEWEST_FIRST)

        assertEquals(listOf("Ekmek Al"), result.map { it.title })
    }

    @Test
    fun `categoryFilter sadece o kategorideki gorevleri birakir`() {
        val tasks = listOf(
            task(1, "A", categoryId = 10L),
            task(2, "B", categoryId = 20L),
            task(3, "C", categoryId = null),
        )

        val result = useCase(tasks, searchQuery = "", categoryFilter = 10L, sortOption = SortOption.DATE_NEWEST_FIRST)

        assertEquals(listOf(1L), result.map { it.id })
    }

    @Test
    fun `arama ve kategori filtresi birlikte uygulanir`() {
        val tasks = listOf(
            task(1, "Ekmek al", categoryId = 10L),
            task(2, "Ekmek sipariş et", categoryId = 20L),
            task(3, "Süt al", categoryId = 10L),
        )

        val result = useCase(tasks, searchQuery = "ekmek", categoryFilter = 10L, sortOption = SortOption.DATE_NEWEST_FIRST)

        assertEquals(listOf(1L), result.map { it.id })
    }

    @Test
    fun `DATE_NEWEST_FIRST en son olusturulani en basa koyar`() {
        val tasks = listOf(task(1, "Eski", createdAt = 100L), task(2, "Yeni", createdAt = 200L))

        val result = useCase(tasks, searchQuery = "", categoryFilter = null, sortOption = SortOption.DATE_NEWEST_FIRST)

        assertEquals(listOf("Yeni", "Eski"), result.map { it.title })
    }

    @Test
    fun `PRIORITY_HIGH_FIRST yuksek oncelikliyi en basa koyar`() {
        val tasks = listOf(
            task(1, "Düşük", priority = Priority.LOW),
            task(2, "Yüksek", priority = Priority.HIGH),
            task(3, "Orta", priority = Priority.MEDIUM),
        )

        val result = useCase(tasks, searchQuery = "", categoryFilter = null, sortOption = SortOption.PRIORITY_HIGH_FIRST)

        assertEquals(listOf("Yüksek", "Orta", "Düşük"), result.map { it.title })
    }

    @Test
    fun `ALPHABETICAL buyuk kucuk harf duyarsiz alfabetik siralar`() {
        val tasks = listOf(task(1, "çilek"), task(2, "Armut"), task(3, "Badem"))

        val result = useCase(tasks, searchQuery = "", categoryFilter = null, sortOption = SortOption.ALPHABETICAL)

        assertEquals(listOf("Armut", "Badem", "çilek"), result.map { it.title })
    }

    @Test
    fun `MANUAL kullanicinin surukle birak sirasini korur`() {
        val tasks = listOf(
            task(1, "Üçüncü", manualOrder = 3),
            task(2, "Birinci", manualOrder = 1),
            task(3, "İkinci", manualOrder = 2),
        )

        val result = useCase(tasks, searchQuery = "", categoryFilter = null, sortOption = SortOption.MANUAL)

        assertEquals(listOf("Birinci", "İkinci", "Üçüncü"), result.map { it.title })
    }
}
