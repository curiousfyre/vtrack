# Autometer — Vehicle Fuel & Maintenance Tracker (Android + iOS)

v1.2.0 released on GitHub (Sep 19, 2026). Dual-platform app for tracking fuel economy and maintenance schedules. Display name is "Autometer"; package/bundle IDs remain `com.vtrack`.

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
- GitHub: `curiousfyre/vtrack`
- Latest: v1.2.0 (versionCode 5, Sep 19, 2026)
- Room DB version 3 (migrations: 1→2 nullable odometer, 2→3 nextDueOdometer)
- 49 unit tests (MpgCalculator, MaintenanceDueCalculator, FormatUtil, Routes)
- Gitleaks pre-commit hook for secret scanning
- F-Droid: auto-updates via `AutoUpdateMode`/`UpdateCheckMode` on tags — no fdroiddata MR needed for routine releases

## Recent Changes (Sep 19, 2026)
- Bidirectional fuel price entry: user can enter either price per gallon or total cost, other auto-calculates (Android only — iOS needs same update)
- Price per gallon now displayed on each fill-up card in the fuel list (Android only — iOS needs same update)
- PriceInputMode enum tracks last-edited field to prevent circular auto-calculation
- Validation relaxed: save requires either price per gallon or total cost (not both)
- Added `formatPricePerGallon` to FormatUtil
- Released v1.2.0 (versionCode 5) on GitHub with signed APK

### Earlier Changes
- v1.1.2: Fix F-Droid reviewer findings
- v1.1.1: Reproducible builds for F-Droid (disabled baseline profiles, VCS info, fixed R8 nondeterminism, single Gradle worker)
- v1.1.0: nextDueOdometer override, visible delete buttons, iOS merge, Autometer rename, F-Droid submission, GPL-3.0 license

## Next Session

### Priority: iOS Parity
- Bidirectional fuel price entry (enter price per gallon or total cost) — needs same ViewModel logic in Swift
- Price per gallon display on fuel list cards — needs SwiftUI update
- Work via Remote Control on Tim's Mac

### App Distribution
1. **F-Droid** — Live. Auto-updates via tags. Release process: bump version in build.gradle.kts, tag, push, create GitHub release with signed APK. No fdroiddata MR needed unless build config changes (e.g. adding product flavors).
2. **Apple App Store** — contacted Apple Developer support (Aug 26, 2026) to resolve payment block on $99/year enrollment. Three different cards from three banks all rejected — account-level issue on Apple's side. Once resolved: configure Xcode signing, archive iOS build, upload to App Store Connect, set up TestFlight beta testing group.
3. **Google Play Store** — $25 one-time developer account needed. Create listing (Auto & Vehicles category), generate AAB with `./gradlew bundleRelease`, enroll in Play App Signing, submit for review.

### Next: Monetization (v1.3.0)
Free tier (ads, 1 vehicle) + one-time premium purchase (no ads, unlimited vehicles). F-Droid stays fully free and unlimited.

**Android:** Add `foss`/`play` Gradle product flavors. `BillingManager` + `AdManager` interfaces in main source, no-op stubs in `foss/`, Google Play Billing + AdMob in `play/`. Hilt DI per flavor. Gate vehicle add in `VehicleFormViewModel`, banner ad on Dashboard. F-Droid metadata changes from `gradle: - yes` to `gradle: - foss`. Full plan in memory: `project_store_distribution.md`.

**iOS:** StoreKit 2 for one-time purchase (native Swift, no third-party SDK). AdMob for ads. Same gating logic. Work via Remote Control on Mac.

### Roadmap
1. DAO integration tests (Robolectric, in-memory Room DB)
3. Odometer validation: enforce monotonically increasing per vehicle
4. First-run experience (welcome flow -> add vehicle -> setup maintenance presets)

### V2 Features (future)
- L/100km and metric units
- Time-based maintenance intervals (schema column exists, logic needed)
- Cloud backup (Google Drive), Fuelio CSV import
- Home screen widgets, photo attachments on maintenance records
