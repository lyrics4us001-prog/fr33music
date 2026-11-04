package com.fr33music.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fr33music.core.designsystem.component.LoadingSkeleton
import com.fr33music.core.designsystem.component.TrackRow
import com.fr33music.core.designsystem.theme.ColorBackground
import com.fr33music.core.designsystem.theme.ColorDivider
import com.fr33music.core.designsystem.theme.ColorPrimary
import com.fr33music.core.designsystem.theme.ColorSurface
import com.fr33music.core.designsystem.theme.ColorTextPrimary
import com.fr33music.core.designsystem.theme.ColorTextSecondary
import com.fr33music.core.designsystem.theme.ColorTextTertiary
import com.fr33music.core.designsystem.theme.Fr33Radius
import com.fr33music.core.designsystem.theme.Fr33Spacing
import com.fr33music.core.designsystem.theme.Fr33Typography

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = ColorPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(Fr33Spacing.small))
                Text(
                    text = "Fr33Music",
                    style = Fr33Typography.headlineMedium,
                    color = ColorTextPrimary
                )
            }

            IconButton(onClick = viewModel::openSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ajustes",
                    tint = ColorTextSecondary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Título de recomendaciones y botón de refresco
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = uiState.recommendationTitle,
                style = Fr33Typography.titleMedium,
                color = ColorTextPrimary,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = viewModel::loadRecommendations) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refrescar recomendaciones",
                    tint = ColorTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Contenido de recomendaciones
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingSkeleton(itemCount = 6)
                }

                uiState.recommendations.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Fr33Spacing.extraLarge),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = ColorTextTertiary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(Fr33Spacing.large))
                        Text(
                            text = "Explora y busca música",
                            style = Fr33Typography.titleMedium,
                            color = ColorTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(Fr33Spacing.small))
                        Text(
                            text = "Busca tus artistas o canciones favoritas en la pestaña Buscar para generar recomendaciones personalizadas.",
                            style = Fr33Typography.bodyMedium,
                            color = ColorTextTertiary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.recommendations, key = { it.trackId }) { track ->
                            TrackRow(
                                track = track,
                                onClick = { viewModel.playTrack(track) }
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

    // Diálogo de Ajustes
    if (uiState.showSettings) {
        AlertDialog(
            onDismissRequest = viewModel::closeSettings,
            containerColor = ColorSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = ColorPrimary
                    )
                    Spacer(modifier = Modifier.width(Fr33Spacing.small))
                    Text(text = "Ajustes", color = ColorTextPrimary, style = Fr33Typography.titleLarge)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Fr33Music v1.0.0 (Fase 2)",
                        style = Fr33Typography.titleMedium,
                        color = ColorTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "NewPipeExtractor: v${uiState.extractorVersion}",
                        style = Fr33Typography.bodyMedium,
                        color = ColorTextSecondary
                    )

                    Spacer(modifier = Modifier.height(Fr33Spacing.large))

                    OutlinedButton(
                        onClick = viewModel::clearSearchHistory,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Fr33Radius.medium)
                    ) {
                        Text(text = "Limpiar historial de búsqueda", color = ColorTextPrimary)
                    }

                    Spacer(modifier = Modifier.height(Fr33Spacing.small))

                    OutlinedButton(
                        onClick = viewModel::clearAppCache,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Fr33Radius.medium)
                    ) {
                        Text(text = "Limpiar caché de red e imágenes", color = ColorTextPrimary)
                    }

                    if (uiState.cacheClearedMessage != null) {
                        Spacer(modifier = Modifier.height(Fr33Spacing.small))
                        Text(
                            text = uiState.cacheClearedMessage.orEmpty(),
                            style = Fr33Typography.labelMedium,
                            color = ColorPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::closeSettings) {
                    Text(text = "Cerrar", color = ColorPrimary)
                }
            }
        )
    }
}
