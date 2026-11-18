package com.example.ime

import android.content.Context
import android.media.AudioManager

class VetroSoundManager(context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    fun playClick(enabled: Boolean, volume: Float) {
        if (!enabled || audioManager == null) return
        try {
            audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK, volume.coerceIn(0f, 1f))
        } catch (_: Exception) {}
    }
}
