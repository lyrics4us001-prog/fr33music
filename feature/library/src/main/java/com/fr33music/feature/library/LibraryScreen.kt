package com.fr33music.feature.library

import android.widget.Toast
import java.io.File
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fr33music.core.database.dao.PlaylistWithSongCount
import com.fr33music.core.database.entity.TrackEntity
import com.fr33music.core.designsystem.component.SelectionBottomBar
import com.fr33music.core.designsystem.component.SelectionTopBar
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
    var parentPlaylistIdForNewSub by remember { mutableStateOf<Long?>(null) }
    var subPlaylistToMerge by remember { mutableStateOf<PlaylistWithSongCount?>(null) }
    var songForPlaylistDialog by remember { mutableStateOf<Track?>(null) }
    var bulkTracksForPlaylistDialog by remember { mutableStateOf<List<Track>?>(null) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }
    var selectedTracks by remember { mutableStateOf<Map<String, Track>>(emptyMap()) }
    val isSelectionMode = selectedTracks.isNotEmpty()
    var artistToDelete by remember { mutableStateOf<String?>(null) }
    var artistToRename by remember { mutableStateOf<String?>(null) }
    var artistToMerge by remember { mutableStateOf<String?>(null) }
    var playlistToExport by remember { mutableStateOf<Pair<String, List<TrackEntity>>?>(null) }
    var playlistForCoverChange by remember { mutableStateOf<Long?>(null) }
    var playlistToRename by remember { mutableStateOf<Pair<Long, String>?>(null) }
    var trackToRename by remember { mutableStateOf<Track?>(null) }
    var artistForCoverChange by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.selectedTab, uiState.selectedArtist, uiState.selectedPlaylistId) {
        selectedTracks = emptyMap()
    }

    val context = LocalContext.current

    val artistCoverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && artistForCoverChange != null) {
            viewModel.updateArtistCover(context, artistForCoverChange!!, uri)
            Toast.makeText(context, "Portada de artista actualizada", Toast.LENGTH_SHORT).show()
            artistForCoverChange = null
        }
    }

    val playlistCoverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && playlistForCoverChange != null) {
            viewModel.updatePlaylistCover(context, playlistForCoverChange!!, uri)
            Toast.makeText(context, "Portada de playlist actualizada", Toast.LENGTH_SHORT).show()
            playlistForCoverChange = null
        }
    }

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
            viewModel.exportFullLibraryToUri(context, uri) { success ->
                Toast.makeText(
                    context,
                    if (success) "Biblioteca completa exportada con éxito" else "Error al exportar biblioteca",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val importPlaylistsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importFullLibraryFromUri(context, uri) { success, message ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Interceptar botón atrás físico para volver de la vista de detalle o salir de selección
    BackHandler(enabled = isSelectionMode) {
        selectedTracks = emptyMap()
    }
    BackHandler(enabled = uiState.selectedArtist != null && !isSelectionMode) {
        viewModel.selectArtist(null)
    }
    BackHandler(enabled = uiState.selectedPlaylistId != null && !isSelectionMode) {
        viewModel.navigateBackPlaylist()
    }

    var isRootSearchVisible by remember { mutableStateOf(false) }
    var rootSearchQuery by remember { mutableStateOf("") }

    BackHandler(enabled = isRootSearchVisible && !isSelectionMode && uiState.selectedArtist == null && uiState.selectedPlaylistId == null) {
        isRootSearchVisible = false
        rootSearchQuery = ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        if (isSelectionMode && uiState.selectedArtist == null && uiState.selectedPlaylistId == null) {
            SelectionTopBar(
                selectedCount = selectedTracks.size,
                onSelectAll = {
                    val visible = if (rootSearchQuery.isBlank()) uiState.allSongs
                    else uiState.allSongs.filter {
                        it.title.contains(rootSearchQuery, ignoreCase = true) ||
                        it.artistName.contains(rootSearchQuery, ignoreCase = true)
                    }
                    selectedTracks = visible.map { it.toDomain() }.associateBy { it.trackId }
                },
                onCancel = { selectedTracks = emptyMap() }
            )
        } else {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        isRootSearchVisible = !isRootSearchVisible
                        if (!isRootSearchVisible) {
                            rootSearchQuery = ""
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar en biblioteca",
                            tint = if (isRootSearchVisible) ColorPrimary else ColorTextPrimary
                        )
                    }

                    var showExportImportMenu by remember { mutableStateOf(false) }

                    Box {
                        IconButton(onClick = { showExportImportMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.ImportExport,
                                contentDescription = "Exportar o Importar Biblioteca",
                                tint = ColorTextPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = showExportImportMenu,
                            onDismissRequest = { showExportImportMenu = false },
                            containerColor = ColorSurface
                        ) {
                            DropdownMenuItem(
                                text = { Text("Exportar Biblioteca Completa", color = ColorTextPrimary) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Upload,
                                        contentDescription = null,
                                        tint = ColorPrimary
                                    )
                                },
                                onClick = {
                                    showExportImportMenu = false
                                    exportAllLauncher.launch("Fr33Music_Backup.json")
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Importar Biblioteca", color = ColorTextPrimary) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = ColorTextSecondary
                                    )
                                },
                                onClick = {
                                    showExportImportMenu = false
                                    importPlaylistsLauncher.launch(arrayOf("application/json", "*/*"))
                                }
                            )
                        }
                    }

                    SortFilterMenu(
                        sortOrder = uiState.sortOrder,
                        onSortOrderSelected = viewModel::setSortOrder
                    )
                }
            }
        }
        }

        if (isRootSearchVisible && !isSelectionMode && uiState.selectedArtist == null && uiState.selectedPlaylistId == null) {
            val placeholderText = when (uiState.selectedTab) {
                LibraryTab.ARTISTS -> "Buscar artistas..."
                LibraryTab.PLAYLISTS -> "Buscar playlists..."
                LibraryTab.ALL_SONGS -> "Buscar canciones..."
            }

            OutlinedTextField(
                value = rootSearchQuery,
                onValueChange = { rootSearchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small),
                placeholder = {
                    Text(
                        text = placeholderText,
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
                    IconButton(onClick = {
                        if (rootSearchQuery.isNotEmpty()) {
                            rootSearchQuery = ""
                        } else {
                            isRootSearchVisible = false
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar búsqueda",
                            tint = ColorTextSecondary
                        )
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

        // Chips de las pestañas: Artistas, Playlists, Todas las canciones
        if (!isSelectionMode) {
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
        }

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

        val onAutoRenameAll: (List<TrackEntity>) -> Unit = { tracksToParse ->
            if (tracksToParse.isNotEmpty()) {
                Toast.makeText(context, "Optimizando nombres con IA...", Toast.LENGTH_SHORT).show()
                viewModel.batchSmartParse(tracksToParse) { updatedCount ->
                    Toast.makeText(
                        context,
                        if (updatedCount > 0) "Se actualizaron $updatedCount canciones" else "Todos los nombres ya estaban optimizados",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        val onRefreshMetadata: (List<TrackEntity>) -> Unit = { tracksToRefresh ->
            if (tracksToRefresh.isNotEmpty()) {
                Toast.makeText(
                    context,
                    "Actualizando metadatos de ${tracksToRefresh.size} canciones...",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.refreshMetadata(tracksToRefresh) {
                    Toast.makeText(
                        context,
                        "Metadatos actualizados",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
                            onAutoRenameAll = onAutoRenameAll,
                            onRefreshMetadata = onRefreshMetadata,
                            onChangeCover = {
                                artistForCoverChange = selectedArtist
                                artistCoverPickerLauncher.launch("image/*")
                            },
                            onPlayTrack = { track, list -> viewModel.playFromList(track, list) },
                            onPlayAll = { tracks -> viewModel.playAll(tracks) },
                            onDownloadAll = onDownloadAll,
                            onToggleSave = { track, isSaved -> viewModel.toggleSave(track, isSaved) },
                            onDeleteDownload = { trackId -> viewModel.deleteDownload(trackId) },
                            onAddToPlaylist = { track -> songForPlaylistDialog = track },
                            onRename = { track -> trackToRename = track },
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
                            onBulkAddToPlaylist = { list -> bulkTracksForPlaylistDialog = list },
                            onBulkAddToFavorites = {
                                val list = selectedTracks.values.toList()
                                viewModel.saveTracks(list)
                                Toast.makeText(context, "${list.size} canciones añadidas a Favoritos", Toast.LENGTH_SHORT).show()
                                selectedTracks = emptyMap()
                            },
                            onBulkDelete = { showBulkDeleteConfirm = true }
                        )
                    } else {
                        val filteredArtists = remember(uiState.artists, rootSearchQuery) {
                            if (rootSearchQuery.isBlank()) uiState.artists
                            else uiState.artists.filter { it.name.contains(rootSearchQuery, ignoreCase = true) }
                        }

                        if (uiState.artists.isEmpty()) {
                            LibraryEmptyView(
                                icon = Icons.Default.Person,
                                title = "Sin artistas",
                                subtitle = "Guarda o descarga canciones para ver a tus artistas organizados aquí."
                            )
                        } else if (filteredArtists.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No se encontraron artistas para \"$rootSearchQuery\"",
                                    style = Fr33Typography.bodyMedium,
                                    color = ColorTextSecondary
                                )
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(filteredArtists, key = { it.name }) { artistItem ->
                                    ArtistRow(
                                        artist = artistItem,
                                        onClick = { viewModel.selectArtist(artistItem.name) },
                                        onDelete = { artistToDelete = artistItem.name },
                                        onRename = { artistToRename = artistItem.name },
                                        onMerge = { artistToMerge = artistItem.name },
                                        onChangeCover = {
                                            artistForCoverChange = artistItem.name
                                            artistCoverPickerLauncher.launch("image/*")
                                        }
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
                            isSubPlaylist = uiState.selectedPlaylistParentId != null,
                            subPlaylists = uiState.subPlaylists,
                            tracks = playlistSongs,
                            recursiveTracks = uiState.playlistRecursiveSongs,
                            sortOrder = uiState.sortOrder,
                            onSortOrderSelected = viewModel::setSortOrder,
                            onBack = { viewModel.navigateBackPlaylist() },
                            onOpenSubPlaylist = { sub -> viewModel.selectPlaylist(sub.id, sub.name) },
                            onCreateSubPlaylist = {
                                parentPlaylistIdForNewSub = selectedPlaylistId
                                showCreatePlaylistDialog = true
                            },
                            onRenameSubPlaylist = { id, name -> playlistToRename = Pair(id, name) },
                            onChangeSubPlaylistCover = { id ->
                                playlistForCoverChange = id
                                playlistCoverPickerLauncher.launch("image/*")
                            },
                            onMergeSubPlaylist = { sub -> subPlaylistToMerge = sub },
                            onDeleteSubPlaylist = { id -> viewModel.deletePlaylist(id) },
                            onExport = {
                                val currentName = uiState.selectedPlaylistName ?: "Playlist"
                                val safeName = currentName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
                                playlistToExport = Pair(currentName, uiState.playlistRecursiveSongs)
                                singlePlaylistExportLauncher.launch("$safeName.json")
                            },
                            onAutoRenameAll = onAutoRenameAll,
                            onRefreshMetadata = onRefreshMetadata,
                            onChangeCover = {
                                playlistForCoverChange = selectedPlaylistId
                                playlistCoverPickerLauncher.launch("image/*")
                            },
                            onPlayTrack = { track, list -> viewModel.playFromList(track, list) },
                            onPlayAll = { tracks, shuffle -> viewModel.playAll(tracks, shuffle = shuffle) },
                            onDownloadAll = onDownloadAll,
                            onToggleSave = { track, isSaved -> viewModel.toggleSave(track, isSaved) },
                            onDeleteDownload = { trackId -> viewModel.deleteDownload(trackId) },
                            onAddToPlaylist = { track -> songForPlaylistDialog = track },
                            onRemoveFromPlaylist = { trackId ->
                                viewModel.removeSongFromPlaylist(selectedPlaylistId, trackId)
                            },
                            onRename = { track -> trackToRename = track },
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
                            onBulkAddToPlaylist = { list -> bulkTracksForPlaylistDialog = list },
                            onBulkAddToFavorites = {
                                val list = selectedTracks.values.toList()
                                viewModel.saveTracks(list)
                                Toast.makeText(context, "${list.size} canciones añadidas a Favoritos", Toast.LENGTH_SHORT).show()
                                selectedTracks = emptyMap()
                            },
                            onBulkDelete = { showBulkDeleteConfirm = true }
                        )
                    } else {
                        val filteredPlaylists = remember(uiState.playlists, rootSearchQuery) {
                            if (rootSearchQuery.isBlank()) uiState.playlists
                            else uiState.playlists.filter { it.name.contains(rootSearchQuery, ignoreCase = true) }
                        }

                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mis Playlists (${filteredPlaylists.size})",
                                    style = Fr33Typography.titleMedium,
                                    color = ColorTextSecondary
                                )
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

                            if (uiState.playlists.isEmpty()) {
                                LibraryEmptyView(
                                    icon = Icons.Default.QueueMusic,
                                    title = "Sin playlists",
                                    subtitle = "Toca el botón 'Nueva' para crear tu primera lista de reproducción."
                                )
                            } else if (filteredPlaylists.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No se encontraron playlists para \"$rootSearchQuery\"",
                                        style = Fr33Typography.bodyMedium,
                                        color = ColorTextSecondary
                                    )
                                }
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(filteredPlaylists, key = { it.id }) { playlist ->
                                        PlaylistRow(
                                            playlist = playlist,
                                            onClick = { viewModel.selectPlaylist(playlist.id, playlist.name) },
                                            onRename = { playlistToRename = Pair(playlist.id, playlist.name) },
                                            onChangeCover = {
                                                playlistForCoverChange = playlist.id
                                                playlistCoverPickerLauncher.launch("image/*")
                                            },
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
                        val filteredEntities = remember(uiState.allSongs, rootSearchQuery) {
                            if (rootSearchQuery.isBlank()) uiState.allSongs
                            else uiState.allSongs.filter {
                                it.title.contains(rootSearchQuery, ignoreCase = true) ||
                                it.artistName.contains(rootSearchQuery, ignoreCase = true)
                            }
                        }
                        val allTracksDomain = remember(filteredEntities) { filteredEntities.map { it.toDomain() } }
                        Column(modifier = Modifier.fillMaxSize()) {
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

                                var showTopMenu by remember { mutableStateOf(false) }
                                var showSortDialog by remember { mutableStateOf(false) }

                                Box {
                                    IconButton(onClick = { showTopMenu = true }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Opciones",
                                            tint = ColorTextPrimary
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showTopMenu,
                                        onDismissRequest = { showTopMenu = false },
                                        containerColor = ColorSurface
                                    ) {
                                        if (allTracksDomain.isNotEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text("Descargar todo", color = ColorTextPrimary) },
                                                leadingIcon = {
                                                    Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = ColorPrimary)
                                                },
                                                onClick = {
                                                    showTopMenu = false
                                                    onDownloadAll(allTracksDomain)
                                                }
                                            )
                                        }

                                        DropdownMenuItem(
                                            text = { Text("Organizar", color = ColorTextPrimary) },
                                            leadingIcon = {
                                                Icon(imageVector = Icons.Default.FilterList, contentDescription = null, tint = ColorTextSecondary)
                                            },
                                            onClick = {
                                                showTopMenu = false
                                                showSortDialog = true
                                            }
                                        )

                                        if (filteredEntities.isNotEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text("Nombres automáticos (IA)", color = ColorTextPrimary) },
                                                leadingIcon = {
                                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = ColorPrimary)
                                                },
                                                onClick = {
                                                    showTopMenu = false
                                                    onAutoRenameAll(filteredEntities)
                                                }
                                            )
                                        }
                                    }
                                }

                                if (showSortDialog) {
                                    SortOrderDialog(
                                        currentOrder = uiState.sortOrder,
                                        onOrderSelected = {
                                            viewModel.setSortOrder(it)
                                            showSortDialog = false
                                        },
                                        onDismiss = { showSortDialog = false }
                                    )
                                }
                            }

                            if (filteredEntities.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No se encontraron canciones para \"$rootSearchQuery\"",
                                        style = Fr33Typography.bodyMedium,
                                        color = ColorTextSecondary
                                    )
                                }
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(filteredEntities, key = { it.id }) { trackEntity ->
                                    val domain = trackEntity.toDomain()
                                    val isSelected = selectedTracks.containsKey(domain.trackId)
                                    TrackRow(
                                        track = domain,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = isSelected,
                                        onClick = {
                                            if (isSelectionMode) {
                                                selectedTracks = if (isSelected) {
                                                    selectedTracks - domain.trackId
                                                } else {
                                                    selectedTracks + (domain.trackId to domain)
                                                }
                                            } else {
                                                viewModel.playFromList(domain, allTracksDomain)
                                            }
                                        },
                                        onLongClick = {
                                            selectedTracks = selectedTracks + (domain.trackId to domain)
                                        },
                                        trailingContent = if (isSelectionMode) null else {
                                            {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    IconButton(onClick = { songForPlaylistDialog = domain }) {
                                                        Icon(
                                                            imageVector = Icons.Default.PlaylistAdd,
                                                            contentDescription = "Añadir a playlist",
                                                            tint = ColorTextSecondary
                                                        )
                                                    }
                                                    TrackKebabMenu(
                                                        isSaved = trackEntity.isSaved,
                                                        onToggleSave = { viewModel.toggleSave(domain, trackEntity.isSaved) },
                                                        onRename = { trackToRename = domain },
                                                        onDelete = { viewModel.deleteDownload(trackEntity.id) }
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

                                item {
                                    ListSummaryFooter(tracks = filteredEntities)
                                }
                            }
                        }
                    }
                }
            }
            }
        }

        if (isSelectionMode && uiState.selectedArtist == null && uiState.selectedPlaylistId == null) {
            SelectionBottomBar(
                onAddToPlaylist = { bulkTracksForPlaylistDialog = selectedTracks.values.toList() },
                onAddToFavorites = {
                    val list = selectedTracks.values.toList()
                    viewModel.saveTracks(list)
                    Toast.makeText(context, "${list.size} canciones añadidas a Favoritos", Toast.LENGTH_SHORT).show()
                    selectedTracks = emptyMap()
                },
                onDelete = { showBulkDeleteConfirm = true }
            )
        }
    }

    // Diálogo para crear nueva playlist o sub-playlist
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            title = if (parentPlaylistIdForNewSub != null) "Nueva Sub-playlist" else "Nueva Playlist",
            onDismiss = {
                showCreatePlaylistDialog = false
                parentPlaylistIdForNewSub = null
            },
            onCreate = { name ->
                viewModel.createPlaylist(name, parentPlaylistIdForNewSub)
                showCreatePlaylistDialog = false
                parentPlaylistIdForNewSub = null
            }
        )
    }

    // Diálogo para añadir canciones seleccionadas en masa a una playlist
    if (bulkTracksForPlaylistDialog != null) {
        val targetTracks = bulkTracksForPlaylistDialog!!
        AddToPlaylistDialog(
            track = targetTracks.first(),
            itemDescription = "${targetTracks.size} canciones seleccionadas",
            playlists = uiState.allPlaylists,
            onDismiss = { bulkTracksForPlaylistDialog = null },
            onSelectPlaylist = { playlistId ->
                viewModel.addSongsToPlaylist(playlistId, targetTracks)
                Toast.makeText(
                    context,
                    "${targetTracks.size} canciones añadidas",
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

    // Diálogo de confirmación para eliminar canciones en masa
    if (showBulkDeleteConfirm && selectedTracks.isNotEmpty()) {
        val count = selectedTracks.size
        val inPlaylistId = uiState.selectedPlaylistId
        AlertDialog(
            onDismissRequest = { showBulkDeleteConfirm = false },
            title = {
                Text(
                    text = if (inPlaylistId != null) "Quitar de la playlist" else "Eliminar canciones",
                    color = ColorTextPrimary
                )
            },
            text = {
                Text(
                    text = if (inPlaylistId != null) {
                        "¿Quitar $count ${if (count == 1) "canción" else "canciones"} de esta playlist?"
                    } else {
                        "¿Eliminar $count ${if (count == 1) "canción" else "canciones"} de tu biblioteca y descargas?"
                    },
                    color = ColorTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val ids = selectedTracks.keys.toList()
                        if (inPlaylistId != null) {
                            viewModel.removeSongsFromPlaylist(inPlaylistId, ids)
                        } else {
                            viewModel.removeTracksFromLibrary(ids)
                        }
                        selectedTracks = emptyMap()
                        showBulkDeleteConfirm = false
                    }
                ) {
                    Text("Eliminar", color = ColorError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDeleteConfirm = false }) {
                    Text("Cancelar", color = ColorTextSecondary)
                }
            },
            containerColor = ColorSurface
        )
    }

    // Diálogo para combinar (Merge) una sub-playlist con otra playlist
    if (subPlaylistToMerge != null) {
        val sourceSub = subPlaylistToMerge!!
        val availableTargets = uiState.allPlaylists.filter { it.id != sourceSub.id }
        AlertDialog(
            onDismissRequest = { subPlaylistToMerge = null },
            title = { Text(text = "Combinar \"${sourceSub.name}\" con...", color = ColorTextPrimary) },
            text = {
                if (availableTargets.isEmpty()) {
                    Text("No hay otras playlists disponibles para combinar.", color = ColorTextSecondary)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(availableTargets, key = { it.id }) { target ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.mergePlaylists(sourceSub.id, target.id)
                                        Toast.makeText(
                                            context,
                                            "\"${sourceSub.name}\" combinada en \"${target.name}\"",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        subPlaylistToMerge = null
                                    }
                                    .padding(vertical = Fr33Spacing.medium, horizontal = Fr33Spacing.small),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (target.parentPlaylistId != null) Icons.Default.Folder else Icons.AutoMirrored.Filled.QueueMusic,
                                    contentDescription = null,
                                    tint = ColorPrimary
                                )
                                Spacer(modifier = Modifier.width(Fr33Spacing.medium))
                                Column {
                                    Text(text = target.name, color = ColorTextPrimary, style = Fr33Typography.bodyLarge)
                                    Text(
                                        text = "${target.songCount} ${if (target.songCount == 1) "canción" else "canciones"}",
                                        color = ColorTextSecondary,
                                        style = Fr33Typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { subPlaylistToMerge = null }) {
                    Text("Cancelar", color = ColorTextSecondary)
                }
            },
            containerColor = ColorSurface
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

    // Diálogo para cambiar nombre de artista
    if (artistToRename != null) {
        val oldName = artistToRename!!
        var newArtistName by remember(oldName) { mutableStateOf(oldName) }
        AlertDialog(
            onDismissRequest = { artistToRename = null },
            title = { Text(text = "Cambiar nombre de artista", color = ColorTextPrimary) },
            text = {
                OutlinedTextField(
                    value = newArtistName,
                    onValueChange = { newArtistName = it },
                    label = { Text("Nombre del artista") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newArtistName.isNotBlank() && !newArtistName.trim().equals(oldName, ignoreCase = true)) {
                            viewModel.renameArtist(oldName, newArtistName.trim())
                            artistToRename = null
                        }
                    },
                    enabled = newArtistName.isNotBlank()
                ) {
                    Text("Guardar", color = ColorPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { artistToRename = null }) {
                    Text("Cancelar", color = ColorTextSecondary)
                }
            },
            containerColor = ColorSurface
        )
    }

    // Diálogo para combinar artista
    if (artistToMerge != null) {
        val sourceArtist = artistToMerge!!
        val otherArtists = remember(uiState.artists, sourceArtist) {
            uiState.artists.filter { !it.name.equals(sourceArtist, ignoreCase = true) }
        }
        AlertDialog(
            onDismissRequest = { artistToMerge = null },
            title = { Text(text = "Combinar artista", color = ColorTextPrimary) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Combinar todas las canciones de \"$sourceArtist\" con:",
                        style = Fr33Typography.bodyMedium,
                        color = ColorTextSecondary
                    )
                    Spacer(modifier = Modifier.height(Fr33Spacing.medium))
                    if (otherArtists.isEmpty()) {
                        Text(
                            text = "No hay otros artistas disponibles para combinar.",
                            style = Fr33Typography.bodySmall,
                            color = ColorTextTertiary
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        ) {
                            items(otherArtists, key = { it.name }) { target ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.mergeArtists(sourceArtist, target.name)
                                            artistToMerge = null
                                        }
                                        .padding(vertical = Fr33Spacing.small),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TrackThumbnail(
                                        thumbnailUrl = target.thumbnailUrl,
                                        contentDescription = target.name,
                                        size = 36.dp,
                                        isCircular = true,
                                        fallbackIcon = Icons.Default.Person
                                    )
                                    Spacer(modifier = Modifier.width(Fr33Spacing.medium))
                                    Text(
                                        text = target.name,
                                        style = Fr33Typography.bodyLarge,
                                        color = ColorTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { artistToMerge = null }) {
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
            playlists = uiState.allPlaylists,
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

    // Diálogo para cambiar nombre de playlist
    if (playlistToRename != null) {
        val (pId, currentName) = playlistToRename!!
        var newPlaylistName by remember(pId) { mutableStateOf(currentName) }
        AlertDialog(
            onDismissRequest = { playlistToRename = null },
            title = { Text(text = "Cambiar nombre de playlist", color = ColorTextPrimary) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Nombre de la playlist") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            viewModel.renamePlaylist(pId, newPlaylistName.trim())
                            playlistToRename = null
                        }
                    },
                    enabled = newPlaylistName.isNotBlank()
                ) {
                    Text("Guardar", color = ColorPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToRename = null }) {
                    Text("Cancelar", color = ColorTextSecondary)
                }
            },
            containerColor = ColorSurface
        )
    }

    // Diálogo para cambiar nombre de canción con IA
    if (trackToRename != null) {
        val targetTrack = trackToRename!!
        EditTrackInfoDialog(
            track = targetTrack,
            onDismiss = { trackToRename = null },
            onConfirm = { newTitle, newArtist ->
                viewModel.updateTrackMetadata(targetTrack.trackId, newTitle, newArtist)
                Toast.makeText(context, "Información actualizada", Toast.LENGTH_SHORT).show()
                trackToRename = null
            }
        )
    }
}

@Composable
private fun TrackKebabMenu(
    onRename: () -> Unit,
    onDelete: () -> Unit,
    isSaved: Boolean? = null,
    onToggleSave: (() -> Unit)? = null,
    onAddToPlaylist: (() -> Unit)? = null,
    addToPlaylistLabel: String = "Añadir a Playlist",
    onRemoveFromPlaylist: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Opciones",
                tint = ColorTextSecondary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = ColorSurface
        ) {
            if (onToggleSave != null && isSaved != null) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (isSaved) "Quitar de Favoritos" else "Añadir a Favoritos",
                            color = ColorTextPrimary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isSaved) ColorPrimary else ColorTextSecondary
                        )
                    },
                    onClick = {
                        expanded = false
                        onToggleSave()
                    }
                )
            }

            DropdownMenuItem(
                text = { Text(text = "Cambiar nombre", color = ColorTextPrimary) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = ColorTextSecondary
                    )
                },
                onClick = {
                    expanded = false
                    onRename()
                }
            )

            if (onAddToPlaylist != null) {
                DropdownMenuItem(
                    text = { Text(text = addToPlaylistLabel, color = ColorTextPrimary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.PlaylistAdd,
                            contentDescription = null,
                            tint = ColorTextSecondary
                        )
                    },
                    onClick = {
                        expanded = false
                        onAddToPlaylist()
                    }
                )
            }

            if (onRemoveFromPlaylist != null) {
                DropdownMenuItem(
                    text = { Text(text = "Quitar de esta Playlist", color = ColorError) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = ColorError
                        )
                    },
                    onClick = {
                        expanded = false
                        onRemoveFromPlaylist()
                    }
                )
            }

            DropdownMenuItem(
                text = { Text(text = "Borrar del dispositivo", color = ColorError) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = ColorError
                    )
                },
                onClick = {
                    expanded = false
                    onDelete()
                }
            )
        }
    }
}

@Composable
private fun ListSummaryFooter(tracks: List<TrackEntity>) {
    val context = LocalContext.current
    val validDurationMs = tracks.filter { it.durationMs > 0 }.sumOf { it.durationMs }
    val totalSeconds = validDurationMs / 1000
    val minutes = if (totalSeconds in 1..59) 1 else totalSeconds / 60
    val trackCount = tracks.size
    val trackLabel = if (trackCount == 1) "1 pista" else "$trackCount pistas"

    // Calcular peso total de archivos locales (opus / m4a)
    val totalBytes = remember(tracks) {
        val downloadsDir = File(context.filesDir, "downloads")
        var sum = 0L
        for (track in tracks) {
            val opusFile = File(downloadsDir, "${track.id}.opus")
            val m4aFile = File(downloadsDir, "${track.id}.m4a")
            if (opusFile.exists()) {
                sum += opusFile.length()
            } else if (m4aFile.exists()) {
                sum += m4aFile.length()
            } else {
                track.localPath?.let { path ->
                    val f = File(path)
                    if (f.exists()) {
                        sum += f.length()
                    }
                }
            }
        }
        sum
    }

    val sizeFormatted = remember(totalBytes) {
        val totalMb = totalBytes.toDouble() / (1024.0 * 1024.0)
        if (totalMb >= 1024.0) {
            val totalGb = totalMb / 1024.0
            String.format(java.util.Locale.US, "%.1f GB", totalGb)
        } else {
            String.format(java.util.Locale.US, "%.1f MB", totalMb)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Fr33Spacing.extraLarge),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$trackLabel • $minutes minutos • $sizeFormatted",
            style = Fr33Typography.bodyMedium,
            color = ColorTextTertiary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SortOrderDialog(
    currentOrder: LibrarySortOrder,
    onOrderSelected: (LibrarySortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Organizar lista", color = ColorTextPrimary) },
        text = {
            Column {
                LibrarySortOrder.entries.forEach { order ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOrderSelected(order) }
                            .padding(vertical = Fr33Spacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = order.title,
                            color = if (currentOrder == order) ColorPrimary else ColorTextPrimary,
                            style = Fr33Typography.bodyLarge,
                            fontWeight = if (currentOrder == order) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = ColorTextSecondary)
            }
        },
        containerColor = ColorSurface
    )
}

@Composable
private fun EditTrackInfoDialog(
    track: Track,
    onDismiss: () -> Unit,
    onConfirm: (newTitle: String, newArtist: String) -> Unit
) {
    var title by remember(track) { mutableStateOf(track.title) }
    var artist by remember(track) { mutableStateOf(track.artistName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Cambiar nombre", color = ColorTextPrimary) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Fr33Spacing.medium)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre de la canción") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artista") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = {
                        val (cleanTitle, cleanArtist) = TrackMetadataParser.parse(title, artist)
                        title = cleanTitle
                        artist = cleanArtist
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Fr33Radius.medium)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Limpiar con IA",
                        tint = ColorPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(Fr33Spacing.small))
                    Text(text = "Limpiar con IA", color = ColorPrimary)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && artist.isNotBlank()) {
                        onConfirm(title.trim(), artist.trim())
                    }
                },
                enabled = title.isNotBlank() && artist.isNotBlank()
            ) {
                Text("Guardar", color = ColorPrimary)
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

@Composable
private fun PlaylistRow(
    playlist: PlaylistWithSongCount,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onChangeCover: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

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

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Más opciones",
                    tint = ColorTextSecondary
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                containerColor = ColorSurface
            ) {
                DropdownMenuItem(
                    text = { Text("Cambiar nombre", color = ColorTextPrimary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = ColorTextSecondary
                        )
                    },
                    onClick = {
                        showMenu = false
                        onRename()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Cambiar portada", color = ColorTextPrimary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = ColorTextSecondary
                        )
                    },
                    onClick = {
                        showMenu = false
                        onChangeCover()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Borrar Playlist", color = ColorError) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = ColorError
                        )
                    },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}

@Composable
private fun SubPlaylistRow(
    subPlaylist: PlaylistWithSongCount,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onChangeCover: () -> Unit,
    onMerge: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(56.dp)) {
            TrackThumbnail(
                thumbnailUrl = subPlaylist.thumbnailUrl,
                contentDescription = subPlaylist.name,
                size = 56.dp,
                shapeRadius = Fr33Radius.medium,
                fallbackIcon = Icons.Default.Folder
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ColorSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Sub-playlist",
                    tint = ColorPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(Fr33Spacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subPlaylist.name,
                style = Fr33Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = ColorTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Carpeta • ${subPlaylist.songCount} ${if (subPlaylist.songCount == 1) "canción" else "canciones"}",
                style = Fr33Typography.bodySmall,
                color = ColorPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opciones de sub-playlist",
                    tint = ColorTextSecondary
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                containerColor = ColorSurface
            ) {
                DropdownMenuItem(
                    text = { Text("Modificar", color = ColorTextPrimary) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = ColorTextSecondary)
                    },
                    onClick = {
                        showMenu = false
                        onRename()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Cambiar portada", color = ColorTextPrimary) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = ColorTextSecondary)
                    },
                    onClick = {
                        showMenu = false
                        onChangeCover()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Combinar", color = ColorTextPrimary) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.CallMerge, contentDescription = null, tint = ColorPrimary)
                    },
                    onClick = {
                        showMenu = false
                        onMerge()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Eliminar", color = ColorError) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = ColorError)
                    },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}

@Composable
private fun PlaylistDetailView(
    playlistName: String,
    isSubPlaylist: Boolean = false,
    subPlaylists: List<PlaylistWithSongCount> = emptyList(),
    tracks: List<TrackEntity>,
    recursiveTracks: List<TrackEntity> = tracks,
    sortOrder: LibrarySortOrder = LibrarySortOrder.RECENTLY_ADDED,
    onSortOrderSelected: (LibrarySortOrder) -> Unit = {},
    onBack: () -> Unit,
    onOpenSubPlaylist: (PlaylistWithSongCount) -> Unit = {},
    onCreateSubPlaylist: () -> Unit = {},
    onRenameSubPlaylist: (Long, String) -> Unit = { _, _ -> },
    onChangeSubPlaylistCover: (Long) -> Unit = {},
    onMergeSubPlaylist: (PlaylistWithSongCount) -> Unit = {},
    onDeleteSubPlaylist: (Long) -> Unit = {},
    onExport: () -> Unit = {},
    onAutoRenameAll: (List<TrackEntity>) -> Unit = {},
    onRefreshMetadata: (List<TrackEntity>) -> Unit = {},
    onChangeCover: () -> Unit = {},
    onPlayTrack: (Track, List<Track>) -> Unit,
    onPlayAll: (List<Track>, Boolean) -> Unit,
    onDownloadAll: (List<Track>) -> Unit = {},
    onToggleSave: (Track, Boolean) -> Unit,
    onDeleteDownload: (String) -> Unit,
    onAddToPlaylist: (Track) -> Unit = {},
    onRemoveFromPlaylist: (String) -> Unit,
    onRename: (Track) -> Unit,
    isSelectionMode: Boolean = false,
    selectedTrackIds: Set<String> = emptySet(),
    onToggleSelection: (Track) -> Unit = {},
    onSelectAll: (List<Track>) -> Unit = {},
    onCancelSelection: () -> Unit = {},
    onBulkAddToPlaylist: (List<Track>) -> Unit = {},
    onBulkAddToFavorites: () -> Unit = {},
    onBulkDelete: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var showTopMenu by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    val displaySubPlaylists = remember(subPlaylists, searchQuery) {
        if (searchQuery.isBlank()) subPlaylists
        else subPlaylists.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val displayTracks = remember(tracks, recursiveTracks, searchQuery) {
        if (searchQuery.isBlank()) tracks
        else recursiveTracks.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.artistName.contains(searchQuery, ignoreCase = true)
        }
    }

    val displayDomainTracks = remember(displayTracks) { displayTracks.map { it.toDomain() } }
    val recursiveDomainTracks = remember(recursiveTracks) { recursiveTracks.map { it.toDomain() } }
    val playSourceDomainTracks = remember(isSubPlaylist, tracks, recursiveTracks) {
        (if (isSubPlaylist) tracks else recursiveTracks).map { it.toDomain() }
    }
    val listState = rememberLazyListState()

    BackHandler(enabled = isSearchVisible && !isSelectionMode) {
        isSearchVisible = false
        searchQuery = ""
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isSelectionMode) {
            SelectionTopBar(
                selectedCount = selectedTrackIds.size,
                onSelectAll = { onSelectAll(displayDomainTracks) },
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
                    text = playlistName,
                    style = Fr33Typography.titleLarge,
                    color = ColorTextPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Botón minimalista circular rojo con Play blanco para reproducir toda la playlist (raíz + sub-playlists)
                if (recursiveDomainTracks.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(Fr33Spacing.small))
                    IconButton(
                        onClick = { onPlayAll(recursiveDomainTracks, false) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ColorPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Reproducir toda la playlist",
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(Fr33Spacing.extraSmall))
                }

                // Botón rápido para crear sub-playlist
                IconButton(onClick = onCreateSubPlaylist) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = "Nueva sub-playlist",
                        tint = ColorTextPrimary
                    )
                }

                // Icono de búsqueda (Lupa) colocado justo a la izquierda de los 3 puntitos
                IconButton(onClick = {
                    isSearchVisible = !isSearchVisible
                    if (!isSearchVisible) {
                        searchQuery = ""
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar canciones",
                        tint = if (isSearchVisible) ColorPrimary else ColorTextPrimary
                    )
                }

                // Menú de 3 puntos en barra superior
                Box {
                    IconButton(onClick = { showTopMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones",
                            tint = ColorTextPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = showTopMenu,
                        onDismissRequest = { showTopMenu = false },
                        containerColor = ColorSurface
                    ) {
                        if (playSourceDomainTracks.isNotEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Descargar todo", color = ColorTextPrimary) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        tint = ColorPrimary
                                    )
                                },
                                onClick = {
                                    showTopMenu = false
                                    onDownloadAll(playSourceDomainTracks)
                                }
                            )
                        }

                        if (tracks.isNotEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Refrescar metadatos", color = ColorTextPrimary) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = ColorPrimary
                                    )
                                },
                                onClick = {
                                    showTopMenu = false
                                    onRefreshMetadata(tracks)
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("Exportar playlist", color = ColorTextPrimary) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = ColorTextSecondary
                                )
                            },
                            onClick = {
                                showTopMenu = false
                                onExport()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Organizar", color = ColorTextPrimary) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = ColorTextSecondary
                                )
                            },
                            onClick = {
                                showTopMenu = false
                                showSortDialog = true
                            }
                        )

                        if (tracks.isNotEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Nombres automáticos (IA)", color = ColorTextPrimary) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = ColorPrimary
                                    )
                                },
                                onClick = {
                                    showTopMenu = false
                                    onAutoRenameAll(tracks)
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("Cambiar portada", color = ColorTextPrimary) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = ColorTextSecondary
                                )
                            },
                            onClick = {
                                showTopMenu = false
                                onChangeCover()
                            }
                        )
                    }
                }
            }
        }

        if (showSortDialog) {
            SortOrderDialog(
                currentOrder = sortOrder,
                onOrderSelected = {
                    onSortOrderSelected(it)
                    showSortDialog = false
                },
                onDismiss = { showSortDialog = false }
            )
        }

        if (isSearchVisible) {
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
                    IconButton(onClick = {
                        if (searchQuery.isNotEmpty()) {
                            searchQuery = ""
                        } else {
                            isSearchVisible = false
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar búsqueda",
                            tint = ColorTextSecondary
                        )
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

        if (tracks.isEmpty() && subPlaylists.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LibraryEmptyView(
                    icon = Icons.Default.MusicNote,
                    title = "Playlist vacía",
                    subtitle = "Usa '+' para añadir canciones o crea una sub-playlist con el icono de carpeta."
                )
            }
        } else if (displaySubPlaylists.isEmpty() && displayTracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(Fr33Spacing.extraLarge),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontraron resultados que coincidan con la búsqueda",
                    style = Fr33Typography.bodyMedium,
                    color = ColorTextTertiary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (displaySubPlaylists.isNotEmpty()) {
                    items(displaySubPlaylists, key = { "sub_${it.id}" }) { sub ->
                        SubPlaylistRow(
                            subPlaylist = sub,
                            onClick = {
                                if (!isSelectionMode) {
                                    onOpenSubPlaylist(sub)
                                }
                            },
                            onRename = { onRenameSubPlaylist(sub.id, sub.name) },
                            onChangeCover = { onChangeSubPlaylistCover(sub.id) },
                            onMerge = { onMergeSubPlaylist(sub) },
                            onDelete = { onDeleteSubPlaylist(sub.id) }
                        )
                        HorizontalDivider(
                            color = ColorDivider,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 72.dp)
                        )
                    }
                }

                items(displayTracks, key = { it.id }) { trackEntity ->
                    val domain = trackEntity.toDomain()
                    val isSelected = selectedTrackIds.contains(domain.trackId)
                    TrackRow(
                        track = domain,
                        isSelectionMode = isSelectionMode,
                        isSelected = isSelected,
                        onClick = {
                            if (isSelectionMode) {
                                onToggleSelection(domain)
                            } else {
                                onPlayTrack(domain, displayDomainTracks)
                            }
                        },
                        onLongClick = { onToggleSelection(domain) },
                        trailingContent = if (isSelectionMode) null else {
                            {
                                TrackKebabMenu(
                                    isSaved = trackEntity.isSaved,
                                    onToggleSave = { onToggleSave(domain, trackEntity.isSaved) },
                                    onRename = { onRename(domain) },
                                    onDelete = { onDeleteDownload(trackEntity.id) },
                                    onAddToPlaylist = { onAddToPlaylist(domain) },
                                    addToPlaylistLabel = "Añadir a otra Playlist",
                                    onRemoveFromPlaylist = { onRemoveFromPlaylist(trackEntity.id) }
                                )
                            }
                        }
                    )
                    HorizontalDivider(
                        color = ColorDivider,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 72.dp)
                    )
                }

                item {
                    ListSummaryFooter(tracks = displayTracks)
                }
            }
        }

        if (isSelectionMode) {
            SelectionBottomBar(
                onAddToPlaylist = {
                    val selected = displayDomainTracks.filter { selectedTrackIds.contains(it.trackId) }
                    onBulkAddToPlaylist(selected)
                },
                onAddToFavorites = onBulkAddToFavorites,
                onDelete = onBulkDelete
            )
        }
    }
}

@Composable
private fun ArtistRow(
    artist: ArtistItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onMerge: () -> Unit,
    onChangeCover: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrackThumbnail(
            thumbnailUrl = artist.thumbnailUrl,
            trackId = artist.firstTrackId,
            contentDescription = artist.name,
            size = 56.dp,
            isCircular = true,
            shapeRadius = Fr33Radius.medium,
            fallbackIcon = Icons.Default.Person
        )

        Spacer(modifier = Modifier.width(Fr33Spacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name,
                style = Fr33Typography.titleMedium,
                color = ColorTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${artist.trackCount} ${if (artist.trackCount == 1) "canción" else "canciones"}",
                style = Fr33Typography.bodyMedium,
                color = ColorTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar",
                tint = ColorTextTertiary
            )
        }

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Más opciones",
                    tint = ColorTextSecondary
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                containerColor = ColorSurface
            ) {
                DropdownMenuItem(
                    text = { Text("Cambiar nombre", color = ColorTextPrimary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = ColorTextSecondary
                        )
                    },
                    onClick = {
                        showMenu = false
                        onRename()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Combinar", color = ColorTextPrimary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CallMerge,
                            contentDescription = null,
                            tint = ColorTextSecondary
                        )
                    },
                    onClick = {
                        showMenu = false
                        onMerge()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Cambiar portada", color = ColorTextPrimary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = ColorTextSecondary
                        )
                    },
                    onClick = {
                        showMenu = false
                        onChangeCover()
                    }
                )
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
    onAutoRenameAll: (List<TrackEntity>) -> Unit = {},
    onRefreshMetadata: (List<TrackEntity>) -> Unit = {},
    onChangeCover: () -> Unit = {},
    onPlayTrack: (Track, List<Track>) -> Unit,
    onPlayAll: (List<Track>) -> Unit,
    onDownloadAll: (List<Track>) -> Unit = {},
    onToggleSave: (Track, Boolean) -> Unit,
    onDeleteDownload: (String) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    onRename: (Track) -> Unit,
    isSelectionMode: Boolean = false,
    selectedTrackIds: Set<String> = emptySet(),
    onToggleSelection: (Track) -> Unit = {},
    onSelectAll: (List<Track>) -> Unit = {},
    onCancelSelection: () -> Unit = {},
    onBulkAddToPlaylist: (List<Track>) -> Unit = {},
    onBulkAddToFavorites: () -> Unit = {},
    onBulkDelete: () -> Unit = {}
) {
    val domainTracks = tracks.map { it.toDomain() }
    val listState = rememberLazyListState()
    var showTopMenu by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredTracks = remember(tracks, searchQuery) {
        if (searchQuery.isBlank()) tracks
        else tracks.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.artistName.contains(searchQuery, ignoreCase = true) ||
            (it.albumName?.contains(searchQuery, ignoreCase = true) == true)
        }
    }
    val filteredDomainTracks = remember(filteredTracks) { filteredTracks.map { it.toDomain() } }

    BackHandler(enabled = isSearchVisible && !isSelectionMode) {
        isSearchVisible = false
        searchQuery = ""
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isSelectionMode) {
            SelectionTopBar(
                selectedCount = selectedTrackIds.size,
                onSelectAll = { onSelectAll(filteredDomainTracks) },
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

            // Icono de búsqueda (Lupa) colocado justo a la izquierda de los 3 puntitos
            IconButton(onClick = {
                isSearchVisible = !isSearchVisible
                if (!isSearchVisible) {
                    searchQuery = ""
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar canciones",
                    tint = if (isSearchVisible) ColorPrimary else ColorTextPrimary
                )
            }

            // Menú de 3 puntos en barra superior
            Box {
                IconButton(onClick = { showTopMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Más opciones",
                        tint = ColorTextPrimary
                    )
                }

                DropdownMenu(
                    expanded = showTopMenu,
                    onDismissRequest = { showTopMenu = false },
                    containerColor = ColorSurface
                ) {
                    if (domainTracks.isNotEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Descargar todo", color = ColorTextPrimary) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = ColorPrimary
                                )
                            },
                            onClick = {
                                showTopMenu = false
                                onDownloadAll(domainTracks)
                            }
                        )
                    }

                    if (tracks.isNotEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Refrescar metadatos", color = ColorTextPrimary) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = ColorPrimary
                                )
                            },
                            onClick = {
                                showTopMenu = false
                                onRefreshMetadata(tracks)
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = { Text("Organizar", color = ColorTextPrimary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = ColorTextSecondary
                            )
                        },
                        onClick = {
                            showTopMenu = false
                            showSortDialog = true
                        }
                    )

                    if (tracks.isNotEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Nombres automáticos (IA)", color = ColorTextPrimary) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = ColorPrimary
                                )
                            },
                            onClick = {
                                showTopMenu = false
                                onAutoRenameAll(tracks)
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = { Text("Cambiar portada", color = ColorTextPrimary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = ColorTextSecondary
                            )
                        },
                        onClick = {
                            showTopMenu = false
                            onChangeCover()
                        }
                    )
                }
            }
        }
        }

        if (isSearchVisible && !isSelectionMode) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small),
                placeholder = {
                    Text(
                        text = "Buscar en este artista...",
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

        if (showSortDialog) {
            SortOrderDialog(
                currentOrder = sortOrder,
                onOrderSelected = {
                    onSortOrderSelected(it)
                    showSortDialog = false
                },
                onDismiss = { showSortDialog = false }
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (filteredTracks.isEmpty() && searchQuery.isNotBlank()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Fr33Spacing.extraLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron canciones que coincidan con \"$searchQuery\"",
                            style = Fr33Typography.bodyMedium,
                            color = ColorTextTertiary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredTracks, key = { it.id }) { trackEntity ->
                    val domain = trackEntity.toDomain()
                    val isSelected = selectedTrackIds.contains(domain.trackId)
                    TrackRow(
                        track = domain,
                        isSelectionMode = isSelectionMode,
                        isSelected = isSelected,
                        onClick = {
                            if (isSelectionMode) {
                                onToggleSelection(domain)
                            } else {
                                onPlayTrack(domain, filteredDomainTracks)
                            }
                        },
                        onLongClick = { onToggleSelection(domain) },
                        trailingContent = if (isSelectionMode) null else {
                            {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { onToggleSave(domain, trackEntity.isSaved) }) {
                                        Icon(
                                            imageVector = if (trackEntity.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = if (trackEntity.isSaved) "Quitar de favoritos" else "Guardar en favoritos",
                                            tint = if (trackEntity.isSaved) ColorPrimary else ColorTextTertiary
                                        )
                                    }
                                    TrackKebabMenu(
                                        onRename = { onRename(domain) },
                                        onDelete = { onDeleteDownload(trackEntity.id) },
                                        onAddToPlaylist = { onAddToPlaylist(domain) }
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

                item {
                    ListSummaryFooter(tracks = filteredTracks)
                }
            }
        }

        if (isSelectionMode) {
            SelectionBottomBar(
                onAddToPlaylist = {
                    val selected = filteredDomainTracks.filter { selectedTrackIds.contains(it.trackId) }
                    onBulkAddToPlaylist(selected)
                },
                onAddToFavorites = onBulkAddToFavorites,
                onDelete = onBulkDelete
            )
        }
    }
}

@Composable
private fun CreatePlaylistDialog(
    title: String = "Nueva Playlist",
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, color = ColorTextPrimary) },
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
    onCreateNewPlaylist: () -> Unit,
    itemDescription: String? = null
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
                    text = itemDescription ?: "${track.title} - ${track.artistName}",
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
                                                    onClick = { onSelectPlaylist(sub.id) },
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
