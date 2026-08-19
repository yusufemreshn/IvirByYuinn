package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Task
import com.yusufemre.ivirbyyuinn.data.TaskRepository

// Kullanıcı sürükle-bırakla listeyi yeniden düzenlediğinde çağrılıyor.
// Parametre olarak "ekranda şu anki yeni sırayla görünen görev listesi"
// alıyoruz, her birine sırayla 0, 1, 2... manualOrder veriyoruz ve tek
// seferde kaydediyoruz. Aradaki boşlukları/karmaşayı düşünmemize gerek
// yok: her sürükleme sonunda listeyi baştan numaralandırmak, kişisel
// bir to-do uygulaması ölçeğinde performans sorunu yaratmayacak kadar
// ucuz bir işlem.
class ReorderTasksUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(reorderedTasks: List<Task>) {
        val renumbered = reorderedTasks.mapIndexed { index, task -> task.copy(manualOrder = index) }
        repository.updateTasks(renumbered)
    }
}
