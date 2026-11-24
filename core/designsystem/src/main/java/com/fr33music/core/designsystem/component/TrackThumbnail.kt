package com.fr33music.core.designsystem.component

import android.net.Uri
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
import androidx.compose.ui.graphics.vector.ImageVector
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

    // 1. Prioridad: carátula descargada localmente en filesDir/downloads/{trackId}.jpg
    val localFile: File? = remember(trackId) {
        if (!trackId.isNullOrBlank()) {
            val file = File(context.filesDir, "downloads/$trackId.jpg")
            if (file.exists() && file.length() > 0) file else null
        } else null
    }

    // 2. Determinar el modelo de imagen seguro (manejo de paths locales obsoletos y URLs remotas)
    val imageModel: Any? = remember(thumbnailUrl, localFile, trackId) {
        if (localFile != null && localFile.exists() && localFile.length() > 0) {
            localFile
        } else if (thumbnailUrl?.startsWith("file:") == true || thumbnailUrl?.startsWith("/") == true) {
            val path = try {
                Uri.parse(thumbnailUrl).path ?: thumbnailUrl.removePrefix("file://")
            } catch (e: Exception) {
                thumbnailUrl.removePrefix("file://")
            }
            val f = File(path)
            if (f.exists() && f.length() > 0) {
                f
            } else if (!trackId.isNullOrBlank()) {
                "https://i.ytimg.com/vi/$trackId/hqdefault.jpg"
            } else {
                null
            }
        } else if (!thumbnailUrl.isNullOrBlank()) {
            Track.upgradeThumbnail(thumbnailUrl)
        } else if (!trackId.isNullOrBlank()) {
            "https://i.ytimg.com/vi/$trackId/hqdefault.jpg"
        } else {
            null
        }
    }

    // Fallback garantizado de YouTube si la imagen primaria falla (ej: URLs caídas o 404)
    val youtubeFallbackUrl: String? = remember(trackId, imageModel) {
        if (!trackId.isNullOrBlank() && imageModel != "https://i.ytimg.com/vi/$trackId/hqdefault.jpg") {
            "https://i.ytimg.com/vi/$trackId/hqdefault.jpg"
        } else {
            null
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
            if (youtubeFallbackUrl != null) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(youtubeFallbackUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        ElegantPlaceholder(size = size, isCircular = isCircular, fallbackIcon = fallbackIcon)
                    },
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
