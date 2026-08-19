package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.BackupRepository
import org.json.JSONException

// Diğer UseCase'lerden farklı olarak burada gerçek bir hata ihtimali var:
// kullanıcı elimize hiç ilgisiz bir dosya (ya da bozulmuş bir yedek)
// verebilir. org.json bunun için JSONException fırlatıyor, biz onu
// burada yakalayıp Result'a çeviriyoruz ki SettingsViewModel/Screen çökme
// yerine "bu geçerli bir yedek dosyası değil" gibi bir mesaj gösterebilsin.
class ImportBackupUseCase(private val backupRepository: BackupRepository) {
    suspend operator fun invoke(json: String): Result<Unit> {
        return try {
            backupRepository.importFromJson(json)
            Result.success(Unit)
        } catch (e: JSONException) {
            Result.failure(e)
        }
    }
}
