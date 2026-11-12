package com.fr33music.core.data

import android.content.Context
import android.util.Log
import com.fr33music.core.domain.model.AudioFormat
import com.fr33music.core.domain.model.Track
import com.fr33music.core.extractor.NewPipeDownloader
import com.fr33music.core.extractor.StreamResolver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioCacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val streamResolver: StreamResolver
) {
    companion object {
        private const val TAG = "AudioCacheManager"
        const val MIN_FILE_SIZE_BYTES = 100 * 1024L // 100 KB mínimo para considerar archivo válido
        private const val MAX_CACHED_TRACKS = 2 // Canción actual y la anterior
    }

    private val downloadsDir = File(context.filesDir, "downloads")
    private val cacheDir = File(context.cacheDir, "audio_cache")

    // Registro en memoria de las pistas cacheadas recientes (FIFO / LRU de tamaño máx. 2)
    private val cachedTrackHistory = ArrayDeque<String>()

    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        cleanupTempFiles()
    }

    /**
     * Busca si el archivo de audio ya existe de forma permanente en filesDir/downloads/ (.opus o .m4a)
     */
    fun getPermanentFile(trackId: String): File? {
        val opus = File(downloadsDir, "$trackId.opus")
        if (opus.exists() && opus.length() >= MIN_FILE_SIZE_BYTES) return opus

        val m4a = File(downloadsDir, "$trackId.m4a")
        if (m4a.exists() && m4a.length() >= MIN_FILE_SIZE_BYTES) return m4a

        return null
    }

    /**
     * Busca si el archivo de audio ya existe en la caché temporal cacheDir/audio_cache/ (.opus o .m4a)
     */
    fun getCachedFile(trackId: String): File? {
        val opus = File(cacheDir, "$trackId.opus")
        if (opus.exists() && opus.length() >= MIN_FILE_SIZE_BYTES) return opus

        val m4a = File(cacheDir, "$trackId.m4a")
        if (m4a.exists() && m4a.length() >= MIN_FILE_SIZE_BYTES) return m4a

        return null
    }

    /**
     * Retorna el archivo de audio si existe de forma permanente o en la caché temporal.
     */
    fun findExistingAudioFile(trackId: String): File? {
        return getPermanentFile(trackId) ?: getCachedFile(trackId)
    }

    /**
     * Descarga la pista indicada a cacheDir/audio_cache/ usando OkHttp y resolución de stream.
     * Si la descarga tiene éxito, devuelve el File descargado y actualiza la política de rotación (máx. 2 pistas).
     * Si falla, devuelve Result.failure con la causa para fallback a streaming online directo.
     */
    suspend fun cacheAudio(track: Track): Result<File> = withContext(Dispatchers.IO) {
        try {
            // Si ya existe en permanente, devolverlo de inmediato
            getPermanentFile(track.trackId)?.let {
                return@withContext Result.success(it)
            }

            // Si ya existe en caché, actualizar rotación y devolverlo
            getCachedFile(track.trackId)?.let {
                recordCachedTrack(track.trackId)
                return@withContext Result.success(it)
            }

            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            // Resolver stream de audio
            Log.d(TAG, "Resolviendo stream para auto-caché: ${track.trackId} - ${track.title}")
            val streamResult = streamResolver.resolveAudio(track)
            val stream = streamResult.getOrElse { error ->
                Log.w(TAG, "Fallo al resolver stream para auto-caché: ${error.message}")
                return@withContext Result.failure(error)
            }

            val isOpus = stream.format == AudioFormat.OPUS
            val extension = if (isOpus) "opus" else "m4a"

            val tempFile = File(cacheDir, "${track.trackId}.tmp")
            val targetFile = File(cacheDir, "${track.trackId}.$extension")

            if (tempFile.exists()) tempFile.delete()

            Log.i(TAG, "Iniciando descarga a caché: ${track.trackId} ($extension) desde ${stream.url}")

            val userAgent = stream.headers["User-Agent"] ?: NewPipeDownloader.USER_AGENT
            val chunkSize = 1024 * 1024L // 1 MB por bloque
            var downloadedBytes = 0L
            var totalBytes = -1L

            // Primer chunk con Range
            val firstReqBuilder = Request.Builder()
                .url(stream.url)
                .header("User-Agent", userAgent)
                .header("Range", "bytes=0-${chunkSize - 1}")

            stream.headers.forEach { (k, v) ->
                if (!k.equals("Range", ignoreCase = true) && !k.equals("User-Agent", ignoreCase = true)) {
                    firstReqBuilder.header(k, v)
                }
            }

            val firstResponse = okHttpClient.newCall(firstReqBuilder.build()).execute()
            if (!firstResponse.isSuccessful || firstResponse.body == null) {
                firstResponse.close()
                throw IOException("HTTP error al descargar a caché: ${firstResponse.code} ${firstResponse.message}")
            }

            val contentType = firstResponse.header("Content-Type")?.lowercase() ?: ""
            if (contentType.contains("text/html") || contentType.contains("text/plain")) {
                firstResponse.close()
                throw IOException("Respuesta HTML recibida en lugar de audio: $contentType")
            }

            if (firstResponse.code == 206) {
                val contentRange = firstResponse.header("Content-Range") ?: ""
                totalBytes = contentRange.substringAfterLast("/", "-1").toLongOrNull() ?: -1L

                FileOutputStream(tempFile).use { fileOut ->
                    firstResponse.body!!.byteStream().use { input ->
                        val buf = ByteArray(32 * 1024)
                        var read: Int
                        while (input.read(buf).also { read = it } != -1) {
                            if (!currentCoroutineContext().isActive) {
                                throw IOException("Descarga de auto-caché cancelada")
                            }
                            fileOut.write(buf, 0, read)
                            downloadedBytes += read
                        }
                    }
                    fileOut.flush()

                    var currentStart = downloadedBytes
                    while (totalBytes > 0 && currentStart < totalBytes && currentCoroutineContext().isActive) {
                        val currentEnd = minOf(currentStart + chunkSize - 1, totalBytes - 1)
                        val chunkReq = Request.Builder()
                            .url(stream.url)
                            .header("User-Agent", userAgent)
                            .header("Range", "bytes=$currentStart-$currentEnd")

                        stream.headers.forEach { (k, v) ->
                            if (!k.equals("Range", ignoreCase = true) && !k.equals("User-Agent", ignoreCase = true)) {
                                chunkReq.header(k, v)
                            }
                        }

                        val chunkRes = okHttpClient.newCall(chunkReq.build()).execute()
                        if (!chunkRes.isSuccessful || chunkRes.body == null) {
                            chunkRes.close()
                            throw IOException("Error en chunk de caché $currentStart-$currentEnd: ${chunkRes.code}")
                        }

                        chunkRes.body!!.byteStream().use { input ->
                            val buf = ByteArray(32 * 1024)
                            var read: Int
                            while (input.read(buf).also { read = it } != -1) {
                                if (!currentCoroutineContext().isActive) {
                                    throw IOException("Descarga de auto-caché cancelada")
                                }
                                fileOut.write(buf, 0, read)
                                downloadedBytes += read
                            }
                        }
                        fileOut.flush()
                        currentStart = downloadedBytes
                    }
                    fileOut.flush()
                    fileOut.fd.sync()
                }
            } else {
                // Servidor no soporta Range: lectura continua del stream completo
                firstResponse.body!!.use { body ->
                    FileOutputStream(tempFile).use { fileOut ->
                        val input = body.byteStream()
                        val buf = ByteArray(64 * 1024)
                        var read: Int
                        while (input.read(buf).also { read = it } != -1) {
                            if (!currentCoroutineContext().isActive) {
                                throw IOException("Descarga de auto-caché cancelada")
                            }
                            fileOut.write(buf, 0, read)
                            downloadedBytes += read
                        }
                        fileOut.flush()
                        fileOut.fd.sync()
                    }
                }
            }

            if (!tempFile.exists() || tempFile.length() < MIN_FILE_SIZE_BYTES) {
                tempFile.delete()
                throw IOException("Archivo de auto-caché incompleto o corrupto (${tempFile.length()} bytes)")
            }

            if (targetFile.exists()) {
                targetFile.delete()
            }
            val renamed = tempFile.renameTo(targetFile)
            if (!renamed && !targetFile.exists()) {
                throw IOException("No se pudo renombrar ${tempFile.name} a ${targetFile.name}")
            }

            Log.i(TAG, "Audio guardado con éxito en caché: ${targetFile.absolutePath} (${targetFile.length()} bytes)")

            // Mantener como máximo 2 canciones en caché (la actual y la anterior)
            recordCachedTrack(track.trackId)

            Result.success(targetFile)
        } catch (e: Exception) {
            Log.w(TAG, "Error durante la descarga a caché de ${track.trackId}: ${e.message}")
            try {
                File(cacheDir, "${track.trackId}.tmp").takeIf { it.exists() }?.delete()
            } catch (_: Exception) {}
            Result.failure(e)
        }
    }

    /**
     * Si el usuario presiona "Descargar" mientras la canción está en caché temporal,
     * mueve el archivo a filesDir/downloads/ para persistencia offline permanente.
     */
    fun promoteCachedToDownload(trackId: String): File? {
        val cached = getCachedFile(trackId) ?: return null
        if (!downloadsDir.exists()) downloadsDir.mkdirs()

        val destFile = File(downloadsDir, cached.name)
        if (destFile.exists()) {
            destFile.delete()
        }

        val moved = cached.renameTo(destFile)
        return if (moved || destFile.exists()) {
            synchronized(cachedTrackHistory) {
                cachedTrackHistory.remove(trackId)
            }
            Log.i(TAG, "Pista $trackId promovida exitosamente de caché a descarga permanente: ${destFile.absolutePath}")
            destFile
        } else {
            Log.e(TAG, "Fallo al mover archivo de caché $cached a destino $destFile")
            null
        }
    }

    /**
     * Registra una pista como activa en caché y elimina la más antigua si supera el límite de 2.
     */
    private fun recordCachedTrack(trackId: String) {
        synchronized(cachedTrackHistory) {
            cachedTrackHistory.remove(trackId)
            cachedTrackHistory.addLast(trackId)

            while (cachedTrackHistory.size > MAX_CACHED_TRACKS) {
                val oldestTrackId = cachedTrackHistory.removeFirst()
                deleteCachedFile(oldestTrackId)
            }
        }
    }

    private fun deleteCachedFile(trackId: String) {
        try {
            val opus = File(cacheDir, "$trackId.opus")
            if (opus.exists()) {
                opus.delete()
                Log.d(TAG, "Caché rotada: archivo eliminado ${opus.name}")
            }
            val m4a = File(cacheDir, "$trackId.m4a")
            if (m4a.exists()) {
                m4a.delete()
                Log.d(TAG, "Caché rotada: archivo eliminado ${m4a.name}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error eliminando archivo antiguo de caché ($trackId): ${e.message}")
        }
    }

    private fun cleanupTempFiles() {
        try {
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.endsWith(".tmp", ignoreCase = true)) {
                    file.delete()
                }
            }
        } catch (_: Exception) {}
    }

    fun clearAudioCache() {
        try {
            synchronized(cachedTrackHistory) {
                cachedTrackHistory.clear()
            }
            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile) file.delete()
            }
            Log.i(TAG, "Caché de audio temporal limpiada completamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al limpiar caché de audio: ${e.message}")
        }
    }
}
