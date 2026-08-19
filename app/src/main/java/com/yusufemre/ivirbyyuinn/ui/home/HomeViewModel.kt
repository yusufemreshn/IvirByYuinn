package com.yusufemre.ivirbyyuinn.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufemre.ivirbyyuinn.domain.GetTaskListsUseCase
import com.yusufemre.ivirbyyuinn.domain.GetTasksUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// Faz 10b: HomeScreen'e "N görev seni bekliyor" gibi canlı bir özet
// eklemek için, tam TaskViewModel'i kurmadan (o, seçili liste/arama/
// filtre/sıralama gibi HomeScreen'in hiç ihtiyacı olmayan bir sürü şey
// taşıyor) küçük, kendi işine odaklı bir ViewModel; ThemeViewModel'le
// aynı "her ekranın kendi kapsamına uygun boyutta bir ViewModel'i olsun"
// deseni.
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    getTaskListsUseCase: GetTaskListsUseCase,
    getTasksUseCase: GetTasksUseCase,
) : ViewModel() {

    // TaskViewModel'in init bloğu "kullanıcı elle seçmediyse ilk listeyi
    // otomatik seç" diyor, burada da aynı varsayılan listeye (kullanıcı
    // "Uygulamaya Gir"e bastığında zaten AYNI listeyi göreceği için) bakıp
    // tamamlanmamış görev sayısını sayıyoruz. Liste henüz yüklenmediyse
    // (ilk milisaniyeler) ya da hiç liste yoksa null, HomeScreen bu
    // durumda özet satırını hiç göstermiyor.
    val activeTaskCount: StateFlow<Int?> = getTaskListsUseCase()
        .map { it.firstOrNull()?.id }
        .filterNotNull()
        .flatMapLatest { taskListId -> getTasksUseCase(taskListId) }
        .map { tasks -> tasks.count { !it.isDone } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
