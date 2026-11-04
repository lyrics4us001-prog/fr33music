package com.fr33music.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fr33music.core.designsystem.theme.ColorTextPrimary
import com.fr33music.core.designsystem.theme.ColorTextSecondary
import com.fr33music.core.designsystem.theme.Fr33Spacing
import com.fr33music.core.designsystem.theme.Fr33Typography
import com.fr33music.core.domain.model.Track

@Composable
fun TrackRow(
    track: Track,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrackThumbnail(
            thumbnailUrl = track.thumbnailUrl,
            trackId = track.trackId,
            contentDescription = track.title,
            size = 56.dp
        )

        Spacer(modifier = Modifier.width(Fr33Spacing.medium))

        Column(
            modifier = Modifier.weight(1f)
        ) {
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

        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(Fr33Spacing.small))
            trailingContent()
        }
    }
}
