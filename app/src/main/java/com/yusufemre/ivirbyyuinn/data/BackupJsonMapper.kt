package com.yusufemre.ivirbyyuinn.data

import org.json.JSONArray
import org.json.JSONObject

// Gson/Moshi/kotlinx.serialization gibi bir kütüphaneye hiç ihtiyaç yok.
// org.json zaten Android SDK'sının içinde geliyor (cihazda hazır), bu
// yüzden kütüphane kullanmama kararımız gereği JSON'ı elden, satır satır
// kendimiz üretip okuyoruz. Dosya küçük kalması için tek bir dosyada,
// sadece iki fonksiyonda topluyoruz: biri BackupSnapshot'ı JSON'a çeviriyor,
// diğeri tam tersini yapıyor.
//
// "version" alanını şimdiden koyuyoruz: ileride yedek formatı değişirse
// (yeni bir tablo eklenirse mesela), eski bir yedeği okurken hangi
// formatta olduğunu buradan anlayabiliriz. Şu an tek bir format olduğu
// için hep 1 yazıyoruz/bekliyoruz.
private const val BACKUP_FORMAT_VERSION = 1

fun BackupSnapshot.toJson(): String {
    val root = JSONObject()
    root.put("version", BACKUP_FORMAT_VERSION)
    root.put("exportedAt", exportedAt)

    val taskListsArray = JSONArray()
    taskLists.forEach { list ->
        val obj = JSONObject()
        obj.put("id", list.id)
        obj.put("name", list.name)
        obj.put("sortOrder", list.sortOrder)
        obj.put("isArchived", list.isArchived)
        taskListsArray.put(obj)
    }
    root.put("taskLists", taskListsArray)

    val categoriesArray = JSONArray()
    categories.forEach { category ->
        val obj = JSONObject()
        obj.put("id", category.id)
        obj.put("name", category.name)
        obj.put("colorHex", category.colorHex)
        categoriesArray.put(obj)
    }
    root.put("categories", categoriesArray)

    val tasksArray = JSONArray()
    tasks.forEach { task ->
        val obj = JSONObject()
        obj.put("id", task.id)
        obj.put("title", task.title)
        obj.put("isDone", task.isDone)
        obj.put("createdAt", task.createdAt)
        obj.put("taskListId", task.taskListId)
        // categoryId/dueAt/note nullable: JSONObject.put(String, null gibi
        // bir şey) kabul etmiyor, JSONObject.NULL yazmamız lazım, yoksa
        // "put" o alanı sessizce atlar ve okurken alanın var olduğunu
        // sanıp yanlışlıkla varsayılan değer koyabiliriz.
        obj.put("categoryId", task.categoryId ?: JSONObject.NULL)
        obj.put("priority", task.priority.level)
        obj.put("dueAt", task.dueAt ?: JSONObject.NULL)
        obj.put("note", task.note ?: JSONObject.NULL)
        obj.put("recurrence", task.recurrence.level)
        obj.put("isDeleted", task.isDeleted)
        obj.put("manualOrder", task.manualOrder)
        obj.put("isArchived", task.isArchived)
        obj.put("completedAt", task.completedAt ?: JSONObject.NULL)
        tasksArray.put(obj)
    }
    root.put("tasks", tasksArray)

    val subtasksArray = JSONArray()
    subtasks.forEach { subtask ->
        val obj = JSONObject()
        obj.put("id", subtask.id)
        obj.put("taskId", subtask.taskId)
        obj.put("title", subtask.title)
        obj.put("isDone", subtask.isDone)
        subtasksArray.put(obj)
    }
    root.put("subtasks", subtasksArray)

    val completionsArray = JSONArray()
    completions.forEach { completion ->
        val obj = JSONObject()
        obj.put("id", completion.id)
        obj.put("taskId", completion.taskId)
        obj.put("completedAt", completion.completedAt)
        completionsArray.put(obj)
    }
    root.put("completions", completionsArray)

    // indent(2) zorunlu değil ama dosyayı biri merakla açıp bakarsa (ör.
    // "gerçekten benim verilerim mi bunlar" diye) okunabilir olsun diye.
    return root.toString(2)
}

// Bozuk/eksik bir dosya gelirse org.json zaten JSONException fırlatıyor.
// Burada onu YAKALAMIYORUZ, çağıran taraf (BackupRepository) yakalayıp
// kullanıcıya anlamlı bir mesaj gösterecek. Burada sessizce yutup boş bir
// snapshot dönersek, kullanıcı "içe aktardım ama hiçbir şey olmadı" diye
// çok daha kafası karışık bir duruma düşerdi.
fun parseBackupJson(json: String): BackupSnapshot {
    val root = JSONObject(json)

    val taskLists = mutableListOf<TaskListEntity>()
    val taskListsArray = root.getJSONArray("taskLists")
    for (i in 0 until taskListsArray.length()) {
        val obj = taskListsArray.getJSONObject(i)
        taskLists.add(
            TaskListEntity(
                id = obj.getLong("id"),
                name = obj.getString("name"),
                // optInt/optBoolean: eski (bu alanlardan önceki) yedek
                // dosyalarında bu alanlar yok, o zaman Entity'nin kendi
                // varsayılanıyla (0/false) aynı değere düşüyoruz.
                sortOrder = obj.optInt("sortOrder", 0),
                isArchived = obj.optBoolean("isArchived", false),
            ),
        )
    }

    val categories = mutableListOf<Category>()
    val categoriesArray = root.getJSONArray("categories")
    for (i in 0 until categoriesArray.length()) {
        val obj = categoriesArray.getJSONObject(i)
        categories.add(
            Category(
                id = obj.getLong("id"),
                name = obj.getString("name"),
                colorHex = obj.getString("colorHex"),
            ),
        )
    }

    val tasks = mutableListOf<Task>()
    val tasksArray = root.getJSONArray("tasks")
    for (i in 0 until tasksArray.length()) {
        val obj = tasksArray.getJSONObject(i)
        tasks.add(
            Task(
                id = obj.getLong("id"),
                title = obj.getString("title"),
                isDone = obj.getBoolean("isDone"),
                createdAt = obj.getLong("createdAt"),
                taskListId = obj.getLong("taskListId"),
                categoryId = if (obj.isNull("categoryId")) null else obj.getLong("categoryId"),
                priority = Priority.entries.first { it.level == obj.getInt("priority") },
                dueAt = if (obj.isNull("dueAt")) null else obj.getLong("dueAt"),
                note = if (obj.isNull("note")) null else obj.getString("note"),
                recurrence = Recurrence.entries.first { it.level == obj.getInt("recurrence") },
                isDeleted = obj.getBoolean("isDeleted"),
                manualOrder = obj.getInt("manualOrder"),
                // optBoolean/aşağıdaki null kontrolü: eski yedeklerde
                // isArchived/completedAt henüz yoktu (bkz. taskLists'teki
                // AYNI gerekçe), arşiv yoksa görev normal listeye düşüyor.
                isArchived = obj.optBoolean("isArchived", false),
                completedAt = if (obj.has("completedAt") && !obj.isNull("completedAt")) obj.getLong("completedAt") else null,
            ),
        )
    }

    val subtasks = mutableListOf<Subtask>()
    val subtasksArray = root.getJSONArray("subtasks")
    for (i in 0 until subtasksArray.length()) {
        val obj = subtasksArray.getJSONObject(i)
        subtasks.add(
            Subtask(
                id = obj.getLong("id"),
                taskId = obj.getLong("taskId"),
                title = obj.getString("title"),
                isDone = obj.getBoolean("isDone"),
            ),
        )
    }

    val completions = mutableListOf<TaskCompletionEntity>()
    val completionsArray = root.getJSONArray("completions")
    for (i in 0 until completionsArray.length()) {
        val obj = completionsArray.getJSONObject(i)
        completions.add(
            TaskCompletionEntity(
                id = obj.getLong("id"),
                taskId = obj.getLong("taskId"),
                completedAt = obj.getLong("completedAt"),
            ),
        )
    }

    return BackupSnapshot(
        exportedAt = root.getLong("exportedAt"),
        taskLists = taskLists,
        categories = categories,
        tasks = tasks,
        subtasks = subtasks,
        completions = completions,
    )
}
