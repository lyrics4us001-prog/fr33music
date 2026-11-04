package com.fr33music.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fr33music.core.database.entity.AlbumEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Query("SELECT * FROM albums ORDER BY title ASC")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE id = :id LIMIT 1")
    suspend fun getAlbumSync(id: String): AlbumEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>)

    @Update
    suspend fun updateAlbum(album: AlbumEntity)

    @Query("DELETE FROM albums WHERE id = :id")
    suspend fun deleteAlbum(id: String)
}
