package com.fr33music.core.extractor

import com.fr33music.core.domain.model.Track

interface SearchSource {
    suspend fun search(query: String, page: Int = 0): Result<List<Track>>
}
