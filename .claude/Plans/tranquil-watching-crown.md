# VTrack — Android Vehicle Fuel & Maintenance Tracker

## Context

Tim currently uses Fuelio to track fuel economy in his truck. He wants a custom Android app that combines fuel economy tracking with maintenance scheduling. The key insight: odometer readings from fuel fill-ups double as the mileage input that drives maintenance reminders — no separate mileage entry needed.

## Tech Stack

| Layer | Choice |
|-------|--------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Database | Room (SQLite) |
| Architecture | MVVM + Repository |
| DI | Hilt |
| Navigation | Compose Navigation (single activity) |
| Background | WorkManager (daily maintenance checks) |
| Charts | Vico (Compose-native) |
| Settings | DataStore Preferences |
| Build | Gradle Kotlin DSL + Version Catalog |
| Min SDK | API 26 (Android 8.0) |

## Data Model

### Vehicle
`id, name, make, model, year, initialOdometer, isActive, createdAt`

### FuelEntry
`id, vehicleId (FK CASCADE), date, odometer, gallons, pricePerGallon, totalCost, isPartialFill, notes, createdAt`
Indexes: `(vehicleId)`, `(vehicleId, odometer)`, `(vehicleId, date)`

### MaintenanceType
`id, vehicleId (FK CASCADE), name, intervalMiles, intervalMonths (nullable), description, isActive, createdAt`
Index: `(vehicleId)`

### MaintenanceRecord
`id, maintenanceTypeId (FK CASCADE), vehicleId (FK CASCADE), date, odometer, cost, notes, createdAt`
Indexes: `(maintenanceTypeId)`, `(vehicleId)`, `(vehicleId, maintenanceTypeId, odometer)`

**Key decisions:**
- CASCADE deletes on all FKs — deleting a vehicle removes everything
- `totalCost` stored (not computed) to avoid floating-point display drift
- `isActive` soft-delete on MaintenanceType to preserve history
- MPG calculated on read (not stored) to avoid stale data when entries are edited/deleted

## Package Structure

```
com.example.vtrack/
  VTrackApplication.kt
  MainActivity.kt
  data/db/ (AppDatabase, Converters, dao/)
  data/repository/ (VehicleRepository, FuelRepository, MaintenanceRepository)
  data/model/ (entities + FuelEntryWithMpg)
  di/ (DatabaseModule, RepositoryModule)
  feature/
    dashboard/ (Screen + ViewModel)
    fuel/list/ + fuel/entry/
    maintenance/types/ + maintenance/history/
    vehicle/ (list + form)
    stats/
    settings/
  navigation/ (AppNavHost, Routes, BottomNavBar)
  ui/theme/ + ui/components/
  util/ (MpgCalculator, MaintenanceDueCalculator, formatters)
  worker/ (MaintenanceCheckWorker, NotificationHelper)
```

## Screens

**Bottom nav (3 tabs):** Dashboard | Fuel | Maintenance

| Screen | Purpose |
|--------|---------|
| Dashboard | Vehicle summary, last fill-up, upcoming maintenance, lifetime averages |
| Fuel List | Chronological fill-ups with MPG, swipe-to-delete, FAB to add |
| Fuel Entry Form | Date, odometer, gallons, price, total, partial fill toggle, notes |
| Maintenance Types | Defined items with status badges (OK/Due Soon/Overdue), FAB to add |
| Maintenance Type Form | Name, interval miles, optional months, preset templates |
| Maintenance History | Service records per type, FAB to log service |
| Log Maintenance | Type, date, odometer (defaults to latest), cost, notes |
| Vehicle List | Vehicle cards, FAB to add |
| Vehicle Form | Name, make, model, year, initial odometer |
| Stats | MPG line chart, cost/month bar chart, summary stats, date range filter |
| Settings | Currency symbol, notification prefs, CSV export, about |

## Key Business Logic

### MPG Calculation
- Full fill to full fill: `(currentOdometer - previousFullFillOdometer) / totalGallonsBetween`
- Partial fills: accumulate gallons, skip MPG display until next full fill
- Calculated on read via DAO query (not stored)

### Maintenance Due Detection
- `milesSinceService = currentOdometer - lastServiceOdometer`
- `percentUsed = milesSinceService / intervalMiles`
- **OK:** < 90% | **Due Soon:** 90-99% | **Overdue:** 100%+
- `currentOdometer` = MAX(odometer) from fuel_entries, falling back to vehicle.initialOdometer

### Notifications
- WorkManager PeriodicWorkRequest, 24h interval, battery-not-low constraint
- Checks all active maintenance types across all vehicles
- Fires notifications for DUE_SOON and OVERDUE items
- Notification IDs: `vehicleId * 10000 + maintenanceTypeId` (stable, prevents duplicates)
- Android 13+ POST_NOTIFICATIONS runtime permission requested after first maintenance type setup

## V1 Scope (MVP)

**In:** Multi-vehicle CRUD, fuel logging with partial fills, MPG calculation, maintenance types with mileage intervals, maintenance records, due/overdue notifications, dashboard, stats with MPG chart, CSV export, Material 3 dynamic theming, fully offline

**Deferred to v2:** Metric units (L/100km), time-based maintenance intervals (schema ready), cloud backup, fuel type tracking, trip logs, photo attachments, home screen widgets, Fuelio CSV import

## Implementation Order

1. **Foundation** — Project setup, dependencies, theme, Room DB, DI, nav skeleton
2. **Vehicle CRUD** — List + form (needed before anything else)
3. **Fuel Tracking** — Entry form with validation, list with MPG display, partial fill handling
4. **Maintenance** — Type CRUD with presets, record logging, due calculation, status badges
5. **Dashboard** — Aggregates fuel + maintenance data
6. **Stats** — Vico charts for MPG and cost trends
7. **Notifications** — WorkManager + notification channel + permission flow
8. **Polish** — CSV export, settings, empty states, first-run experience, unit tests for calculators

## Pitfalls to Watch

- **Odometer validation:** Must be monotonically increasing per vehicle by date. Validate on entry and on edit.
- **Deleting mid-sequence fill-ups:** MPG for neighboring entries changes. Calculating on read (not storing) handles this automatically.
- **Room migrations:** Export schema from day one. Never use destructiveMigration in production.
- **WorkManager timing:** Not exact — batches for battery. Fine for daily maintenance checks.

## Verification

- Unit tests for `MpgCalculator` and `MaintenanceDueCalculator` (pure functions)
- Room DAO integration tests with in-memory database
- Manual testing: add vehicle, log fill-ups, verify MPG, set up maintenance, hit threshold, verify notification fires
