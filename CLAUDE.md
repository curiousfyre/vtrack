# VTrack — Vehicle Fuel & Maintenance Tracker (Android + iOS)

v1.0.0 released on GitHub (Aug 10, 2026). Dual-platform app for tracking fuel economy and maintenance schedules.

## Quick Reference

```bash
# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test

# Build signed release APK
./gradlew assembleRelease

# Install on connected device/emulator
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Platforms

### Android (master branch)
Kotlin, Jetpack Compose, Material 3, Room 2.6.1, Hilt 2.51.1, Compose Navigation, WorkManager, DataStore, Gradle Kotlin DSL (version catalog)

- **Min SDK:** 26 | **Target SDK:** 35 | **Compile SDK:** 35
- **Package:** `com.vtrack`
- **Keystore:** `vtrack-release.jks` in project root (gitignored), passwords in `local.properties`

### iOS (ios-app branch)
Swift, SwiftUI, SwiftData. Source in `vtrack-ios/VTrack/` with Models, Views, ViewModels, Utilities directories. Xcode project at `vtrack-ios/VTrack.xcodeproj`.

**Important:** Features and bug fixes should be applied to both platforms. When making Android changes, flag whether the iOS app needs the same update.

## Architecture
MVVM + StateFlow. 4 Room entities, 4 DAOs, 3 repositories, 11 screens + 11 ViewModels.

### Key Paths
- `app/src/main/java/com/vtrack/` — all source code
- `data/entity/` — Room entities: Vehicle, FuelEntry, MaintenanceType, MaintenanceRecord
- `data/dao/` — DAOs with CRUD + specialized queries (MPG calc, maintenance due)
- `data/repository/` — Vehicle, Fuel, Maintenance repositories
- `ui/screens/` — 11 Compose screens
- `ui/viewmodels/` — 11 ViewModels
- `util/MpgCalculator.kt` — MPG with partial fill accumulation
- `util/MaintenanceDueCalculator.kt` — OK/DUE_SOON/OVERDUE urgency levels
- `worker/MaintenanceCheckWorker.kt` — WorkManager daily notification check

### Navigation
3-tab bottom bar (Dashboard/Fuel/Maintenance) + overflow menu (Vehicles, Settings, Statistics)

## Release
- GitHub: `tkraus13/vtrack` (currently **private** — needs to be made public)
- Release APK: 2.1MB, minified with R8
- 49 unit tests (MpgCalculator, MaintenanceDueCalculator, FormatUtil, Routes)
- Gitleaks pre-commit hook for secret scanning

## Next Session

### Bugs (confirmed on-device Aug 10)
1. **First fill-up baseline problem** — App uses `initialOdometer` as a fake "previous fill" baseline, producing wrong MPG. Fix: first fill-up should always be treated as a baseline entry (record data but never calculate MPG from it). MPG starts from fill-up #2. This is standard Fuelly/Fuelio behavior.
2. **"Unknown" initial odometer** — Users should be able to skip the initial odometer reading when adding a vehicle. Make `initialOdometer` nullable. Fixes the junk MPG problem when someone doesn't know their exact odometer.
3. ~~**No back navigation from overflow screens**~~ — Fixed in commit `46d1085`.

### Roadmap (priority order)
1. Make GitHub repo **public**
2. Submit to **F-Droid** — add LICENSE, Fastlane metadata, submit fdroiddata MR
3. Add proper **launcher icons** (currently default mipmap placeholders)
4. DAO integration tests (Robolectric, in-memory Room DB)
5. Odometer validation: enforce monotonically increasing per vehicle
6. First-run experience (welcome flow -> add vehicle -> setup maintenance presets)

### V2 Features (future)
- L/100km and metric units
- Time-based maintenance intervals (schema column exists, logic needed)
- Cloud backup (Google Drive), Fuelio CSV import
- Home screen widgets, photo attachments on maintenance records
