package com.yusufemre.ivirbyyuinn.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

// Uygulama adını "AppDatabase" gibi genel tuttuk çünkü uygulama ismi zaten
// bir kez değişti (Ivır -> IvırByYuinn); veritabanı sınıfının adını marka
// ismine bağlamak, isim her değiştiğinde gereksiz bir yeniden adlandırma
// zinciri yaratırdı.
//
// version 8: Faz 12b'de task_lists'e sortOrder/isArchived eklendi (liste
// düzeyinde sıralama/arşivleme).
// version 9 (2026-08-11): tasks'e isArchived/completedAt eklendi (görev
// düzeyinde arşiv, task_lists'inkiyle aynı isimli ama ayrı bir alan,
// karıştırmamak lazım).
@Database(
    entities = [
        Task::class, TaskListEntity::class, Category::class, Subtask::class,
        TaskCompletionEntity::class, TaskTemplateEntity::class,
    ],
    version = 9,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun taskListDao(): TaskListDao
    abstract fun categoryDao(): CategoryDao
    abstract fun subtaskDao(): SubtaskDao
    abstract fun taskCompletionDao(): TaskCompletionDao
    abstract fun taskTemplateDao(): TaskTemplateDao

    companion object {
        // @Volatile + synchronized klasik Room/Java singleton deseni: birden
        // fazla thread aynı anda veritabanını iki kere oluşturmaya kalkışırsa
        // bunu engelliyor. Ekstra kütüphane gerekmeden elle yazdık.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ivirbyyuinn.db",
                )
                    .addMigrations(
                        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                        MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
                    )
                    // MIGRATION_1_2 sadece Faz 1'den YÜKSELEN cihazlarda
                    // çalışıyor (senin cihazın gibi). Uygulamayı ilk kez
                    // kuran biri için hiç migration çalışmaz, tablolar
                    // sıfırdan Room tarafından kuruluyor. Bu yüzden
                    // "Genel" listesini oluşturmayı unutabilirdik. Bu
                    // callback, veritabanı SIFIRDAN oluştuğunda (yani
                    // fresh install'da) devreye giriyor ve aynı varsayılan
                    // listeyi burada da açıyor. Böylece iki senaryoda da
                    // (yükseltme ve fresh install) sonuç aynı oluyor.
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // sortOrder/isArchived'ı ELLE veriyoruz: Room,
                            // entity'deki Kotlin varsayılanlarını (= 0,
                            // = false) SQL DEFAULT'a çevirmiyor, bu yüzden
                            // fresh install'da Room'un kendi kurduğu
                            // NOT NULL şemaya karşı bu sütunları atlayan
                            // bir INSERT başarısız olurdu.
                            db.execSQL(
                                "INSERT INTO task_lists (id, name, sortOrder, isArchived) VALUES (1, 'Genel', 0, 0)",
                            )
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
