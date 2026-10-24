package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AktuHolidays
import com.example.data.local.AppDatabase
import com.example.data.local.InitialTimetableData
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.util.AttendanceCalculationEngine
import com.example.data.model.ClassType
import com.example.data.model.DateOverride
import com.example.data.model.OverallAttendanceStat
import com.example.data.model.StudentProfile
import com.example.data.model.TimetableSlot
import com.example.data.model.TimetableVersion
import com.example.data.remote.FirestoreAttendanceRepository
import com.example.data.repository.AttendanceRepository
import com.example.data.model.MotivationalQuote
import com.example.data.model.MotivationalQuotesRepository
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationPreferences
import com.example.data.notification.NotificationAutomationHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TimetableScope {
    THIS_DATE_ONLY,
    THIS_DAY_ONWARDS,
    RECURRING_WEEKLY,
    ENTIRE_TIMETABLE
}

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AttendanceRepository = AttendanceRepository.getInstance(AppDatabase.getInstance(application))
    val firestoreRepo: FirestoreAttendanceRepository = FirestoreAttendanceRepository(application)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Selected date in Daily & Home view
    private val _selectedDate = MutableStateFlow(dateFormat.format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Profile State
    private val _profile = MutableStateFlow(StudentProfile())
    val profile: StateFlow<StudentProfile> = _profile.asStateFlow()

    // Current Timetable Version
    private val _currentVersion = MutableStateFlow<TimetableVersion?>(null)
    val currentVersion: StateFlow<TimetableVersion?> = _currentVersion.asStateFlow()

    // Timetable management state
    private val _selectedTimetableDay = MutableStateFlow("MONDAY")
    val selectedTimetableDay: StateFlow<String> = _selectedTimetableDay.asStateFlow()

    // Firebase Auth State
    private val _currentUser = MutableStateFlow<FirebaseUser?>(Firebase.auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    // Snackbar/Toast events
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Undo buffer for Bulk Mark All
    private var lastBulkState: Pair<String, List<AttendanceRecord>>? = null

    val allAttendanceRecords: StateFlow<List<AttendanceRecord>> = repository.allAttendanceFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOverrides: StateFlow<List<DateOverride>> = repository.allOverridesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVersions: StateFlow<List<TimetableVersion>> = repository.allVersionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<NotificationItem>> = repository.allNotificationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.unreadNotificationsCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val notificationPreferences: StateFlow<NotificationPreferences?> = repository.notificationPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // All slots for current version (for weekly grid or preview)
    @OptIn(ExperimentalCoroutinesApi::class)
    val allSlotsForCurrentVersion: StateFlow<List<TimetableSlot>> = currentVersion.flatMapLatest { ver: TimetableVersion? ->
        val vId = ver?.versionId ?: 1
        repository.getSlotsForVersion(vId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Overall attendance statistics calculated dynamically.
    // Upcoming timetable weights (batch / category filtered, holidays skipped) feed the recovery and
    // safe-miss calculators so mixed theory + lab weeks use real weights rather than an estimate.
    private val upcomingWeightsFlow = combine(
        allSlotsForCurrentVersion,
        profile,
        allOverrides
    ) { slots, prof, overrides ->
        val category = prof.academicCategory.trim()
        val eligible = slots.filter { sl ->
            (sl.batch == "ALL" || sl.batch.equals(prof.batch.removePrefix("Batch").trim(), ignoreCase = true)) &&
                !(category.equals("Major", true) && sl.subjectCode.contains("MNPM", true)) &&
                !(category.equals("Minor", true) && sl.subjectCode.contains("HTFT", true)) &&
                !(category.equals("Library", true) &&
                    (sl.subjectCode.contains("HTFT", true) || sl.subjectCode.contains("MNPM", true)))
        }
        val holidays = overrides.filter { it.isHoliday }.map { it.date }.toSet()
        val today = java.time.LocalDate.now()
        val isHoliday = { d: String -> d in holidays || AktuHolidays.isOfficialHoliday(d) }
        val allHolidayDates = (0 until 120).map { today.plusDays(it.toLong()).toString() }
            .filter(isHoliday).toSet()
        AttendanceCalculationEngine.upcomingWeights(
            slotsByDay = eligible.groupBy { it.dayOfWeek.uppercase() },
            startDate = today,
            holidayDates = allHolidayDates
        )
    }

    val overallStats: StateFlow<OverallAttendanceStat> = combine(
        allAttendanceRecords,
        profile,
        upcomingWeightsFlow
    ) { records, prof, upcoming ->
        repository.calculateStatistics(
            records,
            prof.targetAttendancePct,
            upcoming.first,
            upcoming.second
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        OverallAttendanceStat(0, 0, 0, 0, 0f, 75, 0, 0, emptyList())
    )

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
        }

        viewModelScope.launch {
            repository.profileFlow.collect { prof ->
                if (prof != null) {
                    _profile.value = prof
                }
            }
        }

        viewModelScope.launch {
            repository.allVersionsFlow.collect { versions ->
                _currentVersion.value = versions.firstOrNull { it.isCurrent } ?: versions.firstOrNull()
            }
        }

        // Listen for Firebase Auth changes
        Firebase.auth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser
            if (auth.currentUser != null) {
                // Trigger background cloud sync
                viewModelScope.launch {
                    try {
                        val localRecords = allAttendanceRecords.value
                        firestoreRepo.syncAllLocalToCloud(localRecords, _profile.value)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        // Initialize Android notification channels
        NotificationAutomationHelper.createNotificationChannels(application)

        // Seed demo notifications if empty & schedule periodic evaluation
        viewModelScope.launch {
            NotificationAutomationHelper.seedSampleNotificationsIfEmpty(
                repository = repository,
                stats = overallStats.value,
                profile = _profile.value
            )
        }

        viewModelScope.launch {
            combine(allAttendanceRecords, profile, overallStats) { recs, prof, stats ->
                Triple(recs, prof, stats)
            }.collect { triple ->
                try {
                    NotificationAutomationHelper.evaluateAndGenerateNotifications(
                        context = application,
                        repository = repository,
                        records = triple.first,
                        profile = triple.second,
                        stats = triple.third
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Motivational Quote State (cycles daily or on tap)
    private val _quoteIndex = MutableStateFlow(
        try {
            (java.time.LocalDate.now().dayOfYear - 1).coerceAtLeast(0)
        } catch (_: Exception) {
            java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR) - 1
        }
    )
    val currentMotivationalQuote: StateFlow<MotivationalQuote> = _quoteIndex.map { idx ->
        MotivationalQuotesRepository.getQuoteByIndex(idx)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MotivationalQuotesRepository.getDailyQuote())

    fun cycleQuote() {
        _quoteIndex.value = _quoteIndex.value + 1
    }

    // Current Timetable Slots for selectedTimetableDay filtered strictly by user's active Lab Batch
    @OptIn(ExperimentalCoroutinesApi::class)
    val timetableSlotsForDay: StateFlow<List<TimetableSlot>> = combine(
        selectedTimetableDay,
        currentVersion,
        profile
    ) { day, ver, prof ->
        Triple(day, ver?.versionId ?: 1, prof.batch)
    }.flatMapLatest { triple: Triple<String, Int, String> ->
        repository.getSlotsForDay(triple.first, triple.second).map { slots ->
            slots.filter { it.batch == "ALL" || it.batch.equals(triple.third, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily Items for the selected date
    val dailyAttendanceItems: StateFlow<List<DailyAttendanceUiItem>> = combine(
        selectedDate,
        profile,
        allAttendanceRecords,
        allOverrides
    ) { dateStr, prof, records, overrides ->
        buildDailyItems(dateStr, prof, records, overrides)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingUnmarkedCount: StateFlow<Int> = dailyAttendanceItems.map { items ->
        items.count { it.countsInAttendance && it.currentStatus == AttendanceStatus.UNMARKED }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** (weighted attended, weighted conducted) for the current month, via the shared engine. */
    val monthlyAttendanceSummary: StateFlow<Pair<Double, Double>> = allAttendanceRecords.map { records ->
        val currentMonthPrefix = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        AttendanceCalculationEngine.weightedTotals(records.filter { it.date.startsWith(currentMonthPrefix) })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0.0, 0.0))

    val recentActivityList: StateFlow<List<AttendanceRecord>> = allAttendanceRecords.map { records ->
        records.sortedByDescending { it.timestamp }.take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun buildDailyItems(
        dateStr: String,
        prof: StudentProfile,
        records: List<AttendanceRecord>,
        overrides: List<DateOverride>
    ): List<DailyAttendanceUiItem> {
        val cal = Calendar.getInstance().apply {
            time = dateFormat.parse(dateStr) ?: Date()
        }
        val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "MONDAY"
            Calendar.TUESDAY -> "TUESDAY"
            Calendar.WEDNESDAY -> "WEDNESDAY"
            Calendar.THURSDAY -> "THURSDAY"
            Calendar.FRIDAY -> "FRIDAY"
            Calendar.SATURDAY -> "SATURDAY"
            Calendar.SUNDAY -> "SUNDAY"
            else -> "MONDAY"
        }

        // Check if date is marked as Holiday
        val holiday = overrides.find { it.date == dateStr && it.isHoliday }
        if (holiday != null) {
            return emptyList()
        }

        // Fetch regular slots for this day from current version
        val vId = currentVersion.value?.versionId ?: 1
        val dbSlots = InitialTimetableData.getDefaultSlots(vId).filter { it.dayOfWeek == dayOfWeek }

        // Filter by batch (slot batch must be ALL or match user's batch)
        val userBatch = prof.batch
        val filteredSlots = dbSlots.filter { slot ->
            slot.batch == "ALL" || slot.batch.equals(userBatch, ignoreCase = true)
        }

        // Extra classes on this date
        val extraOverrides = overrides.filter { it.date == dateStr && it.isExtraClass }
        val recordsMap = records.filter { it.date == dateStr }.associateBy { it.period }

        val category = prof.academicCategory.trim()
        val result = mutableListOf<DailyAttendanceUiItem>()

        for (slot in filteredSlots) {
            // Swachhta Abhiyan and doubt clearing are no longer classes in this app
            if (AttendanceCalculationEngine.isExcludedFromAttendance(slot.subjectCode, slot.subjectName, slot.classType)) continue
            // Check if this slot is the elective/category slot
            val isElective = isElectiveSlot(slot.subjectCode, slot.subjectName)

            var resolvedCode = slot.subjectCode
            var resolvedName = slot.subjectName
            var resolvedClassType = slot.classType

            if (isElective) {
                when {
                    category.equals("Major", ignoreCase = true) -> {
                        val isTutorial = slot.subjectCode.contains("-T") || slot.subjectName.contains("Tutorial", ignoreCase = true)
                        if (isTutorial) {
                            resolvedCode = "HTFT 501-T"
                            resolvedName = "HTFT 501-T — Clothing Comfort Tutorial"
                            resolvedClassType = ClassType.TUTORIAL.name
                        } else {
                            resolvedCode = "HTFT 501"
                            resolvedName = "HTFT 501 — Clothing Comfort"
                            resolvedClassType = ClassType.LECTURE.name
                        }
                    }
                    category.equals("Minor", ignoreCase = true) -> {
                        val isTutorial = slot.subjectCode.contains("-T") || slot.subjectName.contains("Tutorial", ignoreCase = true)
                        if (isTutorial) {
                            resolvedCode = "MNPM 501-T"
                            resolvedName = "MNPM 501-T — Production Planning & Control Tutorial"
                            resolvedClassType = ClassType.TUTORIAL.name
                        } else {
                            resolvedCode = "MNPM 501"
                            resolvedName = "MNPM 501 — Production Planning & Control"
                            resolvedClassType = ClassType.LECTURE.name
                        }
                    }
                    category.equals("Library", ignoreCase = true) -> {
                        resolvedCode = "DLIB"
                        resolvedName = "Digital Library"
                        resolvedClassType = ClassType.LIBRARY.name
                    }
                }
            } else {
                // For non-elective classes:
                if (category.equals("Major", ignoreCase = true) && slot.subjectCode.contains("MNPM", ignoreCase = true)) {
                    continue
                }
                if (category.equals("Minor", ignoreCase = true) && slot.subjectCode.contains("HTFT", ignoreCase = true)) {
                    continue
                }
                if (category.equals("Library", ignoreCase = true) &&
                    (slot.subjectCode.contains("HTFT", ignoreCase = true) || slot.subjectCode.contains("MNPM", ignoreCase = true))) {
                    continue
                }
            }

            // Only mark true Library or Swachh sessions as isLibrary / isSwachh.
            // Elective Clothing Comfort (Major) is NEVER Library!
            val isLibrary = resolvedClassType.equals(ClassType.LIBRARY.name, ignoreCase = true) ||
                            resolvedCode.equals("LIB", ignoreCase = true) ||
                            resolvedCode.equals("DLIB", ignoreCase = true) ||
                            (!isElective && resolvedName.contains("Library", ignoreCase = true)) ||
                            (isElective && category.equals("Library", ignoreCase = true))
            val isSwachh = resolvedCode.equals("SWACH", ignoreCase = true) ||
                           resolvedName.contains("Swachh", ignoreCase = true)
            val isDoubt = resolvedCode.equals("DOUBT", ignoreCase = true) ||
                          resolvedName.contains("Doubt", ignoreCase = true) ||
                          resolvedClassType.equals(ClassType.DOUBT_CLEARING.name, ignoreCase = true)

            val rec = recordsMap[slot.period]
            val status = when {
                isLibrary || isSwachh -> AttendanceStatus.PRESENT
                isDoubt -> AttendanceStatus.UNMARKED
                rec?.status == AttendanceStatus.PRESENT.name -> AttendanceStatus.PRESENT
                rec?.status == AttendanceStatus.ABSENT.name -> AttendanceStatus.ABSENT
                rec?.status == AttendanceStatus.CANCELLED.name -> AttendanceStatus.CANCELLED
                else -> AttendanceStatus.UNMARKED
            }

            val counts = if (isDoubt) false else slot.countsInAttendance

            result.add(
                DailyAttendanceUiItem(
                    slotId = slot.id,
                    period = slot.period,
                    periodRoman = InitialTimetableData.getPeriodRoman(slot.period),
                    startTime = slot.startTime,
                    endTime = slot.endTime,
                    subjectCode = resolvedCode,
                    subjectName = resolvedName,
                    classType = resolvedClassType,
                    batch = slot.batch,
                    countsInAttendance = counts,
                    currentStatus = status,
                    isOverride = false,
                    notes = slot.notes
                )
            )
        }

        for (extra in extraOverrides) {
            if (AttendanceCalculationEngine.isExcludedFromAttendance(extra.subjectCode, extra.subjectName, extra.classType)) continue
            val isElective = isElectiveSlot(extra.subjectCode, extra.subjectName)
            var resolvedCode = extra.subjectCode
            var resolvedName = extra.subjectName
            var resolvedClassType = extra.classType

            if (isElective) {
                when {
                    category.equals("Major", ignoreCase = true) -> {
                        val isTutorial = extra.subjectCode.contains("-T") || extra.subjectName.contains("Tutorial", ignoreCase = true)
                        if (isTutorial) {
                            resolvedCode = "HTFT 501-T"
                            resolvedName = "HTFT 501-T — Clothing Comfort Tutorial"
                            resolvedClassType = ClassType.TUTORIAL.name
                        } else {
                            resolvedCode = "HTFT 501"
                            resolvedName = "HTFT 501 — Clothing Comfort"
                            resolvedClassType = ClassType.LECTURE.name
                        }
                    }
                    category.equals("Minor", ignoreCase = true) -> {
                        val isTutorial = extra.subjectCode.contains("-T") || extra.subjectName.contains("Tutorial", ignoreCase = true)
                        if (isTutorial) {
                            resolvedCode = "MNPM 501-T"
                            resolvedName = "MNPM 501-T — Production Planning & Control Tutorial"
                            resolvedClassType = ClassType.TUTORIAL.name
                        } else {
                            resolvedCode = "MNPM 501"
                            resolvedName = "MNPM 501 — Production Planning & Control"
                            resolvedClassType = ClassType.LECTURE.name
                        }
                    }
                    category.equals("Library", ignoreCase = true) -> {
                        resolvedCode = "DLIB"
                        resolvedName = "Digital Library"
                        resolvedClassType = ClassType.LIBRARY.name
                    }
                }
            } else {
                if (category.equals("Major", ignoreCase = true) && extra.subjectCode.contains("MNPM", ignoreCase = true)) continue
                if (category.equals("Minor", ignoreCase = true) && extra.subjectCode.contains("HTFT", ignoreCase = true)) continue
                if (category.equals("Library", ignoreCase = true) &&
                    (extra.subjectCode.contains("HTFT", ignoreCase = true) || extra.subjectCode.contains("MNPM", ignoreCase = true))) continue
            }

            val isLibrary = resolvedClassType.equals(ClassType.LIBRARY.name, ignoreCase = true) ||
                            resolvedCode.equals("LIB", ignoreCase = true) ||
                            resolvedCode.equals("DLIB", ignoreCase = true) ||
                            (!isElective && resolvedName.contains("Library", ignoreCase = true)) ||
                            (isElective && category.equals("Library", ignoreCase = true))
            val isSwachh = resolvedCode.equals("SWACH", ignoreCase = true) ||
                           resolvedName.contains("Swachh", ignoreCase = true)
            val isDoubt = resolvedCode.equals("DOUBT", ignoreCase = true) ||
                          resolvedName.contains("Doubt", ignoreCase = true) ||
                          resolvedClassType.equals(ClassType.DOUBT_CLEARING.name, ignoreCase = true)

            val rec = recordsMap[extra.period]
            val status = when {
                isLibrary || isSwachh -> AttendanceStatus.PRESENT
                isDoubt -> AttendanceStatus.UNMARKED
                rec?.status == AttendanceStatus.PRESENT.name -> AttendanceStatus.PRESENT
                rec?.status == AttendanceStatus.ABSENT.name -> AttendanceStatus.ABSENT
                rec?.status == AttendanceStatus.CANCELLED.name -> AttendanceStatus.CANCELLED
                else -> AttendanceStatus.UNMARKED
            }
            val counts = if (isDoubt) false else extra.countsInAttendance

            result.add(
                DailyAttendanceUiItem(
                    slotId = extra.id,
                    period = extra.period,
                    periodRoman = InitialTimetableData.getPeriodRoman(extra.period),
                    startTime = extra.startTime,
                    endTime = extra.endTime,
                    subjectCode = resolvedCode,
                    subjectName = resolvedName,
                    classType = resolvedClassType,
                    batch = extra.batch,
                    countsInAttendance = counts,
                    currentStatus = status,
                    isOverride = true,
                    notes = "Exceptional Session"
                )
            )
        }

        return mergeLabBlocks(result.sortedBy { it.period })
    }

    private fun isElectiveSlot(code: String, name: String): Boolean {
        val upperCode = code.uppercase()
        val upperName = name.uppercase()
        return upperCode.contains("HTFT501") || upperCode.contains("HTFT 501") ||
               upperCode.contains("MNPM501") || upperCode.contains("MNPM 501") ||
               upperName.contains("HTFT501") || upperName.contains("MNPM501") ||
               (upperName.contains("CC") && upperName.contains("PP&C")) ||
               upperName.contains("CLOTHING COMFORT") || upperName.contains("PRODUCTION PLANNING")
    }

    // --- Actions ---

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun stepDay(offset: Int) {
        val cal = Calendar.getInstance().apply {
            time = dateFormat.parse(_selectedDate.value) ?: Date()
            add(Calendar.DAY_OF_YEAR, offset)
        }
        _selectedDate.value = dateFormat.format(cal.time)
    }

    fun setToday() {
        _selectedDate.value = dateFormat.format(Date())
    }

    fun setSelectedTimetableDay(day: String) {
        _selectedTimetableDay.value = day
    }

    fun updateBatch(newBatch: String) {
        viewModelScope.launch {
            val updated = _profile.value.copy(batch = newBatch)
            _profile.value = updated
            repository.saveProfile(updated)
            if (Firebase.auth.currentUser != null) {
                try { firestoreRepo.saveProfile(updated) } catch (_: Exception) {}
            }
        }
    }

    fun updateProfile(updated: StudentProfile) {
        viewModelScope.launch {
            _profile.value = updated
            repository.saveProfile(updated)
            if (Firebase.auth.currentUser != null) {
                try { firestoreRepo.saveProfile(updated) } catch (_: Exception) {}
            }
        }
    }

    fun updateProfilePhoto(photoUri: String) {
        viewModelScope.launch {
            val updated = _profile.value.copy(profilePhotoUri = photoUri)
            _profile.value = updated
            repository.saveProfile(updated)
            if (Firebase.auth.currentUser != null) {
                try { firestoreRepo.saveProfile(updated) } catch (_: Exception) {}
            }
        }
    }

    fun setThemePreference(theme: String) {
        viewModelScope.launch {
            val updated = _profile.value.copy(themePreference = theme)
            _profile.value = updated
            repository.saveProfile(updated)
            if (Firebase.auth.currentUser != null) {
                try { firestoreRepo.saveProfile(updated) } catch (_: Exception) {}
            }
        }
    }

    fun setAccentTheme(accent: String) {
        viewModelScope.launch {
            val updated = _profile.value.copy(accentTheme = accent)
            _profile.value = updated
            repository.saveProfile(updated)
            if (Firebase.auth.currentUser != null) {
                try { firestoreRepo.saveProfile(updated) } catch (_: Exception) {}
            }
        }
    }

    fun toggleDashboardCard(cardKey: String, visible: Boolean) {
        viewModelScope.launch {
            val current = _profile.value
            val updated = when (cardKey) {
                "overview" -> current.copy(showOverviewCard = visible)
                "today" -> current.copy(showTodayCard = visible)
                "weekly" -> current.copy(showWeeklyCard = visible)
                "recent" -> current.copy(showRecentCard = visible)
                "monthly" -> current.copy(showMonthlyCard = visible)
                else -> current
            }
            _profile.value = updated
            repository.saveProfile(updated)
            if (Firebase.auth.currentUser != null) {
                try { firestoreRepo.saveProfile(updated) } catch (_: Exception) {}
            }
        }
    }

    fun updateAcademicCategory(category: String) {
        viewModelScope.launch {
            val updated = _profile.value.copy(academicCategory = category)
            _profile.value = updated
            repository.saveProfile(updated)
            if (Firebase.auth.currentUser != null) {
                try { firestoreRepo.saveProfile(updated) } catch (_: Exception) {}
            }
        }
    }

    fun markStatus(item: DailyAttendanceUiItem, status: AttendanceStatus) {
        val date = _selectedDate.value
        if (AktuHolidays.isOfficialHoliday(date)) {
            // Requirement: Disable marking attendance for holidays
            return
        }

        val isElective = isElectiveSlot(item.subjectCode, item.subjectName)
        val isLibrary = !isElective && (
            item.classType.equals(ClassType.LIBRARY.name, ignoreCase = true) ||
            item.subjectCode.equals("LIB", ignoreCase = true) ||
            item.subjectCode.equals("DLIB", ignoreCase = true) ||
            item.subjectName.contains("Library", ignoreCase = true)
        )
        val isSwachh = item.subjectCode.equals("SWACH", ignoreCase = true) ||
                       item.subjectName.contains("Swachh", ignoreCase = true)
        val isDoubt = item.subjectCode.equals("DOUBT", ignoreCase = true) ||
                      item.subjectName.contains("Doubt", ignoreCase = true)

        if (isDoubt) return // Doubt clearing sessions are not counted as classes
        if ((isLibrary || isSwachh) && status == AttendanceStatus.ABSENT) {
            // Requirement: Swachhata Abhiyan and Library attendance should be always present marked
            return
        }

        // A lab block (two periods) is one control: write or clear both period records together.
        val periods = listOfNotNull(item.period, item.pairedPeriod)
        viewModelScope.launch {
            for (period in periods) {
                if (status == AttendanceStatus.UNMARKED) {
                    repository.resetAttendanceForSlot(date, period)
                    if (Firebase.auth.currentUser != null) {
                        try { firestoreRepo.deleteAttendanceRecord(date, period) } catch (_: Exception) {}
                    }
                } else {
                    val record = AttendanceRecord(
                        date = date,
                        period = period,
                        subjectCode = item.subjectCode,
                        subjectName = item.subjectName,
                        status = status.name
                    )
                    repository.markAttendance(record)
                    if (Firebase.auth.currentUser != null) {
                        try { firestoreRepo.saveAttendanceRecord(record) } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    fun markAllPresentForDate() {
        viewModelScope.launch {
            val date = _selectedDate.value
            if (AktuHolidays.isOfficialHoliday(date)) {
                return@launch // Disable marking on official holidays
            }
            val currentItems = expandLabBlocks(dailyAttendanceItems.value)
            if (currentItems.isEmpty()) {
                return@launch
            }

            val currentRecords = allAttendanceRecords.value.filter { it.date == date }
            lastBulkState = Pair(date, currentRecords)

            // Exclude doubt clearing sessions from marking
            val eligibleItems = currentItems.filter { item ->
                !item.subjectCode.equals("DOUBT", ignoreCase = true) &&
                !item.subjectName.contains("Doubt", ignoreCase = true)
            }

            val slots = eligibleItems.map { item ->
                TimetableSlot(
                    dayOfWeek = getDayOfWeek(date),
                    period = item.period,
                    startTime = item.startTime,
                    endTime = item.endTime,
                    subjectCode = item.subjectCode,
                    subjectName = item.subjectName,
                    batch = item.batch
                )
            }

            val count = repository.markAllPresent(date, slots, _profile.value.batch)
            if (Firebase.auth.currentUser != null) {
                try {
                    eligibleItems.forEach { item ->
                        firestoreRepo.saveAttendanceRecord(
                            AttendanceRecord(
                                date = date,
                                period = item.period,
                                subjectCode = item.subjectCode,
                                subjectName = item.subjectName,
                                status = AttendanceStatus.PRESENT.name
                            )
                        )
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun markAllAbsentForDate() {
        viewModelScope.launch {
            val date = _selectedDate.value
            if (AktuHolidays.isOfficialHoliday(date)) {
                return@launch // Disable marking on official holidays
            }
            val currentItems = expandLabBlocks(dailyAttendanceItems.value)
            if (currentItems.isEmpty()) {
                return@launch
            }

            val currentRecords = allAttendanceRecords.value.filter { it.date == date }
            lastBulkState = Pair(date, currentRecords)

            // Swachhata and Library are ALWAYS marked Present, never Absent!
            // Doubt sessions are not counted as classes.
            val slotsToMarkAbsent = currentItems.filter { item ->
                val isLib = item.classType.equals(ClassType.LIBRARY.name, ignoreCase = true) ||
                            item.subjectCode.equals("LIB", ignoreCase = true) ||
                            item.subjectCode.equals("DLIB", ignoreCase = true) ||
                            item.subjectName.contains("Library", ignoreCase = true)
                val isSwachh = item.subjectCode.equals("SWACH", ignoreCase = true) ||
                               item.subjectName.contains("Swachh", ignoreCase = true)
                val isDoubt = item.subjectCode.equals("DOUBT", ignoreCase = true) ||
                              item.subjectName.contains("Doubt", ignoreCase = true)
                !isLib && !isSwachh && !isDoubt
            }

            val slots = slotsToMarkAbsent.map { item ->
                TimetableSlot(
                    dayOfWeek = getDayOfWeek(date),
                    period = item.period,
                    startTime = item.startTime,
                    endTime = item.endTime,
                    subjectCode = item.subjectCode,
                    subjectName = item.subjectName,
                    batch = item.batch
                )
            }

            val count = repository.markAllAbsent(date, slots, _profile.value.batch)
            if (Firebase.auth.currentUser != null) {
                try {
                    slotsToMarkAbsent.forEach { item ->
                        firestoreRepo.saveAttendanceRecord(
                            AttendanceRecord(
                                date = date,
                                period = item.period,
                                subjectCode = item.subjectCode,
                                subjectName = item.subjectName,
                                status = AttendanceStatus.ABSENT.name
                            )
                        )
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun markAllCancelledForDate() {
        viewModelScope.launch {
            val date = _selectedDate.value
            if (AktuHolidays.isOfficialHoliday(date)) {
                return@launch // Disable marking on official holidays
            }
            val currentItems = expandLabBlocks(dailyAttendanceItems.value)
            if (currentItems.isEmpty()) {
                return@launch
            }

            val currentRecords = allAttendanceRecords.value.filter { it.date == date }
            lastBulkState = Pair(date, currentRecords)

            val slots = currentItems.map { item ->
                TimetableSlot(
                    dayOfWeek = getDayOfWeek(date),
                    period = item.period,
                    startTime = item.startTime,
                    endTime = item.endTime,
                    subjectCode = item.subjectCode,
                    subjectName = item.subjectName,
                    batch = item.batch
                )
            }

            repository.markAllCancelled(date, slots, _profile.value.batch)
            if (Firebase.auth.currentUser != null) {
                try {
                    currentItems.forEach { item ->
                        firestoreRepo.saveAttendanceRecord(
                            AttendanceRecord(
                                date = date,
                                period = item.period,
                                subjectCode = item.subjectCode,
                                subjectName = item.subjectName,
                                status = AttendanceStatus.CANCELLED.name
                            )
                        )
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun resetAttendanceForDate() {
        viewModelScope.launch {
            val date = _selectedDate.value
            val currentRecords = allAttendanceRecords.value.filter { it.date == date }
            lastBulkState = Pair(date, currentRecords)

            repository.resetAttendanceForDate(date)
            if (Firebase.auth.currentUser != null) {
                try {
                    currentRecords.forEach {
                        firestoreRepo.deleteAttendanceRecord(date, it.period)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun undoLastBulkAction() {
        viewModelScope.launch {
            val bulk = lastBulkState
            if (bulk != null) {
                val (date, prevRecords) = bulk
                repository.resetAttendanceForDate(date)
                for (rec in prevRecords) {
                    repository.markAttendance(rec)
                    if (Firebase.auth.currentUser != null) {
                        try { firestoreRepo.saveAttendanceRecord(rec) } catch (_: Exception) {}
                    }
                }
                lastBulkState = null
            }
        }
    }

    fun clearAllAttendance() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    // --- Timetable Management Actions ---

    fun addOrUpdateSlot(
        slot: TimetableSlot,
        scope: TimetableScope,
        effectiveDate: String
    ) {
        viewModelScope.launch {
            when (scope) {
                TimetableScope.THIS_DATE_ONLY -> {
                    val override = DateOverride(
                        date = _selectedDate.value,
                        isExtraClass = true,
                        period = slot.period,
                        startTime = slot.startTime,
                        endTime = slot.endTime,
                        subjectCode = slot.subjectCode,
                        subjectName = slot.subjectName,
                        classType = slot.classType,
                        batch = slot.batch,
                        countsInAttendance = slot.countsInAttendance
                    )
                    repository.addDateOverride(override)
                }
                TimetableScope.THIS_DAY_ONWARDS,
                TimetableScope.RECURRING_WEEKLY,
                TimetableScope.ENTIRE_TIMETABLE -> {
                    if (slot.id > 0) {
                        repository.updateSlot(slot)
                    } else {
                        repository.addSlot(slot)
                    }
                }
            }
        }
    }

    fun deleteSlot(slotId: Long) {
        viewModelScope.launch {
            repository.deleteSlot(slotId)
        }
    }

    fun markDayAsHoliday(date: String, holidayName: String) {
        viewModelScope.launch {
            val override = DateOverride(
                date = date,
                isHoliday = true,
                holidayName = holidayName.ifBlank { "Holiday / Off Day" }
            )
            repository.addDateOverride(override)
        }
    }

    fun removeHoliday(date: String) {
        viewModelScope.launch {
            repository.clearDateOverrides(date)
        }
    }

    fun restoreTimetableVersion(versionId: Int) {
        viewModelScope.launch {
            repository.restoreTimetableVersion(versionId)
        }
    }

    suspend fun exportCsv(): String {
        return repository.generateCsvReport(_profile.value)
    }

    suspend fun exportJson(): String {
        return repository.exportBackupJson()
    }

    suspend fun restoreJson(json: String): Boolean {
        return repository.restoreBackupJson(json)
    }

    fun getDayOfWeek(dateStr: String): String {
        val cal = Calendar.getInstance().apply {
            time = dateFormat.parse(dateStr) ?: Date()
        }
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "MONDAY"
            Calendar.TUESDAY -> "TUESDAY"
            Calendar.WEDNESDAY -> "WEDNESDAY"
            Calendar.THURSDAY -> "THURSDAY"
            Calendar.FRIDAY -> "FRIDAY"
            Calendar.SATURDAY -> "SATURDAY"
            Calendar.SUNDAY -> "SUNDAY"
            else -> "MONDAY"
        }
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    fun updateNotificationPreferences(preferences: NotificationPreferences) {
        viewModelScope.launch {
            repository.saveNotificationPreferences(preferences)
        }
    }

    fun generateSampleNotifications() {
        viewModelScope.launch {
            NotificationAutomationHelper.seedSampleNotificationsIfEmpty(
                repository = repository,
                stats = overallStats.value,
                profile = _profile.value
            )
        }
    }

    fun bulkMarkAttendance(date: String, status: AttendanceStatus) {
        when (status) {
            AttendanceStatus.PRESENT -> markAllPresentForDate()
            AttendanceStatus.ABSENT -> markAllAbsentForDate()
            AttendanceStatus.CANCELLED -> markAllCancelledForDate()
            AttendanceStatus.UNMARKED -> resetAttendanceForDate()
        }
    }

    fun undoBulkMark() {
        undoLastBulkAction()
    }

    fun markDateAsHoliday(date: String, isHoliday: Boolean, holidayName: String = "Institute Holiday") {
        if (isHoliday) {
            markDayAsHoliday(date, holidayName)
        } else {
            removeHoliday(date)
        }
    }

    fun deleteSlot(slot: TimetableSlot, scope: TimetableScope, date: String) {
        viewModelScope.launch {
            when (scope) {
                TimetableScope.THIS_DATE_ONLY -> {
                    repository.addDateOverride(
                        DateOverride(
                            date = date,
                            isHoliday = false,
                            holidayName = "Cancelled session",
                            period = slot.period,
                            countsInAttendance = false
                        )
                    )
                }
                else -> {
                    repository.deleteSlot(slot.id)
                }
            }
        }
    }

    fun markAttendance(
        slotId: Long,
        date: String,
        status: AttendanceStatus,
        subjectCode: String = "",
        subjectName: String = "",
        classType: String = "",
        period: Int = 1,
        counts: Boolean = true
    ) {
        if (AktuHolidays.isOfficialHoliday(date)) {
            // Requirement: Disable marking attendance for holidays
            return
        }

        val isDoubt = subjectCode.equals("DOUBT", ignoreCase = true) ||
                      subjectName.contains("Doubt", ignoreCase = true) ||
                      classType.equals(ClassType.DOUBT_CLEARING.name, ignoreCase = true)
        if (isDoubt) return // Doubt clearing sessions are not counted as classes

        val isElective = isElectiveSlot(subjectCode, subjectName)
        val isLibOrSwachh = !isElective && (
            subjectCode.equals("LIB", ignoreCase = true) ||
            subjectCode.equals("DLIB", ignoreCase = true) ||
            subjectCode.equals("SWACH", ignoreCase = true) ||
            subjectName.contains("Library", ignoreCase = true) ||
            subjectName.contains("Swachh", ignoreCase = true) ||
            classType.equals(ClassType.LIBRARY.name, ignoreCase = true)
        )

        val targetStatus = if (isLibOrSwachh && status == AttendanceStatus.ABSENT) {
            AttendanceStatus.PRESENT
        } else {
            status
        }

        viewModelScope.launch {
            if (targetStatus == AttendanceStatus.UNMARKED) {
                repository.resetAttendanceForSlot(date, period)
                if (Firebase.auth.currentUser != null) {
                    try { firestoreRepo.deleteAttendanceRecord(date, period) } catch (_: Exception) {}
                }
            } else {
                val record = AttendanceRecord(
                    date = date,
                    period = period,
                    subjectCode = subjectCode,
                    subjectName = subjectName,
                    status = targetStatus.name
                )
                repository.markAttendance(record)
                if (Firebase.auth.currentUser != null) {
                    try { firestoreRepo.saveAttendanceRecord(record) } catch (_: Exception) {}
                }
            }
        }
    }
}

