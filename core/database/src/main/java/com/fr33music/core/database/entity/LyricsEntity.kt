package com.fr33music.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lyrics")
data class LyricsEntity(
    @PrimaryKey
    val trackId: String,
    val plainText: String,
    val source: String,
    val timestamp: Long = System.currentTimeMillis()
)
