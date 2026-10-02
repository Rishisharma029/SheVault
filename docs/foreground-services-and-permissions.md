# Foreground Services and Permissions Guide (Android 14+ / API 34+)

## Android 14 Enforcement Overview
In Android 14 (UPSIDE_DOWN_CAKE) and above, starting a foreground service without declaring its exact type in the AndroidManifest and passing that type to `startForeground()` will trigger a runtime `SecurityException`.

### Service Declarations in SheVault

| Service | Types Required | Purpose |
|---|---|---|
| `IncidentService` | `location`, `microphone`, `dataSync` | Transmits live GPS coordinates, streams ambient evidence audio, and synchronizes real-time SOS status. |
| `LocationService` | `location` | Background journey and route deviance detection for SafeRoute. |
| `SensorService` | `dataSync` | Detects physical shake/fall gestures without holding audio or GPS locks continuously. |
| `DeliveryService` | `dataSync` | Encrypted offline vault sync when transitioning from dead zones back to network. |

### Permissions Checklist
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />

<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```
