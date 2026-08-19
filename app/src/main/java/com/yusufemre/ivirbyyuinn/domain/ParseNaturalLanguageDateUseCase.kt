package com.yusufemre.ivirbyyuinn.domain

import java.util.Calendar
import java.util.Locale

// Bir metinden çıkarılan tarih önerisi. `matchedKeyword`, UI'ın hangi
// kelimeyi (ör. "yarın") yakaladığını göstermesi için, kullanıcı önerinin
// NEREDEN geldiğini anlasın diye.
data class ParsedDate(val millis: Long, val matchedKeyword: String)

// Faz 12a: hızlı eklemede yazılan metinden basit bir tarih/saat çıkarımı.
// NLP kütüphanesi YOK (roadmap'in kütüphane politikasına uygun): sabit bir
// Türkçe kelime haritası + birkaç regex. KESİN/OTOMATİK bir değişiklik
// YAPMIYOR: sadece "böyle bir tarih önerebilirim" diye bir sonuç
// döndürüyor, TaskEditorSheet bunu bir öneri çipi olarak gösterip
// kullanıcı onaylarsa uyguluyor, kullanıcının yazdığı METNİ asla
// sessizce değiştirmiyoruz.
class ParseNaturalLanguageDateUseCase {

    operator fun invoke(text: String, now: Calendar = Calendar.getInstance()): ParsedDate? {
        val lower = text.lowercase(TURKISH_LOCALE)

        // Gün bulunamazsa saat de anlamsız, önce günü arıyoruz. Varsayılan
        // saat 09:00, kullanıcı ayrıca bir saat yazmadıysa "günün başı"
        // makul bir varsayılan.
        val dayCalendar = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val matchedKeyword: String = when {
            unicodeWordRegex("bugün").containsMatchIn(lower) -> "bugün"
            unicodeWordRegex("yarın").containsMatchIn(lower) -> {
                dayCalendar.add(Calendar.DAY_OF_YEAR, 1)
                "yarın"
            }
            unicodeWordRegex("gelecek hafta").containsMatchIn(lower) -> {
                dayCalendar.add(Calendar.DAY_OF_YEAR, 7)
                "gelecek hafta"
            }
            else -> matchWeekday(lower, dayCalendar, now)
        }
            ?: return null

        extractTime(lower)?.let { (hour, minute) ->
            dayCalendar.set(Calendar.HOUR_OF_DAY, hour)
            dayCalendar.set(Calendar.MINUTE, minute)
        }

        return ParsedDate(millis = dayCalendar.timeInMillis, matchedKeyword = matchedKeyword)
    }

    // Metinde "pazartesi".."pazar" kelimelerinden biri geçiyorsa, o
    // haftanın günü BUGÜNDEN sonraki en yakın oluşumuna ayarlanıyor.
    // Bugün TAM O gün ise (offset 0 çıkarsa) "gelecek hafta" kastedildiği
    // varsayılıyor: kullanıcı bugünü kastetseydi zaten "bugün" yazardı.
    private fun matchWeekday(lower: String, dayCalendar: Calendar, now: Calendar): String? {
        val (name, dayOfWeek) = WEEKDAYS.entries
            .firstOrNull { (weekdayName, _) -> unicodeWordRegex(weekdayName).containsMatchIn(lower) }
            ?: return null

        var offset = (dayOfWeek - now.get(Calendar.DAY_OF_WEEK) + 7) % 7
        if (offset == 0) offset = 7
        dayCalendar.add(Calendar.DAY_OF_YEAR, offset)
        return name
    }

    // \b (word boundary), Java'da varsayılan olarak Unicode-farkında DEĞİL:
    // \w'yi sadece [a-zA-Z0-9_] sayıyor. "salı" ("ı" ile bitiyor) ve
    // "çarşamba"/"perşembe" (ç/ş ile başlıyor) gibi Türkçe harf içeren
    // kelimelerde \b hiç oluşmuyor, bu yüzden bu üç gün hiç eşleşmiyordu.
    //
    // İlk düzeltme denemesi `(?U)` (Pattern.UNICODE_CHARACTER_CLASS) inline
    // bayrağıyla yapılmıştı, masaüstü JVM'de (unit testlerde) çalıştı ama
    // Android'in ICU tabanlı regex motorunda desteklenmiyor, PatternSyntaxException
    // fırlatıyordu; bu da görev başlığı her tuş vuruşunda ayrıştırıldığı için
    // (bkz. TaskEditorSheet.parsedDate) uygulamanın İLK harf yazılır yazılmaz
    // çökmesine yol açıyordu. `\p{L}`/`\p{N}` (Unicode harf/rakam kategorisi)
    // kullanan bu lookaround, hem masaüstünde hem Android'de aynı şekilde
    // çalışan, taşınabilir standart regex sözdizimi.
    private fun unicodeWordRegex(word: String): Regex =
        Regex("(?<![\\p{L}\\p{N}])${Regex.escape(word)}(?![\\p{L}\\p{N}])")

    // Üç ayrı, DAR kalıp deniyoruz (tek geniş bir regex yerine): "saat 17",
    // "17:30", "5'te" gibi AÇIKÇA bir saat belirten ifadeleri yakalamak
    // için. Bilerek çıplak sayılara (ör. bir görev başlığındaki "2 adet")
    // yanlışlıkla saat muamelesi yapmıyoruz.
    private fun extractTime(lower: String): Pair<Int, Int>? {
        TIME_WITH_LABEL.find(lower)?.let { match ->
            val hour = match.groupValues[1].toIntOrNull() ?: return null
            val minute = match.groupValues[2].toIntOrNull() ?: 0
            return validOrNull(hour, minute)
        }
        TIME_COLON.find(lower)?.let { match ->
            val hour = match.groupValues[1].toIntOrNull() ?: return null
            val minute = match.groupValues[2].toIntOrNull() ?: return null
            return validOrNull(hour, minute)
        }
        TIME_SUFFIX.find(lower)?.let { match ->
            val hour = match.groupValues[1].toIntOrNull() ?: return null
            return validOrNull(hour, 0)
        }
        return null
    }

    private fun validOrNull(hour: Int, minute: Int): Pair<Int, Int>? =
        if (hour in 0..23 && minute in 0..59) hour to minute else null

    private companion object {
        val TURKISH_LOCALE: Locale = Locale.forLanguageTag("tr")

        // Calendar.DAY_OF_WEEK: Pazar=1, Pazartesi=2, ... Cumartesi=7.
        val WEEKDAYS = mapOf(
            "pazartesi" to Calendar.MONDAY,
            "salı" to Calendar.TUESDAY,
            "çarşamba" to Calendar.WEDNESDAY,
            "perşembe" to Calendar.THURSDAY,
            "cuma" to Calendar.FRIDAY,
            "cumartesi" to Calendar.SATURDAY,
            "pazar" to Calendar.SUNDAY,
        )

        val TIME_WITH_LABEL = Regex("saat\\s+(\\d{1,2})(?::(\\d{2}))?")
        val TIME_COLON = Regex("\\b(\\d{1,2}):(\\d{2})\\b")
        val TIME_SUFFIX = Regex("\\b(\\d{1,2})'?(?:te|da|de|ta)\\b")
    }
}
