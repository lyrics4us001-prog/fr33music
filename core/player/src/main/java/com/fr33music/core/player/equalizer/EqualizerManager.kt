package com.fr33music.core.player.equalizer

import android.media.audiofx.Equalizer
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class EqualizerBand(
    val index: Int,
    val name: String,
    val frequencyLabel: String,
    val levelDb: Int // Rango de -10 a +10 dB
)

data class EqualizerState(
    val isEnabled: Boolean = true,
    val bands: List<EqualizerBand> = defaultBands(),
    val selectedPreset: String? = "Plano"
) {
    companion object {
        fun defaultBands(): List<EqualizerBand> = listOf(
            EqualizerBand(0, "Graves", "60 Hz", 0),
            EqualizerBand(1, "Medios-Graves", "230 Hz", 0),
            EqualizerBand(2, "Medios", "910 Hz", 0),
            EqualizerBand(3, "Medios-Agudos", "3.6 kHz", 0),
            EqualizerBand(4, "Agudos", "14 kHz", 0)
        )
    }
}

@Singleton
class EqualizerManager @Inject constructor(
    private val exoPlayer: ExoPlayer
) {
    private var equalizer: Equalizer? = null
    private var currentSessionId: Int = C.AUDIO_SESSION_ID_UNSET

    private val _state = MutableStateFlow(EqualizerState())
    val state: StateFlow<EqualizerState> = _state.asStateFlow()

    val presets: Map<String, List<Int>> = linkedMapOf(
        "Plano" to listOf(0, 0, 0, 0, 0),
        "Rock" to listOf(4, 2, -1, 2, 4),
        "Pop" to listOf(-1, 2, 4, 2, -1),
        "Refuerzo de Graves" to listOf(6, 4, 1, 0, 0),
        "Vocal" to listOf(-2, 1, 4, 3, 1)
    )

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY || playbackState == Player.STATE_BUFFERING) {
                    val sId = exoPlayer.audioSessionId
                    if (sId != C.AUDIO_SESSION_ID_UNSET && sId > 0 && sId != currentSessionId) {
                        attachToAudioSession(sId)
                    }
                }
            }
        })
        val initialSession = exoPlayer.audioSessionId
        if (initialSession != C.AUDIO_SESSION_ID_UNSET && initialSession > 0) {
            attachToAudioSession(initialSession)
        }
    }

    fun checkSession() {
        val sId = exoPlayer.audioSessionId
        if (sId != C.AUDIO_SESSION_ID_UNSET && sId > 0 && sId != currentSessionId) {
            attachToAudioSession(sId)
        } else if (equalizer == null) {
            attachToAudioSession(if (sId > 0) sId else 0)
        }
    }

    @Synchronized
    fun attachToAudioSession(sessionId: Int) {
        if (sessionId == currentSessionId && equalizer != null) return
        if (sessionId < 0) return

        try {
            equalizer?.release()
        } catch (_: Exception) {}

        try {
            val eq = Equalizer(0, sessionId)
            eq.enabled = _state.value.isEnabled
            equalizer = eq
            currentSessionId = sessionId

            // Aplicar niveles actuales de las 5 bandas
            _state.value.bands.forEach { band ->
                applyBandLevelToHardware(band.index, band.levelDb)
            }
            Log.i("EqualizerManager", "Equalizer vinculado con éxito a sesión $sessionId (${eq.numberOfBands} bandas soportadas)")
        } catch (e: Exception) {
            Log.w("EqualizerManager", "No se pudo instanciar Equalizer para audioSessionId $sessionId: ${e.message}")
        }
    }

    fun setEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(isEnabled = enabled)
        try {
            equalizer?.enabled = enabled
        } catch (e: Exception) {
            Log.w("EqualizerManager", "Error al activar/desactivar ecualizador: ${e.message}")
        }
    }

    fun setBandLevel(bandIndex: Int, levelDb: Int) {
        val clampedLevel = levelDb.coerceIn(-10, 10)
        val currentBands = _state.value.bands.toMutableList()
        if (bandIndex in currentBands.indices) {
            currentBands[bandIndex] = currentBands[bandIndex].copy(levelDb = clampedLevel)
            _state.value = _state.value.copy(bands = currentBands, selectedPreset = null)
            applyBandLevelToHardware(bandIndex, clampedLevel)
        }
    }

    fun applyPreset(presetName: String) {
        val levels = presets[presetName] ?: return
        val currentBands = _state.value.bands.toMutableList()
        for (i in levels.indices) {
            if (i in currentBands.indices) {
                currentBands[i] = currentBands[i].copy(levelDb = levels[i])
                applyBandLevelToHardware(i, levels[i])
            }
        }
        _state.value = _state.value.copy(bands = currentBands, selectedPreset = presetName)
    }

    private fun applyBandLevelToHardware(bandIndex: Int, levelDb: Int) {
        try {
            val eq = equalizer ?: return
            val minRange = eq.bandLevelRange?.getOrNull(0) ?: -1000
            val maxRange = eq.bandLevelRange?.getOrNull(1) ?: 1000
            // 1 dB = 100 millibels (mB)
            val milliBels = (levelDb * 100).coerceIn(minRange.toInt(), maxRange.toInt()).toShort()
            if (bandIndex < eq.numberOfBands) {
                eq.setBandLevel(bandIndex.toShort(), milliBels)
            }
        } catch (e: Exception) {
            Log.w("EqualizerManager", "Error al aplicar nivel de banda $bandIndex: ${e.message}")
        }
    }

    fun release() {
        try {
            equalizer?.release()
            equalizer = null
            currentSessionId = C.AUDIO_SESSION_ID_UNSET
        } catch (_: Exception) {}
    }
}
