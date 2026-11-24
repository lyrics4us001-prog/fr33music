package com.fr33music.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fr33music.core.database.entity.PlaylistEntity
import com.fr33music.core.database.entity.PlaylistSongCrossRef
import com.fr33music.core.database.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

data class PlaylistWithSongCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val thumbnailUrl: String?,
    val songCount: Int
)

@Dao
interface PlaylistDao {

    @Query("""
        SELECT p.id, p.name, p.createdAt, 
               COALESCE(
                   NULLIF(p.thumbnailUrl, ''),
                   (
                       SELECT t.thumbnailUrl FROM tracks t 
                       INNER JOIN playlist_songs ps ON t.id = ps.trackId 
                       WHERE ps.playlistId = p.id AND t.thumbnailUrl IS NOT NULL AND t.thumbnailUrl != '' 
                       ORDER BY ps.addedAt ASC LIMIT 1
                   ),
                   (
                       SELECT 'https://i.ytimg.com/vi/' || t.id || '/hqdefault.jpg' FROM tracks t 
                       INNER JOIN playlist_songs ps ON t.id = ps.trackId 
                       WHERE ps.playlistId = p.id 
                       ORDER BY ps.addedAt ASC LIMIT 1
                   )
               ) as thumbnailUrl, 
               (SELECT COUNT(*) FROM playlist_songs ps INNER JOIN tracks t ON ps.trackId = t.id WHERE ps.playlistId = p.id) as songCount 
        FROM playlists p 
        ORDER BY p.createdAt DESC
    """)
    fun getAllPlaylists(): Flow<List<PlaylistWithSongCount>>

    @Query("""
        SELECT p.id, p.name, p.createdAt, 
               COALESCE(
                   NULLIF(p.thumbnailUrl, ''),
                   (
                       SELECT t.thumbnailUrl FROM tracks t 
                       INNER JOIN playlist_songs ps ON t.id = ps.trackId 
                       WHERE ps.playlistId = p.id AND t.thumbnailUrl IS NOT NULL AND t.thumbnailUrl != '' 
                       ORDER BY ps.addedAt ASC LIMIT 1
                   ),
                   (
                       SELECT 'https://i.ytimg.com/vi/' || t.id || '/hqdefault.jpg' FROM tracks t 
                       INNER JOIN playlist_songs ps ON t.id = ps.trackId 
                       WHERE ps.playlistId = p.id 
                       ORDER BY ps.addedAt ASC LIMIT 1
                   )
               ) as thumbnailUrl, 
               (SELECT COUNT(*) FROM playlist_songs ps INNER JOIN tracks t ON ps.trackId = t.id WHERE ps.playlistId = p.id) as songCount 
        FROM playlists p 
        ORDER BY p.createdAt DESC
    """)
    suspend fun getAllPlaylistsSync(): List<PlaylistWithSongCount>

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    fun getPlaylist(id: Long): Flow<PlaylistEntity?>

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun getPlaylistSync(id: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :id")
    suspend fun deletePlaylistSongs(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongToPlaylist(crossRef: PlaylistSongCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongsToPlaylist(crossRefs: List<PlaylistSongCrossRef>)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeSongFromPlaylist(playlistId: Long, trackId: String)

    @Query("""
        SELECT t.* FROM tracks t
        INNER JOIN playlist_songs ps ON t.id = ps.trackId
        WHERE ps.playlistId = :playlistId
        ORDER BY ps.addedAt ASC
    """)
    fun getSongsForPlaylist(playlistId: Long): Flow<List<TrackEntity>>

    @Query("""
        SELECT t.* FROM tracks t
        INNER JOIN playlist_songs ps ON t.id = ps.trackId
        WHERE ps.playlistId = :playlistId
        ORDER BY ps.addedAt ASC
    """)
    suspend fun getSongsForPlaylistSync(playlistId: Long): List<TrackEntity>

    fun getSongsFromPlaylist(playlistId: Long): Flow<List<TrackEntity>> = getSongsForPlaylist(playlistId)

    suspend fun getSongsFromPlaylistSync(playlistId: Long): List<TrackEntity> = getSongsForPlaylistSync(playlistId)

    @Query("""
        SELECT COUNT(*) FROM playlist_songs ps
        INNER JOIN tracks t ON ps.trackId = t.id
        WHERE ps.playlistId = :playlistId
    """)
    fun getPlaylistCount(playlistId: Long): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM playlist_songs ps
        INNER JOIN tracks t ON ps.trackId = t.id
        WHERE ps.playlistId = :playlistId
    """)
    suspend fun getPlaylistCountSync(playlistId: Long): Int

    @Query("SELECT EXISTS(SELECT 1 FROM playlist_songs WHERE trackId = :trackId)")
    suspend fun isTrackInAnyPlaylist(trackId: String): Boolean

    @Query("""
        INSERT OR IGNORE INTO tracks (id, title, artistName, durationMs, isSaved, isDownloaded, timestamp)
        SELECT DISTINCT ps.trackId, 'Desconocido', 'Artista Desconocido', 0, 0, 0, ps.addedAt
        FROM playlist_songs ps 
        LEFT JOIN tracks t ON ps.trackId = t.id 
        WHERE t.id IS NULL
    """)
    suspend fun restoreMissingPlaylistTracks()

    @Query("UPDATE playlists SET thumbnailUrl = :thumbnailUrl WHERE id = :playlistId")
    suspend fun updatePlaylistThumbnail(playlistId: Long, thumbnailUrl: String)

    @Query("UPDATE playlists SET name = :newName WHERE id = :playlistId")
    suspend fun renamePlaylist(playlistId: Long, newName: String)
}
