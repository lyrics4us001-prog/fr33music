package com.fr33music.core.player.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import com.fr33music.core.extractor.NewPipeSearchSource
import com.fr33music.core.extractor.NewPipeStreamResolver
import com.fr33music.core.extractor.SearchSource
import com.fr33music.core.extractor.StreamResolver
import com.fr33music.core.player.PlayerRepository
import com.fr33music.core.player.PlayerRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerBindingModule {
    @Binds
    @Singleton
    abstract fun bindPlayerRepository(impl: PlayerRepositoryImpl): PlayerRepository
}

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @Volatile
    private var simpleCacheInstance: SimpleCache? = null

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideSimpleCache(@ApplicationContext context: Context): SimpleCache {
        return simpleCacheInstance ?: synchronized(this) {
            simpleCacheInstance ?: run {
                val cacheDir = File(context.cacheDir, "exoplayer_cache")
                val evictor = LeastRecentlyUsedCacheEvictor(500 * 1024 * 1024L) // 500 MB
                val databaseProvider = StandaloneDatabaseProvider(context)
                SimpleCache(cacheDir, evictor, databaseProvider).also {
                    simpleCacheInstance = it
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 25000,
                /* maxBufferMs = */ 50000,
                /* bufferForPlaybackMs = */ 2500,
                /* bufferForPlaybackAfterRebufferMs = */ 5000
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        return ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setLoadControl(loadControl)
            .build()
    }

    @Provides
    @Singleton
    fun provideSearchSource(): SearchSource {
        return NewPipeSearchSource()
    }

    @Provides
    @Singleton
    fun provideStreamResolver(
        @ApplicationContext context: Context,
        offlineAudioSource: com.fr33music.core.domain.model.OfflineAudioSource
    ): StreamResolver {
        return NewPipeStreamResolver(
            offlineAudioSource = offlineAudioSource,
            context = context
        )
    }
}
