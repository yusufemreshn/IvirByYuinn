package com.yusufemre.ivirbyyuinn.ui.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

// Compose'un Dialog implementasyonu (ve genel olarak birçok Android
// API'si) Activity Context'ini doğrudan değil, bir ContextWrapper
// zinciriyle sarmalayarak veriyor, "context as? Activity" bu yüzden
// Dialog içinden çağrılınca hep null dönebiliyor (bkz. Faz 7c'de dil
// değiştirmenin çalışmadığı hata). Zincirdeki baseContext'leri tek tek
// açıp gerçek Activity'yi buluyoruz. .NET'te bir DI container'ın
// "decorator" katmanlarını soyup asıl implementasyona ulaşmaya benziyor.
fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
