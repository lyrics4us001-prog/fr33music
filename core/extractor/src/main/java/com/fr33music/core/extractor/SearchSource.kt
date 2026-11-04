package com.fr33music.core.extractor

import com.fr33music.core.domain.model.AlbumCard
import com.fr33music.core.domain.model.ChannelCard
import com.fr33music.core.domain.model.LookingSearchResult
import com.fr33music.core.domain.model.Track

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

    suspend fun getAlbumTracks(
        album: AlbumCard
    ): Result<List<Track>>
}
