package com.yusufemre.ivirbyyuinn.domain

import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// ParseNaturalLanguageDateUseCase `now: Calendar` parametresini enjekte
// etmeye izin veriyor (varsayılanı Calendar.getInstance() olsa da), bu
// yüzden "bugün"ü sabit, bilinen bir referans tarihe kilitleyip testleri
// deterministik hale getirebiliyoruz. 1 Ocak 2024 bilinen bir Pazartesi.
class ParseNaturalLanguageDateUseCaseTest {

    private val useCase = ParseNaturalLanguageDateUseCase()

    private fun mondayNoon(): Calendar = Calendar.getInstance().apply {
        set(2024, Calendar.JANUARY, 1, 10, 15, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private fun Long.toCalendar(): Calendar = Calendar.getInstance().apply { timeInMillis = this@toCalendar }

    @Test
    fun `eslesen kelime yoksa null doner`() {
        val result = useCase("bu bir görev başlığı", mondayNoon())

        assertNull(result)
    }

    @Test
    fun `bugun bugunun tarihini saat 9 00 ile dondurur`() {
        val result = useCase("bugün ödeme yap", mondayNoon())!!
        val resultCalendar = result.millis.toCalendar()

        assertEquals("bugün", result.matchedKeyword)
        assertEquals(1, resultCalendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, resultCalendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCalendar.get(Calendar.MINUTE))
    }

    @Test
    fun `yarin bir sonraki gunu dondurur`() {
        val result = useCase("yarın toplantı var", mondayNoon())!!
        val resultCalendar = result.millis.toCalendar()

        assertEquals("yarın", result.matchedKeyword)
        assertEquals(2, resultCalendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `gelecek hafta yedi gun sonrasini dondurur`() {
        val result = useCase("gelecek hafta görüşelim", mondayNoon())!!
        val resultCalendar = result.millis.toCalendar()

        assertEquals("gelecek hafta", result.matchedKeyword)
        assertEquals(8, resultCalendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `haftanin gunu bugunden sonraki en yakin olusuma ayarlanir`() {
        // "now" Pazartesi (1 Ocak); Salı bir sonraki gün (2 Ocak) olmalı.
        val result = useCase("salı buluşalım", mondayNoon())!!
        val resultCalendar = result.millis.toCalendar()

        assertEquals("salı", result.matchedKeyword)
        assertEquals(2, resultCalendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `bugun tam o haftanin gunuyse gelecek haftaya atlar`() {
        // "now" zaten Pazartesi, "pazartesi" yazılırsa bugünü değil,
        // GELECEK pazartesiyi (7 gün sonra, 8 Ocak) kastettiği varsayılıyor.
        val result = useCase("pazartesi buluşalım", mondayNoon())!!
        val resultCalendar = result.millis.toCalendar()

        assertEquals("pazartesi", result.matchedKeyword)
        assertEquals(8, resultCalendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `saat label ile birlikte yaziliyorsa okunur`() {
        val result = useCase("yarın saat 17'de", mondayNoon())!!
        val resultCalendar = result.millis.toCalendar()

        assertEquals(17, resultCalendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCalendar.get(Calendar.MINUTE))
    }

    @Test
    fun `saat iki nokta ust uste formatiyla okunur`() {
        val result = useCase("yarın 17:30", mondayNoon())!!
        val resultCalendar = result.millis.toCalendar()

        assertEquals(17, resultCalendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, resultCalendar.get(Calendar.MINUTE))
    }

    @Test
    fun `saat te da suffixiyle okunur`() {
        val result = useCase("yarın 5'te buluşalım", mondayNoon())!!
        val resultCalendar = result.millis.toCalendar()

        assertEquals(5, resultCalendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCalendar.get(Calendar.MINUTE))
    }

    @Test
    fun `gecersiz saat gun eslesmesini bozmadan yoksayilir`() {
        // 45 geçerli bir saat değil (0-23 aralığı dışında), extractTime
        // bunun için null dönmeli, ama "yarın" hâlâ eşleşmiş olmalı ve
        // saat varsayılan 09:00'da kalmalı.
        val result = useCase("yarın saat 45", mondayNoon())!!
        val resultCalendar = result.millis.toCalendar()

        assertEquals("yarın", result.matchedKeyword)
        assertEquals(9, resultCalendar.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `baglamsiz cikilcak sayi saat sanilmiyor`() {
        // "2 adet" gibi bir ifadedeki çıplak sayı saat gibi yorumlanmamalı
        // Sadece "bugün" eşleşip varsayılan 09:00 kullanılmalı.
        val result = useCase("bugün 2 adet ekmek al", mondayNoon())!!
        val resultCalendar = result.millis.toCalendar()

        assertEquals(9, resultCalendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCalendar.get(Calendar.MINUTE))
    }
}
