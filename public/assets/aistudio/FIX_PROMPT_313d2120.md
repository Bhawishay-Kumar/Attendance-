# Fix prompt: app does not open after applying the 19 changes

The 19 changes from APPLY_PROMPT.md were applied and the build and unit tests pass, but the installed app does not open. Passing tests do not cover app launch, so find the real cause first, then fix it, then tidy the project, then set up APK archiving. Do the parts in this order and build after each part.

## Part 1. Find and fix the launch failure

1. Uninstall any old copy first (`adb uninstall com.aistudio.iihtattendance.vkmp`), install the fresh debug build, and start it:
   `adb logcat -c`, `adb shell am start -n com.aistudio.iihtattendance.vkmp/com.example.MainActivity`, then `adb logcat -d | grep -A40 "FATAL EXCEPTION"`.
   If no emulator or device is available, trace the launch path by reading the code: `MainActivity` → `AttendanceViewModel` initialisation → `AppDatabase` → `AttendanceRepository.initializeDefaultsIfNeeded()` → the first screen. Say clearly that you could not run it.
2. Note whether it also crashes after a clean install. If it only crashes over old data, the cause is old saved data and needs handling in code.
3. If you have a stack trace, fix that exact cause. If not, check these suspects in order. They are the places the 19 changes touched.
   - **Property order in `ui/viewmodel/AttendanceViewModel.kt`.** `upcomingWeightsFlow`, `overallStats`, `allSlotsForCurrentVersion`, `profile`, `allOverrides` and `currentVersion` must each be declared before any property that uses them. A property read before it is initialised compiles fine but is null at runtime and crashes on launch.
   - **`timetableDao.deleteRemovedSessionSlots()`** in `initializeDefaultsIfNeeded()` and its `@Query` in `Daos.kt`. Confirm the Room code is generated and the query runs.
   - **`java.time.LocalDate`** in `AttendanceViewModel.kt` and `AttendanceCalculationEngine.kt`. `minSdk` is 24 and core library desugaring is not enabled, so it fails on Android 7 and below. Enable desugaring (`isCoreLibraryDesugaringEnabled = true` plus `coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")`) or use `Calendar`.
   - **Duplicate lazy-list keys.** `DailyScreen.kt`, `WhatIfPredictorScreen.kt` and `ReportsScreen.kt` use keys built from date, slot id and period. A repeated key crashes Compose with "Key was already used". Make each key unique, for example by adding the list index or the subject code.
   - **`DailyAttendanceUiItem`** was moved to `ui/viewmodel/DailyAttendanceUiItem.kt`. Make sure it is declared only once and `mergeLabBlocks` / `expandLabBlocks` are used in `buildDailyItems` and the three "mark all" functions.
   - **Older code paths:** Firebase and App Check start-up, the `firestore_database_id` string, `google-services.json` package name `com.aistudio.iihtattendance.vkmp`, and `debug.keystore`.
4. Fix the root cause. Do not hide the failure by wrapping large blocks in try/catch, and do not remove features. Add a regression test where one is possible.

## Part 2. Make the code better structured (behaviour must not change)

Work one file at a time. After each file: build, run `./gradlew testDebugUnitTest`, and confirm the app still opens.

- Split the very large files into several files **in the same package**, so no imports change: `ui/screens/whatif/WhatIfPredictorScreen.kt` (about 3,000 lines), `ui/components/CommonComponents.kt` (about 2,300), `ui/screens/SettingsScreen.kt` (about 2,200), `ui/screens/TimetableScreen.kt`, `ui/screens/HomeScreen.kt`, `ui/screens/ReportsScreen.kt`, and `ui/viewmodel/AttendanceViewModel.kt`. Group by purpose, for example one file per screen section or dialog, and give each a clear name.
- Keep all attendance rules in `data/util/AttendanceCalculationEngine.kt`. No screen may compute percentages, tiers, recovery or safe-miss itself.
- Remove unused imports, unused functions and commented-out code that nothing refers to.
- Keep these folders: `data/model`, `data/local`, `data/repository`, `data/remote`, `data/notification`, `data/util`, `ui/components`, `ui/screens`, `ui/theme`, `ui/viewmodel`.

## Part 3. Delete unwanted files

Before deleting anything, search the project for references and confirm it builds without it. Delete one group at a time and rebuild.

Candidates to check:
- `tmp/` (logo script) and `public/assets/aistudio/` (design mockups, prompts, font zip).
- `.aistudio/artifacts/**/.tempmediaStorage/` images and the stored APK metadata files.
- The root `src/assets/` folder if it only duplicates `app/src/main/assets/` and `res/`.
- The duplicate `data/firestore/FirestoreAttendanceRepository.kt`. The app uses the one in `data/remote/`. Keep one repository class, and update `app/src/test/.../AttendanceRepositoryRuleTest.kt` to the one that remains so tests still compile.
- Fonts in `res/font/`, drawables, mipmaps or logo copies that nothing uses (check `R.font.*` and `R.drawable.*` references).
- Any `.apk` file anywhere in the project other than `Download apk/` and Gradle's own `build/` folder.

Do NOT delete: `app/google-services.json`, `firestore.rules`, `firebase.json`, `firestore.test.js`, the Gradle files, `.env.example`, anything in `res/mipmap-*` used by the manifest, or any file the app or tests reference.

## Part 4. APK archive: "Download apk" folder with V1, V2, V3...

1. Copy `app/apk-archive.gradle.kts` from this kit into the project's `app/` folder.
2. Add this as the last line of `app/build.gradle.kts`: `apply(from = "apk-archive.gradle.kts")`
3. How it works: after every `assembleDebug` or `assembleRelease`, the new APK is copied to `Download apk/` in the project root as `AttendanceTracker-V<n>.apk`, where n goes up by one each build (V1, V2, V3...). Only the latest 3 are kept and older ones are deleted. The counter is stored in `Download apk/version-counter.txt`. It is compatible with the project's configuration cache. Do not change that behaviour.
4. Delete every other APK file in the project (outside `build/`).
5. Run `./gradlew assembleDebug` and confirm `Download apk/` holds the new file and at most 3 APKs in total.

## Part 5. Verify and report

- `./gradlew assembleDebug` and `./gradlew testDebugUnitTest` both succeed.
- The app opens on a clean install. Open every tab (Home, Daily, Timetable, Reports, What-If, Settings), mark one lab block present, and check `adb logcat` shows no FATAL EXCEPTION or error from `com.example`.
- Report: the original crash stack trace (or say it could not be reproduced), what caused it, files changed, files deleted, the contents of `Download apk/`, and anything you could not verify. Do not say something works unless you ran it.
