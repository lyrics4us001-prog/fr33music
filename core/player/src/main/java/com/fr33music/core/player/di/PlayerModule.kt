package com.fr33music.core.player.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
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

    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        return ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
    }

    @Provides
    @Singleton
    fun provideSearchSource(): SearchSource {
        return NewPipeSearchSource()
    }

    @Provides
    @Singleton
    fun provideStreamResolver(): StreamResolver {
        return NewPipeStreamResolver()
    }
}
