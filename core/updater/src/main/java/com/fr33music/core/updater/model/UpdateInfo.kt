package com.fr33music.core.updater.model

data class UpdateInfo(
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String?
)
