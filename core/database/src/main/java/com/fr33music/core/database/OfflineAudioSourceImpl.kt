package com.fr33music.core.database

import android.content.Context
import com.fr33music.core.database.dao.TrackDao
import com.fr33music.core.domain.model.OfflineAudioSource
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineAudioSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackDao: TrackDao
) : OfflineAudioSource {

    companion object {
        private const val MIN_FILE_SIZE_BYTES = 100 * 1024L
    }

    override suspend fun getLocalAudioPath(trackId: String): String? {
        val track = trackDao.getTrackSync(trackId)
        if (track?.isDownloaded == true && !track.localPath.isNullOrBlank()) {
            val file = File(track.localPath)
            if (file.exists() && file.length() >= MIN_FILE_SIZE_BYTES) return file.absolutePath
        }

        // Fallback directo al sistema de archivos privados
        val downloadsDir = File(context.filesDir, "downloads")
        val opus = File(downloadsDir, "$trackId.opus")
        if (opus.exists() && opus.length() >= MIN_FILE_SIZE_BYTES) return opus.absolutePath

        val m4a = File(downloadsDir, "$trackId.m4a")
        if (m4a.exists() && m4a.length() >= MIN_FILE_SIZE_BYTES) return m4a.absolutePath

        return null
    }
}
