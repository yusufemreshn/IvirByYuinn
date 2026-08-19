package com.yusufemre.ivirbyyuinn.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

// notification/ReminderScheduler'ın hatırlatmalar için oynadığı rolü
// widget için oynuyor: TaskViewModel gibi domain-dışı katmanların Glance'a
// doğrudan bağımlı olmasını önleyen ince bir sarmalayıcı.
//
// updateAll(context), IvirWidget'ın o an ekranda duran TÜM örneklerini
// (hiç eklenmemişse hiçbir şey yapmadan) yeniden çizdiriyor, hangi widget
// id'lerinin var olduğuyla biz ilgilenmiyoruz, Glance kendi hallediyor.
class WidgetRefresher(private val context: Context) {
    suspend fun refresh() {
        IvirWidget().updateAll(context)
    }
}
