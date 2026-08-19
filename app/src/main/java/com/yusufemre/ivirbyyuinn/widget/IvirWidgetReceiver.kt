package com.yusufemre.ivirbyyuinn.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

// Android'in widget sistemi hâlâ eski usul bir BroadcastReceiver
// bekliyor (AppWidgetManager'ın APPWIDGET_UPDATE yayınını yakalayan),
// Glance bu köprüyü bizim için GlanceAppWidgetReceiver ile kuruyor,
// tek yapmamız gereken hangi GlanceAppWidget'ı kullanacağını söylemek.
class IvirWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = IvirWidget()
}
