package com.fr33music.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lyrics_offsets")
data class LyricsOffsetEntity(
    @PrimaryKey
    val videoId: String,
    val offsetMs: Long
)
