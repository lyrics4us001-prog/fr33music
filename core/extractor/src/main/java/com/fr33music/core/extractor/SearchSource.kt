package com.fr33music.core.extractor

import com.fr33music.core.domain.model.AlbumCard
import com.fr33music.core.domain.model.ChannelCard
import com.fr33music.core.domain.model.LookingSearchResult
import com.fr33music.core.domain.model.Track

data class ChannelVideosResult(
    val tracks: List<Track>,
    val nextPageToken: String? = null,
    val totalVideos: Long? = null
)

interface SearchSource {
    suspend fun search(
        query: String,
        page: Int = 0
    ): Result<List<Track>>

    suspend fun searchLookingDetails(
        query: String
    ): Result<LookingSearchResult>

    suspend fun getChannelVideos(
        channel: ChannelCard
    ): Result<List<Track>>

    suspend fun getChannelVideosPaged(
        channel: ChannelCard,
        nextPageToken: String? = null
    ): Result<ChannelVideosResult>

    suspend fun searchChannelVideos(
        channel: ChannelCard,
        query: String,
        nextPageToken: String? = null
    ): Result<ChannelVideosResult>

    suspend fun getAlbumTracks(
        album: AlbumCard
    ): Result<List<Track>>

    suspend fun getSuggestions(
        query: String
    ): Result<List<String>>
}
