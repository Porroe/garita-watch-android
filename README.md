# Garita Watch Android

Garita Watch is a native Android app for monitoring U.S. Customs and Border Protection border wait times. Users can browse ports of entry, filter by vehicle or pedestrian crossings, add ports to a local watchlist, and refresh wait-time data from the CBP feed.

## Features

- Live border wait-time data from the CBP XML endpoint.
- Watchlist of monitored ports persisted locally with Room.
- Dashboard focused on selected ports.
- Port search by port name or crossing name.
- Vehicle and pedestrian filters.
- Pull-to-refresh and manual refresh actions.
- Periodic background sync using WorkManager.
- Material 3 UI built with Jetpack Compose.

## Tech Stack

- Kotlin
- Android Gradle Plugin
- Jetpack Compose and Material 3
- Navigation Compose
- Hilt for dependency injection
- Retrofit, OkHttp, and TikXML for CBP XML networking
- Room for local persistence
- WorkManager for periodic sync
- JUnit for unit tests

## Project Structure

```text
app/src/main/java/com/porroe/garitawatch
├── data
│   ├── local          Room database, DAO, and watchlist entity
│   ├── remote         CBP API service and XML response models
│   └── repository     BorderRepository data coordinator
├── di                 Hilt modules for database and network objects
├── domain
│   ├── model          App-facing border wait-time models
│   └── util           Wait-time normalization helpers
├── ui
│   ├── dashboard      Watchlist dashboard screen and state
│   ├── search         Search/manage ports screen and state
│   └── theme          Compose theme configuration
├── worker             WorkManager sync worker
├── GaritaWatchApp.kt  Application class and periodic sync setup
└── MainActivity.kt    Compose entry point and navigation graph
```

## Requirements

- Android Studio with Android SDK support for compile SDK 35.
- JDK 11 or newer.
- Network access to download Gradle dependencies and query the CBP feed.
- An emulator or physical device running Android 7.0, API 24, or newer.

The project uses the checked-in Gradle wrapper, so a separate Gradle install is not required.

## Getting Started

1. Clone the repository.
2. Open the project in Android Studio.
3. Let Android Studio sync Gradle.
4. Select the `app` run configuration.
5. Run the app on an emulator or connected device.

You can also build from the command line:

```bash
./gradlew assembleDebug
```

Install the debug APK on a connected device:

```bash
./gradlew installDebug
```

## Common Commands

Run local unit tests:

```bash
./gradlew testDebugUnitTest
```

Run Android instrumentation tests on a connected emulator or device:

```bash
./gradlew connectedDebugAndroidTest
```

Build a debug APK:

```bash
./gradlew assembleDebug
```

Build a release APK:

```bash
./gradlew assembleRelease
```

## Data Flow

Garita Watch fetches border wait-time XML from:

```text
https://bwt.cbp.gov/xml/bwt.xml
```

The app uses Retrofit with TikXML to parse the response into remote DTOs. `WaitTimeNormalizer` converts the CBP response shape into app-domain models with normalized lane types and wait times in minutes.

`BorderRepository` owns the in-memory stream of the latest border data and exposes refresh state. It also coordinates the local Room watchlist through `MonitoredPortDao`.

The dashboard combines:

- latest border data from the repository,
- locally monitored port numbers from Room,
- current refresh state.

Only ports saved in the watchlist appear on the dashboard. The search screen observes the same source data, applies the text query and lane-type filters, and lets users toggle a port in or out of the watchlist.

## Background Sync

`GaritaWatchApp` schedules a unique periodic WorkManager request when the application starts. The worker:

- requires network connectivity,
- runs approximately every 15 minutes,
- calls `BorderRepository.refreshData()`,
- retries if the sync throws an exception.

Hilt is configured as the WorkManager worker factory in the application class, and the default WorkManager initializer is disabled in `AndroidManifest.xml`.

## Persistence

The local database is named:

```text
garitawatch_db
```

It currently stores one table, `monitored_ports`, keyed by `portNumber`. The table stores the minimum metadata needed to preserve the user's watchlist:

- port number,
- port name,
- crossing name,
- border.

Live wait-time values are not persisted; they are refreshed from the CBP feed.

## Testing

Current unit coverage focuses on:

- wait-time parsing and normalization behavior,
- CBP XML parsing with TikXML.

Relevant test files:

- `app/src/test/java/com/porroe/garitawatch/domain/util/WaitTimeNormalizerTest.kt`
- `app/src/test/java/com/porroe/garitawatch/data/remote/CbpXmlParsingTest.kt`

Before opening a pull request or shipping a change, run:

```bash
./gradlew testDebugUnitTest
```

For UI or device behavior changes, also run:

```bash
./gradlew connectedDebugAndroidTest
```

## Development Notes

- Dependency versions are managed in `gradle/libs.versions.toml`.
- The app targets SDK 35 and supports API 24+.
- XML parsing is intentionally configured with `exceptionOnUnreadXml(false)` so the app can tolerate CBP feed fields that are not modeled yet.
- Wait-time strings such as `30 min`, `1 hr 15 min`, raw numeric values, empty strings, and `N/A` are normalized to integer minutes.
- Background sync uses `ExistingPeriodicWorkPolicy.KEEP`, so the app does not enqueue duplicate periodic sync jobs on repeated starts.

## Known Limitations

- Repository refresh errors are currently printed rather than surfaced as user-visible UI state.
- The dashboard shows passenger and pedestrian lanes; commercial lane data is parsed and available in the model but not currently rendered on the dashboard.
- Search and dashboard data are in-memory after refresh; only the selected watchlist is stored locally.

