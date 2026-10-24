# Redesign Attendance-Marking Card UI (Claude HTML Design & Reference Image)

Comprehensive architecture and design plan to translate the provided Claude HTML/CSS implementation and reference image into a high-performance, native Jetpack Compose attendance-marking card across the application.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The clarifying questions were answered as follows:
> - **Action Controls**: Include the third button from the image/HTML for **No Class / Cancelled** alongside **Present** and **Absent**.
> - **Marked State UX**: Show the HTML-style **Marked Result Banner** with timestamp and an **Undo button** to switch back to unmarked state.
> - **Visual Atmosphere**: Render the layered glassmorphism background with **custom hardware-accelerated Compose Canvas wave curves** and ambient status glow effects.

- **Confirmed Decision 1**: The new card will feature 3 action buttons when unmarked (Present, Absent, and No Class icon button), seamlessly transforming into an animated Result Bar with an Undo action once marked.
- **Confirmed Decision 2**: Both Light and Dark theme design tokens from the Claude HTML (colors, borders, gradients, shadows, text styles) are mapped 1-to-1 into Compose tokens.
- **Confirmed Decision 3**: The updated card replaces the legacy `ClassSessionCard` in `CommonComponents.kt`, automatically upgrading both `HomeScreen` and `DailyScreen` with zero data regressions.

---

### 1. Overview & Core Concept

- **What It Does**: Upgrades every period card in the College Attendance Tracker into a glassmorphic attendance card based on the user's reference mockup and Claude HTML code.
- **Target Audience / Persona**: College engineering students tracking class attendance across multiple daily periods.
- **Key Value**: Provides instant visual clarity on class timing, subject codes, and attendance status with tactile, animated buttons and clear feedback banners, eliminating cluttered legacy button rows.

---

### 2. User Experience & Visual Design

#### Key User Flows

1. **Viewing Scheduled Periods**:
   - The user opens Home or Daily Attendance: each period is rendered inside a soft glassmorphic card with subtle ambient wave curves flowing across the bottom.
   - Top row displays a gradient capsule badge with the period number (e.g. `Period I`), a clock icon with timings (`09:30 AM – 10:20 AM`), and a non-actionable status pill on the far right (`Not Marked`, `Present`, `Absent`, or `No Class`).
   - Subject section displays a rounded-square icon tile with a book symbol, prominent bold subject title, and a subtitle with the subject code in accent blue/cyan and lecture type.

2. **Marking Attendance (Present / Absent / No Class)**:
   - When unmarked, the card displays:
     - **Present**: Pill button with vibrant turquoise/emerald gradient, checkmark icon, and white text.
     - **Absent**: Soft pink container with crisp red/rose outline, cross icon, and bold red text.
     - **No Class**: Rounded square ghost button with clock/event icon and subtle tooltip feedback.
   - Tapping an action triggers a tactile ripple and an immediate status update in Room/Firestore.
   - The button row smoothly transitions into a **Result Banner**:
     - Displays "Marked present at [Time]" (or "Marked absent" / "No class held") with a status badge.
     - Features an **Undo** button allowing the student to revert or modify their response with one tap.

3. **Light and Dark Theme Behavior**:
   - **Light Theme**: Frosted white and pale blue surfaces (`#FFFFFF` to `#E8F2FF`), lavender/cyan wave accents, dark navy typography (`#0E1250`), and soft drop shadows.
   - **Dark Theme**: Deep midnight blue gradients (`#0B2260` to `#07163F`), cyan/blue wave lines, crisp white titles (`#F3F6FF`), electric cyan code tags (`#22D3EE`), and subtle edge glows.

---

### 3. Key Product Decisions & Trade-Offs

#### Decision 1: Canvas Wave Rendering vs Static Images
- **Chosen Approach**: Use Compose `Canvas` with cubic bezier curves (`cubicTo`) and linear gradient brushes matching the SVG paths from the HTML (`.waves .l1, .l2, .l3, .tp`).
- **Why**: Zero asset bloat, crisp rendering at all screen densities, hardware-accelerated 60/120fps performance, and exact alignment with light/dark theme colors.

#### Decision 2: Replacing Legacy ClassSessionCard in-place
- **Chosen Approach**: Replace the implementation of `ClassSessionCard` in `CommonComponents.kt` while maintaining its signature interface (`item: DailyAttendanceUiItem, onMarkStatus: (AttendanceStatus) -> Unit`).
- **Why**: Ensures 100% consistent design across `HomeScreen` (Today's Schedule) and `DailyScreen` (Daily Attendance) without breaking existing ViewModel bindings, database mutations, or test suites.

#### Decision 3: Preserving Attendance Handlers & Data Layer
- **Chosen Approach**: Wire `Present` -> `AttendanceStatus.PRESENT`, `Absent` -> `AttendanceStatus.ABSENT`, `No Class` -> `AttendanceStatus.CANCELLED`, and `Undo` -> `AttendanceStatus.UNMARKED`.
- **Why**: Preserves all existing attendance percentages, safe margin calculations ("Can miss X classes"), Room local storage, and Firebase Firestore cloud synchronization.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                               UI Layer                                 │
│                                                                        │
│   ┌───────────────────────┐              ┌──────────────────────────┐  │
│   │      HomeScreen       │              │       DailyScreen        │  │
│   │ - Today's Schedule    │              │ - Full Daily Attendance  │  │
│   └───────────┬───────────┘              └────────────┬─────────────┘  │
│               │                                       │                │
│               ▼                                       ▼                │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │                 ClassSessionCard (Redesigned)                   │  │
│   │                                                                 │  │
│   │  [Background]: Glassmorphic Card + Canvas Bezier Waves + Glow   │  │
│   │  [Top Row]: Period Capsule | Clock Time | Status Indicator Pill │  │
│   │  [Subject Row]: Rounded Icon Box + Subject Title + Code & Type  │  │
│   │  [Action Slot]:                                                 │  │
│   │    - Unmarked: [Present] [Absent] [No Class Icon]               │  │
│   │    - Marked:   [Result Banner (e.g. Marked Present)] [Undo]     │  │
│   └───────────────────────────────┬─────────────────────────────────┘  │
└───────────────────────────────────┼────────────────────────────────────┘
                                    │ onMarkStatus(status)
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        AttendanceViewModel                             │
│                                                                        │
│   - markStatus(item, AttendanceStatus)                                │
│   - Updates Room Database (AttendanceRecord)                           │
│   - Syncs to Cloud Firestore (if authenticated)                        │
│   - Recalculates OverallAttendanceStat & Percentage                    │
└────────────────────────────────────────────────────────────────────────┘
```

#### Component Structure & Tokens

1. **Color Tokens (`Color.kt` / Card Tokens)**:
   - `PresentGradient`: `Brush.linearGradient(listOf(Color(0xFF19CF9C), Color(0xFF07A8B8)))`
   - `AbsentBgLight`: `Color(0xFFFFF3F7)`, `AbsentBorderLight`: `Color(0x80F05078)`, `AbsentFgLight`: `Color(0xFFF0305A)`
   - `AbsentBgDark`: `Color(0xB30A1946)`, `AbsentBorderDark`: `Color(0xCCFF5A8C)`, `AbsentFgDark`: `Color(0xFFFF5B86)`
   - `PeriodChipGradient`: `Brush.linearGradient(listOf(Color(0xFF2F6BFF), Color(0xFF7A4DFF)))`

2. **Wave Background Canvas (`CardWaveBackground`)**:
   - `Canvas` drawing 3 distinct bezier wave paths (`w1`, `w2`, `w3`) and a top subtle curve with translucent gradient fills.

3. **Status Pill (`CardStatusPill`)**:
   - Non-actionable indicator showing `Not Marked`, `Present`, `Absent`, or `No Class` with the respective icon and color styling.

4. **Action Row & Result Banner**:
   - Animated content switcher (`AnimatedContent` or crossfade) transitioning between the 3 action buttons and the confirmation result bar.

---

### Verification & Quality Plan

1. **Compilation & Syntax**: Run `compile_applet` to ensure zero compilation or type errors.
2. **Visual Fidelity Verification**:
   - Verify Light theme matches top card of reference image (frosted white, turquoise Present button, pink Absent button, clock button).
   - Verify Dark theme matches bottom card of reference image (deep navy, electric turquoise Present button, neon pink Absent outline).
3. **Interactive Testing**:
   - Mark a class Present: verify result banner appears with "Marked present" and Undo button.
   - Tap Undo: verify card returns to unmarked state.
   - Mark Absent and No Class: verify respective states and data persistence.
4. **Data & Calculation Integrity**:
   - Run unit test suite (`gradle :app:testDebugUnitTest`) to verify calculations, percentages, and attendance logic remain intact.
