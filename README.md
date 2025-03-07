# SoundTune

SoundTune is an Android application that automatically adjusts media volume based on ambient noise levels. Built with Jetpack Compose and Material Design 3, it provides a seamless experience for managing audio volume in different environments.

## Features

- **Automatic Volume Adjustment**: Dynamically adjusts media volume based on ambient noise levels
- **Background Operation**: Continues monitoring and adjusting volume even when the app is in the background
- **Customizable Settings**:
  - Minimum and maximum volume limits
  - Sensitivity adjustment for noise detection
  - Headphone mode (limits max volume to 75% when headphones are connected)
  - Option to prevent volume from reaching zero
- **Theme Support**: Light, Dark, and System themes
- **Battery Efficient**: Optimized background service with coroutines

## Requirements

- Android 7.0 (API level 24) or higher
- Permissions:
  - RECORD_AUDIO (for noise detection)
  - FOREGROUND_SERVICE (for background operation)
  - MODIFY_AUDIO_SETTINGS (for volume control)
  - POST_NOTIFICATIONS (Android 13 and above)

## Installation

1. Clone the repository
2. Open the project in Android Studio
3. Build and run the app on your device

## Usage

1. Launch the app
2. Grant the required permissions when prompted
3. Adjust the volume range and sensitivity settings
4. Toggle the service on/off using the main button
5. Access additional settings through the settings icon in the top-right corner

## Technical Details

- Built with Kotlin and Jetpack Compose
- Uses MVVM architecture
- Implements Material Design 3
- Uses DataStore for settings persistence
- Background service for continuous noise monitoring
- Efficient audio processing with coroutines

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details. 