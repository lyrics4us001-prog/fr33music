package com.fr33music.core.database.download

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.fr33music.core.database.dao.AlbumDao
import com.fr33music.core.database.dao.ArtistDao
import com.fr33music.core.database.dao.DownloadDao
import com.fr33music.core.database.dao.TrackDao
import com.fr33music.core.database.entity.AlbumEntity
import com.fr33music.core.database.entity.ArtistEntity
import com.fr33music.core.database.entity.DownloadStatus
import com.fr33music.core.database.entity.TrackEntity
import com.fr33music.core.domain.model.AudioFormat
import com.fr33music.core.domain.model.Track
import com.fr33music.core.extractor.StreamResolver
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DownloadWorkerEntryPoint {
    fun streamResolver(): StreamResolver
    fun trackDao(): TrackDao
    fun artistDao(): ArtistDao
    fun albumDao(): AlbumDao
    fun downloadDao(): DownloadDao
    fun okHttpClient(): OkHttpClient
}

class DownloadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "DownloadWorker"
        const val KEY_TRACK_ID = "track_id"
        const val KEY_TITLE = "title"
        const val KEY_ARTIST = "artist"
        const val KEY_DURATION = "duration"
        const val KEY_THUMBNAIL = "thumbnail"
        const val KEY_ALBUM = "album"
        const val MIN_FILE_SIZE_BYTES = 100 * 1024L // 100 KB mínimo para evitar archivos corruptos/incompletos
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val trackId = inputData.getString(KEY_TRACK_ID) ?: run {
            Log.e(TAG, "No se proporcionó KEY_TRACK_ID en inputData")
            return@withContext Result.failure()
        }
        val title = inputData.getString(KEY_TITLE) ?: "Desconocido"
        val artist = inputData.getString(KEY_ARTIST) ?: "Desconocido"
        val duration = inputData.getLong(KEY_DURATION, 0L)
        val thumbnail = inputData.getString(KEY_THUMBNAIL)
        val album = inputData.getString(KEY_ALBUM)

        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            DownloadWorkerEntryPoint::class.java
        )
        val streamResolver = entryPoint.streamResolver()
        val trackDao = entryPoint.trackDao()
        val artistDao = entryPoint.artistDao()
        val albumDao = entryPoint.albumDao()
        val downloadDao = entryPoint.downloadDao()
        val okHttpClient = entryPoint.okHttpClient()

        val downloadsDir = File(applicationContext.filesDir, "downloads")
        val opusFile = File(downloadsDir, "$trackId.opus")
        val m4aFile = File(downloadsDir, "$trackId.m4a")
        val validLocalFile = when {
            opusFile.exists() && opusFile.length() >= MIN_FILE_SIZE_BYTES -> opusFile
            m4aFile.exists() && m4aFile.length() >= MIN_FILE_SIZE_BYTES -> m4aFile
            else -> null
        }
        val existingDownload = downloadDao.getDownloadSync(trackId)
        if (existingDownload?.status == DownloadStatus.COMPLETED && validLocalFile != null) {
            Log.i(TAG, "DownloadWorker: La pista $trackId ya está descargada y completa (${validLocalFile.length()} bytes). Omitiendo.")
            return@withContext Result.success(workDataOf("localPath" to validLocalFile.absolutePath))
        }

        Log.i(TAG, "Iniciando descarga (intento $runAttemptCount/3): $trackId - $title ($artist)")

        var tempFile: File? = null
        var outputFile: File? = null

        try {
            downloadDao.updateStatus(trackId, DownloadStatus.DOWNLOADING, progress = 10)

            val track = Track(
                trackId = trackId,
                title = title,
                artistName = artist,
                durationMs = duration,
                thumbnailUrl = thumbnail,
                albumName = album
            )

            // Descargar carátula localmente en almacenamiento privado para soporte 100% offline
            var localThumbnail = thumbnail
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val coverFile = File(downloadsDir, "$trackId.jpg")

            if (!coverFile.exists() || coverFile.length() == 0L) {
                if (!thumbnail.isNullOrBlank()) {
                    try {
                        val hdUrl = Track.upgradeThumbnail(thumbnail) ?: thumbnail
                        Log.d(TAG, "Descargando miniatura HD offline para $trackId desde $hdUrl")
                        var coverReq = Request.Builder().url(hdUrl).build()
                        var coverRes = okHttpClient.newCall(coverReq).execute()

                        // Fallback a SD si maxresdefault devuelve 404 u otro error
                        if (!coverRes.isSuccessful && hdUrl.contains("/maxresdefault.jpg")) {
                            val sdUrl = hdUrl.replace("/maxresdefault.jpg", "/sddefault.jpg")
                            Log.d(TAG, "maxresdefault falló (${coverRes.code}), reintentando con SD: $sdUrl")
                            coverReq = Request.Builder().url(sdUrl).build()
                            coverRes = okHttpClient.newCall(coverReq).execute()
                        }

                        // Fallback a HQ si SD también falló
                        if (!coverRes.isSuccessful && hdUrl.contains("/maxresdefault.jpg")) {
                            val hqUrl = hdUrl.replace("/maxresdefault.jpg", "/hqdefault.jpg")
                            Log.d(TAG, "sddefault falló (${coverRes.code}), reintentando con HQ: $hqUrl")
                            coverReq = Request.Builder().url(hqUrl).build()
                            coverRes = okHttpClient.newCall(coverReq).execute()
                        }

                        if (coverRes.isSuccessful && coverRes.body != null) {
                            FileOutputStream(coverFile).use { out ->
                                val imgBuffer = ByteArray(16 * 1024)
                                coverRes.body!!.byteStream().use { input ->
                                    var bytes: Int
                                    while (input.read(imgBuffer).also { bytes = it } != -1) {
                                        out.write(imgBuffer, 0, bytes)
                                    }
                                }
                                out.flush()
                            }
                            Log.d(TAG, "Miniatura offline guardada para $trackId: ${coverFile.length()} bytes")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "No se pudo descargar carátula local para $trackId: ${e.message}")
                    }
                }
            } else {
                Log.d(TAG, "Miniatura ya existe localmente para $trackId: ${coverFile.length()} bytes")
            }

            if (coverFile.exists() && coverFile.length() > 0) {
                localThumbnail = Uri.fromFile(coverFile).toString()
            }

            val streamResult = streamResolver.resolveAudio(track)
            val stream = streamResult.getOrElse { throwable ->
                val errorMsg = throwable.message ?: "No se pudo obtener el stream de audio"
                Log.e(TAG, "Error al resolver stream para $trackId en intento $runAttemptCount: $errorMsg", throwable)
                if (runAttemptCount < 3) {
                    downloadDao.updateStatus(
                        trackId = trackId,
                        status = DownloadStatus.DOWNLOADING,
                        progress = 0,
                        error = "Reintentando resolver stream (${runAttemptCount + 1}/3)..."
                    )
                    return@withContext Result.retry()
                } else {
                    downloadDao.updateStatus(
                        trackId = trackId,
                        status = DownloadStatus.ERROR,
                        error = errorMsg
                    )
                    return@withContext Result.failure()
                }
            }

            val isOpus = stream.format == AudioFormat.OPUS
            val extension = if (isOpus) "opus" else "m4a"

            if (!downloadsDir.exists()) {
                val created = downloadsDir.mkdirs()
                if (!created && !downloadsDir.exists()) {
                    val errorMsg = "No se pudo crear el directorio de descargas"
                    Log.e(TAG, errorMsg)
                    downloadDao.updateStatus(trackId, DownloadStatus.ERROR, error = errorMsg)
                    return@withContext Result.failure()
                }
            }

            // Usar archivo temporal (.tmp) durante la descarga para que nunca quede un archivo corrupto accesible
            tempFile = File(downloadsDir, "$trackId.tmp")
            outputFile = File(downloadsDir, "$trackId.$extension")

            var totalBytes = -1L
            var downloadedBytes = 0L
            var lastReportedProgress = 5

            // Usar descarga por bloques Range de 1 MB para evitar que YouTube limite la velocidad (throttling)
            val chunkSize = 1024 * 1024L // 1 MB por bloque
            val firstChunkReq = Request.Builder().url(stream.url)
            stream.headers.forEach { (k, v) -> if (!k.equals("Range", ignoreCase = true)) firstChunkReq.header(k, v) }
            firstChunkReq.header("Range", "bytes=0-${chunkSize - 1}")

            Log.d(TAG, "Iniciando descarga acelerada por chunks para $trackId con formato ${stream.format}")
            val firstResponse = okHttpClient.newCall(firstChunkReq.build()).execute()

            if (!firstResponse.isSuccessful || firstResponse.body == null) {
                val errorMsg = "HTTP error ${firstResponse.code}: ${firstResponse.message}"
                Log.e(TAG, "Error HTTP al descargar $trackId: $errorMsg")
                tempFile.delete()

                if (runAttemptCount < 3) {
                    downloadDao.updateStatus(
                        trackId = trackId,
                        status = DownloadStatus.DOWNLOADING,
                        progress = 0,
                        error = "Reintentando por fallo HTTP (${runAttemptCount + 1}/3)..."
                    )
                    return@withContext Result.retry()
                } else {
                    downloadDao.updateStatus(
                        trackId = trackId,
                        status = DownloadStatus.ERROR,
                        error = errorMsg
                    )
                    return@withContext Result.failure()
                }
            }

            // Verificar Content-Type: rechazar páginas de error HTML de YouTube
            val contentType = firstResponse.header("Content-Type")?.lowercase() ?: ""
            if (contentType.contains("text/html") || contentType.contains("text/plain")) {
                tempFile.delete()
                val errorMsg = "YouTube devolvió página HTML en lugar de audio ($contentType)"
                Log.e(TAG, errorMsg)
                throw IOException(errorMsg)
            }

            if (firstResponse.code == 206) {
                // Servidor soporta Range. Extraer tamaño total de Content-Range (ej. bytes 0-1048575/4892011)
                val contentRange = firstResponse.header("Content-Range") ?: ""
                totalBytes = contentRange.substringAfterLast("/", "-1").toLongOrNull() ?: -1L

                FileOutputStream(tempFile).use { fileOut ->
                    firstResponse.body!!.byteStream().use { input ->
                        val buf = ByteArray(32 * 1024)
                        var bytesRead: Int
                        while (input.read(buf).also { bytesRead = it } != -1) {
                            fileOut.write(buf, 0, bytesRead)
                            downloadedBytes += bytesRead
                        }
                    }
                    fileOut.flush()

                    if (totalBytes > 0) {
                        val currentProgress = (downloadedBytes * 100 / totalBytes).toInt().coerceIn(5, 95)
                        downloadDao.updateStatus(trackId, DownloadStatus.DOWNLOADING, progress = currentProgress)
                    }

                    // Descargar chunks sucesivos de 1MB a toda velocidad
                    var currentStart = downloadedBytes
                    while (totalBytes > 0 && currentStart < totalBytes) {
                        val currentEnd = minOf(currentStart + chunkSize - 1, totalBytes - 1)
                        val chunkReq = Request.Builder().url(stream.url)
                        stream.headers.forEach { (k, v) -> if (!k.equals("Range", ignoreCase = true)) chunkReq.header(k, v) }
                        chunkReq.header("Range", "bytes=$currentStart-$currentEnd")

                        val chunkRes = okHttpClient.newCall(chunkReq.build()).execute()
                        if (!chunkRes.isSuccessful || chunkRes.body == null) {
                            throw IOException("Error en chunk $currentStart-$currentEnd: HTTP ${chunkRes.code}")
                        }

                        chunkRes.body!!.byteStream().use { input ->
                            val buf = ByteArray(32 * 1024)
                            var bytesRead: Int
                            while (input.read(buf).also { bytesRead = it } != -1) {
                                fileOut.write(buf, 0, bytesRead)
                                downloadedBytes += bytesRead
                            }
                        }
                        fileOut.flush()

                        val currentProgress = (downloadedBytes * 100 / totalBytes).toInt().coerceIn(5, 95)
                        if (currentProgress >= lastReportedProgress + 10) {
                            lastReportedProgress = currentProgress
                            downloadDao.updateStatus(trackId, DownloadStatus.DOWNLOADING, progress = currentProgress)
                        }

                        currentStart = downloadedBytes
                    }

                    fileOut.flush()
                    fileOut.fd.sync()
                }
            } else {
                // Si respondió 200 OK (sin Range), leer stream completo continuo con buffer amplio
                val body = firstResponse.body!!
                totalBytes = body.contentLength()

                FileOutputStream(tempFile).use { fileOut ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(64 * 1024)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            fileOut.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            if (totalBytes > 0) {
                                val currentProgress = (downloadedBytes * 100 / totalBytes).toInt().coerceIn(5, 95)
                                if (currentProgress >= lastReportedProgress + 10) {
                                    lastReportedProgress = currentProgress
                                    downloadDao.updateStatus(trackId, DownloadStatus.DOWNLOADING, progress = currentProgress)
                                }
                            }
                        }
                        fileOut.flush()
                        fileOut.fd.sync()
                    }
                }
            }

            // Verificar que no se haya cortado la conexión antes de tiempo
            if (totalBytes > 0L && downloadedBytes < totalBytes) {
                tempFile.delete()
                val errorMsg = "Descarga incompleta: recibidos $downloadedBytes de $totalBytes bytes"
                Log.e(TAG, errorMsg)
                throw IOException(errorMsg)
            }

            // Verificar tamaño mínimo (>= 100 KB) para asegurar que no sea un stub corrupto
            if (!tempFile.exists() || tempFile.length() < MIN_FILE_SIZE_BYTES) {
                val size = tempFile.length()
                tempFile.delete()
                val errorMsg = "Archivo descargado demasiado pequeño ($size bytes < $MIN_FILE_SIZE_BYTES)"
                Log.e(TAG, errorMsg)
                throw IOException(errorMsg)
            }

            // Mover el archivo temporal al destino final (.opus / .m4a) de forma atómica
            if (outputFile.exists()) {
                outputFile.delete()
            }
            val renamed = tempFile.renameTo(outputFile)
            if (!renamed) {
                // Fallback si renameTo falla entre montajes
                tempFile.copyTo(outputFile, overwrite = true)
                tempFile.delete()
            }

            Log.i(TAG, "Descarga completada y verificada: ${outputFile.absolutePath} (${outputFile.length()} bytes)")
            downloadDao.updateStatus(
                trackId = trackId,
                status = DownloadStatus.COMPLETED,
                localPath = outputFile.absolutePath,
                progress = 100
            )

            val isRealAlbum = !album.isNullOrBlank() &&
                    !album.equals(artist, ignoreCase = true) &&
                    !album.equals("Sencillos", ignoreCase = true) &&
                    !album.equals("YouTube", ignoreCase = true)
            val effectiveAlbum: String = if (isRealAlbum) album!! else "Sencillos"
            val finalThumb = localThumbnail ?: thumbnail
            val existing = trackDao.getTrackSync(trackId)
            if (existing != null) {
                trackDao.setDownloaded(
                    id = trackId,
                    isDownloaded = true,
                    localPath = outputFile.absolutePath,
                    format = stream.format.name
                )
                val updatedThumb = if (!finalThumb.isNullOrBlank()) finalThumb else existing.thumbnailUrl
                val updatedAlbum = if (!existing.albumName.isNullOrBlank() && !existing.albumName.equals(artist, ignoreCase = true)) existing.albumName else effectiveAlbum
                trackDao.updateTrack(existing.copy(thumbnailUrl = updatedThumb, albumName = updatedAlbum, isDownloaded = true, localPath = outputFile.absolutePath))
            } else {
                trackDao.insertTrack(
                    TrackEntity(
                        id = trackId,
                        title = title,
                        artistName = artist,
                        albumName = effectiveAlbum,
                        durationMs = duration,
                        thumbnailUrl = finalThumb,
                        format = stream.format.name,
                        isDownloaded = true,
                        isSaved = false,
                        localPath = outputFile.absolutePath
                    )
                )
            }

            // Asegurar que ArtistEntity y AlbumEntity queden guardados con su thumbnail (priorizando file:// si existe)
            if (artist.isNotBlank() && !finalThumb.isNullOrBlank()) {
                val existingArtist = artistDao.getArtistSync(artist)
                if (existingArtist == null || existingArtist.thumbnailUrl.isNullOrBlank() || finalThumb.startsWith("file:")) {
                    artistDao.insertArtist(ArtistEntity(name = artist, thumbnailUrl = finalThumb))
                }
            }
            if (!finalThumb.isNullOrBlank()) {
                val albumId = "${artist}_$effectiveAlbum"
                val existingAlbum = albumDao.getAlbumSync(albumId)
                if (existingAlbum == null || existingAlbum.thumbnailUrl.isNullOrBlank() || finalThumb.startsWith("file:")) {
                    albumDao.insertAlbum(AlbumEntity(id = albumId, title = effectiveAlbum, artistName = artist, thumbnailUrl = finalThumb))
                }
            }

            Result.success(workDataOf("localPath" to outputFile.absolutePath))
        } catch (e: Exception) {
            Log.e(TAG, "Excepción durante la descarga de $trackId (intento $runAttemptCount): ${e.message}", e)
            try {
                tempFile?.takeIf { it.exists() }?.delete()
                outputFile?.takeIf { it.exists() && it.length() < MIN_FILE_SIZE_BYTES }?.delete()
            } catch (_: Exception) {}

            if (runAttemptCount < 3) {
                downloadDao.updateStatus(
                    trackId = trackId,
                    status = DownloadStatus.DOWNLOADING,
                    progress = 0,
                    error = "Reintentando descarga (${runAttemptCount + 1}/3): ${e.message}"
                )
                Result.retry()
            } else {
                downloadDao.updateStatus(
                    trackId = trackId,
                    status = DownloadStatus.ERROR,
                    error = e.message ?: "Fallo al descargar tras varios intentos"
                )
                Result.failure()
            }
        }
    }
}
