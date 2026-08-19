package com.yusufemre.ivirbyyuinn.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Material3'ün varsayılan köşe yuvarlaklığı (extraSmall=4dp ... extraLarge=28dp)
// şu ana kadar hiç değiştirilmemişti. Her kart/sheet/dialog "jenerik Material"
// görünüyordu. Değerleri biraz büyüterek (her rolde birkaç dp fazla) daha
// yumuşak, "elle çizilmiş" hisseden tek bir kimlik kuruyoruz; Card (TaskRow),
// ModalBottomSheet (TaskEditorSheet), AlertDialog gibi bileşenler otomatik
// olarak buradan besleniyor, ayrı ayrı elle ayarlamamıza gerek kalmıyor.
val IvirShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    // TaskRow kartları burayı kullanacak (Faz 10a).
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(32.dp),
)
