package com.fr33music.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fr33music.core.designsystem.theme.ColorBackground
import com.fr33music.core.designsystem.theme.ColorPrimary
import com.fr33music.core.designsystem.theme.ColorSurface
import com.fr33music.core.designsystem.theme.ColorTextPrimary
import com.fr33music.core.designsystem.theme.ColorTextSecondary
import com.fr33music.core.designsystem.theme.ColorTextTertiary
import com.fr33music.core.player.PlayerRepository
import com.fr33music.feature.home.HomeScreen
import com.fr33music.feature.library.LibraryScreen
import com.fr33music.feature.player.PlayerScreen
import com.fr33music.feature.search.SearchScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

enum class NavTab(val title: String, val icon: ImageVector) {
    HOME("Principal", Icons.Default.Home),
    SEARCH("Buscar", Icons.Default.Search),
    LIBRARY("Biblioteca", Icons.Default.MusicNote)
}

@HiltViewModel
class MainViewModel @Inject constructor(
    val playerRepository: PlayerRepository
) : ViewModel()

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val playbackState by viewModel.playerRepository.playbackState.collectAsStateWithLifecycle()
    var currentTab by remember { mutableStateOf(NavTab.SEARCH) }
    var isPlayerExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    LaunchedEffect(playbackState.error) {
        val err = playbackState.error
        if (!err.isNullOrBlank()) {
            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
        }
    }

    BackHandler(enabled = isPlayerExpanded) {
        isPlayerExpanded = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        Scaffold(
            bottomBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    MiniPlayer(
                        playbackState = playbackState,
                        onPlayPauseClick = { viewModel.playerRepository.togglePlayPause() },
                        onClick = { isPlayerExpanded = true }
                    )

                    NavigationBar(
                        containerColor = ColorSurface,
                        contentColor = ColorTextPrimary
                    ) {
                        NavTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = { Text(text = tab.title) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ColorPrimary,
                                    selectedTextColor = ColorPrimary,
                                    indicatorColor = ColorSurface,
                                    unselectedIconColor = ColorTextTertiary,
                                    unselectedTextColor = ColorTextSecondary
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    NavTab.HOME -> HomeScreen()
                    NavTab.SEARCH -> SearchScreen(
                        onTrackSelected = {
                            isPlayerExpanded = true
                        }
                    )
                    NavTab.LIBRARY -> LibraryScreen()
                }
            }
        }

        // Full Screen Player Overlay
        AnimatedVisibility(
            visible = isPlayerExpanded,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            PlayerScreen(
                onCollapse = { isPlayerExpanded = false },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
