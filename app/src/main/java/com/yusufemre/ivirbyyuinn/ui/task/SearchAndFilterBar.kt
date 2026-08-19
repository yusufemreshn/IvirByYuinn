package com.yusufemre.ivirbyyuinn.ui.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.data.Category
import com.yusufemre.ivirbyyuinn.domain.SortOption

// sıralama seçeneğinin ekranda görünecek adı: enum'un kendi adını
// (DATE_NEWEST_FIRST gibi) doğrudan kullanıcıya göstermek istemedik.
@Composable
private fun SortOption.label(): String = when (this) {
    SortOption.DATE_NEWEST_FIRST -> stringResource(R.string.sort_newest)
    SortOption.PRIORITY_HIGH_FIRST -> stringResource(R.string.sort_priority)
    SortOption.ALPHABETICAL -> stringResource(R.string.sort_alphabetical)
    SortOption.MANUAL -> stringResource(R.string.sort_manual)
}

// Arama kutusu + kategori filtre chip'leri + sıralama seçici, listenin
// hemen üstünde duruyor. Kategori chip'leri LazyRow'da çünkü kategori
// sayısı artabilir, hepsi tek satırda kaydırılarak görünsün istedik.
@Composable
fun SearchAndFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    sortOption: SortOption,
    onSortOptionChange: (SortOption) -> Unit,
    // Faz 12a: akıllı listelerde (Bugün/Yaklaşan/Süresi Geçmiş) görevler
    // birden fazla listeden geldiği için sürükle-bırak sıralamasının
    // (MANUAL) hiçbir anlamı yok. Menüden tamamen kaldırıyoruz.
    hideManualSort: Boolean = false,
) {
    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text(stringResource(R.string.search_placeholder)) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // weight(1f) vermezsek LazyRow, Row'un tüm genişliğini kendine
            // ayırmaya çalışıyor ve sağdaki sıralama butonuna neredeyse hiç
            // yer kalmıyordu. "En yeni" yazısı bu yüzden "En / ye / ni"
            // diye üç satıra bölünüp sıkışıyordu. weight(1f) ile LazyRow'a
            // "önce sıralama butonu kendi doğal genişliğini alsın, sana ne
            // kalırsa o kadar yer var" diyoruz.
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 8.dp),
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { onCategorySelected(null) },
                        label = { Text(stringResource(R.string.filter_all)) },
                    )
                }
                items(items = categories, key = { it.id }) { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { onCategorySelected(category.id) },
                        label = { Text(category.name) },
                    )
                }
            }

            var sortMenuExpanded by remember { mutableStateOf(false) }
            Box {
                TextButton(onClick = { sortMenuExpanded = true }) {
                    Text(sortOption.label(), maxLines = 1)
                }
                DropdownMenu(expanded = sortMenuExpanded, onDismissRequest = { sortMenuExpanded = false }) {
                    SortOption.entries
                        .filter { !hideManualSort || it != SortOption.MANUAL }
                        .forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label()) },
                                onClick = {
                                    onSortOptionChange(option)
                                    sortMenuExpanded = false
                                },
                            )
                        }
                }
            }
        }
    }
}
