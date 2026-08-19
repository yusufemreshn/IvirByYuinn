package com.yusufemre.ivirbyyuinn.data

import androidx.room.withTransaction

// Diğer repository'lerin aksine bu, tek bir DAO'yu değil BEŞ tabloyu
// birden ilgilendiriyor, o yüzden tek tek DAO'ları değil, doğrudan
// AppDatabase'i (withTransaction için) ve ilgili repository'leri alıyor.
// .NET tarafında bir "Unit of Work" nesnesine, birden fazla repository'yi
// tek bir transaction altında koordine eden şeye karşılık geliyor.
class BackupRepository(
    private val database: AppDatabase,
    private val taskListRepository: TaskListRepository,
    private val categoryRepository: CategoryRepository,
    private val taskRepository: TaskRepository,
    private val subtaskRepository: SubtaskRepository,
    private val taskCompletionRepository: TaskCompletionRepository,
) {

    suspend fun exportToJson(): String {
        val snapshot = BackupSnapshot(
            exportedAt = System.currentTimeMillis(),
            taskLists = taskListRepository.getAllTaskListsOnce(),
            categories = categoryRepository.getAllCategoriesOnce(),
            tasks = taskRepository.getAllTasksOnce(),
            subtasks = subtaskRepository.getAllSubtasksOnce(),
            completions = taskCompletionRepository.getAllCompletionsOnce(),
        )
        return snapshot.toJson()
    }

    // JSON'ı parse ederken (bozuk dosya, eksik alan, vb.) org.json bir
    // JSONException fırlatabilir; burada özellikle YAKALAMIYORUZ, çağıran
    // ImportBackupUseCase bunu yakalayıp kullanıcıya "geçersiz yedek
    // dosyası" gibi bir mesaja çeviriyor. Repository katmanı sadece "işi
    // yap ya da hata fırlat" yapıyor, kullanıcıya gösterilecek mesajı
    // seçmek domain/UI'ın işi.
    suspend fun importFromJson(json: String) {
        val snapshot = parseBackupJson(json)

        // withTransaction: içindeki adımlardan biri bile patlarsa TÜM
        // işlem geri sarılıyor: yarıda kalmış, yarısı eski yarısı yeni
        // veriden oluşan bozuk bir veritabanı durumuna asla düşmüyoruz.
        // .NET'teki TransactionScope'un Room/coroutine karşılığı bu.
        database.withTransaction {
            // Önce çocuk tablolar temizleniyor, sonra ebeveynler, foreign
            // key kurallarını ihlal etmemek için bu sıra önemli (aslında
            // CASCADE tanımlı olduğu için ebeveyni silmek çocukları da
            // otomatik silerdi, ama sırayı elle netleştirmek daha güvenli
            // ve okuyanın niyeti anlaması daha kolay).
            subtaskRepository.clearAll()
            taskCompletionRepository.clearAll()
            taskRepository.clearAll()
            categoryRepository.clearAll()
            taskListRepository.clearAll()

            // Şimdi tam tersi sırayla, ebeveynden çocuğa doğru yeniden
            // yazıyoruz: bir görev eklenmeden önce onun taskListId'sinin
            // gerçekten var olması lazım, yoksa foreign key hatası alırız.
            taskListRepository.insertAll(snapshot.taskLists)
            categoryRepository.insertAll(snapshot.categories)
            taskRepository.insertAll(snapshot.tasks)
            subtaskRepository.insertAll(snapshot.subtasks)
            taskCompletionRepository.insertAll(snapshot.completions)
        }
    }
}
