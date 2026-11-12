package com.fr33music.core.player.service

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.fr33music.core.player.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class Fr33MusicPlayerService : MediaSessionService() {

    @Inject
    lateinit var exoPlayer: ExoPlayer

    private var mediaSession: MediaSession? = null

    companion object {
        @Volatile
        var isRunning: Boolean = false
            internal set
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        isRunning = true

        // 1. MediaSessionService gestiona la notificación 100% de forma nativa
        val notificationProvider = DefaultMediaNotificationProvider(this).apply {
            setSmallIcon(R.drawable.ic_notification)
        }
        setMediaNotificationProvider(notificationProvider)

        val sessionCallback = object : MediaSession.Callback {
            override fun onConnect(
                session: MediaSession,
                controller: MediaSession.ControllerInfo
            ): MediaSession.ConnectionResult {
                val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon().build()
                val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                    .add(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
                    .add(Player.COMMAND_SEEK_TO_NEXT)
                    .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                    .add(Player.COMMAND_SEEK_TO_MEDIA_ITEM)
                    .add(Player.COMMAND_PLAY_PAUSE)
                    .add(Player.COMMAND_PREPARE)
                    .add(Player.COMMAND_STOP)
                    .add(Player.COMMAND_SET_MEDIA_ITEMS_METADATA)
                    .add(Player.COMMAND_CHANGE_MEDIA_ITEMS)
                    .build()
                return MediaSession.ConnectionResult.accept(sessionCommands, playerCommands)
            }
        }

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                this,
                0,
                it,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val sessionBuilder = MediaSession.Builder(this, exoPlayer)
            .setCallback(sessionCallback)

        if (pendingIntent != null) {
            sessionBuilder.setSessionActivity(pendingIntent)
        }

        val session = sessionBuilder.build()
        mediaSession = session
        addSession(session)
    }

    @OptIn(UnstableApi::class)
    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        val player = session.player
        val keepNotificationAlive = player.mediaItemCount > 0 && player.playbackState != Player.STATE_IDLE
        super.onUpdateNotification(session, startInForegroundRequired || keepNotificationAlive)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player ?: exoPlayer
        if (player.isPlaying || player.playWhenReady || player.playbackState == Player.STATE_BUFFERING) {
            return
        }
        stopSelf()
    }

    override fun onDestroy() {
        isRunning = false
        mediaSession?.run {
            removeSession(this)
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
