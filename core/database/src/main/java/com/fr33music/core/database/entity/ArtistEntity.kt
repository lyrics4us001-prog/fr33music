package com.fr33music.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey
    val name: String,
    val trackCount: Int = 1,
    val thumbnailUrl: String? = null
)
