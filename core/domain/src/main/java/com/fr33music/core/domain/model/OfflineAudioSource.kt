package com.fr33music.core.domain.model

interface OfflineAudioSource {
    suspend fun getLocalAudioPath(trackId: String): String?
}
