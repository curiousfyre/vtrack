# Autometer — Vehicle Fuel & Maintenance Tracker (Android + iOS)

v1.1.0 released on GitHub (Aug 23, 2026). Dual-platform app for tracking fuel economy and maintenance schedules. Display name is "Autometer"; package/bundle IDs remain `com.vtrack`.

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

### iOS (merged to master, also on ios-app branch)
Swift, SwiftUI, SwiftData. Source in `vtrack-ios/VTrack/` with Models, Views, ViewModels, Utilities directories. Xcode project at `vtrack-ios/VTrack.xcodeproj`. iOS development happens via Remote Control on Tim's Mac.

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
- GitHub: `tkraus13/vtrack` (currently **private** — needs to be made public for F-Droid)
- v1.1.0 release APK: 2.1MB, minified with R8
- Room DB version 3 (migrations: 1→2 nullable odometer, 2→3 nextDueOdometer)
- 49 unit tests (MpgCalculator, MaintenanceDueCalculator, FormatUtil, Routes)
- Gitleaks pre-commit hook for secret scanning

## Recent Changes (Aug 23, 2026)
- Added `nextDueOdometer` optional override on MaintenanceType (both platforms)
- Replaced hidden swipe-to-delete with visible delete button + confirmation dialog (both platforms)
- Merged iOS app into master branch
- Renamed display name from VTrack to Autometer (both platforms)
- Published v1.1.0 release on GitHub

## Next Session

### Priority: App Distribution
1. **Submit to F-Droid** — make repo public, add LICENSE (GPL/AGPL), add Fastlane metadata structure (`fastlane/metadata/android/`), submit merge request to fdroiddata repo
2. **Submit to TestFlight** — configure Xcode signing with Apple Developer account, archive iOS build, upload to App Store Connect, set up TestFlight beta testing group

### Bugs (confirmed on-device Aug 10)
1. **First fill-up baseline problem** — App uses `initialOdometer` as a fake "previous fill" baseline, producing wrong MPG. Fix: first fill-up should always be treated as a baseline entry (record data but never calculate MPG from it). MPG starts from fill-up #2. This is standard Fuelly/Fuelio behavior.
2. **"Unknown" initial odometer** — Users should be able to skip the initial odometer reading when adding a vehicle. Make `initialOdometer` nullable. Fixes the junk MPG problem when someone doesn't know their exact odometer.

### Roadmap (after distribution)
1. Add proper **launcher icons** (currently default mipmap placeholders)
2. DAO integration tests (Robolectric, in-memory Room DB)
3. Odometer validation: enforce monotonically increasing per vehicle
4. First-run experience (welcome flow -> add vehicle -> setup maintenance presets)

### V2 Features (future)
- L/100km and metric units
- Time-based maintenance intervals (schema column exists, logic needed)
- Cloud backup (Google Drive), Fuelio CSV import
- Home screen widgets, photo attachments on maintenance records
