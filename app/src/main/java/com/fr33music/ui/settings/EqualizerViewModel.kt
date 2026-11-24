package com.fr33music.ui.settings

import androidx.lifecycle.ViewModel
import com.fr33music.core.player.equalizer.EqualizerManager
import com.fr33music.core.player.equalizer.EqualizerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val equalizerManager: EqualizerManager
) : ViewModel() {

    val state: StateFlow<EqualizerState> = equalizerManager.state

    init {
        equalizerManager.checkSession()
    }

    fun setEnabled(enabled: Boolean) {
        equalizerManager.setEnabled(enabled)
    }

    fun setBandLevel(bandIndex: Int, levelDb: Int) {
        equalizerManager.setBandLevel(bandIndex, levelDb)
    }

    fun applyPreset(presetName: String) {
        equalizerManager.applyPreset(presetName)
    }
}
