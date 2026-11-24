package com.fr33music.core.database.repository

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import com.fr33music.core.database.dao.AlbumDao
import com.fr33music.core.database.dao.ArtistDao
import com.fr33music.core.database.dao.DownloadDao
import com.fr33music.core.database.dao.PlaylistDao
import com.fr33music.core.database.dao.PlaylistWithSongCount
import com.fr33music.core.database.dao.TrackDao
import com.fr33music.core.database.entity.AlbumEntity
import com.fr33music.core.database.entity.ArtistEntity
import com.fr33music.core.database.entity.PlaylistEntity
import com.fr33music.core.database.entity.PlaylistSongCrossRef
import com.fr33music.core.database.entity.TrackEntity
import com.fr33music.core.domain.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackDao: TrackDao,
    private val artistDao: ArtistDao,
    private val albumDao: AlbumDao,
    private val downloadDao: DownloadDao,
    private val playlistDao: PlaylistDao
) : LibraryRepository {

    companion object {
        private const val TAG = "LibraryRepository"
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                playlistDao.restoreMissingPlaylistTracks()
                trackDao.sanitizeLegacyTrackTitles()
            } catch (e: Exception) {
                Log.w(TAG, "Error restaurando pistas huérfanas de playlist: ${e.message}")
            }
        }
    }

    override suspend fun removeFromFavorites(trackId: String) = withContext(Dispatchers.IO) {
        Log.d(TAG, "Quitando $trackId de favoritos")
        val existing = trackDao.getTrackSync(trackId)
        if (existing != null) {
            val inPlaylist = playlistDao.isTrackInAnyPlaylist(trackId)
            if (existing.isDownloaded || inPlaylist) {
                trackDao.setSaved(trackId, false)
            } else {
                trackDao.deleteTrack(trackId)
            }
        }
    }

    override suspend fun toggleSave(track: Track, isCurrentlySaved: Boolean) {
        withContext(Dispatchers.IO) {
            if (isCurrentlySaved) {
                removeFromFavorites(track.trackId)
            } else {
                Log.d(TAG, "Guardando ${track.trackId} en favoritos")
                save(track)
            }
        }
    }

    override suspend fun save(track: Track) {
        withContext(Dispatchers.IO) {
            val existing = trackDao.getTrackSync(track.trackId)
            if (existing != null) {
                val newTitle = if (track.title.isNotBlank() && !track.title.startsWith("Pista ") && track.title != "Desconocido") track.title else existing.title
                val newArtist = if (track.artistName.isNotBlank() && track.artistName != "Desconocido" && track.artistName != "Artista Desconocido") track.artistName else existing.artistName
                val newDuration = if (track.durationMs > 0) track.durationMs else existing.durationMs
                val newThumb = if (!track.thumbnailUrl.isNullOrBlank()) track.thumbnailUrl else existing.thumbnailUrl
                trackDao.updateTrack(
                    existing.copy(
                        title = newTitle,
                        artistName = newArtist,
                        durationMs = newDuration,
                        thumbnailUrl = newThumb,
                        isSaved = true
                    )
                )
            } else {
                trackDao.insertTrack(
                    TrackEntity.fromDomain(track, isSaved = true)
                )
            }

            val artist = track.artistName
            val isRealAlbum = !track.albumName.isNullOrBlank() &&
                    !track.albumName.equals(artist, ignoreCase = true) &&
                    !track.albumName.equals("Sencillos", ignoreCase = true) &&
                    !track.albumName.equals("YouTube", ignoreCase = true)
            val effectiveAlbum: String = if (isRealAlbum) track.albumName!! else "Sencillos"

            val localCoverFile = File(context.filesDir, "downloads/${track.trackId}.jpg")
            val trackThumb = track.thumbnailUrl
            val thumb = if (localCoverFile.exists() && localCoverFile.length() > 0) {
                android.net.Uri.fromFile(localCoverFile).toString()
            } else if (!trackThumb.isNullOrBlank() && !trackThumb.startsWith("file:")) {
                trackThumb
            } else {
                "https://i.ytimg.com/vi/${track.trackId}/hqdefault.jpg"
            }

            if (artist.isNotBlank()) {
                val existingArtist = artistDao.getArtistSync(artist)
                val officialAvatar = track.artistThumbnailUrl
                if (!officialAvatar.isNullOrBlank()) {
                    artistDao.insertArtist(ArtistEntity(name = artist, thumbnailUrl = officialAvatar))
                } else if (existingArtist == null || existingArtist.thumbnailUrl.isNullOrBlank()) {
                    if (!thumb.isNullOrBlank()) {
                        artistDao.insertArtist(ArtistEntity(name = artist, thumbnailUrl = thumb))
                    }
                }
            }
            if (!thumb.isNullOrBlank()) {
                val albumId = "${artist}_$effectiveAlbum"
                val existingAlbum = albumDao.getAlbumSync(albumId)
                if (existingAlbum == null || existingAlbum.thumbnailUrl.isNullOrBlank()) {
                    albumDao.insertAlbum(AlbumEntity(id = albumId, title = effectiveAlbum, artistName = artist, thumbnailUrl = thumb))
                }
            }
        }
    }

    override suspend fun deleteDownload(trackId: String) {
        withContext(Dispatchers.IO) {
            Log.i(TAG, "Eliminando descarga física para trackId: $trackId")

            try {
                WorkManager.getInstance(context).cancelUniqueWork("download_$trackId")
            } catch (e: Exception) {
                Log.w(TAG, "Error cancelando WorkManager: ${e.message}")
            }

            try {
                val downloadsDir = File(context.filesDir, "downloads")
                val opusFile = File(downloadsDir, "$trackId.opus")
                if (opusFile.exists()) {
                    opusFile.delete()
                }

                val m4aFile = File(downloadsDir, "$trackId.m4a")
                if (m4aFile.exists()) {
                    m4aFile.delete()
                }

                File(downloadsDir, "$trackId.tmp").takeIf { it.exists() }?.delete()
                File(downloadsDir, "$trackId.jpg").takeIf { it.exists() }?.delete()
                File(downloadsDir, "$trackId.png").takeIf { it.exists() }?.delete()
                File(downloadsDir, "$trackId.webp").takeIf { it.exists() }?.delete()

                val download = downloadDao.getDownloadSync(trackId)
                download?.localPath?.let { File(it).takeIf { f -> f.exists() }?.delete() }

                val track = trackDao.getTrackSync(trackId)
                track?.localPath?.let { File(it).takeIf { f -> f.exists() }?.delete() }
            } catch (e: Exception) {
                Log.e(TAG, "Error al borrar archivo físico para $trackId: ${e.message}")
            }

            downloadDao.deleteDownload(trackId)

            val track = trackDao.getTrackSync(trackId)
            if (track != null) {
                val inPlaylist = playlistDao.isTrackInAnyPlaylist(trackId)
                if (track.isSaved || inPlaylist) {
                    trackDao.setDownloaded(trackId, isDownloaded = false, localPath = null, format = null)
                } else {
                    trackDao.deleteTrack(trackId)
                }
            }
        }
    }

    override suspend fun deleteArtist(artistName: String): Unit = withContext(Dispatchers.IO) {
        val tracks = trackDao.getTracksByArtistSync(artistName)
        for (track in tracks) {
            deleteDownload(track.id)
            val inPlaylist = playlistDao.isTrackInAnyPlaylist(track.id)
            if (inPlaylist) {
                trackDao.setSaved(track.id, false)
            } else {
                trackDao.deleteTrack(track.id)
            }
        }
        artistDao.deleteArtist(artistName)
        Log.i(TAG, "Artista $artistName y sus canciones asociadas han sido eliminados de la biblioteca.")
    }

    override suspend fun renameArtist(oldName: String, newName: String): Unit = withContext(Dispatchers.IO) {
        val cleanOld = oldName.trim()
        val cleanNew = newName.trim()
        if (cleanOld.isEmpty() || cleanNew.isEmpty() || cleanOld.equals(cleanNew, ignoreCase = true)) return@withContext

        trackDao.updateArtistName(cleanOld, cleanNew)
        val oldEntity = artistDao.getArtistSync(cleanOld)
        artistDao.deleteArtist(cleanOld)
        val existingNew = artistDao.getArtistSync(cleanNew)
        val thumb = existingNew?.thumbnailUrl ?: oldEntity?.thumbnailUrl
        artistDao.insertArtist(ArtistEntity(name = cleanNew, thumbnailUrl = thumb))

        // Renombrar archivo de portada local si existe
        val coversDir = File(context.filesDir, "artist_covers")
        val oldSafe = cleanOld.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val newSafe = cleanNew.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val oldFile = File(coversDir, "$oldSafe.jpg")
        val newFile = File(coversDir, "$newSafe.jpg")
        if (oldFile.exists()) {
            oldFile.renameTo(newFile)
        }
        Log.i(TAG, "Artista renombrado de '$cleanOld' a '$cleanNew'")
    }

    override suspend fun mergeArtists(sourceArtist: String, targetArtist: String): Unit = withContext(Dispatchers.IO) {
        val cleanSource = sourceArtist.trim()
        val cleanTarget = targetArtist.trim()
        if (cleanSource.isEmpty() || cleanTarget.isEmpty() || cleanSource.equals(cleanTarget, ignoreCase = true)) return@withContext

        trackDao.updateArtistName(cleanSource, cleanTarget)
        artistDao.deleteArtist(cleanSource)

        val coversDir = File(context.filesDir, "artist_covers")
        val sourceSafe = cleanSource.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val sourceFile = File(coversDir, "$sourceSafe.jpg")
        if (sourceFile.exists()) {
            sourceFile.delete()
        }
        Log.i(TAG, "Artista '$cleanSource' combinado con '$cleanTarget'")
    }

    override suspend fun updateArtistThumbnail(artistName: String, thumbnailUrl: String): Unit = withContext(Dispatchers.IO) {
        val cleanArtist = artistName.trim()
        if (cleanArtist.isEmpty()) return@withContext
        artistDao.insertArtist(ArtistEntity(name = cleanArtist, thumbnailUrl = thumbnailUrl))
        Log.i(TAG, "Portada de artista '$cleanArtist' actualizada a $thumbnailUrl")
    }

    override suspend fun updatePlaylistThumbnail(playlistId: Long, thumbnailUrl: String): Unit = withContext(Dispatchers.IO) {
        playlistDao.updatePlaylistThumbnail(playlistId, thumbnailUrl)
        Log.i(TAG, "Portada de playlist $playlistId actualizada a $thumbnailUrl")
    }

    override suspend fun remove(trackId: String) {
        deleteDownload(trackId)
        withContext(Dispatchers.IO) {
            trackDao.deleteTrack(trackId)
            Log.i(TAG, "Canción $trackId eliminada por completo.")
        }
    }

    override suspend fun updateTrackMetadata(trackId: String, title: String, artistName: String) = withContext(Dispatchers.IO) {
        val cleanTitle = title.trim()
        val cleanArtist = artistName.trim()
        val existing = trackDao.getTrackSync(trackId)
        if (existing != null) {
            trackDao.updateTrackMetadata(trackId, cleanTitle, cleanArtist)
            if (cleanArtist.isNotBlank()) {
                val existingArtist = artistDao.getArtistSync(cleanArtist)
                if (existingArtist == null) {
                    artistDao.insertArtist(ArtistEntity(name = cleanArtist, thumbnailUrl = existing.thumbnailUrl))
                }
            }
            Log.i(TAG, "Pista $trackId actualizada: '$cleanTitle' - '$cleanArtist'")
        }
    }

    // Playlists
    override fun getPlaylists(): Flow<List<PlaylistWithSongCount>> = playlistDao.getAllPlaylists()

    override fun getSongsForPlaylist(playlistId: Long): Flow<List<TrackEntity>> =
        playlistDao.getSongsForPlaylist(playlistId)

    override suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        val trimmed = name.trim().ifEmpty { "Mi Playlist" }
        playlistDao.insertPlaylist(PlaylistEntity(name = trimmed))
    }

    override suspend fun renamePlaylist(playlistId: Long, newName: String) = withContext(Dispatchers.IO) {
        val trimmed = newName.trim()
        if (trimmed.isNotEmpty()) {
            playlistDao.renamePlaylist(playlistId, trimmed)
        }
    }

    override suspend fun deletePlaylist(id: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylistSongs(id)
        playlistDao.deletePlaylist(id)
    }

    override suspend fun addSongToPlaylist(playlistId: Long, track: Track) = withContext(Dispatchers.IO) {
        // Asegurar que la pista existe en Room para que el JOIN funcione y actualizar metadatos si ya existía
        val existing = trackDao.getTrackSync(track.trackId)
        if (existing == null) {
            trackDao.insertTrack(TrackEntity.fromDomain(track, isSaved = false))
        } else {
            val newTitle = if (track.title.isNotBlank() && !track.title.startsWith("Pista ") && track.title != "Desconocido") track.title else existing.title
            val newArtist = if (track.artistName.isNotBlank() && track.artistName != "Desconocido" && track.artistName != "Artista Desconocido") track.artistName else existing.artistName
            val newDuration = if (track.durationMs > 0) track.durationMs else existing.durationMs
            val newThumb = if (!track.thumbnailUrl.isNullOrBlank()) track.thumbnailUrl else existing.thumbnailUrl
            trackDao.updateTrack(
                existing.copy(
                    title = newTitle,
                    artistName = newArtist,
                    durationMs = newDuration,
                    thumbnailUrl = newThumb
                )
            )
        }

        playlistDao.insertSongToPlaylist(PlaylistSongCrossRef(playlistId = playlistId, trackId = track.trackId))

        // Si la playlist no tiene miniatura, actualizar con la de esta canción
        val playlist = playlistDao.getPlaylistSync(playlistId)
        val trackThumb = track.thumbnailUrl
        if (playlist != null && playlist.thumbnailUrl.isNullOrBlank() && !trackThumb.isNullOrBlank()) {
            playlistDao.updatePlaylistThumbnail(playlistId, trackThumb)
        }
    }

    override suspend fun addSongsToPlaylist(playlistId: Long, tracks: List<Track>) = withContext(Dispatchers.IO) {
        if (tracks.isEmpty()) return@withContext
        for (track in tracks) {
            val existing = trackDao.getTrackSync(track.trackId)
            if (existing == null) {
                trackDao.insertTrack(TrackEntity.fromDomain(track, isSaved = false))
            } else {
                val newTitle = if (track.title.isNotBlank() && !track.title.startsWith("Pista ") && track.title != "Desconocido") track.title else existing.title
                val newArtist = if (track.artistName.isNotBlank() && track.artistName != "Desconocido" && track.artistName != "Artista Desconocido") track.artistName else existing.artistName
                val newDuration = if (track.durationMs > 0) track.durationMs else existing.durationMs
                val newThumb = if (!track.thumbnailUrl.isNullOrBlank()) track.thumbnailUrl else existing.thumbnailUrl
                trackDao.updateTrack(
                    existing.copy(
                        title = newTitle,
                        artistName = newArtist,
                        durationMs = newDuration,
                        thumbnailUrl = newThumb
                    )
                )
            }
        }

        val crossRefs = tracks.map {
            PlaylistSongCrossRef(playlistId = playlistId, trackId = it.trackId)
        }
        playlistDao.insertSongsToPlaylist(crossRefs)

        val playlist = playlistDao.getPlaylistSync(playlistId)
        val firstThumb = tracks.firstOrNull { !it.thumbnailUrl.isNullOrBlank() }?.thumbnailUrl
        if (playlist != null && playlist.thumbnailUrl.isNullOrBlank() && !firstThumb.isNullOrBlank()) {
            playlistDao.updatePlaylistThumbnail(playlistId, firstThumb)
        }
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, trackId: String) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId, trackId)
        val track = trackDao.getTrackSync(trackId)
        if (track != null && !track.isSaved && !track.isDownloaded) {
            val inOtherPlaylist = playlistDao.isTrackInAnyPlaylist(trackId)
            if (!inOtherPlaylist) {
                trackDao.deleteTrack(trackId)
            }
        }
    }
}
