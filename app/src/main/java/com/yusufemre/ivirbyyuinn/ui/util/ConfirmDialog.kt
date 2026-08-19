package com.yusufemre.ivirbyyuinn.ui.util

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.yusufemre.ivirbyyuinn.R

// ConfirmDeleteDialog'un genel amaçlı kardeşi: "Sil" gibi silmeye özel
// sabit bir buton metni yerine Evet/Hayır kullanan, silme DIŞINDAKİ onay
// noktaları için (görev tamamlama/tamamlamama, uygulamadan çıkış gibi).
// Aynı görünüm/davranış, farklı buton metni; TaskListScreen'in çıkış
// onayı ve TaskRow'un tamamlama onayı bu AYNI bileşeni paylaşıyor.
@Composable
fun ConfirmDialog(
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
            }) { Text(stringResource(R.string.action_yes)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_no)) }
        },
    )
}
