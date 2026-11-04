package com.fr33music.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fr33music.core.domain.model.Track

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artistName: String,
    val albumName: String? = null,
    val durationMs: Long,
    val thumbnailUrl: String? = null,
    val format: String? = null,
    val isDownloaded: Boolean = false,
    val isSaved: Boolean = false,
    val localPath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): Track = Track(
        trackId = id,
        title = title,
        artistName = artistName,
        albumName = albumName,
        durationMs = durationMs,
        thumbnailUrl = thumbnailUrl
    )

    companion object {
        fun fromDomain(
            track: Track,
            isSaved: Boolean = false,
            isDownloaded: Boolean = false,
            localPath: String? = null,
            format: String? = null
        ): TrackEntity {
            val isRealAlbum = !track.albumName.isNullOrBlank() &&
                    !track.albumName.equals(track.artistName, ignoreCase = true) &&
                    !track.albumName.equals("Sencillos", ignoreCase = true) &&
                    !track.albumName.equals("YouTube", ignoreCase = true)
            val effectiveAlbum = if (isRealAlbum) track.albumName!! else "Sencillos"
            return TrackEntity(
                id = track.trackId,
                title = track.title,
                artistName = track.artistName,
                albumName = effectiveAlbum,
                durationMs = track.durationMs,
                thumbnailUrl = track.thumbnailUrl,
                format = format,
                isDownloaded = isDownloaded,
                isSaved = isSaved,
                localPath = localPath
            )
        }
    }
}
