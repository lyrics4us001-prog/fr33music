package com.fr33music.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "trackId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("playlistId"),
        Index("trackId")
    ]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val trackId: String,
    val addedAt: Long = System.currentTimeMillis()
)
