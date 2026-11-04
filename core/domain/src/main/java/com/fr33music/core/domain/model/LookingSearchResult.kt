package com.fr33music.core.domain.model

data class ChannelCard(
    val name: String,
    val avatarUrl: String?,
    val subscriberCount: Long = 0,
    val description: String? = null,
    val url: String,
    val channelId: String
)

data class LookingSearchResult(
    val channels: List<ChannelCard> = emptyList(),
    val videos: List<Track> = emptyList(),
    val playlists: List<AlbumCard> = emptyList()
)
