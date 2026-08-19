package com.yusufemre.ivirbyyuinn.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Projedeki ilk gerçek migration'ımız. Tüm migration'ları burada, tek
// dosyada topluyoruz: versiyon geçtikçe altına MIGRATION_2_3 diye
// yenileri eklenecek, hepsini bir arada görmek kolay olsun diye.
//
// ÖNEMLİ bir SQLite detayı: ALTER TABLE ile yeni sütun eklemek mümkün,
// ama foreign key EKLEMEK mümkün değil (foreign key'ler sadece tablo
// ilk kurulurken tanımlanabiliyor). tasks tablosuna taskListId/categoryId
// foreign key'leri gerektiği için, "yeni şemayla bir tablo kur, eski
// veriyi oraya kopyala, eskisini sil, yeni tabloyu eski adına çevir"
// yolunu izliyoruz; bu, SQLite dünyasında foreign key eklemenin standart
// yöntemi.
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1) Listeler için yeni tablo, ve var olan görevlerin düşeceği
        // varsayılan "Genel" listesi, id'sini elle 1 veriyoruz ki az
        // sonra tasks tablosuna yazacağımız varsayılan taskListId ile
        // eşleşsin.
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS task_lists (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL("INSERT INTO task_lists (id, name) VALUES (1, 'Genel')")

        // 2) Kategoriler için yeni tablo.
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                colorHex TEXT NOT NULL
            )
            """.trimIndent(),
        )

        // 3) tasks tablosunu yeni sütunlar ve foreign key'lerle yeniden kur.
        db.execSQL(
            """
            CREATE TABLE tasks_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                isDone INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                taskListId INTEGER NOT NULL,
                categoryId INTEGER,
                priority INTEGER NOT NULL,
                dueAt INTEGER,
                note TEXT,
                FOREIGN KEY(taskListId) REFERENCES task_lists(id) ON DELETE CASCADE,
                FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE SET NULL
            )
            """.trimIndent(),
        )
        // Var olan görevleri yeni tabloya kopyalıyoruz: taskListId=1
        // ("Genel"), categoryId=NULL (henüz kategorisi yok), priority=1
        // (Priority.MEDIUM.level), dueAt ve note de NULL.
        db.execSQL(
            """
            INSERT INTO tasks_new (id, title, isDone, createdAt, taskListId, categoryId, priority, dueAt, note)
            SELECT id, title, isDone, createdAt, 1, NULL, 1, NULL, NULL FROM tasks
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE tasks")
        db.execSQL("ALTER TABLE tasks_new RENAME TO tasks")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_taskListId ON tasks(taskListId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_categoryId ON tasks(categoryId)")

        // 4) Alt görevler için yeni tablo: tasks tablosu artık yeniden
        // kurulduğu için bu adımı ondan sonra yapıyoruz.
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS subtasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                taskId INTEGER NOT NULL,
                title TEXT NOT NULL,
                isDone INTEGER NOT NULL,
                FOREIGN KEY(taskId) REFERENCES tasks(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_subtasks_taskId ON subtasks(taskId)")
    }
}

// Faz 3: tasks tablosuna tekrar sıklığını tutan düz bir sütun ekliyoruz.
// MIGRATION_1_2'nin aksine burada foreign key YOK, o yüzden tabloyu
// yeniden kurmaya gerek yok: SQLite'ın ALTER TABLE ... ADD COLUMN'u
// düz sütun eklemek için zaten yeterli. Aradaki fark tam da bu: foreign
// key eklerken tablo yeniden kurulur, düz sütun eklerken kurulmaz.
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 0 = Recurrence.NONE.level, var olan tüm görevler tekrarsız
        // olarak işaretleniyor.
        db.execSQL("ALTER TABLE tasks ADD COLUMN recurrence INTEGER NOT NULL DEFAULT 0")
    }
}

// Faz 4a: çöp kutusu için düz bir boolean sütun. Yine foreign key yok,
// yine tek satırlık bir ALTER TABLE yeterli. Var olan hiçbir görev
// silinmiş sayılmasın diye varsayılan 0 (false).
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
    }
}

// Faz 4c: elle sürükle-bırak sıralaması için bir sütun. Düz ALTER TABLE
// yeterli, ama bu sefer sadece 0 vermek yetmiyor: tüm görevler 0'da
// eşitse "sırala" dediğimizde anlamlı bir sıra olmaz. Bu yüzden ikinci
// bir UPDATE ile var olan görevlere, hâlihazırda gördükleri "en yeni
// önce" sırasına denk gelen bir manualOrder veriyoruz (0 = en yeni),
// kullanıcı ilk kez manuel sıralamaya geçtiğinde liste anlamsızca
// karışmasın diye.
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN manualOrder INTEGER NOT NULL DEFAULT 0")
        db.execSQL(
            """
            UPDATE tasks SET manualOrder = (
                SELECT COUNT(*) FROM tasks t2
                WHERE t2.taskListId = tasks.taskListId AND t2.createdAt > tasks.createdAt
            )
            """.trimIndent(),
        )
    }
}

// Faz 5: istatistik/streak için görev tamamlanma anlarını ayrı bir
// tabloda biriktiriyoruz. Bu, önceki migration'ların aksine VAR OLAN bir
// tabloya sütun eklemiyor, tamamen YENİ bir tablo kuruyor, bu yüzden
// MIGRATION_1_2'deki "tabloyu yeniden kur" derdi burada yok, foreign
// key'i doğrudan CREATE TABLE içinde tanımlayabiliyoruz. Bilinçli bir
// karar: geçmişte (bu güncellemeden önce) tamamlanmış görevler için
// geriye dönük tahmini bir kayıt EKLEMİYORUZ: hangi tarihte
// tamamlandıkları gerçekten bilinmiyor, tahmini bir tarih uydurmak
// istatistiği yanıltırdı. İstatistik/streak bu güncellemeden itibaren
// sıfırdan birikmeye başlıyor.
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS task_completions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                taskId INTEGER NOT NULL,
                completedAt INTEGER NOT NULL,
                FOREIGN KEY(taskId) REFERENCES tasks(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_task_completions_taskId ON task_completions(taskId)")
    }
}

// Faz 12a: sık kullanılan görevler için tek dokunuşluk şablonlar. task_completions
// gibi TAMAMEN yeni bir tablo, var olan bir tabloya dokunmuyor; foreign key
// de bilerek yok (bkz. TaskTemplateEntity'deki yorum), o yüzden burada da
// (MIGRATION_5_6'daki gibi) tabloyu yeniden kurmaya gerek kalmadı.
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS task_templates (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                priority INTEGER NOT NULL,
                categoryId INTEGER,
                note TEXT
            )
            """.trimIndent(),
        )
    }
}

// Faz 12b: liste düzeyinde sıralama/arşivleme. MIGRATION_4_5'teki
// (Task.manualOrder) AYNI desen: düz ALTER TABLE + var olan listelere
// mevcut id sırasına denk gelen bir başlangıç sortOrder veren UPDATE:
// aksi halde tüm listeler 0'da eşit kalır ve "sırala" anlamsız olurdu.
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE task_lists ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE task_lists ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
        db.execSQL(
            """
            UPDATE task_lists SET sortOrder = (
                SELECT COUNT(*) FROM task_lists t2
                WHERE t2.id < task_lists.id
            )
            """.trimIndent(),
        )
    }
}

// 2026-08-11: görev arşivi. MIGRATION_3_4'teki (Task.isDeleted) AYNI
// desen, foreign key yok, düz ALTER TABLE yeterli. Faz 5'in
// task_completions kararıyla tutarlı olarak var olan tamamlanmış
// görevler için completedAt'i UYDURMUYORUZ, NULL bırakıyoruz (gerçekten
// ne zaman tamamlandıkları bilinmiyor).
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE tasks ADD COLUMN completedAt INTEGER")
    }
}
