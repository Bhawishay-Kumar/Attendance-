# Prompt for Google AI Studio — Redesign "College Attendance Tracker" UI

**Attach together with this prompt:** the developer-card screenshot (`1000091825.png`), `college-attendance-redesign.html` (the visual + animation reference), `app_logo_new.svg` / `app_logo_new.png` (new logo), and your existing project.

---

## 0. What you are doing

This is a native **Kotlin + Jetpack Compose** app (package `com.aistudio.iihtattendance.vkmp`) with Room, Firebase Auth/Firestore (Google sign-in), BiometricPrompt, notifications and vibration. Redesign **only the UI layer** of every screen so it matches `college-attendance-redesign.html` — a fully working prototype of the new design (open it in a browser to see every screen, animation and transition).

**Hard rule: UI only.** Do not change the ViewModel, repository, Room entities/DAOs, Firestore sync, attendance calculations, notification automation logic, navigation routes, or stored data. Reuse every existing state/callback; only rebuild composables, theme tokens, shapes, animations and layout. Keep all existing `testTag`s and string resources. If a requirement below needs a new setting (haptics), add it the same way existing settings are persisted (DataStore / preferences already used by the app).

## 1. Design language (copy from the HTML)

- **Glassmorphism** cards: translucent surface, 1.5 dp gradient hairline border, soft shadow, 24 dp radius (dock/pills fully rounded). Use `Modifier.blur`/render-effect on Android 12+, a translucent gradient fallback below.
- **Scenic animated background** that follows the theme: day = soft sky with warm sun, drifting clouds, pale hills; night = deep navy→violet sky, twinkling stars, glowing moon, aurora, rare shooting star. The sun sinks / moon rises as the theme slider moves. Draw with `Canvas`/`Brush`, animate only transforms/alpha; pause when the app is in the background.
- **Typography:** Plus Jakarta Sans (add as a font resource), weights 500–800. Keep the existing Caveat font for the handwritten "Thank you for being here!" line.
- **Colours:** use the tokens in the HTML `html[data-theme="dark"|"light"]` blocks. Primary gradient `#3B6BFF → #7A4DFF`; Present gradient `#13DBA2 → #08B49C`; Absent `#FF2F75 → #E51C4A`; No-class violet `#4A4FA8`. **Remove the accent-colour palette feature completely** (no accent picker, no `AccentTheme` selection UI); the app uses this single palette in light and dark.
- **Subject icons** (Lucide-style vector, accent tinted tile): yarn (BHT503), weave (BHT055), shirt (BHT051/HTFT501), loom-grid (BHT501), flask (BHT502/labs), plan (MNPM501), landmark (BNC501), leaf (SWACH), book (library/tutorial), help (doubt clearing), users (seminar). Map by subject code first, then by class type. Never use emoji for subject icons.
- **New logo:** replace `assets/logo.png` and `assets/app_logo.png` (and the launcher icon foreground) with `app_logo_new.svg/png`. Used in the header, splash screen and launcher. Use it as an adaptive icon (foreground = calendar+check+cap, background = the blue→violet gradient).

## 2. Global shell (all screens)

- **Header (kept from the original):** logo, screen title, subtitle `Batch X • Smart Tracker`, **day/night slider toggle** (replaces the old Light/Dark pill but keeps the same behaviour: it sets the saved theme preference), notification bell with unread badge → opens the Notification Center. Title cross-fades with a small upward slide when the tab changes.
- **Theme toggle behaviour:** glass track, spring-driven thumb (stiffness ≈ 380, damping ≈ 27), thumb stretches slightly while moving and swells while pressed, draggable, snaps to the nearest side, flick velocity carries on. Sun→moon icon rotates/crossfades; a cloud (day) and stars (night) show inside the track. Everything (track, thumb, sky, sun/moon) is driven by one progress value 0–1 so it stays in sync. Settings also has Light / Dark / System; choosing System follows the OS, and the header slider reflects the result. Switching themes must not reset any state.
- **Bottom navigation dock (liquid glass):** the existing 5 items in the same order and routes: Home, Attendance (Daily), Timetable, Reports, Settings. Translucent blurred pill with gradient hairline. Only the **selected** item shows its label (icon + name side by side inside a blue→violet glass capsule); others show icon only. The capsule slides between items (≈420 ms, `cubic-bezier(.3,.85,.35,1)`), stretching slightly while travelling and settling with a small spring, with a light shine sweep. Tap **or drag** the capsule to switch. Per-icon micro animations on select: house hop, check tick draws, clock hand spins, bars grow, gear turns 180°. Keep ≥ 48 dp touch targets and respect gesture-navigation insets.
- **Page transitions:** new page slides in 40 dp from the direction of travel with a short blur-fade (≈ 420 ms); content blocks rise in with a 60–70 ms stagger.
- **Splash / opening screen:** dark gradient, new logo scaling in with two expanding pulse rings, title "College Attendance Tracker", tagline "B-TECH IN IIHT • SMART ATTENDANCE", three bouncing dots, then fade/scale out (≈ 2 s). Keep the existing opening/onboarding logic.
- **Dialogs and sheets:** all confirmations are glass dialogs (scale-in spring) and all forms are bottom sheets (slide up 500 ms with a grab handle and dimmed blurred scrim). Keep every existing confirmation and its exact message.

## 3. Screens

### Home (`HomeScreen`)
Greeting card with initials monogram (no photo), time-based greeting + first name + full date. **Pending Attendance Reminder** banner (animated bell) with button `btn_mark_pending_action`. **Overall Attendance hero** — animated gradient ring, attended / conducted, target chip, On Track / Below Target chip, and the safe-margin or "attend N more" note (use the existing forecast values). **Quick Actions** (Daily, Timetable, Calendar). **Today's Schedule** rows with one-tap Present / Absent / No-class icon buttons, Library periods shown as "Always Present", and **Mark All Present** (`btn_mark_all_present_home`) with the existing confirmation dialog. **Weekly Timetable** carousel (today highlighted), **Monthly Overview** (progress bar + present/absent/no-class), **Recent Attendance Logs** (last 5), motivational quote card (existing quotes repository). Each card respects the existing dashboard-card visibility settings.

### Attendance / Daily (`DailyScreen`)
Date card (previous / next, tappable date opens the existing date picker, Today, batch chip). **Bulk Attendance Actions** card with counter chip and animated progress bar: All Present, All Absent, All No Class, Reset — keep the existing confirmation dialogs. Session cards exactly as in the prototype: period badge, time, status pill, subject icon, title, `CODE • TYPE`, Present / Absent / **No-class icon-only button** (label only for accessibility "No class held"), and after marking: green/red/violet confirmation strip + Undo. Library sessions = "Always Present" strip. Holiday card, empty state, **Add Exceptional Class** sheet, remove button on exceptional classes. Marking effects: ripple, small particle burst (respect the effects setting), card glow ring, icon pop, shine sweep on the strip, pill icon pop, progress counter bump. Marking must feel instant (no waiting for animations).

### Timetable (`TimetableScreen`)
Date navigation + scrollable day segmented control (Mon–Sat) with a sliding glass thumb, **Institute Holiday** switch with holiday-name field, list of sessions (period chip, time, icon, name, `CODE • TYPE`, batch / track / "This date only" tags) with Edit and Delete icon buttons, floating **Add Class** button (`btn_add_class`). Add/Edit sheet: name, code, class type, period (auto-fills times), start/end, applicable batch, "Count in Attendance Calculation" switch, notes, scope ("This date only" / "This day onwards"). Delete dialog keeps the original wording ("This will not delete your past attendance history") with options: skip this date only, delete permanently.

### Reports (`ReportsScreen`)
Glass segmented control with sliding thumb: **Overview / Calendar / History** (keep the existing tab tags). Overview: overall ring + counts + forecast note, **Export CSV** button, per-subject cards (icon, name, code, %, animated progress bar with a target marker line, P / A / NC chips, "Eligible… can miss N" or "Attend N more…" forecast; bars animate from 0 on entry). Calendar: month navigation, coloured days (Full Present = green, Absent = red, Incomplete = yellow, No Class = grey), legend, tapping a day shows that date's records and **View in Attendance** (`btn_view_date`). History: filter chips All / Present / Absent / Cancelled, records grouped by date, existing empty states.

### Settings (`SettingsScreen`)
Cards, each animating in with a stagger:
1. **Student Profile** — edit button (section 7C), initials monogram + name, roll no., college, course, branch, semester, academic year, lab batch. **Remove the profile-picture/avatar selection option entirely** (no image picker, no avatar gallery).
2. **Appearance & Theme** — Light / Dark / System sliding segmented control (linked to the header slider). **Remove the accent colour palette option.**
3. **My Lab Batch** — A / B with the existing confirmation dialog.
4. **Academic Category** — Major / Minor / Library (keep existing tags `btn_category_*` and explanatory text).
5. **Attendance Target** — +/- buttons, slider 50–100, number field with "Must be 50% – 100%" validation, tick labels 50 / 75 (Target) / 100.
6. **Customize Dashboard Cards** — the five existing switches with descriptions.
7. **Haptics & Effects (NEW)** — Haptic Feedback switch, strength (Light / Medium / Strong), "Test haptic feedback" button, Particle Effects switch. See section 4.
8. **Notifications** — opens Notification Settings.
9. **Cloud Backup & Sync** — existing Google sign-in / sign-out flow, restyled.
10. **Data Management** — Share Backup, Restore From Backup, Restore Official Timetable, Clear All Attendance Records — keep all existing dialogs/wording.
11. **Developer card** (see section 7D) followed by the **message card** "A MESSAGE FOR YOU" — keep the text. Tapping a **heart** turns it red with a beat animation, a ring and a burst of small hearts; tapping the **smile** makes it bounce/laugh. Footer "Made with 💙 by Bhawishya Kumar".

### Notification Center (`NotificationCenterScreen`)
Full-screen glass panel sliding in from the right: back button, title with unread badge, mark-all-read, settings, clear-all. Android permission card with Grant button (`btn_grant_notification_permission`), category filter chips, cards with category icon/colour, "New" tag, time-ago, action button (Go to Attendance / Check Schedule / View Report / Open Settings), swipe-style dismiss animation, empty state with `btn_seed_sample_notifications`. Notification Settings sheet: master, marking reminders, upcoming-class alerts + lead time (5/10/15/30), low-attendance, weekly summary, monthly report, quiet hours — bound to the existing preferences.

## 4. Haptic feedback (new, user-requested)

Add `HapticManager` (`LocalHapticFeedback` + `VibratorManager`/`Vibrator` fallback), persisted in the existing settings store: `hapticsEnabled` (default true) and `hapticStrength` (light = 0.6×, medium = 1×, strong = 1.7×). Patterns: **tap** 8 ms, **select** 6 ms, **success** 10-40-14 ms, **warn** 28-60-28 ms. Use: tab changes, theme slider, switches/segments, bulk actions, marking Present/No-class (success) and Absent (warn), validation errors (warn), confirmations. Respect the system "touch feedback" setting. Never vibrate when disabled.

## 5. Animation spec (use Compose `spring`/`tween`)

Buttons and card press: scale 0.94–0.96 with a bouncy spring (≈ 200 ms). Surface/colour changes 300–450 ms. Page and sheet transitions 420–500 ms with `FastOutSlowIn`-style curve (`cubic-bezier(.2,.9,.3,1)`). Capsule/segment thumbs: slide 420–450 ms with overshoot-free ease plus slight stretch. Respect `Settings.Global.ANIMATOR_DURATION_SCALE` / reduce-motion: disable particles, background drift and stretch effects. Keep the main thread free: animate `graphicsLayer` properties only; no continuous animations except the slow background drift and star twinkle.


## 7. Revision 2 — corrections and additions (apply on top of everything above)

**A. Timetable data must match `InitialTimetableData` (do not invent classes).** The prototype now mirrors the real default timetable read from the app: **Monday–Friday only, nothing on Saturday/Sunday** (remove any Saturday tab/column/card from Timetable, Daily and Home weekly preview). Constitution of India (BNC501) is **Wednesday (I) and Friday (I) only — not Tuesday/Thursday**. Weekly layout (period: subject):
- **Mon:** I BHT503 · II BHT055 · III BHT051 · IV BHT501 · V HTFT501/MNPM501/Digital Library · VI BHT502 · VII Library · VIII Swachhta Abhiyan
- **Tue:** I BHT051 · II Library · III HTFT501/MNPM501/Digital Library · IV BHT502 · V BHT055 · VI Library · VII BHT503 · VIII Doubt Clearing
- **Wed:** I BNC501 · II BHT503 · III BHT501 · IV BHT502 · V BHT051 · VI–VII BHT553 Lab (Batch A) / Seminar (Batch B) · VIII Doubt Clearing
- **Thu:** I BHT501 · II HTFT501/MNPM501/Digital Library · III–IV BHT551 Lab (A) / BHT552 Lab (B) · V BHT502-T Tutorial · VI–VII Seminar (A) / BHT553 Lab (B) · VIII Doubt Clearing
- **Fri:** I BNC501 · II HTFT501/MNPM501/Digital Library Tutorial · III–IV BHT552 Lab (A) / BHT551 Lab (B) · V BHT501-T · VI BHT503-T · VII BHT055 · VIII Doubt Clearing

Every screen (Home today list + weekly preview, Daily, Timetable, Reports) must read the same timetable source so the day-wise classes are identical everywhere.

**B. Always-present sessions.** **Library, Digital Library, Swachhta Abhiyan** (and Doubt Clearing, which the app data flags the same way) are *always present*: show them with an "Always Present" strip, no Present/Absent/No-class buttons, never counted in percentages, and excluded from Mark All / pending reminders. The "HTFT501 / MNPM501 / Digital Library" parallel period shows Clothing Comfort for Major, Production Planning & Control for Minor, and Digital Library (always present) for the Library category.

**C. Edit Profile (new).** Add an edit icon button on the Student Profile card that opens a bottom sheet to edit name, roll number, college, course, branch, semester and academic year (name required). Save through the existing profile DAO/repository and update Home greeting, initials monogram and Reports header instantly. Still **no profile-picture selection**.

**D. Developer card (new, from the attached screenshot).** Add it in Settings **just before "A MESSAGE FOR YOU"**: glass card with the BK logo in a circular gradient ring (cyan → blue → violet, spins once on tap with a haptic), "DEVELOPER" label, **Bhawishya Kumar**, "App Developer & Creator", handwritten (Caveat) "Built with passion" + tappable heart (turns red, beat, ring and heart burst — same as the message card), and "BETTER APPS FOR A BRIGHTER TOMORROW" with a blue→violet gradient bar. Use the circular BK logo crop from the screenshot as a drawable (`dev_logo.png`). On narrow phones the tagline wraps under the text.

**E. Smoothness / lag fixes (important).**
- The day/night slider was laggy because every frame re-blurred and recomposed the whole screen. Drive the sky/sun/moon from **one `Animatable<Float>` progress read only inside the background `Canvas`/`graphicsLayer` lambdas** (no recomposition of screens per frame); update the theme colours once when progress crosses 0.5.
- While the slider moves: **temporarily disable blur on the dock/header, pause the cloud/star/aurora animations and skip colour/surface transitions**, then restore when it settles.
- Do **not** use blur on every card; cards use a tinted translucent gradient (blur only on the dock, dialogs and sheets). Never animate blur radius. Page transitions use translate + alpha only.
- Use `graphicsLayer` for press/scale/slide, `remember`ed `Brush`es, stable keys in `LazyColumn`, and avoid re-creating lists on every tick. Target 60 fps on mid-range devices.

## 6. Acceptance checklist

- Every screen above restyled; all existing features, tags, strings, routes and logic untouched.
- Light and dark both look correct; switching is instant and keeps state.
- No avatar picker, no accent palette anywhere.
- New logo in header, splash and launcher.
- Haptic settings work and persist.
- No crashes on rotation, small phones (≈ 360 dp wide) or large phones; no clipped text; gesture-nav insets respected.
- The app builds and runs; list the files you changed.
