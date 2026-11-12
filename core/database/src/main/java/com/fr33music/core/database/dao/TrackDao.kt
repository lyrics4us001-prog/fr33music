package com.fr33music.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fr33music.core.database.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Query("SELECT * FROM tracks WHERE isSaved = 1 ORDER BY timestamp DESC")
    fun getSavedTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE isDownloaded = 1 ORDER BY timestamp DESC")
    fun getDownloadedTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY timestamp DESC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    fun getTrack(id: String): Flow<TrackEntity?>

    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackSync(id: String): TrackEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM tracks WHERE id = :id AND isSaved = 1)")
    fun isTrackSaved(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM tracks WHERE id = :id AND isDownloaded = 1)")
    fun isTrackDownloaded(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Update
    suspend fun updateTrack(track: TrackEntity)

    @Query("UPDATE tracks SET isSaved = :isSaved, timestamp = :timestamp WHERE id = :id")
    suspend fun setSaved(id: String, isSaved: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE tracks SET isDownloaded = :isDownloaded, localPath = :localPath, format = :format WHERE id = :id")
    suspend fun setDownloaded(id: String, isDownloaded: Boolean, localPath: String?, format: String?)

    @Query("UPDATE tracks SET isDownloaded = 0, localPath = NULL, format = NULL")
    suspend fun clearAllDownloadedFlags()

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteTrack(id: String)

    @Query("SELECT * FROM tracks WHERE artistName = :artistName")
    suspend fun getTracksByArtistSync(artistName: String): List<TrackEntity>

    @Query("DELETE FROM tracks WHERE artistName = :artistName")
    suspend fun deleteTracksByArtist(artistName: String)

    @Query("SELECT DISTINCT artistName FROM tracks WHERE isSaved = 1 ORDER BY artistName ASC")
    fun getDistinctArtists(): Flow<List<String>>

    @Query("SELECT * FROM tracks WHERE artistName = :artistName AND isSaved = 1 ORDER BY title ASC")
    fun getTracksByArtist(artistName: String): Flow<List<TrackEntity>>

    @Query("SELECT DISTINCT albumName FROM tracks WHERE albumName IS NOT NULL AND (isSaved = 1 OR isDownloaded = 1) ORDER BY albumName ASC")
    fun getDistinctAlbums(): Flow<List<String>>

    @Query("SELECT * FROM tracks WHERE albumName = :albumName AND (isSaved = 1 OR isDownloaded = 1) ORDER BY title ASC")
    fun getTracksByAlbum(albumName: String): Flow<List<TrackEntity>>

    @Query("""
        SELECT 
            COALESCE(NULLIF(albumName, ''), 'Sencillos') AS albumName,
            artistName,
            COUNT(*) AS trackCount,
            MAX(thumbnailUrl) AS thumbnailUrl
        FROM tracks 
        WHERE isSaved = 1 OR isDownloaded = 1
        GROUP BY COALESCE(NULLIF(albumName, ''), 'Sencillos'), artistName
        ORDER BY albumName ASC
    """)
    fun getAlbumSummaries(): Flow<List<AlbumSummary>>

    @Query("SELECT * FROM tracks WHERE COALESCE(NULLIF(albumName, ''), 'Sencillos') = :albumName AND artistName = :artistName AND (isSaved = 1 OR isDownloaded = 1) ORDER BY title ASC")
    fun getTracksByAlbumAndArtist(albumName: String, artistName: String): Flow<List<TrackEntity>>
}

data class AlbumSummary(
    val albumName: String,
    val artistName: String,
    val trackCount: Int,
    val thumbnailUrl: String?
)
