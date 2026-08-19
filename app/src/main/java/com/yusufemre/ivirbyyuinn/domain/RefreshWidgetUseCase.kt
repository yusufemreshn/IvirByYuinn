package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.widget.WidgetRefresher

// Görev ekleme/güncelleme/tamamlama/silme gibi widget'ın gösterdiği veriyi
// değiştiren her işlemden sonra çağrılır: widget'ın 30 dakikalık pasif
// yenileme periyodunu (bkz. res/xml/ivir_widget_info.xml) beklemeden anında
// güncel görünmesini sağlar. Widget hiç eklenmemişse WidgetRefresher.refresh()
// zaten no-op, burada ekstra bir kontrole gerek yok.
class RefreshWidgetUseCase(private val widgetRefresher: WidgetRefresher) {
    suspend operator fun invoke() = widgetRefresher.refresh()
}
