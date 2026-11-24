package com.fr33music.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fr33music.core.database.entity.LyricsOffsetEntity

@Dao
interface LyricsOffsetDao {

    @Query("SELECT offsetMs FROM lyrics_offsets WHERE videoId = :videoId LIMIT 1")
    suspend fun getOffsetForTrack(videoId: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateOffset(offset: LyricsOffsetEntity)

    @Query("DELETE FROM lyrics_offsets WHERE videoId = :videoId")
    suspend fun deleteOffset(videoId: String)
}
