package com.fr33music.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.fr33music.core.designsystem.theme.ColorPrimary
import com.fr33music.core.designsystem.theme.ColorSurfaceVariant
import com.fr33music.core.designsystem.theme.Fr33Radius
import com.fr33music.core.domain.model.Track
import java.io.File

import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun TrackThumbnail(
    thumbnailUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    trackId: String? = null,
    size: Dp = 56.dp,
    shapeRadius: Dp = Fr33Radius.small,
    isCircular: Boolean = false,
    fallbackIcon: ImageVector = Icons.Default.MusicNote
) {
    val context = LocalContext.current
    val shape = if (isCircular) CircleShape else RoundedCornerShape(shapeRadius)

    // Si la pista tiene imagen descargada localmente en filesDir/downloads/{trackId}.jpg, usarla prioritariamente
    val localFile: File? = remember(trackId) {
        if (!trackId.isNullOrBlank()) {
            val file = File(context.filesDir, "downloads/$trackId.jpg")
            if (file.exists() && file.length() > 0) file else null
        } else null
    }

    val hdUrl = remember(thumbnailUrl) {
        Track.upgradeThumbnail(thumbnailUrl)
    }

    val sdFallbackUrl = remember(hdUrl) {
        if (hdUrl?.contains("/maxresdefault.jpg") == true) {
            hdUrl.replace("/maxresdefault.jpg", "/sddefault.jpg")
        } else {
            null
        }
    }

    val hqFallbackUrl = remember(hdUrl) {
        if (hdUrl?.contains("/maxresdefault.jpg") == true) {
            hdUrl.replace("/maxresdefault.jpg", "/hqdefault.jpg")
        } else {
            null
        }
    }

    val imageModel: Any? = remember(hdUrl, localFile) {
        if (localFile != null && localFile.exists()) {
            localFile
        } else if (hdUrl?.startsWith("file:") == true) {
            hdUrl
        } else {
            hdUrl
        }
    }

    if (imageModel == null || (imageModel is String && imageModel.isBlank())) {
        Box(modifier = modifier.size(size)) {
            ElegantPlaceholder(size = size, isCircular = isCircular, fallbackIcon = fallbackIcon)
        }
        return
    }

    val request = remember(imageModel) {
        ImageRequest.Builder(context)
            .data(imageModel)
            .crossfade(true)
            .build()
    }

    SubcomposeAsyncImage(
        model = request,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(ColorSurfaceVariant),
        loading = {
            ElegantPlaceholder(size = size, isCircular = isCircular, fallbackIcon = fallbackIcon)
        },
        error = {
            if (sdFallbackUrl != null) {
                // Reintento 1: calidad SD si maxresdefault devolvió 404
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(sdFallbackUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = {
                        if (hqFallbackUrl != null) {
                            // Reintento 2: calidad HQ si SD también falló
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(hqFallbackUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = contentDescription,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                error = {
                                    if (localFile != null) {
                                        SubcomposeAsyncImage(
                                            model = localFile,
                                            contentDescription = contentDescription,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                            error = {
                                                ElegantPlaceholder(size = size, isCircular = isCircular, fallbackIcon = fallbackIcon)
                                            }
                                        )
                                    } else {
                                        ElegantPlaceholder(size = size, isCircular = isCircular, fallbackIcon = fallbackIcon)
                                    }
                                }
                            )
                        } else if (localFile != null) {
                            SubcomposeAsyncImage(
                                model = localFile,
                                contentDescription = contentDescription,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                error = {
                                    ElegantPlaceholder(size = size, isCircular = isCircular, fallbackIcon = fallbackIcon)
                                }
                            )
                        } else {
                            ElegantPlaceholder(size = size, isCircular = isCircular, fallbackIcon = fallbackIcon)
                        }
                    }
                )
            } else if (localFile != null) {
                SubcomposeAsyncImage(
                    model = localFile,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = {
                        ElegantPlaceholder(size = size, isCircular = isCircular, fallbackIcon = fallbackIcon)
                    }
                )
            } else {
                ElegantPlaceholder(size = size, isCircular = isCircular, fallbackIcon = fallbackIcon)
            }
        }
    )
}

@Composable
private fun ElegantPlaceholder(
    size: Dp,
    isCircular: Boolean = false,
    fallbackIcon: ImageVector = Icons.Default.MusicNote
) {
    val shape = if (isCircular) CircleShape else RoundedCornerShape(Fr33Radius.small)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF242426), Color(0xFF161618))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.7f)
                .clip(CircleShape)
                .background(Color(0xFF1F1F21)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = fallbackIcon,
                contentDescription = null,
                tint = ColorPrimary.copy(alpha = 0.8f),
                modifier = Modifier.size(size * 0.35f)
            )
        }
    }
}
