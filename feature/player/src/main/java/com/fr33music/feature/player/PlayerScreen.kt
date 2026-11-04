package com.fr33music.feature.player

import androidx.activity.compose.BackHandler

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.fr33music.core.database.dao.PlaylistWithSongCount
import com.fr33music.core.database.download.DownloadState
import com.fr33music.core.database.entity.DownloadStatus
import com.fr33music.core.designsystem.component.TrackThumbnail
import com.fr33music.core.designsystem.theme.ColorBackground
import com.fr33music.core.designsystem.theme.ColorError
import com.fr33music.core.designsystem.theme.ColorPrimary
import com.fr33music.core.designsystem.theme.ColorSurface
import com.fr33music.core.designsystem.theme.ColorSurfaceVariant
import com.fr33music.core.designsystem.theme.ColorTextPrimary
import com.fr33music.core.designsystem.theme.ColorTextSecondary
import com.fr33music.core.designsystem.theme.ColorTextTertiary
import com.fr33music.core.designsystem.theme.Fr33Radius
import com.fr33music.core.designsystem.theme.Fr33Spacing
import com.fr33music.core.designsystem.theme.Fr33Typography
import com.fr33music.core.domain.model.Track
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val track = uiState.track

    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        if (uiState.showLyricsSheet) {
            viewModel.closeLyrics()
        } else {
            onCollapse()
        }
    }

    var isDraggingSlider by remember { mutableStateOf(false) }
    var dragPositionMs by remember { mutableFloatStateOf(0f) }

    // Contenedor raíz con fondo sólido 0xFF030303 que consume todos los toques para evitar click-through
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030303))
            .pointerInput(Unit) {
                detectTapGestures {
                    // Consumir el toque
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Down arrow button to collapse & Add to Playlist button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
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

                IconButton(
                    onClick = { showAddToPlaylistDialog = true },
                    enabled = track != null
                ) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = "Añadir a playlist",
                        tint = if (track != null) ColorTextPrimary else ColorTextTertiary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Fr33Spacing.medium))

            // Large Artwork
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
                    trackId = track?.trackId,
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

            Spacer(modifier = Modifier.height(Fr33Spacing.large))

            // Title, Artist y Botón de Favorito / Guardar en Biblioteca (Corazón)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Fr33Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
                }

                // Botón Guardar en Biblioteca (Corazón)
                IconButton(
                    onClick = viewModel::toggleSave,
                    enabled = track != null
                ) {
                    Icon(
                        imageVector = if (uiState.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (uiState.isSaved) "Guardado en biblioteca" else "Guardar en biblioteca",
                        tint = if (uiState.isSaved) ColorPrimary else ColorTextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

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

            Spacer(modifier = Modifier.height(Fr33Spacing.medium))

            // Position Slider & Timers
            val hasValidDuration = uiState.durationMs > 0L
            val totalDuration = if (hasValidDuration) uiState.durationMs.toFloat() else 1f
            val currentPosition = if (isDraggingSlider) {
                dragPositionMs
            } else if (hasValidDuration) {
                uiState.positionMs.toFloat().coerceIn(0f, totalDuration)
            } else {
                0f
            }

            Slider(
                value = if (hasValidDuration) currentPosition.coerceIn(0f, totalDuration) else 0f,
                onValueChange = { newValue ->
                    if (hasValidDuration) {
                        isDraggingSlider = true
                        dragPositionMs = newValue
                    }
                },
                onValueChangeFinished = {
                    if (hasValidDuration) {
                        viewModel.seekTo(dragPositionMs.toLong())
                        isDraggingSlider = false
                    }
                },
                enabled = hasValidDuration,
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

            Spacer(modifier = Modifier.height(Fr33Spacing.medium))

            // Playback Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle (Aleatorio sin repetir hasta agotar cola)
                IconButton(
                    onClick = viewModel::toggleShuffle,
                    enabled = track != null
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Aleatorio",
                        tint = if (uiState.isShuffle) ColorPrimary else ColorTextTertiary,
                        modifier = Modifier.size(28.dp)
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

                // Espaciador para equilibrar visualmente el botón Shuffle del extremo izquierdo
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(Fr33Spacing.medium))

            // Fila de acciones secundarias: Descargar y Letras
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Descargar
                val isDownloaded = downloadState is DownloadState.Completed || uiState.isDownloaded
                val isDownloading = downloadState is DownloadState.Downloading || downloadState is DownloadState.Queued
                val isError = downloadState is DownloadState.Error
                val canDownload = track != null && !isDownloaded && !isDownloading

                IconButton(
                    onClick = viewModel::downloadCurrentTrack,
                    enabled = canDownload
                ) {
                    when {
                        isDownloaded -> {
                            Icon(
                                imageVector = Icons.Default.DownloadDone,
                                contentDescription = "Descargado offline",
                                tint = ColorPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        isDownloading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = ColorPrimary,
                                strokeWidth = 2.dp
                            )
                        }
                        isError -> {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Error al descargar. Toca para reintentar.",
                                tint = ColorPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Descargar audio OPUS",
                                tint = if (track != null) ColorTextSecondary else ColorTextTertiary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                // Botón Letras
                IconButton(
                    onClick = viewModel::openLyrics,
                    enabled = track != null
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Article,
                        contentDescription = "Ver letras",
                        tint = if (track != null) ColorTextSecondary else ColorTextTertiary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // ModalBottomSheet para mostrar la letra de la canción
        if (uiState.showLyricsSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = viewModel::closeLyrics,
                sheetState = sheetState,
                containerColor = ColorSurface,
                shape = RoundedCornerShape(topStart = Fr33Radius.large, topEnd = Fr33Radius.large)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.medium),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Letra",
                        style = Fr33Typography.headlineSmall,
                        color = ColorTextPrimary
                    )
                    Text(
                        text = "${track?.title} — ${track?.artistName}",
                        style = Fr33Typography.bodyMedium,
                        color = ColorTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(Fr33Spacing.large))

                    if (uiState.isLoadingLyrics) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = ColorPrimary)
                        }
                    } else {
                        val scrollState = rememberScrollState()
                        Text(
                            text = uiState.lyrics ?: "Letra no disponible",
                            style = Fr33Typography.bodyLarge,
                            color = ColorTextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                                .verticalScroll(scrollState)
                                .padding(vertical = Fr33Spacing.medium)
                        )
                    }

                    Spacer(modifier = Modifier.height(Fr33Spacing.large))
                }
            }
        }

        // Diálogo para añadir a playlist desde el reproductor
        if (showAddToPlaylistDialog && track != null) {
            AddToPlaylistDialog(
                track = track,
                playlists = playlists,
                onDismiss = { showAddToPlaylistDialog = false },
                onSelectPlaylist = { playlistId ->
                    viewModel.addSongToPlaylist(playlistId, track)
                    showAddToPlaylistDialog = false
                },
                onDeletePlaylist = { playlistId ->
                    viewModel.deletePlaylist(playlistId)
                },
                onCreateNewPlaylist = {
                    showCreatePlaylistDialog = true
                }
            )
        }

        // Diálogo para crear nueva playlist
        if (showCreatePlaylistDialog) {
            CreatePlaylistDialog(
                onDismiss = { showCreatePlaylistDialog = false },
                onCreate = { name ->
                    viewModel.createPlaylist(name)
                    showCreatePlaylistDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AddToPlaylistDialog(
    track: Track,
    playlists: List<PlaylistWithSongCount>,
    onDismiss: () -> Unit,
    onSelectPlaylist: (Long) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onCreateNewPlaylist: () -> Unit
) {
    var playlistToDelete by remember { mutableStateOf<PlaylistWithSongCount?>(null) }

    if (playlistToDelete != null) {
        val target = playlistToDelete!!
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            title = { Text(text = "Eliminar playlist", color = ColorTextPrimary) },
            text = {
                Text(
                    text = "¿Eliminar la playlist \"${target.name}\"?",
                    color = ColorTextSecondary,
                    style = Fr33Typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePlaylist(target.id)
                        playlistToDelete = null
                    }
                ) {
                    Text("Eliminar", color = ColorError)
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToDelete = null }) {
                    Text("Cancelar", color = ColorTextSecondary)
                }
            },
            containerColor = ColorSurface
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Añadir a Playlist", color = ColorTextPrimary) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${track.title} - ${track.artistName}",
                    style = Fr33Typography.bodySmall,
                    color = ColorTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(Fr33Spacing.medium))

                if (playlists.isEmpty()) {
                    Text(
                        text = "No tienes playlists creadas todavía.",
                        style = Fr33Typography.bodyMedium,
                        color = ColorTextTertiary
                    )
                } else {
                    Text(
                        text = "Mantén presionado para eliminar una playlist",
                        style = Fr33Typography.labelSmall,
                        color = ColorTextTertiary,
                        modifier = Modifier.padding(bottom = Fr33Spacing.small)
                    )
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(playlists, key = { it.id }) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(Fr33Radius.small))
                                    .combinedClickable(
                                        onClick = { onSelectPlaylist(playlist.id) },
                                        onLongClick = { playlistToDelete = playlist }
                                    )
                                    .padding(vertical = Fr33Spacing.small, horizontal = Fr33Spacing.extraSmall),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QueueMusic,
                                    contentDescription = null,
                                    tint = ColorPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(Fr33Spacing.medium))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = playlist.name,
                                        style = Fr33Typography.titleMedium,
                                        color = ColorTextPrimary
                                    )
                                    Text(
                                        text = "${playlist.songCount} canciones",
                                        style = Fr33Typography.bodySmall,
                                        color = ColorTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onCreateNewPlaylist()
                }
            ) {
                Text("+ Nueva Playlist", color = ColorPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = ColorTextSecondary)
            }
        },
        containerColor = ColorSurface
    )
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Nueva Playlist", color = ColorTextPrimary) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre de la playlist") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onCreate(name.trim()) },
                enabled = name.isNotBlank()
            ) {
                Text("Crear", color = ColorPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = ColorTextSecondary)
            }
        },
        containerColor = ColorSurface
    )
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
