package com.yusufemre.ivirbyyuinn.data

// Yedekleme anında tüm tabloların o anki halini bir arada tutan düz bir
// veri torbası, ne Room entity'si ne de UI state'i, sadece dışa/içe
// aktarma sırasında elden ele dolaşan bir taşıyıcı. exportedAt, kullanıcı
// dosyanın adını unutsa bile "bu yedek ne zaman alınmış" diye
// SettingsScreen'de gösterebilmemiz için var.
data class BackupSnapshot(
    val exportedAt: Long,
    val taskLists: List<TaskListEntity>,
    val categories: List<Category>,
    val tasks: List<Task>,
    val subtasks: List<Subtask>,
    val completions: List<TaskCompletionEntity>,
)
