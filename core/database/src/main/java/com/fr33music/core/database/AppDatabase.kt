package com.fr33music.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fr33music.core.database.dao.AlbumDao
import com.fr33music.core.database.dao.ArtistDao
import com.fr33music.core.database.dao.DownloadDao
import com.fr33music.core.database.dao.LyricsDao
import com.fr33music.core.database.dao.PlaylistDao
import com.fr33music.core.database.dao.SearchHistoryDao
import com.fr33music.core.database.dao.TrackDao
import com.fr33music.core.database.entity.AlbumEntity
import com.fr33music.core.database.entity.ArtistEntity
import com.fr33music.core.database.entity.DownloadEntity
import com.fr33music.core.database.entity.LyricsEntity
import com.fr33music.core.database.entity.PlaylistEntity
import com.fr33music.core.database.entity.PlaylistSongCrossRef
import com.fr33music.core.database.entity.SearchHistoryEntity
import com.fr33music.core.database.entity.TrackEntity

@Database(
    entities = [
        TrackEntity::class,
        ArtistEntity::class,
        AlbumEntity::class,
        LyricsEntity::class,
        SearchHistoryEntity::class,
        DownloadEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun artistDao(): ArtistDao
    abstract fun albumDao(): AlbumDao
    abstract fun lyricsDao(): LyricsDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun downloadDao(): DownloadDao
    abstract fun playlistDao(): PlaylistDao
}
