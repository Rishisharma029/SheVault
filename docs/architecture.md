# SheVault Native Safety Application Architecture

## 1. System Overview
SheVault is an Android-first native personal safety platform designed to provide resilient, low-latency protection under duress.

### Core Stack
- **Android Client**: Kotlin, Jetpack Compose, Material 3, Kotlin Coroutines, Room, Android Keystore, Fused Location Provider, Foreground Services, BiometricPrompt, Retrofit/OkHttp.
- **Backend API**: FastAPI, PostgreSQL, SQLAlchemy Async, Redis, WebSockets, Celery/Async Workers.
- **Trusted Circle Dashboard**: React (TypeScript), WebSockets.

---

## 2. Android Multi-Module Topology
```
shevault/
├── android/
│   ├── app/                      # Application entrypoint & SheVaultNavHost
│   ├── core/
│   │   ├── design/               # SheVaultTheme, Tokens, Buttons, Badges
│   │   ├── database/             # Room DB, Offline breadcrumbs & Contacts
│   │   ├── network/              # Retrofit & WebSocket Clients
│   │   ├── security/             # Keystore AES-256 GCM & Biometrics
│   │   ├── permissions/          # Android 14+ FGS & Location Permissions
│   │   └── common/               # Dispatchers, Result models
│   ├── feature/
│   │   ├── home/                 # Main Dashboard with SOS Pulsing Hero
│   │   ├── sos/                  # Countdown & Dispatch Screen
│   │   ├── incident/             # Live tracking & evidence
│   │   ├── trustedcircle/        # Guardian contacts manager
│   │   ├── disretmode/           # Decoy calculator interface
│   │   ├── saferoute/            # Safe journey monitoring
│   │   ├── history/              # Past incident logs
│   │   ├── settings/             # Hardware triggers & security prefs
│   │   └── onboarding/           # Permission grants & contacts setup
│   └── service/
│       ├── IncidentService       # FGS: location|microphone|dataSync
│       ├── LocationService       # FGS: location (FusedLocationProvider)
│       ├── SensorService         # FGS: dataSync (Accelerometer shake)
│       └── DeliveryService       # FGS: dataSync (Encrypted offline vault sync)
```

---

## 3. Foreground Service Requirements (Android 14+)
Modern Android strictly enforces explicit `foregroundServiceType` declarations:
- **IncidentService**: `location|microphone|dataSync` enables simultaneous GPS streaming, ambient audio evidence capture, and WebSocket transmission during SOS panic mode.
- **LocationService**: `location` enables continuous journey monitoring even when the screen is locked.
- **SensorService**: Detects violent shake or fall events to initiate background emergency escalation.
- **DeliveryService**: Uploads encrypted offline logs and evidence without data loss when network drops.
