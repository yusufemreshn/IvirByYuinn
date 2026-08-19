package com.yusufemre.ivirbyyuinn.ui.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.ui.theme.IvirIcons

// Hazır bir "PIN girişi" bileşeni Android'in kendi kütüphanelerinde yok
// (parola alanları genelde düz bir TextField ile çözülür), o yüzden
// Kütüphane kullanmama kararımız gereği burada elden 3x4'lük bir tuş takımı
// çiziyoruz: 1'den 9'a rakamlar, sonra boşluk/0/geri sil.
//
// Tuşlara SABİT bir boyut (KEY_SIZE) veriyoruz, aspectRatio(1f) DEĞİL.
// İlk denemede aspectRatio kullanmıştık ama Row'un yüksekliği de belirsiz
// olduğu için (wrap content) genişlik/yükseklik birbirini büyütüp devasa,
// bozuk daireler oluşuyordu. Sabit boyut, layout'un iki yönde de net bir
// kısıtı olduğu için öngörülebilir sonuç veriyor.
// 72dp -> 76dp: büyük sistem yazı tipi ölçeğinde rakamla daire arasında
// biraz daha pay bırakmak için.
private val KEY_SIZE = 76.dp

@Composable
fun PinPad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9'),
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                row.forEach { digit -> PinPadKey(digit = digit, onClick = { onDigit(digit) }) }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            // Sol alt köşe bilerek boş: 0 ortada durmalı, telefon
            // arama tuş takımlarındaki yerleşimin aynısı, kullanıcı zaten
            // buna alışkın.
            Spacer(modifier = Modifier.size(KEY_SIZE))
            PinPadKey(digit = '0', onClick = { onDigit('0') })
            // material-icons-core'da hazır bir "backspace" ikonu yok
            // (BarChart'ta olduğu gibi, bkz. rehber/faz5); Faz 10a'ya kadar
            // burada düz bir metin glifi (⌫) vardı, artık IvirIcons'taki
            // elden çizilmiş backspace ikonu kullanılıyor.
            val backspaceDescription = stringResource(R.string.pin_backspace_cd)
            IconButton(
                onClick = onBackspace,
                modifier = Modifier
                    .size(KEY_SIZE)
                    .semantics { contentDescription = backspaceDescription },
            ) {
                Icon(imageVector = IvirIcons.Backspace, contentDescription = null)
            }
        }
    }
}

@Composable
private fun PinPadKey(digit: Char, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(KEY_SIZE),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // headlineMedium yerine titleLarge: tek karakterlik rakam için
            // görsel olarak hâlâ büyük/net ama büyük erişilebilirlik yazı
            // tipi ölçeğinde 76dp'lik daireye sığma payı daha fazla.
            // maxLines/overflow, o payı da aşan uç durumlarda dairenin
            // dışına taşmak yerine düzgün kırpılmasını garanti ediyor.
            Text(
                text = digit.toString(),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}
