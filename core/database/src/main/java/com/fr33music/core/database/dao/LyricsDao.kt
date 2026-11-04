package com.fr33music.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fr33music.core.database.entity.LyricsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LyricsDao {

    @Query("SELECT * FROM lyrics WHERE trackId = :trackId LIMIT 1")
    fun getLyrics(trackId: String): Flow<LyricsEntity?>

    @Query("SELECT * FROM lyrics WHERE trackId = :trackId LIMIT 1")
    suspend fun getLyricsSync(trackId: String): LyricsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLyrics(lyrics: LyricsEntity)

    @Query("DELETE FROM lyrics WHERE trackId = :trackId")
    suspend fun deleteLyrics(trackId: String)
}
