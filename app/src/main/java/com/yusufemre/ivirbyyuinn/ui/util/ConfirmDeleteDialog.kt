package com.yusufemre.ivirbyyuinn.ui.util

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.yusufemre.ivirbyyuinn.R

// Kullanıcı geri bildirimi: "herhangi bir şeyi silerken emin misiniz ekranı
// gelsin." Daha önce TrashScreen'in kalıcı silme onayı (kendi başına,
// gömülü bir AlertDialog olarak) vardı, şimdi AYNI görünüm/davranış tüm
// silme noktalarında (görev, kategori, şablon, alt görev, toplu silme)
// tekrar kullanılıyor. Sadece başlık/metin içeriği çağrıya özel.
@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onConfirm()
            }) { Text(stringResource(R.string.action_delete)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
