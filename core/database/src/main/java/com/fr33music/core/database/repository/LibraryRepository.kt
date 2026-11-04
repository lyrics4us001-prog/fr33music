package com.fr33music.core.database.repository

import com.fr33music.core.database.dao.PlaylistWithSongCount
import com.fr33music.core.database.entity.TrackEntity
import com.fr33music.core.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    suspend fun toggleSave(track: Track, isCurrentlySaved: Boolean)
    suspend fun save(track: Track)
    suspend fun deleteDownload(trackId: String)
    suspend fun remove(trackId: String)

    // Playlists
    fun getPlaylists(): Flow<List<PlaylistWithSongCount>>
    fun getSongsForPlaylist(playlistId: Long): Flow<List<TrackEntity>>
    suspend fun createPlaylist(name: String): Long
    suspend fun deletePlaylist(id: Long)
    suspend fun addSongToPlaylist(playlistId: Long, track: Track)
    suspend fun removeSongFromPlaylist(playlistId: Long, trackId: String)
}
