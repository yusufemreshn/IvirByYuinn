package com.yusufemre.ivirbyyuinn.ui.task

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.data.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Hazır bir Compose takvim bileşeni yok, sıfırdan bir ay ızgarası
// çiziyoruz. .NET tarafında bir WinForms/WPF MonthCalendar'ın hazır
// gelmesine alışıksındır, burada onun elle yazılmış hali bu dosya.
// Locale.getDefault(): bkz. TaskRow.dueDateFormatter'daki aynı yorum.
private val monthYearFormatter get() = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

// Faz 11: seçili gün panelinin üç hali (henüz seçim yok / seçili günde
// görev yok / seçili günde görev var) artık AnimatedContent ile geçiyor,
// önceden bu üçü arasında anlık, sert bir değişim vardı.
private enum class DayPanelState { PROMPT, EMPTY, LIST }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    tasks: List<Task>,
    onClose: () -> Unit,
    onTaskClick: (Task) -> Unit,
) {
    var displayedMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    // 2026-08-09: ay/yıl başlığına basınca açılan tekerlek seçici (iOS
    // tarzı), sadece Tamam'a basılınca displayedMonth'a uygulanacak
    // "taslak" değerler burada, pencere açıkken tutuluyor.
    var showMonthYearPicker by remember { mutableStateOf(false) }
    var pendingMonth by remember { mutableStateOf(displayedMonth.get(Calendar.MONTH)) }
    var pendingYear by remember { mutableStateOf(displayedMonth.get(Calendar.YEAR)) }

    val dayLabels = listOf(
        stringResource(R.string.day_monday_short),
        stringResource(R.string.day_tuesday_short),
        stringResource(R.string.day_wednesday_short),
        stringResource(R.string.day_thursday_short),
        stringResource(R.string.day_friday_short),
        stringResource(R.string.day_saturday_short),
        stringResource(R.string.day_sunday_short),
    )

    // Bu ayın içinde, son tarihi olan görevleri "ayın kaçıncı günü"ne
    // göre grupluyoruz, ızgarada hangi güne nokta koyacağımızı, altta
    // da seçili günün görevlerini bu haritadan buluyoruz.
    //
    // 2026-08-11: artık SADECE dueAt'e göre değil, completedAt'e göre de
    // gruplanıyor, kullanıcı geçmiş bir günde tamamladığı (ve belki
    // arşivlenmiş) bir görevi de o günün altında görebilsin diye. Bir
    // görevin son tarihi VE tamamlanma günü FARKLI aylara/günlere denk
    // gelebiliyor, o yüzden bir görev iki ayrı güne birden düşebiliyor
    // (aynı güne denk gelirse Set sayesinde tek kere ekleniyor).
    val year = displayedMonth.get(Calendar.YEAR)
    val month = displayedMonth.get(Calendar.MONTH)
    val tasksByDay = remember(tasks, year, month) {
        val grouped = mutableMapOf<Int, MutableList<Task>>()
        val cal = Calendar.getInstance()
        tasks.forEach { task ->
            val daysInThisMonth = mutableSetOf<Int>()
            task.dueAt?.let { dueAt ->
                cal.timeInMillis = dueAt
                if (cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month) {
                    daysInThisMonth += cal.get(Calendar.DAY_OF_MONTH)
                }
            }
            task.completedAt?.let { completedAt ->
                cal.timeInMillis = completedAt
                if (cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month) {
                    daysInThisMonth += cal.get(Calendar.DAY_OF_MONTH)
                }
            }
            daysInThisMonth.forEach { day ->
                grouped.getOrPut(day) { mutableListOf() }.add(task)
            }
        }
        grouped
    }

    // Faz 11: "bugün" hangi gün, hiç işaretlenmiyordu, sadece SEÇİLİ gün
    // vurgulanıyordu, kullanıcı ayı gezindikçe "şu an neredeyim" sorusunun
    // cevabını kaybediyordu. Görüntülenen ay bugünün ayıyla aynıysa bugünün
    // günü hesaplanıyor, değilse null (hiçbir hücre "bugün" işaretlenmiyor).
    val today = remember { Calendar.getInstance() }
    val todayOfMonth = if (year == today.get(Calendar.YEAR) && month == today.get(Calendar.MONTH)) {
        today.get(Calendar.DAY_OF_MONTH)
    } else {
        null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.calendar_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_close_cd),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            // Ay değiştirme satırı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val previousMonthDescription = stringResource(R.string.calendar_previous_month_cd)
                val nextMonthDescription = stringResource(R.string.calendar_next_month_cd)
                TextButton(
                    onClick = {
                        displayedMonth = (displayedMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                        selectedDay = null
                    },
                    modifier = Modifier.semantics { contentDescription = previousMonthDescription },
                ) { Text("‹") }
                Text(
                    text = monthYearFormatter.format(displayedMonth.time).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            pendingMonth = displayedMonth.get(Calendar.MONTH)
                            pendingYear = displayedMonth.get(Calendar.YEAR)
                            showMonthYearPicker = true
                        },
                )
                TextButton(
                    onClick = {
                        displayedMonth = (displayedMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                        selectedDay = null
                    },
                    modifier = Modifier.semantics { contentDescription = nextMonthDescription },
                ) { Text("›") }
            }

            // Haftanın günleri başlığı
            Row(modifier = Modifier.fillMaxWidth()) {
                dayLabels.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Ay ızgarası: ayın ilk gününün haftanın hangi gününe denk
            // geldiğini bulup (Pazartesi=0 olacak şekilde), o kadar boş
            // hücre bırakıp ardından günleri sırayla diziyoruz.
            val firstDayCalendar = (displayedMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
            // Calendar.DAY_OF_WEEK: Pazar=1, Pazartesi=2, ... Cumartesi=7.
            // Bizim başlığımız Pazartesi'den başladığı için 2 (Pazartesi)
            // offset 0 olacak şekilde çeviriyoruz.
            val firstDayOffset = (firstDayCalendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
            val daysInMonth = displayedMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
            val totalCells = firstDayOffset + daysInMonth
            val rowCount = (totalCells + 6) / 7

            for (row in 0 until rowCount) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val day = row * 7 + col - firstDayOffset + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clickable(enabled = day in 1..daysInMonth) { selectedDay = day },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (day in 1..daysInMonth) {
                                val isSelected = selectedDay == day
                                val isToday = todayOfMonth == day
                                // Faz 11: anlık renk değişimi yerine yumuşak
                                // bir geçiş, TaskRow'daki başlık soluması ile
                                // aynı desen.
                                val circleColor by animateColorAsState(
                                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    label = "calendar-day-selection",
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(circleColor)
                                            // Bugün, seçili OLMASA bile ince
                                            // bir kontur ile işaretleniyor,
                                            // kullanıcı ayı gezindikçe "şu an
                                            // neredeyim" cevabını kaybetmesin.
                                            .then(
                                                if (isToday && !isSelected) {
                                                    Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                                } else {
                                                    Modifier
                                                },
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        // Daire sabit 28dp, bodyMedium
                                        // (bodyLarge yerine) daha fazla boşluk
                                        // bırakıyor, maxLines/overflow ise
                                        // büyük sistem yazı tipinde rakamın
                                        // daireyi ikiye bölüp taşırmasını
                                        // (wrap) engelliyor.
                                        Text(
                                            text = day.toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Clip,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else if (isToday) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                        )
                                    }
                                    if (tasksByDay.containsKey(day)) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 2.dp)
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.secondary),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            val dayTasks = selectedDay?.let { tasksByDay[it] }.orEmpty()
            val panelState = when {
                selectedDay == null -> DayPanelState.PROMPT
                dayTasks.isEmpty() -> DayPanelState.EMPTY
                else -> DayPanelState.LIST
            }
            AnimatedContent(targetState = panelState, label = "calendar-day-panel") { state ->
                when (state) {
                    DayPanelState.PROMPT -> {
                        Text(
                            text = stringResource(R.string.calendar_tap_prompt),
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    DayPanelState.EMPTY -> {
                        Text(
                            text = stringResource(R.string.calendar_no_tasks_day),
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    DayPanelState.LIST -> {
                        LazyColumn {
                            items(items = dayTasks, key = { it.id }) { task ->
                                // Faz 11: düz bir Text satırı yerine, TaskRow'un
                                // sadeleştirilmiş bir versiyonu, öncelik rengi
                                // görünür olsun, ama swipe/checkbox gibi tam
                                // TaskRow karmaşıklığı burada gereksiz.
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .clickable { onTaskClick(task) },
                                    shape = MaterialTheme.shapes.small,
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    ),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(priorityColor(task.priority)),
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.weight(1f),
                                        )
                                        // 2026-08-11: bu görev o günde NEDEN
                                        // listeleniyor: son tarih mi,
                                        // tamamlanma mı (ikisi de olabilir),
                                        // net olsun diye tamamlanmışsa küçük
                                        // bir onay ikonu.
                                        if (task.isDone) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = stringResource(R.string.calendar_completed_on_this_day_cd),
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Material3'ün AlertDialog'u zaten varsayılan olarak karartılmış bir
    // scrim + ölçekli/soluklu bir giriş animasyonuyla açılıyor,
    // TaskEditorSheet'teki DatePicker/TimePicker dialoglarıyla AYNI desen,
    // "yakınlaşma + karartma" için ekstra bir animasyon kodu gerekmedi.
    if (showMonthYearPicker) {
        AlertDialog(
            onDismissRequest = { showMonthYearPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // ÖNCE günü 1'e çekiyoruz, yoksa ör. displayedMonth
                    // 31 Ocak'taysa ve kullanıcı Şubat'ı seçtiyse, Calendar
                    // 31 Şubat'ı otomatik olarak Mart'a taşırdı (klasik bir
                    // Calendar tuzağı, bkz. combineDateAndTime'daki benzer
                    // yorum TaskEditorSheet'te).
                    displayedMonth = (displayedMonth.clone() as Calendar).apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.YEAR, pendingYear)
                        set(Calendar.MONTH, pendingMonth)
                    }
                    selectedDay = null
                    showMonthYearPicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showMonthYearPicker = false }) { Text(stringResource(R.string.action_cancel)) }
            },
            text = {
                MonthYearWheelPicker(
                    initialMonth = pendingMonth,
                    initialYear = pendingYear,
                    onMonthChange = { pendingMonth = it },
                    onYearChange = { pendingYear = it },
                )
            },
        )
    }
}
