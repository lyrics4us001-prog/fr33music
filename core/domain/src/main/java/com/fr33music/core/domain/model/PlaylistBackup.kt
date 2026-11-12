package com.fr33music.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrackExportDto(
    @SerialName("videoId") val videoId: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("artist") val artist: String = "",
    @SerialName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerialName("durationMs") val durationMs: Long = 0L,
    @SerialName("albumName") val albumName: String? = null
)

@Serializable
data class PlaylistExportDto(
    @SerialName("name") val name: String = "Playlist Importada",
    @SerialName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerialName("tracks") val tracks: List<TrackExportDto> = emptyList()
)

@Serializable
data class PlaylistBackupDto(
    @SerialName("version") val version: Int = 1,
    @SerialName("playlists") val playlists: List<PlaylistExportDto> = emptyList()
)
