package com.yusufemre.ivirbyyuinn.ui.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yusufemre.ivirbyyuinn.ui.util.WheelPicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// CalendarScreen'in "‹ Ocak 2026 ›" başlığına basınca açılan pencerenin
// içeriği: ay ve yıl için yan yana iki tekerlek (paylaşılan
// ui/util/WheelPicker.kt üzerinden, bkz. oradaki yorumlar). Gün seçimi
// burada YOK, CalendarScreen zaten kendi takvim ızgarasında ayrı olarak
// yaptırıyor.
@Composable
fun MonthYearWheelPicker(
    initialMonth: Int,
    initialYear: Int,
    onMonthChange: (Int) -> Unit,
    onYearChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val monthLabels = remember { localizedMonthNames() }
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val years = remember(currentYear) { ((currentYear - 50)..(currentYear + 50)).toList() }
    val yearLabels = remember(years) { years.map { it.toString() } }

    var selectedMonthIndex by remember { mutableIntStateOf(initialMonth) }
    var selectedYearIndex by remember { mutableIntStateOf(years.indexOf(initialYear).coerceAtLeast(0)) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        WheelPicker(
            items = monthLabels,
            selectedIndex = selectedMonthIndex,
            onSelectedIndexChange = { index ->
                selectedMonthIndex = index
                onMonthChange(index)
            },
            modifier = Modifier.weight(1.3f),
        )
        WheelPicker(
            items = yearLabels,
            selectedIndex = selectedYearIndex,
            onSelectedIndexChange = { index ->
                selectedYearIndex = index
                onYearChange(years[index])
            },
            modifier = Modifier.weight(1f),
        )
    }
}

// "Ocak", "Şubat"... gibi yerelleştirilmiş ay isimleri, CalendarScreen'in
// monthYearFormatter'ıyla aynı SimpleDateFormat("MMMM") deseni, sadece
// tek bir ay yerine 12'sini birden üretiyor.
private fun localizedMonthNames(): List<String> {
    val formatter = SimpleDateFormat("MMMM", Locale.getDefault())
    val calendar = Calendar.getInstance()
    return (0..11).map { month ->
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.MONTH, month)
        formatter.format(calendar.time).replaceFirstChar { it.uppercase() }
    }
}
