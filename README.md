# VTrack

Android vehicle fuel economy and maintenance tracking app. Built with Kotlin, Jetpack Compose, and Material 3.

## Features

- Multi-vehicle management
- Fuel fill-up logging with MPG calculation (handles partial fills)
- Maintenance type scheduling with mileage intervals
- Due/overdue status detection and notifications
- Dashboard with vehicle summary and upcoming maintenance
- Stats with MPG chart and monthly spending
- CSV data export
- Fully offline (Room/SQLite)

## Tech Stack

| Layer | Choice |
|-------|--------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Database | Room (SQLite) |
| Architecture | MVVM + Repository |
| DI | Hilt |
| Navigation | Compose Navigation |
| Background | WorkManager |
| Build | Gradle Kotlin DSL + Version Catalog |
| Min SDK | API 26 (Android 8.0) |

## Building

Requires Android SDK with platform 35 and build tools 34.

```bash
export ANDROID_HOME=~/Android/Sdk
./gradlew assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## Running in Emulator

### Setup (one-time)

```bash
export ANDROID_HOME=~/Android/Sdk

# Install system image (if not already present)
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "system-images;android-35;google_apis;x86_64"

# Create AVD
export ANDROID_AVD_HOME=~/.config/.android/avd
echo "no" | $ANDROID_HOME/cmdline-tools/latest/bin/avdmanager create avd \
  -n vtrack_test \
  -k "system-images;android-35;google_apis;x86_64" \
  -d pixel_6
```

### Launch Emulator

```bash
export ANDROID_HOME=~/Android/Sdk
export ANDROID_AVD_HOME=~/.config/.android/avd

# With GUI window (needs display + GPU)
$ANDROID_HOME/emulator/emulator -avd vtrack_test -gpu host -memory 2048 &

# Headless (no display required)
$ANDROID_HOME/emulator/emulator -avd vtrack_test -no-window -no-audio \
  -gpu swiftshader_indirect -memory 2048 &
```

### Install and Run

```bash
# Wait for boot
adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done'

# Install
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch
adb shell am start -n com.vtrack/.MainActivity
```

### Demo with scrcpy

Mirror the emulator to an interactive window:

```bash
scrcpy -s emulator-5554
scrcpy -s emulator-5554 --window-title "VTrack Demo"    # custom title
scrcpy -s emulator-5554 --record vtrack-demo.mp4         # record session
```

### Emulator Tips

- Enable on-screen keyboard: `adb shell settings put secure show_ime_with_hard_keyboard 1`
- Kill emulator: `adb emu kill`
- If emulator shows ANR warnings, use `-gpu host` instead of `-gpu swiftshader_indirect`
