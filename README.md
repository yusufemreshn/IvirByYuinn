# Ivır

A native Android to-do list app written in Kotlin and Jetpack Compose. The goal wasn't just to build a simple todo app, but to learn how to put together almost everything a real app needs from scratch: a local database, notifications, a widget, biometric lock, backups, multi-language support, and so on.

## Features

- Multiple lists, categories, subtasks, priorities, and recurring tasks (daily/weekly/monthly)
- Due date reminders (exact alarms), Snooze/Complete actions from the notification, quiet hours
- Home screen widget and one-tap task adding from the Quick Settings tile
- Calendar view, weekly stats, and a completion streak
- Focus mode (work/break timer)
- App lock with PIN + fingerprint/face unlock
- Full backup/restore to a single file
- Light/dark theme, dynamic color, and a large set of built-in color palettes
- Turkish and English language support, adjustable font size

## Tech stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Database:** Room
- **Architecture:** MVVM + Repository, with a use-case layer
- **Background work:** WorkManager, AlarmManager
- **Widget:** Jetpack Glance
- **Auth:** AndroidX Biometric

No third-party libraries. Everything is built with official Jetpack/AndroidX components.

## Building

```bash
git clone <repo-url>
cd todolistapp
./gradlew assembleDebug
```

Or just open it in Android Studio and run it directly. Minimum SDK 26, target/compile SDK 37.

## License

MIT, see [LICENSE](LICENSE).

---

# Ivır (Türkçe)

Kotlin ve Jetpack Compose ile yazılmış, native bir Android görev/yapılacaklar listesi uygulaması. Amaç sade bir "todo app" yapmak değil, gerçek bir uygulamada karşılaşacağım hemen her şeyi (yerel veritabanı, bildirimler, widget, biyometrik kilit, yedekleme, çoklu dil...) baştan sona kendim kurarak öğrenmekti.

## Özellikler

- Birden fazla liste, kategori, alt görev, öncelik ve tekrarlayan görevler (günlük/haftalık/aylık)
- Son tarih hatırlatmaları (tam zamanlı alarm), bildirimden Ertele/Tamamla, sessiz saatler
- Ana ekran widget'ı ve Hızlı Ayarlar döşemesinden tek dokunuşla görev ekleme
- Takvim görünümü, haftalık istatistikler ve tamamlama serisi (streak)
- Odak modu (çalışma/mola zamanlayıcısı)
- PIN + parmak izi/yüz tanıma ile uygulama kilidi
- Tüm verinin tek dosyaya yedeklenip geri yüklenmesi
- Açık/koyu tema, dinamik renk ve çok sayıda hazır renk paleti
- Türkçe ve İngilizce dil desteği, ayarlanabilir yazı boyutu

## Teknoloji

- **Dil:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Veritabanı:** Room
- **Mimari:** MVVM + Repository, use-case katmanı
- **Arka plan işleri:** WorkManager, AlarmManager
- **Widget:** Jetpack Glance
- **Kimlik doğrulama:** AndroidX Biometric

Üçüncü parti bir kütüphane kullanılmıyor; her şey resmi Jetpack/AndroidX bileşenleriyle yazıldı.

## Kurulum

```bash
git clone <repo-url>
cd todolistapp
./gradlew assembleDebug
```

Android Studio ile açıp doğrudan da çalıştırabilirsin. Minimum SDK 26, hedef/derleme SDK 37.

## Lisans

MIT, bkz. [LICENSE](LICENSE).
