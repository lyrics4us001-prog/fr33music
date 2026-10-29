package com.fr33music.core.extractor

import com.fr33music.core.domain.model.AudioStream
import com.fr33music.core.domain.model.Track

interface StreamResolver {
    suspend fun resolveAudio(track: Track): Result<AudioStream>
}
