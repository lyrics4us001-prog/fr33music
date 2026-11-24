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
        thumbnailUrl = if (thumbnailUrl.isNullOrBlank()) "https://i.ytimg.com/vi/$id/hqdefault.jpg" else thumbnailUrl
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
            val effectiveThumb = if (track.thumbnailUrl.isNullOrBlank()) "https://i.ytimg.com/vi/${track.trackId}/hqdefault.jpg" else track.thumbnailUrl
            val effectiveTitle = track.title.takeIf { it.isNotBlank() && !it.startsWith("Pista ") } ?: "Desconocido"
            val effectiveArtist = track.artistName.takeIf { it.isNotBlank() } ?: "Artista Desconocido"
            val effectiveDuration = if (track.durationMs > 0) track.durationMs else 0L
            return TrackEntity(
                id = track.trackId,
                title = effectiveTitle,
                artistName = effectiveArtist,
                albumName = effectiveAlbum,
                durationMs = effectiveDuration,
                thumbnailUrl = effectiveThumb,
                format = format,
                isDownloaded = isDownloaded,
                isSaved = isSaved,
                localPath = localPath
            )
        }
    }
}
