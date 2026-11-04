package com.fr33music.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

object DownloadStatus {
    const val QUEUED = "QUEUED"
    const val DOWNLOADING = "DOWNLOADING"
    const val COMPLETED = "COMPLETED"
    const val ERROR = "ERROR"
}

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val trackId: String,
    val title: String,
    val artistName: String,
    val thumbnailUrl: String? = null,
    val status: String = DownloadStatus.QUEUED,
    val localPath: String? = null,
    val progress: Int = 0,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
