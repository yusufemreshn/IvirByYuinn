package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.GoogleDriveBackupRepository

// GoogleSignInAccount'u (Google Play Services'in kendi tipi) olduğu gibi
// yukarı sızdırmak yerine sadece UI'ın gerçekten ihtiyaç duyduğu tek alanı
// (e-posta) döndürüyoruz; null demek "bağlı hesap yok ya da appdata izni
// eksik".
class GetGoogleDriveAccountEmailUseCase(private val repository: GoogleDriveBackupRepository) {
    operator fun invoke(): String? = repository.getSignedInAccount()?.email
}
