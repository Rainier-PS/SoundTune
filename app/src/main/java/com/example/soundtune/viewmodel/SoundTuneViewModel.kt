package com.example.soundtune.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundtune.service.NoiseService
import com.example.soundtune.audio.AudioManagerHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SoundTuneViewModel(application: Application) : AndroidViewModel(application) {
    private val audioManagerHelper = AudioManagerHelper(application)
    
    private val _minVolume = MutableStateFlow(0)
    val minVolume: StateFlow<Int> = _minVolume.asStateFlow()
    
    private val _maxVolume = MutableStateFlow(15)
    val maxVolume: StateFlow<Int> = _maxVolume.asStateFlow()
    
    private val _sensitivity = MutableStateFlow(0.5f)
    val sensitivity: StateFlow<Float> = _sensitivity.asStateFlow()
    
    private val _allowZeroVolume = MutableStateFlow(false)
    val allowZeroVolume: StateFlow<Boolean> = _allowZeroVolume.asStateFlow()
    
    private val _isHeadphoneMode = MutableStateFlow(false)
    val isHeadphoneMode: StateFlow<Boolean> = _isHeadphoneMode.asStateFlow()
    
    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    fun updateMinVolume(value: Int) {
        _minVolume.value = value.coerceAtMost(_maxVolume.value)
    }

    fun updateMaxVolume(value: Int) {
        _maxVolume.value = value.coerceAtLeast(_minVolume.value)
    }

    fun updateSensitivity(value: Float) {
        _sensitivity.value = value
    }

    fun updateAllowZeroVolume(value: Boolean) {
        _allowZeroVolume.value = value
        if (!value && _minVolume.value == 0) {
            _minVolume.value = 1
        }
    }

    fun updateHeadphoneMode(value: Boolean) {
        _isHeadphoneMode.value = value
        if (value) {
            val headphoneMax = audioManagerHelper.getHeadphoneMaxVolume()
            if (_maxVolume.value > headphoneMax) {
                _maxVolume.value = headphoneMax
            }
        }
    }

    fun startService() {
        viewModelScope.launch {
            val intent = Intent(getApplication(), NoiseService::class.java).apply {
                action = "START"
                putExtra("minVolume", _minVolume.value)
                putExtra("maxVolume", _maxVolume.value)
                putExtra("sensitivity", _sensitivity.value)
                putExtra("allowZeroVolume", _allowZeroVolume.value)
                putExtra("isHeadphoneMode", _isHeadphoneMode.value)
            }
            getApplication<Application>().startForegroundService(intent)
            _isServiceRunning.value = true
        }
    }

    fun stopService() {
        viewModelScope.launch {
            val intent = Intent(getApplication(), NoiseService::class.java).apply {
                action = "STOP"
            }
            getApplication<Application>().startService(intent)
            _isServiceRunning.value = false
        }
    }

    fun getCurrentVolume(): Int {
        return audioManagerHelper.getCurrentVolume()
    }

    fun getMaxVolume(): Int {
        return if (_isHeadphoneMode.value) {
            audioManagerHelper.getHeadphoneMaxVolume()
        } else {
            audioManagerHelper.getMaxVolume()
        }
    }
} 