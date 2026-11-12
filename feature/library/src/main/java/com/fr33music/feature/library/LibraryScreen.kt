package com.fr33music.feature.library

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fr33music.core.database.dao.PlaylistWithSongCount
import com.fr33music.core.database.entity.TrackEntity
import com.fr33music.core.designsystem.component.TrackRow
import com.fr33music.core.designsystem.component.TrackThumbnail
import com.fr33music.core.designsystem.theme.ColorBackground
import com.fr33music.core.designsystem.theme.ColorDivider
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

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var songForPlaylistDialog by remember { mutableStateOf<Track?>(null) }
    var artistToDelete by remember { mutableStateOf<String?>(null) }
    var playlistToExport by remember { mutableStateOf<Pair<String, List<TrackEntity>>?>(null) }

    val context = LocalContext.current

    val singlePlaylistExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && playlistToExport != null) {
            val (name, tracks) = playlistToExport!!
            viewModel.exportSinglePlaylistToUri(context, uri, name, tracks) { success ->
                Toast.makeText(
                    context,
                    if (success) "Playlist exportada con éxito" else "Error al exportar playlist",
                    Toast.LENGTH_SHORT
                ).show()
            }
            playlistToExport = null
        }
    }

    val exportAllLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportAllPlaylistsToUri(context, uri) { success ->
                Toast.makeText(
                    context,
                    if (success) "Copia de seguridad guardada con éxito" else "Error al exportar playlists",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val importPlaylistsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importPlaylistsFromUri(context, uri) { success, message ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Interceptar botón atrás físico para volver de la vista de detalle en lugar de cerrar la app
    BackHandler(enabled = uiState.selectedArtist != null) {
        viewModel.selectArtist(null)
    }
    BackHandler(enabled = uiState.selectedPlaylistId != null) {
        viewModel.selectPlaylist(null)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Biblioteca",
                style = Fr33Typography.headlineMedium,
                color = ColorTextPrimary
            )

            if (uiState.selectedArtist == null && uiState.selectedPlaylistId == null) {
                SortFilterMenu(
                    sortOrder = uiState.sortOrder,
                    onSortOrderSelected = viewModel::setSortOrder
                )
            }
        }

        // Chips de las 4 pestañas: Artistas, Álbumes, Playlists, Todas las canciones
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = Fr33Spacing.large),
            horizontalArrangement = Arrangement.spacedBy(Fr33Spacing.small)
        ) {
            items(LibraryTab.entries) { tab ->
                val selected = uiState.selectedTab == tab
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.selectTab(tab) },
                    label = { Text(text = tab.title) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ColorPrimary,
                        selectedLabelColor = ColorTextPrimary,
                        containerColor = ColorSurface,
                        labelColor = ColorTextSecondary
                    ),
                    border = null,
                    shape = RoundedCornerShape(Fr33Radius.large)
                )
            }
        }

        Spacer(modifier = Modifier.height(Fr33Spacing.medium))

        // Contenido según la pestaña seleccionada
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (uiState.selectedTab) {
                LibraryTab.ARTISTS -> {
                    val selectedArtist = uiState.selectedArtist
                    if (selectedArtist != null) {
                        val artistTracks = uiState.filteredTracks
                        val artistItem = uiState.artists.firstOrNull { it.name == selectedArtist }
                        ArtistDetailView(
                            artistName = selectedArtist,
                            thumbnailUrl = artistItem?.thumbnailUrl,
                            tracks = artistTracks,
                            sortOrder = uiState.sortOrder,
                            onSortOrderSelected = viewModel::setSortOrder,
                            onBack = { viewModel.selectArtist(null) },
                            onPlayTrack = { track, list -> viewModel.playFromList(track, list) },
                            onPlayAll = { tracks -> viewModel.playAll(tracks) },
                            onDownloadAll = onDownloadAll,
                            onToggleSave = { track, isSaved -> viewModel.toggleSave(track, isSaved) },
                            onDeleteDownload = { trackId -> viewModel.deleteDownload(trackId) },
                            onAddToPlaylist = { track -> songForPlaylistDialog = track }
                        )
                    } else {
                        if (uiState.artists.isEmpty()) {
                            LibraryEmptyView(
                                icon = Icons.Default.Person,
                                title = "Sin artistas",
                                subtitle = "Guarda o descarga canciones para ver a tus artistas organizados aquí."
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(uiState.artists, key = { it.name }) { artistItem ->
                                    GroupRow(
                                        title = artistItem.name,
                                        subtitle = "${artistItem.trackCount} ${if (artistItem.trackCount == 1) "canción" else "canciones"}",
                                        thumbnailUrl = artistItem.thumbnailUrl,
                                        trackId = null,
                                        fallbackIcon = Icons.Default.Person,
                                        isCircular = true,
                                        onClick = { viewModel.selectArtist(artistItem.name) },
                                        onDelete = { artistToDelete = artistItem.name }
                                    )
                                    HorizontalDivider(
                                        color = ColorDivider,
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(start = 80.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                LibraryTab.PLAYLISTS -> {
                    val selectedPlaylistId = uiState.selectedPlaylistId
                    if (selectedPlaylistId != null) {
                        val playlistSongs = uiState.playlistSongs
                        PlaylistDetailView(
                            playlistName = uiState.selectedPlaylistName ?: "Playlist",
                            tracks = playlistSongs,
                            sortOrder = uiState.sortOrder,
                            onSortOrderSelected = viewModel::setSortOrder,
                            onBack = { viewModel.selectPlaylist(null) },
                            onExport = {
                                val currentName = uiState.selectedPlaylistName ?: "Playlist"
                                val safeName = currentName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
                                playlistToExport = Pair(currentName, playlistSongs)
                                singlePlaylistExportLauncher.launch("$safeName.json")
                            },
                            onPlayTrack = { track, list -> viewModel.playFromList(track, list) },
                            onPlayAll = { tracks -> viewModel.playAll(tracks) },
                            onDownloadAll = onDownloadAll,
                            onToggleSave = { track, isSaved -> viewModel.toggleSave(track, isSaved) },
                            onDeleteDownload = { trackId -> viewModel.deleteDownload(trackId) },
                            onRemoveFromPlaylist = { trackId ->
                                viewModel.removeSongFromPlaylist(selectedPlaylistId, trackId)
                            }
                        )
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mis Playlists (${uiState.playlists.size})",
                                    style = Fr33Typography.titleMedium,
                                    color = ColorTextSecondary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Botón Importar Playlists
                                    IconButton(
                                        onClick = {
                                            importPlaylistsLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FolderOpen,
                                            contentDescription = "Importar playlists",
                                            tint = ColorTextPrimary
                                        )
                                    }

                                    // Botón Exportar Todas las Playlists
                                    IconButton(
                                        onClick = {
                                            exportAllLauncher.launch("fr33music_backup.json")
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Upload,
                                            contentDescription = "Exportar todas las playlists",
                                            tint = ColorTextPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Button(
                                        onClick = { showCreatePlaylistDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary),
                                        shape = RoundedCornerShape(Fr33Radius.large)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Nueva Playlist",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Nueva", style = Fr33Typography.labelMedium)
                                    }
                                }
                            }

                            if (uiState.playlists.isEmpty()) {
                                LibraryEmptyView(
                                    icon = Icons.Default.QueueMusic,
                                    title = "Sin playlists",
                                    subtitle = "Toca el botón 'Nueva' para crear tu primera lista de reproducción."
                                )
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(uiState.playlists, key = { it.id }) { playlist ->
                                        PlaylistRow(
                                            playlist = playlist,
                                            onClick = { viewModel.selectPlaylist(playlist.id, playlist.name) },
                                            onDelete = { viewModel.deletePlaylist(playlist.id) }
                                        )
                                        HorizontalDivider(
                                            color = ColorDivider,
                                            thickness = 0.5.dp,
                                            modifier = Modifier.padding(start = 80.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                LibraryTab.ALL_SONGS -> {
                    if (uiState.allSongs.isEmpty()) {
                        LibraryEmptyView(
                            icon = Icons.Default.MusicNote,
                            title = "Biblioteca vacía",
                            subtitle = "Guarda canciones con el corazón o descárgalas para verlas todas aquí."
                        )
                    } else {
                        var songSearchQuery by remember { mutableStateOf("") }
                        val filteredEntities = remember(uiState.allSongs, songSearchQuery) {
                            if (songSearchQuery.isBlank()) uiState.allSongs
                            else uiState.allSongs.filter {
                                it.title.contains(songSearchQuery, ignoreCase = true) ||
                                it.artistName.contains(songSearchQuery, ignoreCase = true)
                            }
                        }
                        val allTracksDomain = remember(filteredEntities) { filteredEntities.map { it.toDomain() } }
                        Column(modifier = Modifier.fillMaxSize()) {
                            OutlinedTextField(
                                value = songSearchQuery,
                                onValueChange = { songSearchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small),
                                placeholder = {
                                    Text(
                                        text = "Buscar canciones...",
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
                                    if (songSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { songSearchQuery = "" }) {
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

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${filteredEntities.size} ${if (filteredEntities.size == 1) "canción" else "canciones"}",
                                    style = Fr33Typography.bodyMedium,
                                    color = ColorTextSecondary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (allTracksDomain.isNotEmpty()) {
                                        IconButton(onClick = { onDownloadAll(allTracksDomain) }) {
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = "Descargar todo",
                                                tint = ColorPrimary
                                            )
                                        }
                                    }
                                    SortFilterMenu(
                                        sortOrder = uiState.sortOrder,
                                        onSortOrderSelected = viewModel::setSortOrder
                                    )
                                }
                            }

                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(filteredEntities, key = { it.id }) { trackEntity ->
                                    val domain = trackEntity.toDomain()
                                    TrackRow(
                                        track = domain,
                                        onClick = { viewModel.playFromList(domain, allTracksDomain) },
                                        trailingContent = {
                                            TrackActionButtons(
                                                isSaved = trackEntity.isSaved,
                                                isDownloaded = trackEntity.isDownloaded,
                                                onToggleSave = { viewModel.toggleSave(domain, trackEntity.isSaved) },
                                                onDeleteDownload = { viewModel.deleteDownload(trackEntity.id) },
                                                onAddToPlaylist = { songForPlaylistDialog = domain }
                                            )
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

    // Diálogo para eliminar artista
    if (artistToDelete != null) {
        val targetArtist = artistToDelete!!
        AlertDialog(
            onDismissRequest = { artistToDelete = null },
            title = { Text(text = "Eliminar Artista", color = ColorTextPrimary) },
            text = {
                Text(
                    text = "¿Eliminar a $targetArtist y todas sus canciones guardadas?",
                    color = ColorTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteArtist(targetArtist)
                        artistToDelete = null
                    }
                ) {
                    Text("Eliminar", color = ColorError)
                }
            },
            dismissButton = {
                TextButton(onClick = { artistToDelete = null }) {
                    Text("Cancelar", color = ColorTextSecondary)
                }
            },
            containerColor = ColorSurface
        )
    }

    // Diálogo para añadir canción a una playlist
    if (songForPlaylistDialog != null) {
        val targetTrack = songForPlaylistDialog!!
        AddToPlaylistDialog(
            track = targetTrack,
            playlists = uiState.playlists,
            onDismiss = { songForPlaylistDialog = null },
            onSelectPlaylist = { playlistId ->
                viewModel.addSongToPlaylist(playlistId, targetTrack)
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
}

@Composable
private fun TrackActionButtons(
    isSaved: Boolean,
    isDownloaded: Boolean,
    onToggleSave: () -> Unit,
    onDeleteDownload: () -> Unit,
    onAddToPlaylist: () -> Unit,
    isInsidePlaylist: Boolean = false,
    onRemoveFromPlaylist: (() -> Unit)? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Corazón (Favorito)
        IconButton(onClick = onToggleSave) {
            Icon(
                imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isSaved) "Quitar de favoritos" else "Guardar en favoritos",
                tint = if (isSaved) ColorPrimary else ColorTextTertiary
            )
        }

        // Bote de basura (solo si está descargada físicamente)
        if (isDownloaded) {
            IconButton(onClick = onDeleteDownload) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar descarga",
                    tint = ColorTextTertiary
                )
            }
        }

        if (isInsidePlaylist) {
            // Quitar de playlist (X)
            if (onRemoveFromPlaylist != null) {
                IconButton(onClick = onRemoveFromPlaylist) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Quitar de playlist",
                        tint = ColorTextTertiary
                    )
                }
            }
        } else {
            // Añadir a playlist (+)
            IconButton(onClick = onAddToPlaylist) {
                Icon(
                    imageVector = Icons.Default.PlaylistAdd,
                    contentDescription = "Añadir a playlist",
                    tint = ColorTextSecondary
                )
            }
        }
    }
}

@Composable
private fun PlaylistRow(
    playlist: PlaylistWithSongCount,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrackThumbnail(
            thumbnailUrl = playlist.thumbnailUrl,
            contentDescription = playlist.name,
            size = 56.dp,
            shapeRadius = Fr33Radius.medium,
            fallbackIcon = Icons.AutoMirrored.Filled.QueueMusic
        )

        Spacer(modifier = Modifier.width(Fr33Spacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = Fr33Typography.titleMedium,
                color = ColorTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${playlist.songCount} ${if (playlist.songCount == 1) "canción" else "canciones"}",
                style = Fr33Typography.bodyMedium,
                color = ColorTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar playlist",
                tint = ColorTextTertiary
            )
        }
    }
}

@Composable
private fun PlaylistDetailView(
    playlistName: String,
    tracks: List<TrackEntity>,
    sortOrder: LibrarySortOrder = LibrarySortOrder.RECENTLY_ADDED,
    onSortOrderSelected: (LibrarySortOrder) -> Unit = {},
    onBack: () -> Unit,
    onExport: () -> Unit = {},
    onPlayTrack: (Track, List<Track>) -> Unit,
    onPlayAll: (List<Track>) -> Unit,
    onDownloadAll: (List<Track>) -> Unit = {},
    onToggleSave: (Track, Boolean) -> Unit,
    onDeleteDownload: (String) -> Unit,
    onRemoveFromPlaylist: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredTracks = remember(tracks, searchQuery) {
        if (searchQuery.isBlank()) tracks
        else tracks.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.artistName.contains(searchQuery, ignoreCase = true)
        }
    }
    val domainTracks = remember(filteredTracks) { filteredTracks.map { it.toDomain() } }
    val allDomainTracks = remember(tracks) { tracks.map { it.toDomain() } }
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
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
                text = playlistName,
                style = Fr33Typography.titleLarge,
                color = ColorTextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 1. Descargar (IconButton)
            if (allDomainTracks.isNotEmpty()) {
                IconButton(onClick = { onDownloadAll(allDomainTracks) }) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Descargar todas las canciones",
                        tint = ColorPrimary
                    )
                }
            }

            // 2. Compartir / Exportar
            IconButton(onClick = onExport) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Exportar playlist",
                    tint = ColorTextPrimary
                )
            }

            // 3. Filtro
            SortFilterMenu(
                sortOrder = sortOrder,
                onSortOrderSelected = onSortOrderSelected
            )
        }

        if (tracks.isNotEmpty()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small),
                placeholder = {
                    Text(
                        text = "Buscar en esta playlist...",
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
        }

        if (tracks.isEmpty()) {
            LibraryEmptyView(
                icon = Icons.Default.MusicNote,
                title = "Playlist vacía",
                subtitle = "Usa el icono '+' en cualquier canción para añadirla a esta lista."
            )
        } else if (filteredTracks.isEmpty()) {
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
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredTracks, key = { it.id }) { trackEntity ->
                    val domain = trackEntity.toDomain()
                    TrackRow(
                        track = domain,
                        onClick = { onPlayTrack(domain, domainTracks) },
                        trailingContent = {
                            TrackActionButtons(
                                isSaved = trackEntity.isSaved,
                                isDownloaded = trackEntity.isDownloaded,
                                onToggleSave = { onToggleSave(domain, trackEntity.isSaved) },
                                onDeleteDownload = { onDeleteDownload(trackEntity.id) },
                                onAddToPlaylist = {},
                                isInsidePlaylist = true,
                                onRemoveFromPlaylist = { onRemoveFromPlaylist(trackEntity.id) }
                            )
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
private fun GroupRow(
    title: String,
    subtitle: String,
    thumbnailUrl: String?,
    fallbackIcon: androidx.compose.ui.graphics.vector.ImageVector,
    trackId: String? = null,
    isCircular: Boolean = false,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrackThumbnail(
            thumbnailUrl = thumbnailUrl,
            trackId = trackId,
            contentDescription = title,
            size = 56.dp,
            isCircular = isCircular,
            shapeRadius = Fr33Radius.medium,
            fallbackIcon = fallbackIcon
        )

        Spacer(modifier = Modifier.width(Fr33Spacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Fr33Typography.titleMedium,
                color = ColorTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = Fr33Typography.bodyMedium,
                color = ColorTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (onDelete != null) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = ColorTextTertiary
                )
            }
        }
    }
}

@Composable
private fun ArtistDetailView(
    artistName: String,
    thumbnailUrl: String?,
    tracks: List<TrackEntity>,
    sortOrder: LibrarySortOrder = LibrarySortOrder.RECENTLY_ADDED,
    onSortOrderSelected: (LibrarySortOrder) -> Unit = {},
    onBack: () -> Unit,
    onPlayTrack: (Track, List<Track>) -> Unit,
    onPlayAll: (List<Track>) -> Unit,
    onDownloadAll: (List<Track>) -> Unit = {},
    onToggleSave: (Track, Boolean) -> Unit,
    onDeleteDownload: (String) -> Unit,
    onAddToPlaylist: (Track) -> Unit
) {
    val domainTracks = tracks.map { it.toDomain() }
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
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

            TrackThumbnail(
                thumbnailUrl = thumbnailUrl,
                contentDescription = artistName,
                size = 40.dp,
                isCircular = true,
                fallbackIcon = Icons.Default.Person
            )
            Spacer(modifier = Modifier.width(Fr33Spacing.small))

            Text(
                text = artistName,
                style = Fr33Typography.titleLarge,
                color = ColorTextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (domainTracks.isNotEmpty()) {
                IconButton(onClick = { onDownloadAll(domainTracks) }) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Descargar todo",
                        tint = ColorPrimary
                    )
                }
            }

            SortFilterMenu(
                sortOrder = sortOrder,
                onSortOrderSelected = onSortOrderSelected
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(tracks, key = { it.id }) { trackEntity ->
                val domain = trackEntity.toDomain()
                TrackRow(
                    track = domain,
                    onClick = { onPlayTrack(domain, domainTracks) },
                    trailingContent = {
                        TrackActionButtons(
                            isSaved = trackEntity.isSaved,
                            isDownloaded = trackEntity.isDownloaded,
                            onToggleSave = { onToggleSave(domain, trackEntity.isSaved) },
                            onDeleteDownload = { onDeleteDownload(trackEntity.id) },
                            onAddToPlaylist = { onAddToPlaylist(domain) }
                        )
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
private fun LibraryEmptyView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Fr33Spacing.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ColorTextTertiary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(Fr33Spacing.large))

        Text(
            text = title,
            style = Fr33Typography.titleLarge,
            color = ColorTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Fr33Spacing.small))

        Text(
            text = subtitle,
            style = Fr33Typography.bodyMedium,
            color = ColorTextTertiary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SortFilterMenu(
    sortOrder: LibrarySortOrder,
    onSortOrderSelected: (LibrarySortOrder) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { showMenu = true }) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Ordenar lista",
                tint = ColorTextPrimary
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(ColorSurface)
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Añadidos recientemente",
                        color = if (sortOrder == LibrarySortOrder.RECENTLY_ADDED) ColorPrimary else ColorTextPrimary,
                        style = Fr33Typography.bodyMedium
                    )
                },
                onClick = {
                    onSortOrderSelected(LibrarySortOrder.RECENTLY_ADDED)
                    showMenu = false
                }
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text = "Orden alfabético",
                        color = if (sortOrder == LibrarySortOrder.ALPHABETICAL) ColorPrimary else ColorTextPrimary,
                        style = Fr33Typography.bodyMedium
                    )
                },
                onClick = {
                    onSortOrderSelected(LibrarySortOrder.ALPHABETICAL)
                    showMenu = false
                }
            )
        }
    }
}
