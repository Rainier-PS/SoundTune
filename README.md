Got it! I'll keep the formatting exactly as it is and only change what’s necessary to explain the background issue. Here's your updated README:  

---

# **SoundTune**  
SoundTune is an Android application that automatically adjusts media volume based on ambient noise levels. Built with Jetpack Compose and Material Design 3, it provides a seamless experience for managing audio volume in different environments.  

## **Features**  
- **Automatic Volume Adjustment:** Dynamically adjusts media volume based on ambient noise levels  
- **Background Operation:** Continues monitoring and adjusting volume even when the app is in the background *(currently not fully functional, see Known Issues)*  
- **Customizable Settings:**  
  - Minimum and maximum volume limits  
  - Sensitivity adjustment for noise detection  
  - Headphone mode (limits max volume to 75% when headphones are connected)  
  - Option to prevent volume from reaching zero  
- **Theme Support:** Light, Dark, and System themes  
- **Battery Efficient:** Optimized background service with coroutines  

## **Known Issues**  
- **Background Operation Not Working as Expected:**  
  - The app is designed to adjust volume even when running in the background, but currently, it **stops working when the screen is off or the app is minimized**.  
  - The foreground service is active but does not consistently maintain microphone access for noise detection.  
  - Android’s background execution limits might be affecting the service.  

### **Potential Fixes Needed**  
- Improve the foreground service to keep it running across all device states.  
- Ensure microphone access remains persistent for continuous noise detection.  
- Handle battery optimizations and background restrictions on newer Android versions.  

Contributions to fix this issue are welcome!  

## **Requirements**  
- **Android 7.0 (API level 24) or higher**  
- **Permissions:**  
  - `RECORD_AUDIO` (for noise detection)  
  - `FOREGROUND_SERVICE` (for background operation)  
  - `MODIFY_AUDIO_SETTINGS` (for volume control)  
  - `POST_NOTIFICATIONS` (Android 13 and above)  

## **Installation**  
1. Clone the repository:  
   ```bash
   git clone https://github.com/YOUR_GITHUB_USERNAME/SoundTune.git
   cd SoundTune
   ```  
2. Open the project in **Android Studio**  
3. Build and run the app on your device  

## **Usage**  
1. Launch the app  
2. Grant the required permissions when prompted  
3. Adjust the volume range and sensitivity settings  
4. Toggle the service on/off using the main button  
5. Access additional settings through the **settings icon** in the top-right corner  

## **Technical Details**  
- **Built with:** Kotlin and Jetpack Compose  
- **Architecture:** MVVM  
- **Implements:** Material Design 3  
- **Data Persistence:** DataStore for settings storage  
- **Background Processing:** Foreground service for continuous noise monitoring *(needs improvement)*  
- **Efficiency:** Coroutines for optimized audio processing  

## **Contributing**  
Contributions are welcome!  
- If you can help fix the **background operation issue**, feel free to submit a **Pull Request**.  
- Any improvements, optimizations, or feature suggestions are also appreciated.  

## **License**  
This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.  
