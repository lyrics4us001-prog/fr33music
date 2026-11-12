package com.fr33music.core.database.download

import com.fr33music.core.database.entity.DownloadEntity
import com.fr33music.core.domain.model.Track
import kotlinx.coroutines.flow.Flow

sealed interface DownloadState {
    data object Idle : DownloadState
    data object Queued : DownloadState
    data class Downloading(val progress: Int) : DownloadState
    data class Completed(val localPath: String) : DownloadState
    data class Error(val message: String) : DownloadState
}

interface DownloadRepository {
    fun getAllDownloads(): Flow<List<DownloadEntity>>
    fun getDownload(trackId: String): Flow<DownloadEntity?>
    fun getDownloadState(trackId: String): Flow<DownloadState>
    suspend fun syncDownloadedFiles()
    suspend fun download(track: Track)
    suspend fun deleteDownload(trackId: String)
    suspend fun clearAllDownloadedAudios()
}
