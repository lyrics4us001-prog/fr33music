package com.fr33music.core.player.di

import android.content.Context
import android.net.Uri
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import com.fr33music.core.data.AudioCacheManager
import com.fr33music.core.domain.model.Track
import com.fr33music.core.extractor.NewPipeDownloader
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
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
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        simpleCache: SimpleCache,
        audioCacheManager: AudioCacheManager,
        streamResolver: StreamResolver
    ): ExoPlayer {
        // 2. Habilitar Seek hacia atrás y adelante en streams Opus/M4A con ConstantBitrateSeeking
        val extractorsFactory = DefaultExtractorsFactory()
            .setConstantBitrateSeekingEnabled(true)
            .setConstantBitrateSeekingAlwaysEnabled(true)

        val userAgent = NewPipeDownloader.USER_AGENT
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(8000)
            .setReadTimeoutMs(15000)
            .setKeepPostFor302Redirects(true)

        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR or
                CacheDataSource.FLAG_IGNORE_CACHE_FOR_UNSET_LENGTH_REQUESTS
            )

        val baseDataSourceFactory = DefaultDataSource.Factory(context, cacheDataSourceFactory)

        // ResolvingDataSource.Factory: resolución de URIs fr33music://track/{id} en segundo plano
        val resolvingDataSourceFactory = ResolvingDataSource.Factory(
            baseDataSourceFactory,
            ResolvingDataSource.Resolver { dataSpec ->
                val uri = dataSpec.uri
                if (uri.scheme == "fr33music") {
                    val trackId = uri.lastPathSegment?.takeIf { it.isNotBlank() }
                        ?: uri.path?.removePrefix("/")?.takeIf { it.isNotBlank() }
                        ?: return@Resolver dataSpec

                    // 1. Revisar filesDir/downloads/ (descarga permanente)
                    val permanentFile = audioCacheManager.getPermanentFile(trackId)
                    if (permanentFile != null && permanentFile.length() >= AudioCacheManager.MIN_FILE_SIZE_BYTES) {
                        Log.i("ResolvingDS", "Pista local permanente encontrada: ${permanentFile.name}")
                        return@Resolver dataSpec.buildUpon()
                            .setUri(Uri.fromFile(permanentFile))
                            .build()
                    }

                    // 2. Revisar cacheDir/audio_cache/ (caché temporal)
                    val cachedFile = audioCacheManager.getCachedFile(trackId)
                    if (cachedFile != null && cachedFile.length() >= AudioCacheManager.MIN_FILE_SIZE_BYTES) {
                        Log.i("ResolvingDS", "Pista en caché temporal encontrada: ${cachedFile.name}")
                        return@Resolver dataSpec.buildUpon()
                            .setUri(Uri.fromFile(cachedFile))
                            .build()
                    }

                    // 3. NewPipe (con auto-caché y fallback a streaming directo)
                    Log.i("ResolvingDS", "Resolviendo stream vía NewPipe para trackId: $trackId")
                    val track = Track(trackId = trackId, title = "", artistName = "", durationMs = 0L)

                    val cacheResult = runBlocking(Dispatchers.IO) {
                        audioCacheManager.cacheAudio(track)
                    }
                    val autoCachedFile = cacheResult.getOrNull()
                    if (autoCachedFile != null && autoCachedFile.exists() && autoCachedFile.length() >= AudioCacheManager.MIN_FILE_SIZE_BYTES) {
                        Log.i("ResolvingDS", "Auto-caché completado con éxito: ${autoCachedFile.name}")
                        return@Resolver dataSpec.buildUpon()
                            .setUri(Uri.fromFile(autoCachedFile))
                            .build()
                    }

                    val streamResult = runBlocking(Dispatchers.IO) {
                        streamResolver.resolveAudio(track)
                    }
                    val stream = streamResult.getOrThrow()
                    Log.i("ResolvingDS", "Streaming directo resuelto desde NewPipe para: $trackId")
                    return@Resolver dataSpec.buildUpon()
                        .setUri(Uri.parse(stream.url))
                        .setHttpRequestHeaders(stream.headers)
                        .build()
                }
                dataSpec
            }
        )

        val mediaSourceFactory = DefaultMediaSourceFactory(resolvingDataSourceFactory, extractorsFactory)

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 30000,
                /* maxBufferMs = */ 60000,
                /* bufferForPlaybackMs = */ 1000,
                /* bufferForPlaybackAfterRebufferMs = */ 2000
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .setBackBuffer(15000, true)
            .build()

        return ExoPlayer.Builder(context)
            .setLooper(Looper.getMainLooper())
            .setMediaSourceFactory(mediaSourceFactory)
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
