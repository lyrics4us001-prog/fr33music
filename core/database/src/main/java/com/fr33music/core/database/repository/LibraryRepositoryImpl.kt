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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

    override suspend fun toggleSave(track: Track, isCurrentlySaved: Boolean) {
        withContext(Dispatchers.IO) {
            if (isCurrentlySaved) {
                Log.d(TAG, "Quitando ${track.trackId} de favoritos")
                val existing = trackDao.getTrackSync(track.trackId)
                if (existing != null) {
                    if (existing.isDownloaded) {
                        trackDao.setSaved(track.trackId, false)
                    } else {
                        trackDao.deleteTrack(track.trackId)
                    }
                }
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
                trackDao.setSaved(track.trackId, true)
                if (existing.thumbnailUrl.isNullOrBlank() && !track.thumbnailUrl.isNullOrBlank()) {
                    trackDao.updateTrack(existing.copy(thumbnailUrl = track.thumbnailUrl, isSaved = true))
                }
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
            val thumb = if (localCoverFile.exists() && localCoverFile.length() > 0) {
                android.net.Uri.fromFile(localCoverFile).toString()
            } else {
                track.thumbnailUrl
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
                if (track.isSaved) {
                    trackDao.setDownloaded(trackId, isDownloaded = false, localPath = null, format = null)
                } else {
                    trackDao.deleteTrack(trackId)
                }
            }
        }
    }

    override suspend fun remove(trackId: String) {
        deleteDownload(trackId)
        withContext(Dispatchers.IO) {
            trackDao.deleteTrack(trackId)
            Log.i(TAG, "Canción $trackId eliminada por completo.")
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

    override suspend fun deletePlaylist(id: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylistSongs(id)
        playlistDao.deletePlaylist(id)
    }

    override suspend fun addSongToPlaylist(playlistId: Long, track: Track) = withContext(Dispatchers.IO) {
        // Asegurar que la pista existe en Room para que el JOIN funcione
        val existing = trackDao.getTrackSync(track.trackId)
        if (existing == null) {
            trackDao.insertTrack(TrackEntity.fromDomain(track, isSaved = false))
        }

        playlistDao.insertSongToPlaylist(PlaylistSongCrossRef(playlistId = playlistId, trackId = track.trackId))

        // Si la playlist no tiene miniatura, actualizar con la de esta canción
        val playlist = playlistDao.getPlaylistSync(playlistId)
        val trackThumb = track.thumbnailUrl
        if (playlist != null && playlist.thumbnailUrl.isNullOrBlank() && !trackThumb.isNullOrBlank()) {
            playlistDao.updatePlaylistThumbnail(playlistId, trackThumb)
        }
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, trackId: String) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId, trackId)
    }
}
