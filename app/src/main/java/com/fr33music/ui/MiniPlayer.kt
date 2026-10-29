package com.fr33music.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fr33music.core.designsystem.component.TrackThumbnail
import com.fr33music.core.designsystem.theme.ColorPrimary
import com.fr33music.core.designsystem.theme.ColorSurface
import com.fr33music.core.designsystem.theme.ColorSurfaceVariant
import com.fr33music.core.designsystem.theme.ColorTextPrimary
import com.fr33music.core.designsystem.theme.ColorTextSecondary
import com.fr33music.core.designsystem.theme.Fr33Radius
import com.fr33music.core.designsystem.theme.Fr33Spacing
import com.fr33music.core.designsystem.theme.Fr33Typography
import com.fr33music.core.player.PlaybackState

@Composable
fun MiniPlayer(
    playbackState: PlaybackState,
    onPlayPauseClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = playbackState.currentTrack
    val isVisible = playbackState.queue.isNotEmpty() && track != null

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        if (track != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Fr33Spacing.medium, vertical = Fr33Spacing.extraSmall)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(Fr33Radius.medium))
                    .clip(RoundedCornerShape(Fr33Radius.medium))
                    .background(ColorSurface)
                    .clickable(onClick = onClick)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Fr33Spacing.medium, vertical = Fr33Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TrackThumbnail(
                            thumbnailUrl = track.thumbnailUrl,
                            contentDescription = track.title,
                            size = 48.dp,
                            shapeRadius = Fr33Radius.small
                        )

                        Spacer(modifier = Modifier.width(Fr33Spacing.medium))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                style = Fr33Typography.titleMedium,
                                color = ColorTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = track.artistName,
                                style = Fr33Typography.bodyMedium,
                                color = ColorTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(Fr33Spacing.small))

                        if (playbackState.isLoading) {
                            CircularProgressIndicator(
                                color = ColorPrimary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(32.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            IconButton(onClick = onPlayPauseClick) {
                                Icon(
                                    imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (playbackState.isPlaying) "Pausar" else "Reproducir",
                                    tint = ColorTextPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    // Progress bar line at the bottom
                    val progress = if (track.durationMs > 0) {
                        (playbackState.positionMs.toFloat() / track.durationMs.toFloat()).coerceIn(0f, 1f)
                    } else {
                        0f
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp),
                        color = ColorPrimary,
                        trackColor = ColorSurfaceVariant
                    )
                }
            }
        }
    }
}
