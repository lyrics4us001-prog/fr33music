package com.fr33music.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fr33music.core.designsystem.component.TrackThumbnail
import com.fr33music.core.designsystem.theme.ColorBackground
import com.fr33music.core.designsystem.theme.ColorError
import com.fr33music.core.designsystem.theme.ColorPrimary
import com.fr33music.core.designsystem.theme.ColorSurfaceVariant
import com.fr33music.core.designsystem.theme.ColorTextPrimary
import com.fr33music.core.designsystem.theme.ColorTextSecondary
import com.fr33music.core.designsystem.theme.ColorTextTertiary
import com.fr33music.core.designsystem.theme.Fr33Radius
import com.fr33music.core.designsystem.theme.Fr33Spacing
import com.fr33music.core.designsystem.theme.Fr33Typography
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val track = uiState.track

    var isDraggingSlider by remember { mutableStateOf(false) }
    var dragPositionMs by remember { mutableFloatStateOf(0f) }

    // Contenedor raíz con fondo sólido 0xFF030303 que consume todos los toques para evitar click-through
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030303))
            .pointerInput(Unit) {
                detectTapGestures {
                    // Consumir el toque, no hacer nada
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Down arrow button to collapse
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Minimizar",
                        tint = ColorTextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Fr33Spacing.medium))

            // Large Artwork - con interceptor de gestos para que no traspase toques
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Fr33Spacing.large)
                    .aspectRatio(1f)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            // Consumir el toque en la carátula
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                TrackThumbnail(
                    thumbnailUrl = track?.thumbnailUrl,
                    contentDescription = track?.title,
                    size = 320.dp,
                    shapeRadius = Fr33Radius.large,
                    modifier = Modifier.fillMaxSize()
                )

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ColorBackground.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = ColorPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Fr33Spacing.extraLarge))

            // Title and Artist
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Fr33Spacing.small),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = track?.title ?: "Sin reproducción",
                    style = Fr33Typography.headlineMedium,
                    color = ColorTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(Fr33Spacing.extraSmall))

                Text(
                    text = track?.artistName ?: "Selecciona una canción",
                    style = Fr33Typography.bodyLarge,
                    color = ColorTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(Fr33Spacing.small))
                    Text(
                        text = uiState.error.orEmpty(),
                        style = Fr33Typography.bodyMedium,
                        color = ColorError,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(Fr33Spacing.medium))

            // Position Slider & Timers
            val currentPosition = if (isDraggingSlider) dragPositionMs else uiState.positionMs.toFloat()
            val totalDuration = uiState.durationMs.coerceAtLeast(1L).toFloat()

            Slider(
                value = currentPosition.coerceIn(0f, totalDuration),
                onValueChange = { newValue ->
                    isDraggingSlider = true
                    dragPositionMs = newValue
                },
                onValueChangeFinished = {
                    viewModel.seekTo(dragPositionMs.toLong())
                    isDraggingSlider = false
                },
                valueRange = 0f..totalDuration,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = ColorPrimary,
                    activeTrackColor = ColorPrimary,
                    inactiveTrackColor = ColorSurfaceVariant
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(currentPosition.toLong()),
                    style = Fr33Typography.labelSmall,
                    color = ColorTextTertiary
                )
                Text(
                    text = formatDuration(uiState.durationMs),
                    style = Fr33Typography.labelSmall,
                    color = ColorTextTertiary
                )
            }

            Spacer(modifier = Modifier.height(Fr33Spacing.large))

            // Playback Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle (disabled placeholder)
                IconButton(onClick = {}, enabled = false) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Aleatorio",
                        tint = ColorTextTertiary.copy(alpha = 0.38f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous
                IconButton(
                    onClick = viewModel::previous,
                    enabled = track != null
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Anterior",
                        tint = if (track != null) ColorTextPrimary else ColorTextTertiary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Play / Pause (Large Primary Button)
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(ColorPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = viewModel::togglePlayPause,
                        enabled = track != null,
                        modifier = Modifier.size(68.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.isPlaying) "Pausar" else "Reproducir",
                            tint = ColorTextPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Next
                IconButton(
                    onClick = viewModel::next,
                    enabled = track != null
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Siguiente",
                        tint = if (track != null) ColorTextPrimary else ColorTextTertiary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Repeat (disabled placeholder)
                IconButton(onClick = {}, enabled = false) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Repetir",
                        tint = ColorTextTertiary.copy(alpha = 0.38f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
