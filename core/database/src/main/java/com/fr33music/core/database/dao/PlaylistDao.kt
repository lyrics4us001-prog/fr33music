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
        SELECT p.id, p.name, p.createdAt, p.thumbnailUrl, 
               (SELECT COUNT(*) FROM playlist_songs ps WHERE ps.playlistId = p.id) as songCount 
        FROM playlists p 
        ORDER BY p.createdAt DESC
    """)
    fun getAllPlaylists(): Flow<List<PlaylistWithSongCount>>

    @Query("""
        SELECT p.id, p.name, p.createdAt, p.thumbnailUrl, 
               (SELECT COUNT(*) FROM playlist_songs ps WHERE ps.playlistId = p.id) as songCount 
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

    @Query("UPDATE playlists SET thumbnailUrl = :thumbnailUrl WHERE id = :playlistId")
    suspend fun updatePlaylistThumbnail(playlistId: Long, thumbnailUrl: String)
}
