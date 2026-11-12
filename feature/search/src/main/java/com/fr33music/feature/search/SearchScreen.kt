package com.fr33music.feature.search

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fr33music.core.database.dao.PlaylistWithSongCount
import com.fr33music.core.designsystem.component.ErrorView
import com.fr33music.core.designsystem.component.LoadingSkeleton
import com.fr33music.core.designsystem.component.TrackRow
import com.fr33music.core.designsystem.component.TrackThumbnail
import com.fr33music.core.designsystem.theme.ColorBackground
import com.fr33music.core.designsystem.theme.ColorDivider
import com.fr33music.core.designsystem.theme.ColorError
import com.fr33music.core.designsystem.theme.ColorPrimary
import com.fr33music.core.designsystem.theme.ColorSurface
import com.fr33music.core.designsystem.theme.ColorTextPrimary
import com.fr33music.core.designsystem.theme.ColorTextSecondary
import com.fr33music.core.designsystem.theme.ColorTextTertiary
import com.fr33music.core.designsystem.theme.Fr33Radius
import com.fr33music.core.designsystem.theme.Fr33Spacing
import com.fr33music.core.designsystem.theme.Fr33Typography
import com.fr33music.core.domain.model.AlbumCard
import com.fr33music.core.domain.model.ChannelCard
import com.fr33music.core.domain.model.LookingSearchResult
import com.fr33music.core.domain.model.Track

@Composable
fun SearchScreen(
    onTrackSelected: (Track) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedTrackIds by viewModel.savedTrackIds.collectAsStateWithLifecycle()
    val selectedAlbum by viewModel.selectedAlbum.collectAsStateWithLifecycle()
    val albumTracks by viewModel.albumTracks.collectAsStateWithLifecycle()
    val isLoadingAlbum by viewModel.isLoadingAlbum.collectAsStateWithLifecycle()
    val selectedChannel by viewModel.selectedChannel.collectAsStateWithLifecycle()
    val channelVideos by viewModel.channelVideos.collectAsStateWithLifecycle()
    val isLoadingChannel by viewModel.isLoadingChannel.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    var songForPlaylistDialog by remember { mutableStateOf<Track?>(null) }
    var bulkTracksForPlaylistDialog by remember { mutableStateOf<List<Track>?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val textToolbar = LocalTextToolbar.current
    var isSearchFocused by remember { mutableStateOf(false) }

    fun clearSearchFocus() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        textToolbar?.hide()
    }

    val context = LocalContext.current
    val onDownloadAll: (List<Track>) -> Unit = { tracksToDownload ->
        if (tracksToDownload.isNotEmpty()) {
            viewModel.downloadAll(tracksToDownload)
            Toast.makeText(
                context,
                "Descargando ${tracksToDownload.size} ${if (tracksToDownload.size == 1) "canción" else "canciones"}...",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Limpia el foco y oculta el teclado si el usuario presiona Atrás mientras busca
    BackHandler(enabled = isSearchFocused) {
        clearSearchFocus()
    }

    // Pantalla de detalle de canal
    if (selectedChannel != null) {
        val currentChannel = selectedChannel!!
        SearchChannelDetailView(
            channel = currentChannel,
            videos = channelVideos,
            isLoading = isLoadingChannel,
            savedTrackIds = savedTrackIds,
            onBack = { viewModel.selectChannel(null) },
            onTrackClick = { track, list, index ->
                viewModel.playAll(list, index)
                onTrackSelected(track)
            },
            onPlayAll = { list, shuffle ->
                viewModel.playAll(list, 0, shuffle)
                list.firstOrNull()?.let { onTrackSelected(it) }
            },
            onToggleSave = viewModel::toggleSave,
            onAddToPlaylist = { track -> songForPlaylistDialog = track },
            onBulkAddToPlaylist = { tracks -> bulkTracksForPlaylistDialog = tracks }
        )
    } else if (selectedAlbum != null) {
        // Pantalla de detalle de álbum/playlist
        val currentAlbum = selectedAlbum!!
        SearchAlbumDetailView(
            album = currentAlbum,
            tracks = albumTracks,
            isLoading = isLoadingAlbum,
            savedTrackIds = savedTrackIds,
            initialScrollIndex = viewModel.albumScrollIndex,
            initialScrollOffset = viewModel.albumScrollOffset,
            onSaveState = { index, offset ->
                viewModel.albumScrollIndex = index
                viewModel.albumScrollOffset = offset
            },
            onBack = {
                viewModel.albumScrollIndex = 0
                viewModel.albumScrollOffset = 0
                viewModel.selectAlbum(null)
            },
            onTrackClick = { track, list, index ->
                viewModel.playAll(list, index)
                onTrackSelected(track)
            },
            onPlayAll = { tracks, shuffle ->
                if (tracks.isNotEmpty()) {
                    viewModel.playAll(tracks, startIndex = 0, shuffle = shuffle)
                    onTrackSelected(tracks.first())
                }
            },
            onDownloadAll = onDownloadAll,
            onToggleSave = viewModel::toggleSave,
            onAddToPlaylist = { track -> songForPlaylistDialog = track },
            onBulkAddToPlaylist = { tracks -> bulkTracksForPlaylistDialog = tracks }
        )
    } else {
        // Pantalla principal del buscador
        DisposableEffect(Unit) {
            onDispose {
                clearSearchFocus()
            }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(ColorBackground)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            clearSearchFocus()
                        }
                    )
                }
        ) {
        // Search Input Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.medium)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isSearchFocused = it.isFocused },
                placeholder = {
                    Text(
                        text = "Buscar canciones, artistas o canales...",
                        style = Fr33Typography.bodyLarge,
                        color = ColorTextTertiary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = ColorTextSecondary
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar búsqueda",
                                tint = ColorTextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(Fr33Radius.large),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ColorSurface,
                    unfocusedContainerColor = ColorSurface,
                    focusedTextColor = ColorTextPrimary,
                    unfocusedTextColor = ColorTextPrimary,
                    focusedBorderColor = ColorPrimary,
                    unfocusedBorderColor = ColorDivider,
                    cursorColor = ColorPrimary
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        clearSearchFocus()
                        viewModel.search()
                    }
                )
            )
        }

        // Search Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    SearchIdleView()
                }
                is SearchUiState.Loading -> {
                    LoadingSkeleton(itemCount = 8)
                }
                is SearchUiState.LookingSuccess -> {
                    LookingResultsView(
                        result = state.result,
                        savedTrackIds = savedTrackIds,
                        onChannelClick = { channel ->
                            clearSearchFocus()
                            viewModel.selectChannel(channel)
                        },
                        onPlaylistClick = { playlist ->
                            clearSearchFocus()
                            viewModel.selectAlbum(playlist)
                        },
                        onTrackClick = { track ->
                            clearSearchFocus()
                            viewModel.onTrackClick(track)
                            onTrackSelected(track)
                        },
                        onToggleSave = viewModel::toggleSave,
                        onAddToPlaylist = { track ->
                            songForPlaylistDialog = track
                        }
                    )
                }
                is SearchUiState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.tracks, key = { it.trackId }) { track ->
                            TrackRow(
                                track = track,
                                onClick = {
                                    clearSearchFocus()
                                    viewModel.onTrackClick(track)
                                    onTrackSelected(track)
                                },
                                trailingContent = {
                                    val isSaved = savedTrackIds.contains(track.trackId)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { viewModel.toggleSave(track) }) {
                                            Icon(
                                                imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = if (isSaved) "Guardado" else "Guardar",
                                                tint = if (isSaved) ColorPrimary else ColorTextSecondary
                                            )
                                        }
                                        IconButton(onClick = { songForPlaylistDialog = track }) {
                                            Icon(
                                                imageVector = Icons.Default.PlaylistAdd,
                                                contentDescription = "Añadir a playlist",
                                                tint = ColorTextSecondary
                                            )
                                        }
                                    }
                                }
                            )
                            HorizontalDivider(
                                color = ColorDivider,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(start = 72.dp)
                            )
                        }
                    }
                }
                is SearchUiState.Empty -> {
                    SearchEmptyView(query = query)
                }
                is SearchUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = viewModel::retry
                    )
                }
            }
        }
    }
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

    // Diálogo para añadir canción a una playlist
    if (songForPlaylistDialog != null) {
        val targetTrack = songForPlaylistDialog!!
        AddToPlaylistDialog(
            itemDescription = "${targetTrack.title} - ${targetTrack.artistName}",
            playlists = playlists,
            onDismiss = { songForPlaylistDialog = null },
            onSelectPlaylist = { playlistId, playlistName ->
                viewModel.addSongToPlaylist(playlistId, targetTrack)
                Toast.makeText(context, "Añadida a $playlistName", Toast.LENGTH_SHORT).show()
                songForPlaylistDialog = null
            },
            onDeletePlaylist = { playlistId ->
                viewModel.deletePlaylist(playlistId)
            },
            onCreateNewPlaylist = {
                showCreatePlaylistDialog = true
            }
        )
    }

    // Diálogo para añadir canciones en masa a una playlist
    if (bulkTracksForPlaylistDialog != null) {
        val targetTracks = bulkTracksForPlaylistDialog!!
        AddToPlaylistDialog(
            itemDescription = "${targetTracks.size} canciones seleccionadas",
            playlists = playlists,
            onDismiss = { bulkTracksForPlaylistDialog = null },
            onSelectPlaylist = { playlistId, playlistName ->
                viewModel.addSongsToPlaylist(playlistId, targetTracks)
                Toast.makeText(
                    context,
                    "${targetTracks.size} canciones añadidas a $playlistName",
                    Toast.LENGTH_SHORT
                ).show()
                bulkTracksForPlaylistDialog = null
            },
            onDeletePlaylist = { playlistId ->
                viewModel.deletePlaylist(playlistId)
            },
            onCreateNewPlaylist = {
                showCreatePlaylistDialog = true
            }
        )
    }
}

/**
 * Vista de resultados de búsqueda:
 * 1. Canales en la parte superior en un LazyRow (círculos con foto y nombre)
 * 2. Listas de reproducción en un LazyRow con formato de tarjeta
 * 3. Vídeos / canciones en una lista vertical
 */
@Composable
private fun LookingResultsView(
    result: LookingSearchResult,
    savedTrackIds: Set<String>,
    onChannelClick: (ChannelCard) -> Unit,
    onPlaylistClick: (AlbumCard) -> Unit,
    onTrackClick: (Track) -> Unit,
    onToggleSave: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit
) {
    if (result.channels.isEmpty() && result.playlists.isEmpty() && result.videos.isEmpty()) {
        SearchEmptyView(query = "")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Fr33Spacing.extraLarge)
    ) {
        // 1. Canales (Bolitas en LazyRow horizontal)
        if (result.channels.isNotEmpty()) {
            item {
                Text(
                    text = "Canales",
                    style = Fr33Typography.titleMedium,
                    color = ColorTextPrimary,
                    modifier = Modifier.padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = Fr33Spacing.large),
                    horizontalArrangement = Arrangement.spacedBy(Fr33Spacing.large)
                ) {
                    itemsIndexed(
                        items = result.channels,
                        key = { index, channel -> "${channel.channelId}_$index" }
                    ) { _, channel ->
                        ChannelCardItem(
                            channel = channel,
                            onClick = { onChannelClick(channel) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Fr33Spacing.large))
            }
        }

        // 2. Listas de reproducción (Tarjetas en LazyRow horizontal)
        if (result.playlists.isNotEmpty()) {
            item {
                Text(
                    text = "Listas de reproducción",
                    style = Fr33Typography.titleMedium,
                    color = ColorTextPrimary,
                    modifier = Modifier.padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = Fr33Spacing.large),
                    horizontalArrangement = Arrangement.spacedBy(Fr33Spacing.medium)
                ) {
                    itemsIndexed(
                        items = result.playlists,
                        key = { index, playlist -> "${playlist.url?.ifBlank { playlist.title } ?: playlist.title}_$index" }
                    ) { _, playlist ->
                        AlbumCardItem(
                            album = playlist,
                            onClick = { onPlaylistClick(playlist) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Fr33Spacing.large))
            }
        }

        // 3. Vídeos (Vertical en LazyColumn)
        if (result.videos.isNotEmpty()) {
            item {
                Text(
                    text = "Vídeos",
                    style = Fr33Typography.titleMedium,
                    color = ColorTextPrimary,
                    modifier = Modifier.padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small)
                )
            }

            itemsIndexed(
                items = result.videos,
                key = { index, track -> "${track.trackId}_$index" }
            ) { _, track ->
                TrackRow(
                    track = track,
                    onClick = { onTrackClick(track) },
                    trailingContent = {
                        val isSaved = savedTrackIds.contains(track.trackId)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onToggleSave(track) }) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (isSaved) "Guardado" else "Guardar",
                                    tint = if (isSaved) ColorPrimary else ColorTextSecondary
                                )
                            }
                            IconButton(onClick = { onAddToPlaylist(track) }) {
                                Icon(
                                    imageVector = Icons.Default.PlaylistAdd,
                                    contentDescription = "Añadir a playlist",
                                    tint = ColorTextSecondary
                                )
                            }
                        }
                    }
                )
                HorizontalDivider(
                    color = ColorDivider,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 72.dp)
                )
            }
        }
    }
}

/**
 * Tarjeta individual para canales en el LazyRow (círculo con avatar y nombre debajo)
 */
@Composable
private fun ChannelCardItem(
    channel: ChannelCard,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(96.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TrackThumbnail(
            thumbnailUrl = channel.avatarUrl,
            contentDescription = channel.name,
            size = 80.dp,
            isCircular = true,
            fallbackIcon = Icons.Default.Person
        )

        Spacer(modifier = Modifier.height(Fr33Spacing.small))

        Text(
            text = channel.name.ifBlank { "Canal" },
            style = Fr33Typography.bodyMedium,
            color = ColorTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        val subText = if (channel.subscriberCount > 0) {
            formatSubscriberCount(channel.subscriberCount)
        } else {
            "Canal"
        }
        Text(
            text = subText,
            style = Fr33Typography.bodySmall,
            color = ColorTextTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Tarjeta individual para listas de reproducción en el LazyRow
 */
@Composable
private fun AlbumCardItem(
    album: AlbumCard,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick)
    ) {
        TrackThumbnail(
            thumbnailUrl = album.thumbnailUrl,
            contentDescription = album.title,
            size = 130.dp,
            shapeRadius = Fr33Radius.medium,
            fallbackIcon = Icons.Default.Album
        )

        Spacer(modifier = Modifier.height(Fr33Spacing.small))

        Text(
            text = album.title.ifBlank { "Lista de reproducción" },
            style = Fr33Typography.bodyMedium,
            color = ColorTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = when {
                album.songCount > 1L -> "${album.songCount} temas • ${album.artistName}"
                album.songCount == 1L -> "1 tema • ${album.artistName}"
                album.artistName.isNotBlank() -> album.artistName
                else -> "Lista de reproducción"
            },
            style = Fr33Typography.bodySmall,
            color = ColorTextTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatSubscriberCount(count: Long): String {
    return try {
        when {
            count >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM subs", count / 1_000_000.0)
            count >= 1_000 -> String.format(java.util.Locale.US, "%.1fK subs", count / 1_000.0)
            count > 0 -> "$count subs"
            else -> "Canal"
        }
    } catch (_: Exception) {
        "Canal"
    }
}

/**
 * Pantalla de detalle de canal al tocar un canal.
 * Solo muestra encabezado con foto, nombre y lista de vídeos subidos.
 * (Sin botones de Reproducir, Aleatorio ni Descargar).
 */
@Composable
private fun SearchChannelDetailView(
    channel: ChannelCard,
    videos: List<Track>,
    isLoading: Boolean,
    savedTrackIds: Set<String>,
    onBack: () -> Unit,
    onTrackClick: (Track, List<Track>, Int) -> Unit,
    onPlayAll: (List<Track>, Boolean) -> Unit = { _, _ -> },
    onToggleSave: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    onBulkAddToPlaylist: (List<Track>) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredVideos = remember(videos, searchQuery) {
        if (searchQuery.isBlank()) videos
        else videos.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.artistName.contains(searchQuery, ignoreCase = true)
        }
    }

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.small, vertical = Fr33Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = ColorTextPrimary
                )
            }
            Text(
                text = channel.name,
                style = Fr33Typography.titleLarge,
                color = ColorTextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (videos.isNotEmpty()) {
                IconButton(onClick = { onBulkAddToPlaylist(videos) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                        contentDescription = "Añadir todas a una playlist",
                        tint = ColorTextPrimary
                    )
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small),
            placeholder = {
                Text(
                    text = "Buscar en este canal...",
                    style = Fr33Typography.bodyMedium,
                    color = ColorTextTertiary
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = ColorTextSecondary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar",
                            tint = ColorTextSecondary
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(Fr33Radius.large),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ColorSurface,
                unfocusedContainerColor = ColorSurface,
                focusedTextColor = ColorTextPrimary,
                unfocusedTextColor = ColorTextPrimary,
                focusedBorderColor = ColorPrimary,
                unfocusedBorderColor = ColorDivider,
                cursorColor = ColorPrimary
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = Fr33Spacing.extraLarge)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Fr33Spacing.large),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TrackThumbnail(
                        thumbnailUrl = channel.avatarUrl,
                        contentDescription = channel.name,
                        size = 110.dp,
                        isCircular = true,
                        fallbackIcon = Icons.Default.Person
                    )

                    Spacer(modifier = Modifier.height(Fr33Spacing.medium))

                    Text(
                        text = channel.name,
                        style = Fr33Typography.headlineMedium,
                        color = ColorTextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(Fr33Spacing.extraSmall))

                    val subText = if (channel.subscriberCount > 0) {
                        "${formatSubscriberCount(channel.subscriberCount)} • Canal"
                    } else {
                        "Canal de YouTube"
                    }
                    Text(
                        text = subText,
                        style = Fr33Typography.bodyMedium,
                        color = ColorTextTertiary,
                        textAlign = TextAlign.Center
                    )

                    val desc = channel.description
                    if (!desc.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(Fr33Spacing.small))
                        Text(
                            text = desc,
                            style = Fr33Typography.bodySmall,
                            color = ColorTextTertiary,
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(Fr33Spacing.large))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vídeos subidos (${filteredVideos.size})",
                            style = Fr33Typography.titleMedium,
                            color = ColorTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        if (filteredVideos.isNotEmpty()) {
                            IconButton(onClick = { onPlayAll(filteredVideos, false) }) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Reproducir todo",
                                    tint = ColorPrimary
                                )
                            }
                            IconButton(onClick = { onPlayAll(filteredVideos, true) }) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = "Aleatorio",
                                    tint = ColorPrimary
                                )
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Fr33Spacing.extraLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ColorPrimary)
                    }
                }
            } else if (videos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Fr33Spacing.extraLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron vídeos disponibles en este canal",
                            style = Fr33Typography.bodyLarge,
                            color = ColorTextTertiary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (filteredVideos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Fr33Spacing.extraLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron vídeos que coincidan con la búsqueda",
                            style = Fr33Typography.bodyMedium,
                            color = ColorTextTertiary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                itemsIndexed(
                    items = filteredVideos,
                    key = { index, track -> "${track.trackId}_$index" }
                ) { index, track ->
                    TrackRow(
                        track = track,
                        onClick = { onTrackClick(track, filteredVideos, index) },
                        trailingContent = {
                            val isSaved = savedTrackIds.contains(track.trackId)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onToggleSave(track) }) {
                                    Icon(
                                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = if (isSaved) "Guardado" else "Guardar",
                                        tint = if (isSaved) ColorPrimary else ColorTextSecondary
                                    )
                                }
                                IconButton(onClick = { onAddToPlaylist(track) }) {
                                    Icon(
                                        imageVector = Icons.Default.PlaylistAdd,
                                        contentDescription = "Añadir a playlist",
                                        tint = ColorTextSecondary
                                    )
                                }
                            }
                        }
                    )
                    HorizontalDivider(
                        color = ColorDivider,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 72.dp)
                    )
                }
            }
        }
    }
}

/**
 * Pantalla de detalle de álbum/playlist al tocar una tarjeta en la búsqueda
 */
@Composable
private fun SearchAlbumDetailView(
    album: AlbumCard,
    tracks: List<Track>,
    isLoading: Boolean,
    savedTrackIds: Set<String>,
    initialScrollIndex: Int = 0,
    initialScrollOffset: Int = 0,
    onSaveState: (index: Int, offset: Int) -> Unit = { _, _ -> },
    onBack: () -> Unit,
    onTrackClick: (Track, List<Track>, Int) -> Unit,
    onPlayAll: (List<Track>, Boolean) -> Unit,
    onDownloadAll: (List<Track>) -> Unit = {},
    onToggleSave: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    onBulkAddToPlaylist: (List<Track>) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredTracks = remember(tracks, searchQuery) {
        if (searchQuery.isBlank()) tracks
        else tracks.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.artistName.contains(searchQuery, ignoreCase = true)
        }
    }
    val listState = rememberLazyListState(initialScrollIndex, initialScrollOffset)

    DisposableEffect(listState) {
        onDispose {
            onSaveState(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
        }
    }

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.small, vertical = Fr33Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = ColorTextPrimary
                )
            }
            Text(
                text = album.title,
                style = Fr33Typography.titleLarge,
                color = ColorTextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (tracks.isNotEmpty()) {
                IconButton(onClick = { onBulkAddToPlaylist(tracks) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                        contentDescription = "Añadir todas a una playlist",
                        tint = ColorTextPrimary
                    )
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small),
            placeholder = {
                Text(
                    text = "Buscar en esta lista...",
                    style = Fr33Typography.bodyMedium,
                    color = ColorTextTertiary
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = ColorTextSecondary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar",
                            tint = ColorTextSecondary
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(Fr33Radius.large),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ColorSurface,
                unfocusedContainerColor = ColorSurface,
                focusedTextColor = ColorTextPrimary,
                unfocusedTextColor = ColorTextPrimary,
                focusedBorderColor = ColorPrimary,
                unfocusedBorderColor = ColorDivider,
                cursorColor = ColorPrimary
            )
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = Fr33Spacing.extraLarge)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Fr33Spacing.large),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TrackThumbnail(
                        thumbnailUrl = album.thumbnailUrl,
                        contentDescription = album.title,
                        size = 140.dp,
                        shapeRadius = Fr33Radius.large,
                        fallbackIcon = Icons.Default.Album
                    )

                    Spacer(modifier = Modifier.height(Fr33Spacing.medium))

                    Text(
                        text = album.title,
                        style = Fr33Typography.headlineMedium,
                        color = ColorTextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(Fr33Spacing.extraSmall))

                    Text(
                        text = if (tracks.isNotEmpty()) {
                            "${album.artistName} • ${tracks.size} canciones"
                        } else {
                            album.artistName
                        },
                        style = Fr33Typography.bodyMedium,
                        color = ColorTextTertiary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(Fr33Spacing.large))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Canciones (${filteredTracks.size})",
                            style = Fr33Typography.titleMedium,
                            color = ColorTextPrimary
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Fr33Spacing.extraLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ColorPrimary)
                    }
                }
            } else if (tracks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Fr33Spacing.extraLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron canciones en esta lista",
                            style = Fr33Typography.bodyLarge,
                            color = ColorTextTertiary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (filteredTracks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Fr33Spacing.extraLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron canciones que coincidan con la búsqueda",
                            style = Fr33Typography.bodyMedium,
                            color = ColorTextTertiary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                itemsIndexed(
                    items = filteredTracks,
                    key = { index, track -> "${track.trackId}_$index" }
                ) { index, track ->
                    TrackRow(
                        track = track,
                        onClick = { onTrackClick(track, filteredTracks, index) },
                        trailingContent = {
                            val isSaved = savedTrackIds.contains(track.trackId)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onToggleSave(track) }) {
                                    Icon(
                                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = if (isSaved) "Guardado" else "Guardar",
                                        tint = if (isSaved) ColorPrimary else ColorTextSecondary
                                    )
                                }
                                IconButton(onClick = { onAddToPlaylist(track) }) {
                                    Icon(
                                        imageVector = Icons.Default.PlaylistAdd,
                                        contentDescription = "Añadir a playlist",
                                        tint = ColorTextSecondary
                                    )
                                }
                            }
                        }
                    )
                    HorizontalDivider(
                        color = ColorDivider,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 72.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchIdleView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Fr33Spacing.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = ColorTextTertiary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(Fr33Spacing.large))

        Text(
            text = "Busca canciones, artistas o canales",
            style = Fr33Typography.titleLarge,
            color = ColorTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Fr33Spacing.small))

        Text(
            text = "Encuentra tus pistas, álbumes y canales favoritos de YouTube",
            style = Fr33Typography.bodyMedium,
            color = ColorTextTertiary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SearchEmptyView(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Fr33Spacing.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = ColorTextTertiary,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(Fr33Spacing.large))

        Text(
            text = if (query.isNotBlank()) "Sin resultados para \"$query\"" else "No se encontraron resultados",
            style = Fr33Typography.titleMedium,
            color = ColorTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Fr33Spacing.small))

        Text(
            text = "Prueba con otras palabras clave o el nombre del canal",
            style = Fr33Typography.bodyMedium,
            color = ColorTextTertiary,
            textAlign = TextAlign.Center
        )
    }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AddToPlaylistDialog(
    itemDescription: String,
    playlists: List<PlaylistWithSongCount>,
    onDismiss: () -> Unit,
    onSelectPlaylist: (Long, String) -> Unit,
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
                    text = itemDescription,
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
                                        onClick = { onSelectPlaylist(playlist.id, playlist.name) },
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
