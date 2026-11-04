package com.fr33music.core.updater

import com.fr33music.core.updater.model.GitHubRelease
import com.fr33music.core.updater.model.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateChecker @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    suspend fun checkForUpdate(
        currentVersionName: String,
        apiUrl: String = LATEST_RELEASE_URL
    ): Result<UpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(apiUrl)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "Fr33Music-Updater")
                .build()

            val response = okHttpClient.newCall(request).execute()

            // Si devuelve 404 (no hay releases publicadas aún en el repositorio)
            if (response.code == 404) {
                return@withContext Result.success(null)
            }

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IOException("Error al consultar actualizaciones: HTTP ${response.code} ${response.message}")
                )
            }

            val bodyString = response.body?.string()
                ?: return@withContext Result.failure(IOException("Cuerpo de respuesta vacío de GitHub Releases"))

            val release = json.decodeFromString<GitHubRelease>(bodyString)

            // Buscar asset con extensión .apk
            val apkAsset = release.assets.firstOrNull { asset ->
                asset.name.endsWith(".apk", ignoreCase = true) ||
                    asset.browserDownloadUrl.endsWith(".apk", ignoreCase = true)
            } ?: return@withContext Result.success(null) // No hay APK adjunto a la release

            if (isVersionGreater(release.tagName, currentVersionName)) {
                val updateInfo = UpdateInfo(
                    versionName = release.tagName,
                    downloadUrl = apkAsset.browserDownloadUrl,
                    releaseNotes = release.body
                )
                Result.success(updateInfo)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        const val LATEST_RELEASE_URL = "https://api.github.com/repos/lyrics4us001-prog/fr33music/releases/latest"

        /**
         * Compara dos versiones semánticas ignorando prefijos como 'v' o 'V' y sufijos de pre-release.
         * Retorna true si remote > current, false en caso contrario.
         */
        fun isVersionGreater(remote: String, current: String): Boolean {
            val cleanRemote = remote.trim().removePrefix("v").removePrefix("V").substringBefore("-")
            val cleanCurrent = current.trim().removePrefix("v").removePrefix("V").substringBefore("-")

            val remoteParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }
            val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }

            if (remoteParts.isEmpty() && currentParts.isEmpty()) return false

            val maxLength = maxOf(remoteParts.size, currentParts.size)
            for (i in 0 until maxLength) {
                val r = remoteParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
            return false
        }
    }
}
