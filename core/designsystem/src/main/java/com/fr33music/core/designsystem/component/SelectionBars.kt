package com.fr33music.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fr33music.core.designsystem.theme.ColorDivider
import com.fr33music.core.designsystem.theme.ColorError
import com.fr33music.core.designsystem.theme.ColorPrimary
import com.fr33music.core.designsystem.theme.ColorSurface
import com.fr33music.core.designsystem.theme.ColorTextPrimary
import com.fr33music.core.designsystem.theme.ColorTextSecondary
import com.fr33music.core.designsystem.theme.Fr33Spacing
import com.fr33music.core.designsystem.theme.Fr33Typography

@Composable
fun SelectionTopBar(
    selectedCount: Int,
    onSelectAll: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ColorSurface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Fr33Spacing.small, vertical = Fr33Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancelar selección",
                        tint = ColorTextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(Fr33Spacing.small))

                Text(
                    text = "$selectedCount ${if (selectedCount == 1) "seleccionado" else "seleccionados"}",
                    style = Fr33Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ColorTextPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                TextButton(onClick = onSelectAll) {
                    Icon(
                        imageVector = Icons.Default.SelectAll,
                        contentDescription = "Seleccionar Todo",
                        tint = ColorPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Todo",
                        style = Fr33Typography.labelLarge,
                        color = ColorPrimary
                    )
                }
            }
            HorizontalDivider(color = ColorDivider, thickness = 0.5.dp)
        }
    }
}

@Composable
fun SelectionBottomBar(
    onAddToPlaylist: () -> Unit,
    onAddToFavorites: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ColorSurface,
        tonalElevation = 6.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            HorizontalDivider(color = ColorDivider, thickness = 0.5.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Fr33Spacing.small, horizontal = Fr33Spacing.medium),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SelectionActionItem(
                    icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                    label = "Añadir a Playlist",
                    tint = ColorTextPrimary,
                    onClick = onAddToPlaylist
                )

                SelectionActionItem(
                    icon = Icons.Default.Favorite,
                    label = "Favoritos",
                    tint = ColorPrimary,
                    onClick = onAddToFavorites
                )

                if (onDelete != null) {
                    SelectionActionItem(
                        icon = Icons.Default.Delete,
                        label = "Eliminar",
                        tint = ColorError,
                        onClick = onDelete
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectionActionItem(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = Fr33Spacing.medium, vertical = Fr33Spacing.extraSmall),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = Fr33Typography.labelSmall,
            color = if (tint == ColorError) ColorError else ColorTextSecondary
        )
    }
}
