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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import com.fr33music.core.designsystem.component.SelectionBottomBar
import com.fr33music.core.designsystem.component.SelectionTopBar
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
    val isLoadingMoreChannel by viewModel.isLoadingMoreChannel.collectAsStateWithLifecycle()
    val isSearchingInChannel by viewModel.isSearchingInChannel.collectAsStateWithLifecycle()
    val channelSearchQuery by viewModel.channelSearchQuery.collectAsStateWithLifecycle()
    val channelHasMore by viewModel.channelHasMore.collectAsStateWithLifecycle()
    val channelError by viewModel.channelError.collectAsStateWithLifecycle()
    val channelTotalVideos by viewModel.channelTotalVideos.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    var songForPlaylistDialog by remember { mutableStateOf<Track?>(null) }
    var bulkTracksForPlaylistDialog by remember { mutableStateOf<List<Track>?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var selectedTracks by remember { mutableStateOf<Map<String, Track>>(emptyMap()) }
    val isSelectionMode = selectedTracks.isNotEmpty()
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
    BackHandler(enabled = isSelectionMode) {
        selectedTracks = emptyMap()
    }
    BackHandler(enabled = isSearchFocused && !isSelectionMode) {
        clearSearchFocus()
    }

    // Pantalla de detalle de canal
    if (selectedChannel != null) {
        val currentChannel = selectedChannel!!
        SearchChannelDetailView(
            channel = currentChannel,
            videos = channelVideos,
            totalVideos = channelTotalVideos,
            isLoading = isLoadingChannel,
            isLoadingMore = isLoadingMoreChannel,
            isSearching = isSearchingInChannel,
            searchQuery = channelSearchQuery,
            hasMore = channelHasMore,
            channelError = channelError,
            savedTrackIds = savedTrackIds,
            onBack = { viewModel.selectChannel(null) },
            onSearchQueryChange = viewModel::searchInChannel,
            onLoadMore = viewModel::loadMoreChannelVideos,
            onRetryLoadMore = viewModel::retryLoadMoreChannelVideos,
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
            onBulkAddToPlaylist = { tracks -> bulkTracksForPlaylistDialog = tracks },
            isSelectionMode = isSelectionMode,
            selectedTrackIds = selectedTracks.keys,
            onToggleSelection = { track ->
                selectedTracks = if (selectedTracks.containsKey(track.trackId)) {
                    selectedTracks - track.trackId
                } else {
                    selectedTracks + (track.trackId to track)
                }
            },
            onSelectAll = { list -> selectedTracks = list.associateBy { it.trackId } },
            onCancelSelection = { selectedTracks = emptyMap() },
            onBulkAddToFavorites = {
                val list = selectedTracks.values.toList()
                viewModel.saveTracks(list)
                Toast.makeText(
                    context,
                    "${list.size} ${if (list.size == 1) "canción añadida" else "canciones añadidas"} a Favoritos",
                    Toast.LENGTH_SHORT
                ).show()
                selectedTracks = emptyMap()
            }
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
            onBulkAddToPlaylist = { tracks -> bulkTracksForPlaylistDialog = tracks },
            isSelectionMode = isSelectionMode,
            selectedTrackIds = selectedTracks.keys,
            onToggleSelection = { track ->
                selectedTracks = if (selectedTracks.containsKey(track.trackId)) {
                    selectedTracks - track.trackId
                } else {
                    selectedTracks + (track.trackId to track)
                }
            },
            onSelectAll = { list -> selectedTracks = list.associateBy { it.trackId } },
            onCancelSelection = { selectedTracks = emptyMap() },
            onBulkAddToFavorites = {
                val list = selectedTracks.values.toList()
                viewModel.saveTracks(list)
                Toast.makeText(
                    context,
                    "${list.size} ${if (list.size == 1) "canción añadida" else "canciones añadidas"} a Favoritos",
                    Toast.LENGTH_SHORT
                ).show()
                selectedTracks = emptyMap()
            }
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
        // Search Input Bar or Selection Top Bar
        if (isSelectionMode) {
            SelectionTopBar(
                selectedCount = selectedTracks.size,
                onSelectAll = {
                    val allVisible = when (val st = uiState) {
                        is SearchUiState.LookingSuccess -> st.result.videos
                        is SearchUiState.Success -> st.tracks
                        else -> emptyList()
                    }
                    selectedTracks = allVisible.associateBy { it.trackId }
                },
                onCancel = { selectedTracks = emptyMap() }
            )
        } else {
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
                        isSelectionMode = isSelectionMode,
                        selectedTrackIds = selectedTracks.keys,
                        onChannelClick = { channel ->
                            if (!isSelectionMode) {
                                clearSearchFocus()
                                viewModel.selectChannel(channel)
                            }
                        },
                        onPlaylistClick = { playlist ->
                            if (!isSelectionMode) {
                                clearSearchFocus()
                                viewModel.selectAlbum(playlist)
                            }
                        },
                        onTrackClick = { track ->
                            if (isSelectionMode) {
                                selectedTracks = if (selectedTracks.containsKey(track.trackId)) {
                                    selectedTracks - track.trackId
                                } else {
                                    selectedTracks + (track.trackId to track)
                                }
                            } else {
                                clearSearchFocus()
                                viewModel.onTrackClick(track)
                                onTrackSelected(track)
                            }
                        },
                        onTrackLongClick = { track ->
                            clearSearchFocus()
                            selectedTracks = selectedTracks + (track.trackId to track)
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
                            val isSelected = selectedTracks.containsKey(track.trackId)
                            TrackRow(
                                track = track,
                                isSelectionMode = isSelectionMode,
                                isSelected = isSelected,
                                onClick = {
                                    if (isSelectionMode) {
                                        selectedTracks = if (isSelected) {
                                            selectedTracks - track.trackId
                                        } else {
                                            selectedTracks + (track.trackId to track)
                                        }
                                    } else {
                                        clearSearchFocus()
                                        viewModel.onTrackClick(track)
                                        onTrackSelected(track)
                                    }
                                },
                                onLongClick = {
                                    clearSearchFocus()
                                    selectedTracks = selectedTracks + (track.trackId to track)
                                },
                                trailingContent = if (isSelectionMode) null else {
                                    {
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

            // Sugerencias e historial flotante cuando la barra de búsqueda está activa
            if (isSearchFocused && !isSelectionMode && (recentSearches.isNotEmpty() || suggestions.isNotEmpty())) {
                SearchSuggestionsOverlay(
                    recentSearches = recentSearches,
                    suggestions = suggestions,
                    onSelectQuery = { selectedQuery ->
                        clearSearchFocus()
                        viewModel.search(selectedQuery)
                    },
                    onDeleteRecent = { itemQuery ->
                        viewModel.deleteSearchHistoryItem(itemQuery)
                    }
                )
            }
        }

        if (isSelectionMode) {
            SelectionBottomBar(
                onAddToPlaylist = {
                    bulkTracksForPlaylistDialog = selectedTracks.values.toList()
                },
                onAddToFavorites = {
                    val tracksToSave = selectedTracks.values.toList()
                    viewModel.saveTracks(tracksToSave)
                    Toast.makeText(
                        context,
                        "${tracksToSave.size} ${if (tracksToSave.size == 1) "canción añadida" else "canciones añadidas"} a Favoritos",
                        Toast.LENGTH_SHORT
                    ).show()
                    selectedTracks = emptyMap()
                },
                onDelete = null
            )
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
                selectedTracks = emptyMap()
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
    isSelectionMode: Boolean = false,
    selectedTrackIds: Set<String> = emptySet(),
    onChannelClick: (ChannelCard) -> Unit,
    onPlaylistClick: (AlbumCard) -> Unit,
    onTrackClick: (Track) -> Unit,
    onTrackLongClick: (Track) -> Unit = {},
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
                val isSelected = selectedTrackIds.contains(track.trackId)
                TrackRow(
                    track = track,
                    isSelectionMode = isSelectionMode,
                    isSelected = isSelected,
                    onClick = { onTrackClick(track) },
                    onLongClick = { onTrackLongClick(track) },
                    trailingContent = if (isSelectionMode) null else {
                        {
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
    totalVideos: Long? = null,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    isSearching: Boolean,
    searchQuery: String,
    hasMore: Boolean,
    channelError: String?,
    savedTrackIds: Set<String>,
    onBack: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    onTrackClick: (Track, List<Track>, Int) -> Unit,
    onPlayAll: (List<Track>, Boolean) -> Unit = { _, _ -> },
    onToggleSave: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    onBulkAddToPlaylist: (List<Track>) -> Unit = {},
    isSelectionMode: Boolean = false,
    selectedTrackIds: Set<String> = emptySet(),
    onToggleSelection: (Track) -> Unit = {},
    onSelectAll: (List<Track>) -> Unit = {},
    onCancelSelection: () -> Unit = {},
    onBulkAddToFavorites: () -> Unit = {}
) {
    val listState = rememberLazyListState()

    // Scroll infinito: solicitar más cuando el usuario esté a 3 elementos del final
    val shouldLoadMore = remember(videos.size, isLoading, isLoadingMore, isSearching, hasMore) {
        derivedStateOf {
            if (isLoading || isLoadingMore || isSearching || !hasMore || videos.isEmpty()) {
                return@derivedStateOf false
            }
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
            lastVisible.index >= listState.layoutInfo.totalItemsCount - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            onLoadMore()
        }
    }

    BackHandler {
        if (isSelectionMode) onCancelSelection() else onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        if (isSelectionMode) {
            SelectionTopBar(
                selectedCount = selectedTrackIds.size,
                onSelectAll = { onSelectAll(videos) },
                onCancel = onCancelSelection
            )
        } else {
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
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
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
                    IconButton(onClick = { onSearchQueryChange("") }) {
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
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
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
                        val headerTitle = if (searchQuery.isNotBlank()) {
                            "Resultados de búsqueda (${videos.size})"
                        } else {
                            val count = totalVideos ?: videos.size.toLong()
                            val countStr = java.text.NumberFormat.getIntegerInstance().format(count)
                            "Vídeos subidos ($countStr)"
                        }
                        Text(
                            text = headerTitle,
                            style = Fr33Typography.titleMedium,
                            color = ColorTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (isLoading || isSearching) {
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
                        val emptyText = if (searchQuery.isNotBlank()) {
                            "No se encontraron vídeos que coincidan con \"$searchQuery\" en este canal"
                        } else {
                            "No se encontraron vídeos disponibles en este canal"
                        }
                        Text(
                            text = emptyText,
                            style = Fr33Typography.bodyMedium,
                            color = ColorTextTertiary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                itemsIndexed(
                    items = videos,
                    key = { index, track -> "${track.trackId}_$index" }
                ) { index, track ->
                    val isSelected = selectedTrackIds.contains(track.trackId)
                    TrackRow(
                        track = track,
                        isSelectionMode = isSelectionMode,
                        isSelected = isSelected,
                        onClick = {
                            if (isSelectionMode) {
                                onToggleSelection(track)
                            } else {
                                onTrackClick(track, videos, index)
                            }
                        },
                        onLongClick = { onToggleSelection(track) },
                        trailingContent = if (isSelectionMode) null else {
                            {
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
                        }
                    )
                    HorizontalDivider(
                        color = ColorDivider,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 72.dp)
                    )
                }

                // Indicador de carga al final para más elementos (scroll infinito)
                if (isLoadingMore) {
                    item(key = "loading_more_channel_videos") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Fr33Spacing.large),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.5.dp,
                                color = ColorPrimary
                            )
                        }
                    }
                }

                // Botón de reintento si falló la paginación
                if (channelError != null) {
                    item(key = "error_channel_load_more") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Fr33Spacing.medium),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = channelError,
                                style = Fr33Typography.bodySmall,
                                color = ColorError,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(Fr33Spacing.small))
                            OutlinedButton(
                                onClick = onRetryLoadMore,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorPrimary)
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        }

        if (isSelectionMode) {
            SelectionBottomBar(
                onAddToPlaylist = {
                    val selected = videos.filter { selectedTrackIds.contains(it.trackId) }
                    onBulkAddToPlaylist(selected)
                },
                onAddToFavorites = onBulkAddToFavorites,
                onDelete = null
            )
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
    onBulkAddToPlaylist: (List<Track>) -> Unit = {},
    isSelectionMode: Boolean = false,
    selectedTrackIds: Set<String> = emptySet(),
    onToggleSelection: (Track) -> Unit = {},
    onSelectAll: (List<Track>) -> Unit = {},
    onCancelSelection: () -> Unit = {},
    onBulkAddToFavorites: () -> Unit = {}
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

    BackHandler {
        if (isSelectionMode) onCancelSelection() else onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        if (isSelectionMode) {
            SelectionTopBar(
                selectedCount = selectedTrackIds.size,
                onSelectAll = { onSelectAll(filteredTracks) },
                onCancel = onCancelSelection
            )
        } else {
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
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
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
                    val isSelected = selectedTrackIds.contains(track.trackId)
                    TrackRow(
                        track = track,
                        isSelectionMode = isSelectionMode,
                        isSelected = isSelected,
                        onClick = {
                            if (isSelectionMode) {
                                onToggleSelection(track)
                            } else {
                                onTrackClick(track, filteredTracks, index)
                            }
                        },
                        onLongClick = { onToggleSelection(track) },
                        trailingContent = if (isSelectionMode) null else {
                            {
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

        if (isSelectionMode) {
            SelectionBottomBar(
                onAddToPlaylist = {
                    val selected = filteredTracks.filter { selectedTrackIds.contains(it.trackId) }
                    onBulkAddToPlaylist(selected)
                },
                onAddToFavorites = onBulkAddToFavorites,
                onDelete = null
            )
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
                    val rootPlaylists = remember(playlists) { playlists.filter { it.parentPlaylistId == null } }
                    val subPlaylistsByParent = remember(playlists) {
                        playlists.filter { it.parentPlaylistId != null }.groupBy { it.parentPlaylistId }
                    }
                    var expandedPlaylistIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(rootPlaylists, key = { it.id }) { playlist ->
                            val children = subPlaylistsByParent[playlist.id].orEmpty()
                            val isExpanded = expandedPlaylistIds.contains(playlist.id)

                            Column(modifier = Modifier.fillMaxWidth()) {
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
                                        val subText = if (children.isNotEmpty()) {
                                            "${playlist.songCount} canciones • ${children.size} ${if (children.size == 1) "sub-playlist" else "sub-playlists"}"
                                        } else {
                                            "${playlist.songCount} canciones"
                                        }
                                        Text(
                                            text = subText,
                                            style = Fr33Typography.bodySmall,
                                            color = ColorTextSecondary
                                        )
                                    }
                                    if (children.isNotEmpty()) {
                                        IconButton(
                                            onClick = {
                                                expandedPlaylistIds = if (isExpanded) {
                                                    expandedPlaylistIds - playlist.id
                                                } else {
                                                    expandedPlaylistIds + playlist.id
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = if (isExpanded) "Contraer" else "Expandir",
                                                tint = ColorPrimary
                                            )
                                        }
                                    }
                                }

                                if (isExpanded && children.isNotEmpty()) {
                                    children.forEach { sub ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 28.dp)
                                                .clip(RoundedCornerShape(Fr33Radius.small))
                                                .combinedClickable(
                                                    onClick = { onSelectPlaylist(sub.id, sub.name) },
                                                    onLongClick = { playlistToDelete = sub }
                                                )
                                                .padding(vertical = Fr33Spacing.small, horizontal = Fr33Spacing.extraSmall),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Folder,
                                                contentDescription = null,
                                                tint = ColorPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(Fr33Spacing.medium))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = sub.name,
                                                    style = Fr33Typography.bodyLarge,
                                                    color = ColorTextPrimary
                                                )
                                                Text(
                                                    text = "${sub.songCount} canciones",
                                                    style = Fr33Typography.bodySmall,
                                                    color = ColorTextSecondary
                                                )
                                            }
                                        }
                                    }
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
private fun SearchSuggestionsOverlay(
    recentSearches: List<String>,
    suggestions: List<String>,
    onSelectQuery: (String) -> Unit,
    onDeleteRecent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Fr33Spacing.large)
            .padding(bottom = Fr33Spacing.medium),
        colors = CardDefaults.cardColors(containerColor = ColorSurface),
        shape = RoundedCornerShape(Fr33Radius.large),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Fr33Spacing.small)
        ) {
            // 1. Historial de búsquedas recientes (máximo 3)
            if (recentSearches.isNotEmpty()) {
                item {
                    Text(
                        text = "Búsquedas recientes",
                        style = Fr33Typography.labelSmall,
                        color = ColorTextTertiary,
                        modifier = Modifier.padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.extraSmall)
                    )
                }

                items(recentSearches, key = { "recent_$it" }) { itemQuery ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectQuery(itemQuery) }
                            .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Historial",
                            tint = ColorTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Fr33Spacing.medium))
                        Text(
                            text = itemQuery,
                            style = Fr33Typography.bodyMedium,
                            color = ColorTextPrimary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(
                            onClick = { onDeleteRecent(itemQuery) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar de historial",
                                tint = ColorTextTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 2. Sugerencias de YouTube en tiempo real
            if (suggestions.isNotEmpty()) {
                if (recentSearches.isNotEmpty()) {
                    item {
                        HorizontalDivider(
                            color = ColorDivider,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.extraSmall)
                        )
                    }
                }

                item {
                    Text(
                        text = "Sugerencias de YouTube",
                        style = Fr33Typography.labelSmall,
                        color = ColorTextTertiary,
                        modifier = Modifier.padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.extraSmall)
                    )
                }

                items(suggestions, key = { "suggestion_$it" }) { suggestion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectQuery(suggestion) }
                            .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Sugerencia",
                            tint = ColorTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Fr33Spacing.medium))
                        Text(
                            text = suggestion,
                            style = Fr33Typography.bodyMedium,
                            color = ColorTextPrimary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
