package com.example.soundtune.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.soundtune.R
import com.example.soundtune.audio.AudioManagerHelper
import kotlinx.coroutines.*
import kotlin.math.log10
import kotlin.math.pow

class NoiseService : Service() {
    private var serviceJob: Job? = null
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private lateinit var audioManagerHelper: AudioManagerHelper
    
    private var minVolume = 0
    private var maxVolume = 15
    private var sensitivity = 0.5f
    private var allowZeroVolume = false
    private var isHeadphoneMode = false

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "NoiseServiceChannel"
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE = 4096
        private const val TAG = "NoiseService"
    }

    override fun onCreate() {
        super.onCreate()
        try {
            createNotificationChannel()
            audioManagerHelper = AudioManagerHelper(this)
            startForeground(NOTIFICATION_ID, createNotification())
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            when (intent?.action) {
                "START" -> {
                    minVolume = intent.getIntExtra("minVolume", 0)
                    maxVolume = intent.getIntExtra("maxVolume", 15)
                    sensitivity = intent.getFloatExtra("sensitivity", 0.5f)
                    allowZeroVolume = intent.getBooleanExtra("allowZeroVolume", false)
                    isHeadphoneMode = intent.getBooleanExtra("isHeadphoneMode", false)
                    startNoiseDetection()
                }
                "STOP" -> {
                    stopNoiseDetection()
                    stopSelf()
                }
                else -> {
                    Log.e(TAG, "Unknown action: ${intent?.action}")
                    stopSelf()
                    return START_NOT_STICKY
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onStartCommand", e)
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun startNoiseDetection() {
        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val buffer = ShortArray(BUFFER_SIZE)
                val minBufferSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT
                )
                
                if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(TAG, "Invalid buffer size")
                    stopSelf()
                    return@launch
                }

                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    maxOf(BUFFER_SIZE, minBufferSize)
                )
                
                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "AudioRecord initialization failed")
                    stopSelf()
                    return@launch
                }
                
                audioRecord?.startRecording()
                isRecording = true

                while (isRecording) {
                    val readResult = audioRecord?.read(buffer, 0, BUFFER_SIZE) ?: 0
                    if (readResult > 0) {
                        val noiseLevel = calculateNoiseLevel(buffer)
                        adjustVolume(noiseLevel)
                    } else if (readResult == AudioRecord.ERROR_INVALID_OPERATION) {
                        Log.e(TAG, "Error reading audio data")
                        break
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in noise detection", e)
                stopSelf()
            }
        }
    }

    private fun calculateNoiseLevel(buffer: ShortArray): Double {
        try {
            var sum = 0.0
            for (sample in buffer) {
                sum += sample.toDouble().pow(2)
            }
            val rms = (sum / buffer.size).pow(0.5)
            return 20 * log10(rms / 32768.0)
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating noise level", e)
            return 0.0
        }
    }

    private fun adjustVolume(noiseLevel: Double) {
        try {
            val maxVol = if (isHeadphoneMode) {
                audioManagerHelper.getHeadphoneMaxVolume()
            } else {
                maxVolume
            }

            val normalizedNoise = (noiseLevel + 60) / 60 // Normalize to 0-1 range
            val targetVolume = (normalizedNoise * (maxVol - minVolume) + minVolume).toInt()
            
            audioManagerHelper.setVolume(targetVolume, minVolume, maxVol, allowZeroVolume)
        } catch (e: Exception) {
            Log.e(TAG, "Error adjusting volume", e)
        }
    }

    private fun stopNoiseDetection() {
        isRecording = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            serviceJob?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping noise detection", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Noise Detection Service",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Monitors ambient noise and adjusts volume"
                }
                
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating notification channel", e)
            }
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SoundTune")
            .setContentText("Monitoring ambient noise")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopNoiseDetection()
    }
} 