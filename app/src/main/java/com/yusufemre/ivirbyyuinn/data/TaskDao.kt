package com.yusufemre.ivirbyyuinn.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


// DAO = Data Access Object. Burada SQL'i biz yazmıyoruz (Query hariç),
// Room derleme sırasında KSP ile bu interface'in gerçek implementasyonunu
// kendisi üretiyor. .NET'te bir Repository interface'ine EF Core'un
// karşılığını otomatik doldurması gibi düşünebilirsin.
@Dao
interface TaskDao {

    // Long dönmesini istiyoruz: yeni eklenen görevin gerçek id'sini
    // öğrenmezsek, Faz 3'te o görev için bir hatırlatma alarmı
    // kuramayız (alarm, görevin id'sine bağlı bir PendingIntent ile
    // kimliklendiriliyor).
    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    // Room, bir List<Task> verdiğimizde hepsini tek bir veritabanı
    // işleminde (transaction) güncelliyor: sürükle-bırak sıralaması
    // her değiştiğinde etkilenen görevlerin hepsini tek seferde
    // kaydetmek için kullanıyoruz (bkz. domain/ReorderTasksUseCase).
    @Update
    suspend fun updateAll(tasks: List<Task>)

    // Faz 4a'dan itibaren bu, kullanıcının "sil" dediği anda ÇAĞRILMIYOR
    // artık: "silme" artık update ile isDeleted=true yapmak demek. Bu
    // fonksiyon sadece çöp kutusundan "kalıcı olarak sil" dendiğinde
    // kullanılıyor, adını da bunu netleştirmek için hardDelete koyduk.
    @Delete
    suspend fun hardDelete(task: Task)

    // Flow dönmesi sayesinde tablo her değiştiğinde (ekleme/silme/güncelleme)
    // bunu dinleyen ViewModel otomatik olarak yeni listeyi alıyor, elle
    // "yenile" çağırmamıza gerek kalmıyor.
    //
    // Faz 2'den itibaren "tüm görevler" değil, "belirli bir listenin
    // görevleri" sorgulanıyor. Faz 4a'dan itibaren de isDeleted=0 şartı
    // var: çöp kutusundaki görevler normal listede görünmesin.
    // 2026-08-11: isArchived=0 şartı da eklendi, aynı gerekçe, arşivlenmiş
    // bir görev artık normal listede görünmesin, kendi ekranında görünsün.
    @Query("SELECT * FROM tasks WHERE taskListId = :taskListId AND isDeleted = 0 AND isArchived = 0 ORDER BY createdAt DESC")
    fun getTasksForList(taskListId: Long): Flow<List<Task>>

    // Faz 12b: bildirimden gelen Ertele/Tamamla aksiyonları sadece bir
    // taskId taşıyor (Intent'e koca bir Task nesnesi koymak yerine),
    // önce bu görevi tek seferlik yükleyip sonra üzerinde işlem yapıyoruz.
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?

    // Çöp kutusu ekranı için: aynı listenin SİLİNMİŞ görevleri.
    @Query("SELECT * FROM tasks WHERE taskListId = :taskListId AND isDeleted = 1 ORDER BY createdAt DESC")
    fun getDeletedTasks(taskListId: Long): Flow<List<Task>>

    // 2026-08-11: Arşiv ekranı için, aynı listenin ARŞİVLENMİŞ (ama
    // silinmemiş) görevleri, en son tamamlanan en üstte.
    @Query("SELECT * FROM tasks WHERE taskListId = :taskListId AND isDeleted = 0 AND isArchived = 1 ORDER BY completedAt DESC")
    fun getArchivedTasks(taskListId: Long): Flow<List<Task>>

    // 2026-08-11: otomatik arşivleme süpürmesi için, tamamlanmış, henüz
    // arşivlenmemiş/silinmemiş, tamamlanma anından beri 24 saat GEÇMİŞ
    // (yani completedAt <= şimdi-24s) görevler. Flow değil düz liste,
    // çünkü bu tek seferlik bir kontrol (bkz. AutoArchiveWorker).
    @Query(
        """
        SELECT * FROM tasks
        WHERE isDone = 1 AND isArchived = 0 AND isDeleted = 0
          AND completedAt IS NOT NULL AND completedAt <= :cutoffMillis
        """,
    )
    suspend fun getTasksPendingAutoArchive(cutoffMillis: Long): List<Task>

    // Çöp kutusundaki "tümünü sil" butonu için: hardDelete'in tek
    // seferde tüm listeye uygulanan hali: aynı listenin diğer görevlerine
    // (isDeleted = 0) dokunmuyor.
    @Query("DELETE FROM tasks WHERE taskListId = :taskListId AND isDeleted = 1")
    suspend fun hardDeleteAllForList(taskListId: Long)

    // Cihaz yeniden başladığında AlarmManager'daki TÜM alarmlar sistem
    // tarafından siliniyor. Bu yüzden hangi görevlerin hâlâ hatırlatma
    // beklediğini bulup onları yeniden kurmamız gerekiyor (bkz.
    // notification/BootCompletedReceiver). Bu tek seferlik bir sorgu
    // olduğu için Flow değil, düz bir liste dönüyor. Çöp kutusundaki
    // görevlerin hatırlatması olmamalı, o yüzden isDeleted=0 şartı var.
    @Query("SELECT * FROM tasks WHERE dueAt IS NOT NULL AND isDone = 0 AND isDeleted = 0")
    suspend fun getTasksWithPendingReminders(): List<Task>

    // Widget'ta gösterilecek görevler: tüm listelerden, tamamlanmamış,
    // silinmemiş, önce son tarihi olanlar (yakın tarihten uzağa), sonra
    // son tarihi olmayanlar. "(dueAt IS NULL)" SQLite'ta 0/1'e
    // çevriliyor, bu da dueAt'i OLANLARIN önce gelmesini sağlıyor,
    // ekstra bir sıralama sütunu eklemeden.
    @Query(
        """
        SELECT * FROM tasks
        WHERE isDone = 0 AND isDeleted = 0
        ORDER BY (dueAt IS NULL), dueAt ASC, createdAt DESC
        LIMIT 5
        """,
    )
    suspend fun getUpcomingTasksForWidget(): List<Task>

    // Faz 12a (akıllı listeler): TÜM listelerden, tamamlanmamış, silinmemiş,
    // dueAt'ı verilen [start, end) aralığına düşen görevler. "Bugün" ve
    // "Yaklaşan" aynı şekle sahip, tek fark GetSmartViewTasksUseCase'in
    // hesapladığı sınırlar.
    @Query(
        """
        SELECT * FROM tasks
        WHERE isDone = 0 AND isDeleted = 0
          AND dueAt >= :startMillis AND dueAt < :endMillis
        ORDER BY dueAt ASC
        """,
    )
    fun getTasksDueToday(startMillis: Long, endMillis: Long): Flow<List<Task>>

    @Query(
        """
        SELECT * FROM tasks
        WHERE isDone = 0 AND isDeleted = 0
          AND dueAt >= :startMillis AND dueAt < :endMillis
        ORDER BY dueAt ASC
        """,
    )
    fun getTasksDueThisWeek(startMillis: Long, endMillis: Long): Flow<List<Task>>

    // Süresi geçmiş: dueAt'ı OLAN, hâlâ tamamlanmamış, şu ana kadar geçmiş
    // görevler.
    @Query(
        """
        SELECT * FROM tasks
        WHERE isDone = 0 AND isDeleted = 0
          AND dueAt IS NOT NULL AND dueAt < :nowMillis
        ORDER BY dueAt ASC
        """,
    )
    fun getOverdueTasks(nowMillis: Long): Flow<List<Task>>

    // Faz 6: yedekleme için TÜM listelerdeki, çöp kutusundakiler dahil
    // bütün görevleri istiyoruz: yukarıdaki sorgular hep tek bir listeye
    // ya da isDeleted durumuna göre filtreliydi, burada hiç filtre yok,
    // gerçek bir "her şeyin anlık görüntüsü" istiyoruz.
    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksOnce(): List<Task>

    // Geri yükleme öncesi tabloyu tamamen boşaltmak için. Bilerek DELETE
    // kullanıyoruz, Room'un otomatik ürettiği bir "deleteAll" yok.
    @Query("DELETE FROM tasks")
    suspend fun clearAll()

    // Yedekteki görevleri id'leri KORUYARAK geri yazıyoruz, bkz.
    // plan/faz6-veri-yonetimi.md madde 5, foreign key ilişkilerinin
    // (taskListId/categoryId) bozulmaması için şart.
    @Insert
    suspend fun insertAll(tasks: List<Task>)
}
