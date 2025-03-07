package com.example.soundtune.audio

import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.STREAM_MUSIC
import kotlin.math.max
import kotlin.math.min

class AudioManagerHelper(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun getCurrentVolume(): Int {
        return audioManager.getStreamVolume(STREAM_MUSIC)
    }

    fun getMaxVolume(): Int {
        return audioManager.getStreamMaxVolume(STREAM_MUSIC)
    }

    fun setVolume(volume: Int, minVolume: Int, maxVolume: Int, allowZeroVolume: Boolean) {
        val adjustedVolume = when {
            !allowZeroVolume && volume == 0 -> 1
            else -> volume
        }
        
        val finalVolume = min(max(adjustedVolume, minVolume), maxVolume)
        audioManager.setStreamVolume(STREAM_MUSIC, finalVolume, 0)
    }

    fun isHeadphonesConnected(): Boolean {
        return audioManager.isWiredHeadsetOn || audioManager.isBluetoothA2dpOn
    }

    fun getHeadphoneMaxVolume(): Int {
        return (getMaxVolume() * 0.75).toInt()
    }
} 