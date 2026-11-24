package com.fr33music.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fr33music.core.designsystem.theme.ColorBackground
import com.fr33music.core.designsystem.theme.ColorPrimary
import com.fr33music.core.designsystem.theme.ColorSurface
import com.fr33music.core.designsystem.theme.ColorTextPrimary
import com.fr33music.core.designsystem.theme.ColorTextSecondary
import com.fr33music.core.designsystem.theme.ColorTextTertiary
import com.fr33music.core.designsystem.theme.Fr33Radius
import com.fr33music.core.designsystem.theme.Fr33Spacing
import com.fr33music.core.designsystem.theme.Fr33Typography
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EqualizerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EqualizerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // TopBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.medium, vertical = Fr33Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = ColorTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(Fr33Spacing.small))
            Text(
                text = "Ecualizador",
                style = Fr33Typography.headlineMedium,
                color = ColorTextPrimary,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = state.isEnabled,
                onCheckedChange = viewModel::setEnabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ColorTextPrimary,
                    checkedTrackColor = ColorPrimary,
                    uncheckedThumbColor = ColorTextTertiary,
                    uncheckedTrackColor = ColorSurface
                )
            )
        }

        Spacer(modifier = Modifier.height(Fr33Spacing.medium))

        // Sección de Presets
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.large),
            colors = CardDefaults.cardColors(containerColor = ColorSurface),
            shape = RoundedCornerShape(Fr33Radius.large)
        ) {
            Column(modifier = Modifier.padding(Fr33Spacing.large)) {
                Text(
                    text = "Presets de Audio",
                    style = Fr33Typography.titleMedium,
                    color = ColorTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(Fr33Spacing.small))
                Text(
                    text = "Selecciona un perfil o ajusta las frecuencias manualmente",
                    style = Fr33Typography.bodySmall,
                    color = ColorTextSecondary
                )
                Spacer(modifier = Modifier.height(Fr33Spacing.medium))

                val presetList = listOf("Plano", "Rock", "Pop", "Refuerzo de Graves", "Vocal")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Fr33Spacing.small),
                    verticalArrangement = Arrangement.spacedBy(Fr33Spacing.small)
                ) {
                    presetList.forEach { presetName ->
                        val isSelected = state.selectedPreset == presetName
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (state.isEnabled) {
                                    viewModel.applyPreset(presetName)
                                }
                            },
                            enabled = state.isEnabled,
                            label = { Text(text = presetName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ColorPrimary,
                                selectedLabelColor = ColorTextPrimary,
                                containerColor = ColorBackground,
                                labelColor = ColorTextSecondary,
                                disabledContainerColor = ColorBackground.copy(alpha = 0.5f),
                                disabledLabelColor = ColorTextTertiary
                            ),
                            border = null,
                            shape = RoundedCornerShape(Fr33Radius.large)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Fr33Spacing.large))

        // Sección de las 5 Bandas (-10 dB a +10 dB)
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
                    .padding(Fr33Spacing.large)
            ) {
                Text(
                    text = "Bandas de Frecuencia (-10 dB a +10 dB)",
                    style = Fr33Typography.titleMedium,
                    color = ColorTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(Fr33Spacing.medium))

                state.bands.forEach { band ->
                    EqualizerBandRow(
                        name = band.name,
                        frequency = band.frequencyLabel,
                        levelDb = band.levelDb,
                        isEnabled = state.isEnabled,
                        onLevelChange = { newLevel ->
                            viewModel.setBandLevel(band.index, newLevel)
                        }
                    )
                    Spacer(modifier = Modifier.height(Fr33Spacing.small))
                }
            }
        }

        Spacer(modifier = Modifier.height(Fr33Spacing.extraLarge))
    }
}

@Composable
private fun EqualizerBandRow(
    name: String,
    frequency: String,
    levelDb: Int,
    isEnabled: Boolean,
    onLevelChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    style = Fr33Typography.bodyMedium,
                    color = if (isEnabled) ColorTextPrimary else ColorTextTertiary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "($frequency)",
                    style = Fr33Typography.bodySmall,
                    color = ColorTextSecondary
                )
            }
            Text(
                text = "${if (levelDb > 0) "+$levelDb" else "$levelDb"} dB",
                style = Fr33Typography.bodyMedium,
                color = if (isEnabled) ColorPrimary else ColorTextTertiary,
                fontWeight = FontWeight.Bold
            )
        }

        Slider(
            value = levelDb.toFloat(),
            onValueChange = { onLevelChange(it.roundToInt()) },
            valueRange = -10f..10f,
            steps = 19,
            enabled = isEnabled,
            colors = SliderDefaults.colors(
                thumbColor = ColorPrimary,
                activeTrackColor = ColorPrimary,
                inactiveTrackColor = ColorBackground,
                disabledThumbColor = ColorTextTertiary,
                disabledActiveTrackColor = ColorTextTertiary.copy(alpha = 0.5f),
                disabledInactiveTrackColor = ColorBackground
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
