package com.fr33music.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fr33music.core.database.AppDatabase
import com.fr33music.core.database.OfflineAudioSourceImpl
import com.fr33music.core.database.dao.AlbumDao
import com.fr33music.core.database.dao.ArtistDao
import com.fr33music.core.database.dao.DownloadDao
import com.fr33music.core.database.dao.LyricsDao
import com.fr33music.core.database.dao.LyricsOffsetDao
import com.fr33music.core.database.dao.SearchHistoryDao
import com.fr33music.core.database.dao.TrackDao
import com.fr33music.core.database.download.DownloadManager
import com.fr33music.core.database.download.DownloadRepository
import com.fr33music.core.database.repository.LibraryRepository
import com.fr33music.core.database.repository.LibraryRepositoryImpl
import com.fr33music.core.domain.model.OfflineAudioSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE lyrics ADD COLUMN syncedText TEXT")
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS lyrics_offsets (
                    videoId TEXT NOT NULL PRIMARY KEY,
                    offsetMs INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fr33music.db"
        )
            .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTrackDao(database: AppDatabase): TrackDao = database.trackDao()

    @Provides
    @Singleton
    fun provideArtistDao(database: AppDatabase): ArtistDao = database.artistDao()

    @Provides
    @Singleton
    fun provideAlbumDao(database: AppDatabase): AlbumDao = database.albumDao()

    @Provides
    @Singleton
    fun provideLyricsDao(database: AppDatabase): LyricsDao = database.lyricsDao()

    @Provides
    @Singleton
    fun provideLyricsOffsetDao(database: AppDatabase): LyricsOffsetDao = database.lyricsOffsetDao()

    @Provides
    @Singleton
    fun provideSearchHistoryDao(database: AppDatabase): SearchHistoryDao = database.searchHistoryDao()

    @Provides
    @Singleton
    fun provideDownloadDao(database: AppDatabase): DownloadDao = database.downloadDao()

    @Provides
    @Singleton
    fun providePlaylistDao(database: AppDatabase): com.fr33music.core.database.dao.PlaylistDao = database.playlistDao()

    @Provides
    @Singleton
    fun provideDownloadRepository(impl: DownloadManager): DownloadRepository = impl

    @Provides
    @Singleton
    fun provideLibraryRepository(impl: LibraryRepositoryImpl): LibraryRepository = impl

    @Provides
    @Singleton
    fun provideOkHttpClient(): okhttp3.OkHttpClient {
        return okhttp3.OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideOfflineAudioSource(impl: OfflineAudioSourceImpl): OfflineAudioSource = impl
}
