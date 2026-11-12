package com.fr33music.core.database.download

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.fr33music.core.data.AudioCacheManager
import com.fr33music.core.database.dao.AlbumDao
import com.fr33music.core.database.dao.ArtistDao
import com.fr33music.core.database.dao.DownloadDao
import com.fr33music.core.database.dao.TrackDao
import com.fr33music.core.database.entity.AlbumEntity
import com.fr33music.core.database.entity.ArtistEntity
import com.fr33music.core.database.entity.DownloadEntity
import com.fr33music.core.database.entity.DownloadStatus
import com.fr33music.core.database.entity.TrackEntity
import com.fr33music.core.domain.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadDao: DownloadDao,
    private val trackDao: TrackDao,
    private val artistDao: ArtistDao,
    private val albumDao: AlbumDao,
    private val audioCacheManager: AudioCacheManager
) : DownloadRepository {

    private val workManager = WorkManager.getInstance(context)
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "Descarga"
        private const val MIN_FILE_SIZE_BYTES = 100 * 1024L
    }

    init {
        // Sincronizar archivos existentes en filesDir/downloads al iniciar
        managerScope.launch {
            syncDownloadedFiles()
        }
    }

    override fun getAllDownloads(): Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()

    override fun getDownload(trackId: String): Flow<DownloadEntity?> = downloadDao.getDownload(trackId)

    override fun getDownloadState(trackId: String): Flow<DownloadState> {
        return downloadDao.getDownload(trackId)
            .map { entity ->
                // Verificar si el archivo ya existe en almacenamiento local y es válido (>= 50KB)
                val localFile = findLocalDownloadedFile(trackId)
                if (localFile != null && localFile.length() >= MIN_FILE_SIZE_BYTES) {
                    if (entity?.status != DownloadStatus.COMPLETED) {
                        managerScope.launch {
                            downloadDao.updateStatus(
                                trackId = trackId,
                                status = DownloadStatus.COMPLETED,
                                localPath = localFile.absolutePath,
                                progress = 100
                            )
                            trackDao.setDownloaded(
                                id = trackId,
                                isDownloaded = true,
                                localPath = localFile.absolutePath,
                                format = if (localFile.name.endsWith(".opus", true)) "OPUS" else "M4A"
                            )
                        }
                    }
                    return@map DownloadState.Completed(localFile.absolutePath)
                }

                when (entity?.status) {
                    DownloadStatus.DOWNLOADING -> DownloadState.Downloading(entity.progress)
                    DownloadStatus.QUEUED -> DownloadState.Queued
                    DownloadStatus.COMPLETED -> {
                        if (!entity.localPath.isNullOrBlank() && File(entity.localPath).exists() && File(entity.localPath).length() >= MIN_FILE_SIZE_BYTES) {
                            DownloadState.Completed(entity.localPath)
                        } else {
                            DownloadState.Idle
                        }
                    }
                    DownloadStatus.ERROR -> DownloadState.Error(entity.errorMessage ?: "Error al descargar")
                    else -> DownloadState.Idle
                }
            }
            .distinctUntilChanged()
    }

    override suspend fun syncDownloadedFiles() {
        try {
            val downloadsDir = File(context.filesDir, "downloads")
            if (!downloadsDir.exists()) return

            val files = downloadsDir.listFiles() ?: return
            for (file in files) {
                if (file.isFile) {
                    if (file.name.endsWith(".tmp", ignoreCase = true)) {
                        file.delete()
                        continue
                    }
                    // Miniaturas e imágenes locales: conservar siempre intactas
                    if (file.name.endsWith(".jpg", ignoreCase = true) ||
                        file.name.endsWith(".jpeg", ignoreCase = true) ||
                        file.name.endsWith(".png", ignoreCase = true)
                    ) {
                        continue
                    }

                    // Solo procesar archivos de audio válidos (.opus o .m4a)
                    val isOpus = file.name.endsWith(".opus", ignoreCase = true)
                    val isM4a = file.name.endsWith(".m4a", ignoreCase = true)
                    if (!isOpus && !isM4a) {
                        continue
                    }

                    if (file.length() >= MIN_FILE_SIZE_BYTES) {
                        val trackId = file.nameWithoutExtension
                        val format = if (isOpus) "OPUS" else "M4A"

                        trackDao.setDownloaded(
                            id = trackId,
                            isDownloaded = true,
                            localPath = file.absolutePath,
                            format = format
                        )
                        downloadDao.updateStatus(
                            trackId = trackId,
                            status = DownloadStatus.COMPLETED,
                            localPath = file.absolutePath,
                            progress = 100
                        )
                        Log.d(TAG, "Sincronizado archivo de audio existente: ${file.name} (${file.length()} bytes)")
                    } else {
                        // Limpiar archivos de audio incompletos
                        Log.w(TAG, "Eliminando archivo de audio incompleto: ${file.name} (${file.length()} bytes)")
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sincronizando archivos de descarga: ${e.message}")
        }
    }

    override suspend fun download(track: Track) {
        // 1. Verificar si la pista ya está registrada como COMPLETED en Room y existe en disco
        val existingDownload = downloadDao.getDownloadSync(track.trackId)
        val localFile = findLocalDownloadedFile(track.trackId)
        val fileValid = localFile != null && localFile.length() >= MIN_FILE_SIZE_BYTES

        if (existingDownload?.status == DownloadStatus.COMPLETED && fileValid) {
            Log.d(TAG, "La canción ${track.trackId} ya está descargada y completada. Omitiendo encolar en WorkManager.")
            return
        }

        if (fileValid) {
            Log.d(TAG, "Archivo ya existe en disco para ${track.trackId}: ${localFile!!.absolutePath}")
            downloadDao.updateStatus(
                trackId = track.trackId,
                status = DownloadStatus.COMPLETED,
                localPath = localFile.absolutePath,
                progress = 100
            )
            trackDao.setDownloaded(
                id = track.trackId,
                isDownloaded = true,
                localPath = localFile.absolutePath,
                format = if (localFile.name.endsWith(".opus", true)) "OPUS" else "M4A"
            )
            return
        }

        // Si el archivo ya existe en caché temporal, mover a descargas permanentes directamente
        val promotedFile = audioCacheManager.promoteCachedToDownload(track.trackId)
        if (promotedFile != null && promotedFile.length() >= MIN_FILE_SIZE_BYTES) {
            Log.i(TAG, "Canción ${track.trackId} encontrada en caché temporal y promovida a descargas permanentes.")
            val format = if (promotedFile.name.endsWith(".opus", true)) "OPUS" else "M4A"

            val existingTrack = trackDao.getTrackSync(track.trackId)
            if (existingTrack == null) {
                trackDao.insertTrack(
                    TrackEntity(
                        id = track.trackId,
                        title = track.title,
                        artistName = track.artistName,
                        albumName = track.albumName,
                        durationMs = track.durationMs,
                        thumbnailUrl = track.thumbnailUrl,
                        isDownloaded = true,
                        localPath = promotedFile.absolutePath,
                        format = format,
                        isSaved = false
                    )
                )
            } else {
                trackDao.setDownloaded(
                    id = track.trackId,
                    isDownloaded = true,
                    localPath = promotedFile.absolutePath,
                    format = format
                )
            }

            downloadDao.insertDownload(
                DownloadEntity(
                    trackId = track.trackId,
                    title = track.title,
                    artistName = track.artistName,
                    thumbnailUrl = track.thumbnailUrl,
                    status = DownloadStatus.COMPLETED,
                    localPath = promotedFile.absolutePath,
                    progress = 100
                )
            )
            return
        }

        // 2. Registrar o actualizar metadatos en TrackDao
        val existingTrack = trackDao.getTrackSync(track.trackId)
        if (existingTrack == null) {
            trackDao.insertTrack(
                TrackEntity(
                    id = track.trackId,
                    title = track.title,
                    artistName = track.artistName,
                    albumName = track.albumName,
                    durationMs = track.durationMs,
                    thumbnailUrl = track.thumbnailUrl,
                    isDownloaded = false,
                    isSaved = false
                )
            )
        } else if (existingTrack.thumbnailUrl.isNullOrBlank() && !track.thumbnailUrl.isNullOrBlank()) {
            trackDao.updateTrack(existingTrack.copy(thumbnailUrl = track.thumbnailUrl))
        }

        // Upsert en ArtistEntity y AlbumEntity si tienen thumbnail
        val artist = track.artistName
        val thumb = track.thumbnailUrl
        val album = track.albumName

        if (artist.isNotBlank() && !thumb.isNullOrBlank()) {
            val existingArtist = artistDao.getArtistSync(artist)
            if (existingArtist == null || existingArtist.thumbnailUrl.isNullOrBlank()) {
                artistDao.insertArtist(
                    ArtistEntity(
                        name = artist,
                        thumbnailUrl = thumb
                    )
                )
            }
        }
        if (!album.isNullOrBlank() && !thumb.isNullOrBlank()) {
            val albumId = "${artist}_$album"
            val existingAlbum = albumDao.getAlbumSync(albumId)
            if (existingAlbum == null || existingAlbum.thumbnailUrl.isNullOrBlank()) {
                albumDao.insertAlbum(
                    AlbumEntity(
                        id = albumId,
                        title = album,
                        artistName = artist,
                        thumbnailUrl = thumb
                    )
                )
            }
        }

        // Registrar en DownloadDao con estado inicial QUEUED
        downloadDao.insertDownload(
            DownloadEntity(
                trackId = track.trackId,
                title = track.title,
                artistName = track.artistName,
                thumbnailUrl = track.thumbnailUrl,
                status = DownloadStatus.QUEUED,
                progress = 0
            )
        )
        Log.d(TAG, "Estado inicial: ${DownloadStatus.QUEUED} para ${track.trackId}")

        // Encolar WorkManager con política de reintentos exponenciales y ExistingWorkPolicy.KEEP
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = workDataOf(
            DownloadWorker.KEY_TRACK_ID to track.trackId,
            DownloadWorker.KEY_TITLE to track.title,
            DownloadWorker.KEY_ARTIST to track.artistName,
            DownloadWorker.KEY_DURATION to track.durationMs,
            DownloadWorker.KEY_THUMBNAIL to track.thumbnailUrl,
            DownloadWorker.KEY_ALBUM to track.albumName
        )

        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                5,
                TimeUnit.SECONDS
            )
            .addTag("download_${track.trackId}")
            .build()

        // Usar ExistingWorkPolicy.KEEP para no reiniciar descargas activas o completadas
        workManager.enqueueUniqueWork(
            "download_${track.trackId}",
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    override suspend fun deleteDownload(trackId: String) {
        workManager.cancelUniqueWork("download_$trackId")

        val download = downloadDao.getDownloadSync(trackId)
        val path = download?.localPath ?: trackDao.getTrackSync(trackId)?.localPath
        if (!path.isNullOrBlank()) {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }

        val downloadsDir = File(context.filesDir, "downloads")
        File(downloadsDir, "$trackId.opus").takeIf { it.exists() }?.delete()
        File(downloadsDir, "$trackId.m4a").takeIf { it.exists() }?.delete()

        downloadDao.deleteDownload(trackId)
        val track = trackDao.getTrackSync(trackId)
        if (track != null) {
            if (track.isSaved) {
                trackDao.setDownloaded(trackId, isDownloaded = false, localPath = null, format = null)
            } else {
                trackDao.deleteTrack(trackId)
            }
        }
    }

    override suspend fun clearAllDownloadedAudios() {
        try {
            workManager.cancelAllWork()
        } catch (e: Exception) {
            Log.w(TAG, "Error al cancelar tareas de descarga: ${e.message}")
        }

        try {
            val downloadsDir = File(context.filesDir, "downloads")
            if (downloadsDir.exists()) {
                downloadsDir.listFiles()?.forEach { file ->
                    if (file.isFile) {
                        val name = file.name.lowercase()
                        if (name.endsWith(".opus") || name.endsWith(".m4a") || name.endsWith(".tmp")) {
                            file.delete()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error borrando archivos físicos de audio: ${e.message}")
        }

        downloadDao.deleteAllDownloads()
        trackDao.clearAllDownloadedFlags()
        Log.i(TAG, "Todos los archivos de audio descargados y sus estados han sido limpiados.")
    }

    private fun findLocalDownloadedFile(trackId: String): File? {
        val downloadsDir = File(context.filesDir, "downloads")
        val opus = File(downloadsDir, "$trackId.opus")
        if (opus.exists() && opus.length() >= MIN_FILE_SIZE_BYTES) return opus

        val m4a = File(downloadsDir, "$trackId.m4a")
        if (m4a.exists() && m4a.length() >= MIN_FILE_SIZE_BYTES) return m4a

        return null
    }
}
