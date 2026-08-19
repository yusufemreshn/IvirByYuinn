package com.yusufemre.ivirbyyuinn.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.yusufemre.ivirbyyuinn.IvirApplication
import com.yusufemre.ivirbyyuinn.MainActivity
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.data.Task

// Ana ekran widget'ımız. GlanceAppWidget'ın Compose'a çok benzeyen ama
// aslında tamamen farklı bir çalışma modeli var: bu composable'lar gerçek
// View ağacına değil, sistemin RemoteViews'e çevirdiği sınırlı bir küme
// bileşene deriveniyor (Column, Row, Text, Button gibi temel şeyler var,
// ama LazyColumn, Canvas gibi "gerçek" Compose'un zengin bileşenleri yok:
// bu yüzden Faz 11'de ana uygulamaya eklediğimiz elde çizilmiş IvirIcons/
// Canvas illüstrasyonlarının HİÇBİRİ burada kullanılamıyor).
class IvirWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val application = context.applicationContext as IvirApplication
        // Widget'ın kendi reaktif Flow dünyası yok: her güncellemede tek
        // seferlik bir sorgu atıp o anki durumu çiziyoruz.
        val tasks = application.getUpcomingTasksForWidgetUseCase()

        provideContent {
            GlanceTheme {
                WidgetContent(tasks = tasks)
            }
        }
    }
}

@Composable
private fun WidgetContent(tasks: List<Task>) {
    // Glance'ın kendi LocalContext'i (Compose'unkiyle karışmasın diye
    // ayrı bir paketten geliyor), widget'ın çalıştığı process'in
    // context'ini veriyor, MainActivity'yi açacak Intent'i bununla
    // kuruyoruz.
    val context = LocalContext.current
    val openAppAction = actionStartActivity(Intent(context, MainActivity::class.java))

    // Faz 11'e kadar burada sabit renkler (ör. Color(0xFFFDFBF6)) vardı:
    // widget, ana uygulamadaki hiçbir görsel değişikliği (23 tema, karanlık/
    // aydınlık mod) hiç yansıtmıyordu, hep aynı krem rengi kutuydu.
    // GlanceTheme.colors, Material3 ColorScheme'in rollerini (primary,
    // background, onSurfaceVariant...) birebir taşıyan, Android 12+'ta
    // duvar kağıdından türetilen dinamik renklere OTOMATİK uyum sağlayan
    // resmi bir Glance API'si, kendi 23 temamızı widget'a taşımak (Glance'ın
    // ayrı bir tema sistemi olduğu için) ayrı ve büyük bir iş olurdu, ama en
    // azından sistemin karanlık/aydınlık moduna ve Material You duvar kağıdı
    // rengine uyum sağlamak "bedavaya" geliyor, ekstra kütüphane gerekmiyor.
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(12.dp),
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Text(
                text = context.getString(R.string.app_name),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.primary,
                ),
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            // "+" dokunma alanı önceden sadece 14sp metin + 10dp/4dp
            // padding kadardı (~22dp yükseklik), önerilen 48dp dokunma
            // hedefinin çok altındaydı ve işaret parmağıyla tutturması
            // zordu. Şimdi sabit 48x48dp'lik bir kare, içeriği ortalanmış;
            // "+" da 14sp'den 20sp'ye çıktı ki kutu büyürken görsel olarak
            // küçük/boğulmuş kalmasın.
            Box(
                modifier = GlanceModifier
                    .width(48.dp)
                    .height(48.dp)
                    .background(GlanceTheme.colors.secondaryContainer)
                    .clickable(openAppAction),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = GlanceTheme.colors.onSecondaryContainer,
                    ),
                )
            }
        }

        // Not: Spacer'a burada padding() değil height() vermek şart:
        // Glance'ta içeriksiz bir Spacer'a sadece padding modifier'ı vermek
        // (height/width/defaultWeight olmadan) Column'daki bundan SONRAKİ
        // tüm kardeşlerin render'ını sessizce engelliyor (widget boş
        // görünüyordu, hiçbir hata/log yokken), gerçek cihazda widget'ı
        // kaldırıp yeniden eklettirerek doğrulandı.
        Spacer(modifier = GlanceModifier.height(10.dp))

        if (tasks.isEmpty()) {
            Text(
                text = context.getString(R.string.widget_no_tasks),
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 13.sp),
            )
        } else {
            tasks.forEach { task ->
                Text(
                    text = "• ${task.title}",
                    style = TextStyle(color = GlanceTheme.colors.onBackground, fontSize = 13.sp),
                    maxLines = 1,
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable(openAppAction),
                )
            }
        }
    }
}
