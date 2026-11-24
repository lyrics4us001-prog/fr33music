package com.fr33music.ui.settings

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import java.io.File

@Composable
fun SettingsScreen(
    onOpenEqualizer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showConfirmClearAudiosDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.showNoUpdateToast) {
        if (uiState.showNoUpdateToast) {
            Toast.makeText(context, "Ya tienes la última versión", Toast.LENGTH_SHORT).show()
            viewModel.onNoUpdateToastShown()
        }
    }

    LaunchedEffect(uiState.updateError) {
        uiState.updateError?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearUpdateError()
        }
    }

    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedbackMessage()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Encabezado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = ColorPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(Fr33Spacing.small))
            Text(
                text = "Ajustes",
                style = Fr33Typography.headlineMedium,
                color = ColorTextPrimary
            )
        }

        // Tarjeta de Información de Versión
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.large),
            colors = CardDefaults.cardColors(containerColor = ColorSurface),
            shape = RoundedCornerShape(Fr33Radius.large)
        ) {
            Column(modifier = Modifier.padding(Fr33Spacing.large)) {
                Text(
                    text = "Fr33Music v${uiState.appVersion}",
                    style = Fr33Typography.titleMedium,
                    color = ColorTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Motor extractor: NewPipeExtractor v${uiState.extractorVersion}",
                    style = Fr33Typography.bodySmall,
                    color = ColorTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(Fr33Spacing.large))

        // SECCIÓN: Audio y Reproducción
        SettingsSectionHeader(title = "Audio y Reproducción")

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.large),
            colors = CardDefaults.cardColors(containerColor = ColorSurface),
            shape = RoundedCornerShape(Fr33Radius.large)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Opción Ecualizador
                SettingsClickableRow(
                    icon = Icons.Default.Equalizer,
                    title = "Ecualizador",
                    subtitle = "Bandas de frecuencia y perfiles (Rock, Pop, Graves...)",
                    onClick = onOpenEqualizer
                )
            }
        }

        Spacer(modifier = Modifier.height(Fr33Spacing.large))

        // SECCIÓN: Actualizaciones
        SettingsSectionHeader(title = "Actualizaciones")

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.large),
            colors = CardDefaults.cardColors(containerColor = ColorSurface),
            shape = RoundedCornerShape(Fr33Radius.large)
        ) {
            Column(modifier = Modifier.padding(Fr33Spacing.large)) {
                Button(
                    onClick = viewModel::checkForUpdates,
                    enabled = !uiState.isCheckingUpdate && !uiState.isDownloadingUpdate,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Fr33Radius.medium),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                ) {
                    if (uiState.isCheckingUpdate) {
                        CircularProgressIndicator(
                            color = ColorTextPrimary,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(Fr33Spacing.small))
                        Text(text = "Buscando actualizaciones...", color = ColorTextPrimary)
                    } else {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = ColorTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(Fr33Spacing.small))
                        Text(text = "Buscar actualizaciones", color = ColorTextPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Fr33Spacing.large))

        // SECCIÓN: Almacenamiento y Privacidad
        SettingsSectionHeader(title = "Almacenamiento y Privacidad")

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.large),
            colors = CardDefaults.cardColors(containerColor = ColorSurface),
            shape = RoundedCornerShape(Fr33Radius.large)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Fr33Spacing.large),
                verticalArrangement = Arrangement.spacedBy(Fr33Spacing.medium)
            ) {
                OutlinedButton(
                    onClick = viewModel::clearSearchHistory,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Fr33Radius.medium)
                ) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = ColorTextPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(Fr33Spacing.small))
                    Text(text = "Limpiar historial de búsqueda", color = ColorTextPrimary)
                }

                OutlinedButton(
                    onClick = viewModel::clearAppCache,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Fr33Radius.medium)
                ) {
                    Icon(imageVector = Icons.Default.LayersClear, contentDescription = null, tint = ColorTextPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(Fr33Spacing.small))
                    Text(text = "Limpiar caché de red e imágenes", color = ColorTextPrimary)
                }

                OutlinedButton(
                    onClick = { showConfirmClearAudiosDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Fr33Radius.medium),
                    border = BorderStroke(1.dp, ColorError.copy(alpha = 0.6f))
                ) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = ColorError, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(Fr33Spacing.small))
                    Text(text = "Liberar espacio (Borrar audios)", color = ColorError)
                }
            }
        }

        Spacer(modifier = Modifier.height(Fr33Spacing.large))

        // SECCIÓN: Diagnóstico
        SettingsSectionHeader(title = "Diagnóstico")

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.large),
            colors = CardDefaults.cardColors(containerColor = ColorSurface),
            shape = RoundedCornerShape(Fr33Radius.large)
        ) {
            Column(modifier = Modifier.padding(Fr33Spacing.large)) {
                OutlinedButton(
                    onClick = {
                        val crashFile = File(context.filesDir, "last_crash.txt")
                        if (crashFile.exists() && crashFile.length() > 0) {
                            try {
                                val crashContent = crashFile.readText()
                                val uri = try {
                                    FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        crashFile
                                    )
                                } catch (_: Exception) {
                                    null
                                }

                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Fr33Music - Registro de error")
                                    putExtra(Intent.EXTRA_TEXT, crashContent)
                                    if (uri != null) {
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                }
                                val chooser = Intent.createChooser(sendIntent, "Compartir último registro de errores").apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(chooser)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error al compartir log: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "No hay errores registrados", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Fr33Radius.medium)
                ) {
                    Icon(imageVector = Icons.Default.BugReport, contentDescription = null, tint = ColorTextPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(Fr33Spacing.small))
                    Text(text = "Compartir último registro de errores", color = ColorTextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(Fr33Spacing.extraLarge))
    }

    // Diálogo de Confirmación para Borrar Audios Descargados
    if (showConfirmClearAudiosDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearAudiosDialog = false },
            containerColor = ColorSurface,
            title = {
                Text(
                    text = "¿Liberar espacio?",
                    color = ColorTextPrimary,
                    style = Fr33Typography.titleMedium
                )
            },
            text = {
                Text(
                    text = "Se eliminarán permanentemente todos los archivos de audio descargados en el dispositivo para liberar almacenamiento. Las canciones seguirán en tu biblioteca para escuchar en streaming.",
                    color = ColorTextSecondary,
                    style = Fr33Typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmClearAudiosDialog = false
                        viewModel.clearAllDownloadedAudios()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorError)
                ) {
                    Text(text = "Eliminar audios", color = ColorTextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearAudiosDialog = false }) {
                    Text(text = "Cancelar", color = ColorTextSecondary)
                }
            }
        )
    }

    // Diálogo de Actualización disponible
    if (uiState.availableUpdate != null) {
        val update = uiState.availableUpdate!!
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isDownloadingUpdate) viewModel.dismissUpdateDialog()
            },
            containerColor = ColorSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = ColorPrimary
                    )
                    Spacer(modifier = Modifier.width(Fr33Spacing.small))
                    Text(
                        text = "Actualización disponible",
                        color = ColorTextPrimary,
                        style = Fr33Typography.titleMedium
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Nueva versión: ${update.versionName}",
                        style = Fr33Typography.titleSmall,
                        color = ColorPrimary
                    )
                    Spacer(modifier = Modifier.height(Fr33Spacing.small))

                    val releaseNotes = update.releaseNotes
                    if (!releaseNotes.isNullOrBlank()) {
                        Text(
                            text = "Notas de la versión:",
                            style = Fr33Typography.labelLarge,
                            color = ColorTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = releaseNotes,
                            style = Fr33Typography.bodySmall,
                            color = ColorTextTertiary,
                            maxLines = 8,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "Esta actualización contiene mejoras de rendimiento y corrección de errores.",
                            style = Fr33Typography.bodyMedium,
                            color = ColorTextSecondary
                        )
                    }

                    if (uiState.isDownloadingUpdate) {
                        Spacer(modifier = Modifier.height(Fr33Spacing.medium))
                        LinearProgressIndicator(
                            progress = { uiState.downloadProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = ColorPrimary,
                            trackColor = ColorDivider
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Descargando: ${(uiState.downloadProgress * 100).toInt()}%",
                            style = Fr33Typography.labelSmall,
                            color = ColorTextSecondary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::downloadAndInstallUpdate,
                    enabled = !uiState.isDownloadingUpdate,
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                ) {
                    Text(
                        text = if (uiState.isDownloadingUpdate) "Descargando..." else "Actualizar",
                        color = ColorTextPrimary
                    )
                }
            },
            dismissButton = {
                if (!uiState.isDownloadingUpdate) {
                    TextButton(onClick = viewModel::dismissUpdateDialog) {
                        Text(text = "Cancelar", color = ColorTextSecondary)
                    }
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = Fr33Typography.titleSmall,
        color = ColorPrimary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small)
    )
}

@Composable
private fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = ColorPrimary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(Fr33Spacing.medium))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = Fr33Typography.bodyLarge, color = ColorTextPrimary, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = Fr33Typography.bodySmall, color = ColorTextSecondary)
        }
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = ColorTextTertiary)
    }
}
