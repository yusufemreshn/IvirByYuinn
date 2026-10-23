package com.yusufemre.ivirbyyuinn.data

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

// BackupRepository JSON'ı ÜRETİP OKUMAKTAN sorumlu, bu sınıf sadece o JSON'ı
// Drive'a taşımaktan sorumlu; ikisini birleştiren adım (hangi JSON ne zaman
// nereye gider) UploadBackupToDriveUseCase/RestoreBackupFromDriveUseCase'de.
//
// Yedekler appDataFolder'a (Drive'ın "uygulamaya özel, kullanıcının normal
// Drive arayüzünde GÖRMEDİĞİ" gizli alanı) yazılıyor: kullanıcı yanlışlıkla
// silemez/taşıyamaz, ayrıca normal Drive kotasına görünür şekilde dahil
// olmaz. Bunun bedeli, sadece drive.appdata kapsamını (tüm Drive'a değil)
// istediğimiz için kullanıcıya daha dar/az ürkütücü bir izin ekranı
// gösterilmesi.
class GoogleDriveBackupRepository(private val context: Context) {

    fun getSignInClient(): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        return GoogleSignIn.getClient(context, options)
    }

    fun getSignInIntent(): Intent = getSignInClient().signInIntent

    // Sign-in Intent'i bir ActivityResultLauncher ile başlatıp sonucu
    // (data Intent) buraya geri getirdikten sonra çağrılıyor.
    // GoogleSignIn.getSignedInAccountFromIntent'in döndürdüğü Task, bu
    // noktada ZATEN tamamlanmış durumda (sonuç yerelde hazır), bu yüzden
    // getResult() ile senkron okumak Google'ın kendi dokümantasyonunun
    // önerdiği güvenli yöntem. Başarısız girişte (kullanıcı vazgeçti,
    // ağ hatası vb.) null dönüyoruz.
    fun completeSignIn(data: Intent?): String? = try {
        val account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
        account.email
    } catch (e: ApiException) {
        null
    }

    fun getSignedInAccount(): GoogleSignInAccount? {
        // GoogleSignIn.getLastSignedInAccount null DÖNMEZ sadece hesap yoksa,
        // istediğimiz appdata kapsamını içermeyen ESKİ bir girişten kalma
        // hesabı da döner; hasPermissions kontrolü olmadan Drive çağrıları
        // "izin yok" hatasıyla patlardı.
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        return account.takeIf { GoogleSignIn.hasPermissions(it, Scope(DriveScopes.DRIVE_APPDATA)) }
    }

    fun signOut(onComplete: () -> Unit) {
        getSignInClient().signOut().addOnCompleteListener { onComplete() }
    }

    suspend fun uploadBackup(json: String): Unit = withContext(Dispatchers.IO) {
        val drive = driveServiceOrThrow()
        val metadata = File().apply {
            name = "ivir-backup-${System.currentTimeMillis()}.json"
            parents = listOf("appDataFolder")
        }
        val content = ByteArrayContent.fromString("application/json", json)
        drive.files().create(metadata, content).setFields("id").execute()
        pruneOldBackups(drive)
    }

    // En güncel yedeği indirir, hiç yedek yoksa null döner.
    suspend fun downloadLatestBackup(): String? = withContext(Dispatchers.IO) {
        val drive = driveServiceOrThrow()
        val latest = drive.files().list()
            .setSpaces("appDataFolder")
            .setFields("files(id, createdTime)")
            .setOrderBy("createdTime desc")
            .setPageSize(1)
            .execute()
            .files
            ?.firstOrNull() ?: return@withContext null

        val outputStream = ByteArrayOutputStream()
        drive.files().get(latest.id).executeMediaAndDownloadTo(outputStream)
        outputStream.toString("UTF-8")
    }

    private fun pruneOldBackups(drive: Drive) {
        val files = drive.files().list()
            .setSpaces("appDataFolder")
            .setFields("files(id, createdTime)")
            .setOrderBy("createdTime desc")
            .execute()
            .files ?: return
        // En güncel KEEP_COUNT yedeği tutuyoruz, geri kalanı siliyoruz;
        // appDataFolder kullanıcıya görünmediği için sınırsız biriktirmek
        // sessizce Drive kotasını tüketirdi.
        files.drop(KEEP_COUNT).forEach { drive.files().delete(it.id).execute() }
    }

    private fun driveServiceOrThrow(): Drive {
        val account = getSignedInAccount() ?: error("Google hesabı bağlı değil")
        val credential = GoogleAccountCredential.usingOAuth2(context, listOf(DriveScopes.DRIVE_APPDATA))
        credential.selectedAccount = account.account
        return Drive.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
            .setApplicationName("Ivir")
            .build()
    }

    private companion object {
        const val KEEP_COUNT = 5
    }
}
