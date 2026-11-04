package com.fr33music.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artistName: String,
    val thumbnailUrl: String? = null,
    val trackCount: Int = 1
)
