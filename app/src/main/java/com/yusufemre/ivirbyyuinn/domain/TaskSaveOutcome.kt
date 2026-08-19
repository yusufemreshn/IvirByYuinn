package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.Task

// AddTaskUseCase ve UpdateTaskUseCase'in ortak sonuç tipi. Eskiden ikisi de
// sadece Task? dönüyordu (null = "reddedildi", sebebi hep boş başlıktı) ama
// artık reddin İKİ farklı sebebi olabiliyor: boş başlık ya da aynı listede
// aynı isimde başka bir görev. ViewModel bu ikisine FARKLI tepki vermek
// zorunda (boşta sessizce editörü kapat, eski davranış; çakışmada editörü
// AÇIK tutup kullanıcıya haber ver): tek bir Task? ile bu ayrım yapılamazdı.
sealed interface TaskSaveOutcome {
    data class Saved(val task: Task) : TaskSaveOutcome
    data object BlankTitle : TaskSaveOutcome
    data object DuplicateTitle : TaskSaveOutcome
}
