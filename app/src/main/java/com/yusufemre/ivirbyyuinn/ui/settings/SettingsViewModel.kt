package com.yusufemre.ivirbyyuinn.ui.settings

import androidx.lifecycle.ViewModel
import com.yusufemre.ivirbyyuinn.domain.ExportBackupUseCase
import com.yusufemre.ivirbyyuinn.domain.ImportBackupUseCase

// ThemeViewModel gibi bambaşka bir sorumluluk (uygulama ayarları) taşıdığı
// için TaskViewModel'e eklemek yerine kendi küçük ViewModel'ini açtık.
// Faz 7'de PIN kilidi ve dil seçimi de büyük ihtimalle buraya eklenecek.
//
// Diğer ViewModel'lerdeki fonksiyonların aksine burada viewModelScope.launch
// İÇİNDE değil, doğrudan suspend fonksiyonlar olarak duruyorlar; sebebi,
// SettingsScreen'in bu çağrıları zaten kendi coroutine'i içinde, dosya
// URI'sine yazma/okuma adımlarıyla aynı sırada yapması gerekiyor (önce JSON
// üret, sonra dosyaya yaz, ikisi de aynı akışın parçası). viewModelScope'a
// sarıp "ateşle ve unut" yapsaydık, dosyaya yazma adımını ne zaman
// başlatacağımızı bilemezdik.
class SettingsViewModel(
    private val exportBackupUseCase: ExportBackupUseCase,
    private val importBackupUseCase: ImportBackupUseCase,
) : ViewModel() {

    suspend fun exportBackup(): String = exportBackupUseCase()

    suspend fun importBackup(json: String): Result<Unit> = importBackupUseCase(json)
}
