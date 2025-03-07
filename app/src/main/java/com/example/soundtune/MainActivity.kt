package com.example.soundtune

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.soundtune.ui.theme.SoundTuneTheme
import com.example.soundtune.viewmodel.SoundTuneViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val viewModel: SoundTuneViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            viewModel.startService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SoundTuneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel, requestPermissionLauncher)
                }
            }
        }
    }

    companion object {
        fun requestPermissions(activity: MainActivity) {
            val permissions = mutableListOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
            )
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            
            activity.requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: SoundTuneViewModel,
    requestPermissionLauncher: ActivityResultLauncher<Array<String>>
) {
    val context = LocalContext.current
    val minVolume by viewModel.minVolume.collectAsState()
    val maxVolume by viewModel.maxVolume.collectAsState()
    val sensitivity by viewModel.sensitivity.collectAsState()
    val allowZeroVolume by viewModel.allowZeroVolume.collectAsState()
    val isHeadphoneMode by viewModel.isHeadphoneMode.collectAsState()
    val isServiceRunning by viewModel.isServiceRunning.collectAsState()
    var currentVolume by remember { mutableStateOf(viewModel.getCurrentVolume()) }
    val maxSystemVolume = viewModel.getMaxVolume()

    // Update current volume periodically
    LaunchedEffect(Unit) {
        while (true) {
            currentVolume = viewModel.getCurrentVolume()
            delay(500) // Update every 500ms
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SoundTune") },
                actions = {
                    IconButton(
                        onClick = {
                            context.startActivity(Intent(context, SettingsActivity::class.java))
                        }
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Volume Range Section
            Text(
                text = "Volume Range",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Min Volume")
                    Slider(
                        value = minVolume.toFloat(),
                        onValueChange = { viewModel.updateMinVolume(it.toInt()) },
                        valueRange = 0f..maxSystemVolume.toFloat(),
                        steps = maxSystemVolume
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Max Volume")
                    Slider(
                        value = maxVolume.toFloat(),
                        onValueChange = { viewModel.updateMaxVolume(it.toInt()) },
                        valueRange = 0f..maxSystemVolume.toFloat(),
                        steps = maxSystemVolume
                    )
                }
            }

            // Sensitivity Section
            Text(
                text = "Sensitivity",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = sensitivity,
                onValueChange = { viewModel.updateSensitivity(it) },
                valueRange = 0f..1f
            )

            // Toggles Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Headphone Mode",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Max volume: ${maxSystemVolume}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = isHeadphoneMode,
                        onCheckedChange = { viewModel.updateHeadphoneMode(it) }
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Allow Zero Volume",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Min volume: ${if (allowZeroVolume) "0" else "1"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = allowZeroVolume,
                        onCheckedChange = { viewModel.updateAllowZeroVolume(it) }
                    )
                }
            }

            // Service Control
            Button(
                onClick = {
                    if (isServiceRunning) {
                        viewModel.stopService()
                    } else {
                        MainActivity.requestPermissions(context as MainActivity)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isServiceRunning) "Stop Service" else "Start Service")
            }

            // Current Volume Display
            Text(
                text = "Current Volume: $currentVolume",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}