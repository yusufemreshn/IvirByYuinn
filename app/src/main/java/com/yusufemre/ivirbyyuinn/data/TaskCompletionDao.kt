package com.yusufemre.ivirbyyuinn.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskCompletionDao {

    @Insert
    suspend fun insert(completion: TaskCompletionEntity)

    // task_completions kendi başına hangi listeye ait olduğunu bilmiyor
    // (sadece taskId tutuyor), o yüzden tasks tablosuyla JOIN yapıp
    // "bu tamamlanma, şu an seçili olan listenin bir görevine mi ait"
    // sorusunu burada cevaplıyoruz. İstatistik ve streak hesapları için
    // tüm geçmişi (sınırsız) döndürüyoruz, pencereleme (son 7 gün, vb.)
    // ilgili UseCase'lerde bellekte yapılıyor. Bu ölçekteki bir kişisel
    // uygulama için tek satırlık completedAt değerlerini çekmek ucuz.
    @Query(
        """
        SELECT tc.completedAt FROM task_completions tc
        INNER JOIN tasks t ON t.id = tc.taskId
        WHERE t.taskListId = :taskListId
        ORDER BY tc.completedAt ASC
        """,
    )
    fun getCompletionsForList(taskListId: Long): Flow<List<Long>>

    // Faz 6 (yedekleme): yukarıdaki sorgu belirli bir listeye JOIN'li ve
    // Flow, burada tüm geçmişi (hangi listeye ait olduğuna bakmadan) tek
    // seferlik bir liste olarak istiyoruz.
    @Query("SELECT * FROM task_completions")
    suspend fun getAllCompletionsOnce(): List<TaskCompletionEntity>

    @Query("DELETE FROM task_completions")
    suspend fun clearAll()

    @Insert
    suspend fun insertAll(completions: List<TaskCompletionEntity>)
}
