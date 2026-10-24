package com.example.ui.screens.whatif

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AktuHolidays
import com.example.data.local.InitialTimetableData
import com.example.data.model.AttendanceStatus
import com.example.data.util.AttendanceCalculationEngine
import com.example.data.model.ClassType
import com.example.data.model.StudentProfile
import com.example.ui.components.AnimatedCircularAttendanceRing
import com.example.ui.components.GlassCard
import com.example.ui.components.rememberHaptic
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.isAppDarkTheme
import com.example.ui.viewmodel.AttendanceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun WhatIfPredictorScreen(
    viewModel: AttendanceViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = rememberHaptic()
    val isDark = isAppDarkTheme()

    val profile by viewModel.profile.collectAsState()
    val currentVersion by viewModel.currentVersion.collectAsState()
    val allAttendanceRecords by viewModel.allAttendanceRecords.collectAsState()
    val allOverrides by viewModel.allOverrides.collectAsState()
    val overallStats by viewModel.overallStats.collectAsState()
    val allSlots by viewModel.allSlotsForCurrentVersion.collectAsState()

    var currentStep by remember { mutableStateOf(WhatIfScreenStep.INTRO) }

    // Planning state
    var selectedDates by remember { mutableStateOf<Set<String>>(emptySet()) }
    var planningMode by remember { mutableStateOf(WhatIfPlanningMode.WHOLE_DAY) }
    val dayKinds = remember { mutableStateMapOf<String, String>() } // date -> "Leave" or "Bunk"
    val dayPicks = remember { mutableStateMapOf<String, Set<Long>>() } // date -> set of missed slotIds
    var activeDateForClasses by remember { mutableStateOf<String?>(null) }

    // Saved scenarios
    var savedScenarios by remember { mutableStateOf(WhatIfStorage.loadScenarios(context)) }
    var activeScenarioId by remember { mutableStateOf<String?>(null) }
    var showSaveDialog by remember { mutableStateOf(false) }

    // Prediction results
    var predictionResult by remember { mutableStateOf<WhatIfPredictionResult?>(null) }

    // Helper functions
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayStr = remember { dateFormat.format(Date()) }

    fun getDayOfWeek(dateStr: String): String {
        return try {
            val d = dateFormat.parse(dateStr) ?: Date()
            val cal = Calendar.getInstance().apply { time = d }
            when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "MONDAY"
                Calendar.TUESDAY -> "TUESDAY"
                Calendar.WEDNESDAY -> "WEDNESDAY"
                Calendar.THURSDAY -> "THURSDAY"
                Calendar.FRIDAY -> "FRIDAY"
                Calendar.SATURDAY -> "SATURDAY"
                Calendar.SUNDAY -> "SUNDAY"
                else -> "MONDAY"
            }
        } catch (_: Exception) {
            "MONDAY"
        }
    }

    fun isHoliday(dateStr: String): String? {
        val ov = allOverrides.find { it.date == dateStr && it.isHoliday }
        if (ov != null) return ov.holidayName.ifBlank { "Institute Holiday" }
        return AktuHolidays.getHolidayName(dateStr)
    }

    fun getClassesForDate(dateStr: String): List<WhatIfClassItem> {
        val holiday = isHoliday(dateStr)
        if (holiday != null) return emptyList()

        val dayOfWeek = getDayOfWeek(dateStr)
        val cal = Calendar.getInstance().apply {
            time = dateFormat.parse(dateStr) ?: Date()
        }
        val dayNum = cal.get(Calendar.DAY_OF_WEEK)
        if (dayNum == Calendar.SATURDAY || dayNum == Calendar.SUNDAY) return emptyList()

        val vId = currentVersion?.versionId ?: 1
        val dbSlots = if (allSlots.isNotEmpty()) {
            allSlots.filter { it.dayOfWeek == dayOfWeek }
        } else {
            InitialTimetableData.getDefaultSlots(vId).filter { it.dayOfWeek == dayOfWeek }
        }
        val userBatch = profile.batch

        val filtered = dbSlots.filter { slot ->
            slot.batch == "ALL" || slot.batch.equals(userBatch, ignoreCase = true)
        }

        val category = profile.academicCategory.trim()
        val result = mutableListOf<WhatIfClassItem>()

        for (slot in filtered) {
            // Requirement: Exclude doubt clearing sessions from classes (no attendance count)
            val isDoubt = slot.subjectCode.equals("DOUBT", ignoreCase = true) ||
                          slot.subjectName.contains("Doubt", ignoreCase = true) ||
                          slot.classType.equals(ClassType.DOUBT_CLEARING.name, ignoreCase = true)
            if (isDoubt || AttendanceCalculationEngine.isExcludedFromAttendance(slot.subjectCode, slot.subjectName, slot.classType)) continue

            val isLibrary = slot.classType.equals(ClassType.LIBRARY.name, ignoreCase = true) ||
                            slot.subjectCode.equals("LIB", ignoreCase = true) ||
                            slot.subjectCode.equals("DLIB", ignoreCase = true) ||
                            slot.subjectName.contains("Library", ignoreCase = true)
            val isSwachh = slot.subjectCode.equals("SWACH", ignoreCase = true) ||
                           slot.subjectName.contains("Swachh", ignoreCase = true)

            // Requirement: Swachhata Abhiyan and Library are always marked Present
            var always = isLibrary || isSwachh

            val isElective = slot.subjectCode.startsWith("HTFT") ||
                             slot.subjectCode.startsWith("MNPM") ||
                             slot.subjectName.contains("Comfort", ignoreCase = true) ||
                             slot.subjectName.contains("Planning", ignoreCase = true)

            var resolvedCode = slot.subjectCode
            var resolvedName = slot.subjectName
            var resolvedType = slot.classType

            if (isElective) {
                when {
                    category.equals("Minor", ignoreCase = true) -> {
                        resolvedCode = if (slot.classType == ClassType.TUTORIAL.name) "MNPM501-T" else "MNPM501"
                        resolvedName = "Production Planning & Control"
                        resolvedType = slot.classType
                    }
                    category.equals("Library", ignoreCase = true) -> {
                        resolvedCode = "DLIB"
                        resolvedName = "Digital Library"
                        resolvedType = ClassType.LIBRARY.name
                        always = true
                    }
                    else -> {
                        resolvedCode = if (slot.classType == ClassType.TUTORIAL.name) "HTFT501-T" else "HTFT501"
                        resolvedName = "Clothing Comfort"
                        resolvedType = slot.classType
                    }
                }
            }

            // Check if cancelled on this date (not applicable for regular future slots unless holiday)
            val isCancelled = false

            result.add(
                WhatIfClassItem(
                    slotId = slot.id,
                    period = slot.period,
                    periodRoman = toRoman(slot.period),
                    startTime = slot.startTime,
                    endTime = slot.endTime,
                    subjectCode = resolvedCode,
                    subjectName = resolvedName,
                    classType = resolvedType,
                    isAlwaysPresent = always,
                    isCancelled = isCancelled,
                    accentColorHex = getSubjectColor(resolvedCode)
                )
            )
        }
        return mergeWhatIfLabBlocks(result.sortedBy { it.period })
    }

    fun isSelectableDate(dateStr: String): Boolean {
        if (dateStr < todayStr) return false
        if (isHoliday(dateStr) != null) return false
        val classes = getClassesForDate(dateStr)
        return classes.isNotEmpty()
    }

    fun computePrediction(): WhatIfPredictionResult {
        // All figures are weighted and come from the shared AttendanceCalculationEngine rules:
        // theory = 1, lab = 2, auto-credited sessions attended = conducted, tutorials fold into their lecture.
        class SubjTally(
            val code: String,
            val name: String,
            var p: Double,
            var n: Double,
            var p2: Double,
            var n2: Double,
            var plannedMiss: Int = 0,
            var plannedTotal: Int = 0
        )

        val subjMap = mutableMapOf<String, SubjTally>()
        fun tally(code: String, name: String) = subjMap.getOrPut(AttendanceCalculationEngine.canonicalSubjectCode(code)) {
            SubjTally(code = AttendanceCalculationEngine.canonicalSubjectCode(code), name = name, p = 0.0, n = 0.0, p2 = 0.0, n2 = 0.0)
        }

        // 1. Current standing: one engine pass over the real records (already merges tutorials, applies weights)
        for (ss in overallStats.subjectStats) {
            if (AttendanceCalculationEngine.isExcludedFromAttendance(ss.subjectCode, ss.subjectName)) continue
            val t = tally(ss.subjectCode, ss.subjectName)
            t.p += ss.weightedPresent
            t.n += ss.weightedConducted
            t.p2 += ss.weightedPresent
            t.n2 += ss.weightedConducted
        }

        val sortedDates = selectedDates.sorted()
        val dayPlansList = mutableListOf<WhatIfDayPlan>()
        var totalMissed = 0

        fun weightOf(c: WhatIfClassItem): Double =
            AttendanceCalculationEngine.resolveSessionWeight(c.subjectCode, c.subjectName, c.classType).toDouble() * c.periodCount

        for (d in sortedDates) {
            val classes = getClassesForDate(d)
            val eligible = classes.filter { !it.isAlwaysPresent && !it.isCancelled }
            val alwaysClasses = classes.filter { it.isAlwaysPresent && !it.isCancelled }

            // Auto-credited sessions (Library / Seminar / Swachhta): attended and conducted rise together
            for (c in alwaysClasses) {
                val w = weightOf(c)
                if (w <= 0.0) continue
                val entry = tally(c.subjectCode, c.subjectName)
                entry.p2 += w
                entry.n2 += w
                entry.plannedTotal += c.periodCount
            }

            val missedSet = if (planningMode == WhatIfPlanningMode.WHOLE_DAY) {
                eligible.map { it.slotId }.toSet()
            } else {
                dayPicks[d]?.filter { id -> eligible.any { it.slotId == id } }?.toSet() ?: eligible.map { it.slotId }.toSet()
            }

            for (c in eligible) {
                val w = weightOf(c)
                if (w <= 0.0) continue // doubt clearing etc. never counts
                val entry = tally(c.subjectCode, c.subjectName)
                entry.n2 += w
                entry.plannedTotal += c.periodCount
                if (c.slotId in missedSet) {
                    entry.plannedMiss += c.periodCount
                    totalMissed += c.periodCount
                } else {
                    entry.p2 += w
                }
            }

            val cal = Calendar.getInstance().apply { time = dateFormat.parse(d) ?: Date() }
            val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(cal.time)
            dayPlansList.add(
                WhatIfDayPlan(
                    dateStr = d,
                    dayOfWeek = dayName,
                    eligibleClasses = classes,
                    missedSlotIds = missedSet,
                    kind = dayKinds[d] ?: "Leave"
                )
            )
        }

        // Overall = combined weighted totals (never an average of subject percentages)
        val curTotalP = subjMap.values.sumOf { it.p }
        val curTotalN = subjMap.values.sumOf { it.n }
        val curPct = AttendanceCalculationEngine.calculatePercentage(curTotalP, curTotalN).toFloat()

        val projTotalP = subjMap.values.sumOf { it.p2 }
        val projTotalN = subjMap.values.sumOf { it.n2 }
        val projPct = if (projTotalN > 0.0) {
            AttendanceCalculationEngine.calculatePercentage(projTotalP, projTotalN).toFloat()
        } else curPct

        val impacts = subjMap.values.map {
            WhatIfSubjectImpact(
                subjectCode = it.code,
                subjectName = it.name,
                currentAttended = it.p,
                currentConducted = it.n,
                projectedAttended = it.p2,
                projectedConducted = it.n2,
                plannedMissed = it.plannedMiss,
                plannedTotal = it.plannedTotal
            )
        }.sortedByDescending { it.plannedMissed }

        return WhatIfPredictionResult(
            currentOverallPct = curPct,
            projectedOverallPct = projPct,
            targetPct = profile.targetAttendancePct,
            totalMissedClasses = totalMissed,
            dayPlans = dayPlansList,
            subjectImpacts = impacts
        )
    }

    // Back handling
    BackHandler {
        when (currentStep) {
            WhatIfScreenStep.INTRO -> onNavigateBack()
            WhatIfScreenStep.PLAN_CALENDAR -> currentStep = WhatIfScreenStep.INTRO
            WhatIfScreenStep.SAVED_SCENARIOS -> currentStep = WhatIfScreenStep.INTRO
            WhatIfScreenStep.SELECT_CLASSES -> currentStep = WhatIfScreenStep.PLAN_CALENDAR
            WhatIfScreenStep.RESULTS -> currentStep = if (planningMode == WhatIfPlanningMode.SELECTED_CLASSES) {
                WhatIfScreenStep.SELECT_CLASSES
            } else {
                WhatIfScreenStep.PLAN_CALENDAR
            }
            WhatIfScreenStep.BREAKDOWN -> currentStep = WhatIfScreenStep.RESULTS
        }
    }

    // Main layout
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                val forward = targetState.ordinal >= initialState.ordinal
                (slideInHorizontally(
                    initialOffsetX = { w -> if (forward) w / 3 else -w / 3 },
                    animationSpec = tween(380, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(260))) togetherWith
                (slideOutHorizontally(
                    targetOffsetX = { w -> if (forward) -w / 3 else w / 3 },
                    animationSpec = tween(380, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(200)))
            },
            label = "whatif_step_transition"
        ) { step ->
            when (step) {
                WhatIfScreenStep.INTRO -> {
                    WhatIfIntroScreen(
                        currentOverallPct = overallStats.percentage,
                        targetPct = profile.targetAttendancePct,
                        savedCount = savedScenarios.size,
                        isDark = isDark,
                        onBack = onNavigateBack,
                        onStartPlanning = {
                            haptic.select()
                            currentStep = WhatIfScreenStep.PLAN_CALENDAR
                        },
                        onViewSaved = {
                            haptic.select()
                            currentStep = WhatIfScreenStep.SAVED_SCENARIOS
                        }
                    )
                }

                WhatIfScreenStep.PLAN_CALENDAR -> {
                    WhatIfCalendarScreen(
                        selectedDates = selectedDates,
                        planningMode = planningMode,
                        isDark = isDark,
                        isSelectableDate = { isSelectableDate(it) },
                        getHolidayName = { isHoliday(it) },
                        onDateToggled = { dateStr ->
                            haptic.tap()
                            val next = selectedDates.toMutableSet()
                            if (next.contains(dateStr)) {
                                next.remove(dateStr)
                                dayPicks.remove(dateStr)
                                dayKinds.remove(dateStr)
                            } else {
                                next.add(dateStr)
                                val eligible = getClassesForDate(dateStr).filter { !it.isAlwaysPresent && !it.isCancelled }
                                dayPicks[dateStr] = eligible.map { it.slotId }.toSet()
                                dayKinds[dateStr] = "Leave"
                            }
                            selectedDates = next
                        },
                        onModeSelected = { mode ->
                            haptic.select()
                            planningMode = mode
                        },
                        onBack = {
                            currentStep = WhatIfScreenStep.INTRO
                        },
                        onNext = {
                            haptic.select()
                            if (planningMode == WhatIfPlanningMode.SELECTED_CLASSES) {
                                activeDateForClasses = selectedDates.sorted().firstOrNull()
                                currentStep = WhatIfScreenStep.SELECT_CLASSES
                            } else {
                                predictionResult = computePrediction()
                                currentStep = WhatIfScreenStep.RESULTS
                            }
                        },
                        onOpenSaved = {
                            currentStep = WhatIfScreenStep.SAVED_SCENARIOS
                        }
                    )
                }

                WhatIfScreenStep.SELECT_CLASSES -> {
                    WhatIfSelectClassesScreen(
                        selectedDates = selectedDates.sorted(),
                        activeDate = activeDateForClasses ?: selectedDates.sorted().firstOrNull() ?: todayStr,
                        dayPicks = dayPicks,
                        planningMode = planningMode,
                        isDark = isDark,
                        getClassesForDate = { getClassesForDate(it) },
                        onSelectActiveDate = { activeDateForClasses = it },
                        onToggleClass = { dateStr, slotId ->
                            haptic.tap()
                            val current = dayPicks[dateStr]?.toMutableSet() ?: mutableSetOf()
                            if (current.contains(slotId)) {
                                current.remove(slotId)
                            } else {
                                current.add(slotId)
                            }
                            dayPicks[dateStr] = current
                        },
                        onModeChanged = { mode ->
                            haptic.select()
                            planningMode = mode
                        },
                        onBack = {
                            currentStep = WhatIfScreenStep.PLAN_CALENDAR
                        },
                        onCalculate = {
                            haptic.success()
                            predictionResult = computePrediction()
                            currentStep = WhatIfScreenStep.RESULTS
                        }
                    )
                }

                WhatIfScreenStep.RESULTS -> {
                    val result = predictionResult ?: computePrediction().also { predictionResult = it }
                    WhatIfResultsScreen(
                        result = result,
                        isDark = isDark,
                        onBack = {
                            currentStep = if (planningMode == WhatIfPlanningMode.SELECTED_CLASSES) {
                                WhatIfScreenStep.SELECT_CLASSES
                            } else {
                                WhatIfScreenStep.PLAN_CALENDAR
                            }
                        },
                        onEditPlan = {
                            currentStep = WhatIfScreenStep.PLAN_CALENDAR
                        },
                        onToggleKind = { dateStr ->
                            haptic.tap()
                            val cur = dayKinds[dateStr] ?: "Leave"
                            val next = if (cur == "Leave") "Bunk" else "Leave"
                            dayKinds[dateStr] = next
                            predictionResult = computePrediction()
                        },
                        onViewDetails = {
                            haptic.select()
                            currentStep = WhatIfScreenStep.BREAKDOWN
                        },
                        onSaveScenario = {
                            showSaveDialog = true
                        }
                    )
                }

                WhatIfScreenStep.BREAKDOWN -> {
                    val result = predictionResult ?: computePrediction().also { predictionResult = it }
                    WhatIfBreakdownScreen(
                        result = result,
                        isDark = isDark,
                        onBack = { currentStep = WhatIfScreenStep.RESULTS },
                        onEditPlan = { currentStep = WhatIfScreenStep.PLAN_CALENDAR },
                        onSaveScenario = { showSaveDialog = true }
                    )
                }

                WhatIfScreenStep.SAVED_SCENARIOS -> {
                    WhatIfSavedScenariosScreen(
                        scenarios = savedScenarios,
                        targetPct = profile.targetAttendancePct,
                        isDark = isDark,
                        onBack = { currentStep = WhatIfScreenStep.PLAN_CALENDAR },
                        onNewPlan = {
                            selectedDates = emptySet()
                            dayPicks.clear()
                            dayKinds.clear()
                            currentStep = WhatIfScreenStep.PLAN_CALENDAR
                        },
                        onOpenScenario = { scenario ->
                            haptic.select()
                            activeScenarioId = scenario.id
                            selectedDates = scenario.dates.toSet()
                            planningMode = scenario.mode
                            dayKinds.clear()
                            dayKinds.putAll(scenario.dayKinds)
                            dayPicks.clear()
                            scenario.dayPicks.forEach { (k, v) -> dayPicks[k] = v.toSet() }
                            predictionResult = computePrediction()
                            currentStep = WhatIfScreenStep.RESULTS
                        },
                        onDuplicateScenario = { scenario ->
                            haptic.success()
                            val copy = scenario.copy(
                                id = java.util.UUID.randomUUID().toString(),
                                name = "${scenario.name} (Copy)"
                            )
                            val next = listOf(copy) + savedScenarios
                            savedScenarios = next
                            WhatIfStorage.saveScenarios(context, next)
                        },
                        onDeleteScenario = { scenario ->
                            haptic.warn()
                            val next = savedScenarios.filter { it.id != scenario.id }
                            savedScenarios = next
                            WhatIfStorage.saveScenarios(context, next)
                        }
                    )
                }
            }
        }
    }

    // Save Scenario Dialog
    if (showSaveDialog && predictionResult != null) {
        val result = predictionResult!!
        var scenarioName by remember {
            val dateText = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
            mutableStateOf("Leave Plan – $dateText")
        }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = "Save Scenario",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = GlassTokens.textColor(isDark)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Save this prediction to view or compare it later.",
                        fontSize = 12.5.sp,
                        color = GlassTokens.mutedColor(isDark)
                    )
                    OutlinedTextField(
                        value = scenarioName,
                        onValueChange = { scenarioName = it },
                        label = { Text("Scenario Name") },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Predicted attendance:", fontSize = 12.sp, color = GlassTokens.mutedColor(isDark))
                        Text(
                            text = "${result.projectedOverallPct.roundToOneDecimal()}%",
                            fontWeight = FontWeight.Bold,
                            color = GlassTokens.textColor(isDark),
                            fontSize = 13.sp
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Classes missed:", fontSize = 12.sp, color = GlassTokens.mutedColor(isDark))
                        Text(
                            text = "${result.totalMissedClasses}",
                            fontWeight = FontWeight.Bold,
                            color = GlassTokens.textColor(isDark),
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val datesSorted = selectedDates.sorted()
                        val rangeText = if (datesSorted.size > 1) {
                            "${formatShortDate(datesSorted.first())} – ${formatShortDate(datesSorted.last())}"
                        } else if (datesSorted.size == 1) {
                            formatShortDate(datesSorted.first())
                        } else {
                            "No dates"
                        }

                        val uniqueKinds = dayKinds.values.toSet()
                        val kindSum = if (uniqueKinds.size == 1) uniqueKinds.first() else "Mixed"

                        val scenario = WhatIfSavedScenario(
                            id = activeScenarioId ?: java.util.UUID.randomUUID().toString(),
                            name = scenarioName.trim().ifEmpty { "Leave Plan" },
                            dateRangeText = rangeText,
                            dates = datesSorted,
                            mode = planningMode,
                            missedCount = result.totalMissedClasses,
                            predictedPct = result.projectedOverallPct,
                            targetPct = result.targetPct,
                            kindSummary = kindSum,
                            dayKinds = dayKinds.toMap(),
                            dayPicks = dayPicks.mapValues { it.value.toList() }
                        )

                        val next = listOf(scenario) + savedScenarios.filter { it.id != scenario.id }
                        savedScenarios = next
                        WhatIfStorage.saveScenarios(context, next)
                        showSaveDialog = false
                        haptic.success()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassTokens.PrimaryGradient)
                ) {
                    Text("Save Scenario", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSaveDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

// -------------------------------------------------------------
// Sub-screen 1: Intro Screen
// -------------------------------------------------------------
@Composable
fun WhatIfIntroScreen(
    currentOverallPct: Float,
    targetPct: Int,
    savedCount: Int,
    isDark: Boolean,
    onBack: () -> Unit,
    onStartPlanning: () -> Unit,
    onViewSaved: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Bar
        item {
            WhatIfTopBar(
                title = "What-If Predictor",
                onBack = onBack,
                isDark = isDark,
                trailingIcon = Icons.Default.History,
                trailingAction = onViewSaved
            )
        }

        // Hero text
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Plan Smart. Stay on Track.",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GlassTokens.textColor(isDark),
                    letterSpacing = (-0.4).sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Predict your future attendance by planning leaves, bunk days or holidays. See how it impacts your overall attendance and make better decisions.",
                    fontSize = 13.sp,
                    color = GlassTokens.mutedColor(isDark),
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Visual Art Banner
        item {
            GlassCard(isDark = isDark, contentPadding = PaddingValues(16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = Color(0x334A8DFF),
                            border = BorderStroke(1.5.dp, Color(0x807AA2FF)),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ShowChart,
                                    contentDescription = null,
                                    tint = Color(0xFF6F9BFF),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "AI-Driven Attendance Simulator",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            }
        }

        // Current Overall Card + Meter
        item {
            GlassCard(isDark = isDark, contentPadding = PaddingValues(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Current overall attendance",
                                fontSize = 12.5.sp,
                                color = GlassTokens.mutedColor(isDark),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${currentOverallPct.roundToOneDecimal()}%",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GlassTokens.textColor(isDark),
                                letterSpacing = (-0.5).sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val isAbove = currentOverallPct >= targetPct
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = if (isAbove) Color(0x2614D4A0) else Color(0x26FF5B86),
                                border = BorderStroke(1.5.dp, if (isAbove) Color(0xFF14D4A0) else Color(0xFFFF5B86)),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isAbove) "Above target" else "Below target",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAbove) Color(0xFF14D4A0) else Color(0xFFFF5B86)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Target $targetPct%",
                                fontSize = 11.5.sp,
                                color = GlassTokens.mutedColor(isDark),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Meter bar with target notch
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(GlassTokens.chipColor(isDark))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = (currentOverallPct / 100f).coerceIn(0f, 1f))
                                .height(10.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF2F6BFF), Color(0xFF8B5CF6), Color(0xFF14D4A0))
                                    )
                                )
                        )
                    }
                }
            }
        }

        // Features list
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                WhatIfFeatureRow(
                    icon = Icons.Default.CalendarMonth,
                    title = "Plan multiple days",
                    subtitle = "Select your leave/bunk days from calendar",
                    isDark = isDark
                )
                WhatIfFeatureRow(
                    icon = Icons.Default.FormatListBulleted,
                    title = "Whole day or selected classes",
                    subtitle = "Choose what fits your plan",
                    isDark = isDark
                )
                WhatIfFeatureRow(
                    icon = Icons.Default.ShowChart,
                    title = "See accurate results",
                    subtitle = "Get predicted attendance & detailed summary",
                    isDark = isDark
                )
            }
        }

        // Disclaimer info
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = GlassTokens.mutedColor(isDark),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Predictions never change your real attendance records.",
                    fontSize = 11.5.sp,
                    color = GlassTokens.mutedColor(isDark),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onStartPlanning,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassTokens.PrimaryGradient),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text(
                        text = "Start Planning",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = onViewSaved,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Color(0x804F8DFF))
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = GlassTokens.textColor(isDark),
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Saved scenarios ($savedCount)",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassTokens.textColor(isDark)
                    )
                }
            }
        }
    }
}

@Composable
private fun WhatIfFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDark: Boolean
) {
    GlassCard(isDark = isDark, contentPadding = PaddingValues(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0x334A8DFF),
                border = BorderStroke(1.dp, Color(0x667AA2FF)),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF7FA6FF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = GlassTokens.textColor(isDark)
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = GlassTokens.mutedColor(isDark),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Sub-screen 2: Calendar Planning Screen
// -------------------------------------------------------------
@Composable
fun WhatIfCalendarScreen(
    selectedDates: Set<String>,
    planningMode: WhatIfPlanningMode,
    isDark: Boolean,
    isSelectableDate: (String) -> Boolean,
    getHolidayName: (String) -> String?,
    onDateToggled: (String) -> Unit,
    onModeSelected: (WhatIfPlanningMode) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onOpenSaved: () -> Unit
) {
    val cal = remember { Calendar.getInstance() }
    var displayedYear by remember { mutableStateOf(cal.get(Calendar.YEAR)) }
    var displayedMonth by remember { mutableStateOf(cal.get(Calendar.MONTH)) } // 0..11

    val monthNames = remember {
        listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Bar
        item {
            WhatIfTopBar(
                title = "What-If Predictor",
                onBack = onBack,
                isDark = isDark,
                trailingIcon = Icons.Default.History,
                trailingAction = onOpenSaved
            )
        }

        // Segments: Plan | Past Scenarios
        item {
            WhatIfSegmentedTab(
                selectedTab = 0,
                tabs = listOf("Plan", "Past Scenarios"),
                isDark = isDark,
                onTabSelected = { idx ->
                    if (idx == 1) onOpenSaved()
                }
            )
        }

        // Section Title
        item {
            Column {
                Text(
                    text = "Select Dates",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GlassTokens.textColor(isDark)
                )
                Text(
                    text = "Choose the days you want to plan for",
                    fontSize = 12.5.sp,
                    color = GlassTokens.mutedColor(isDark),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Calendar Card
        item {
            GlassCard(isDark = isDark, contentPadding = PaddingValues(14.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Month Navigation Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GlassTokens.chipColor(isDark),
                            border = BorderStroke(1.5.dp, GlassTokens.chipBorderColor(isDark)),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable {
                                    if (displayedMonth == 0) {
                                        displayedMonth = 11
                                        displayedYear -= 1
                                    } else {
                                        displayedMonth -= 1
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "Previous Month",
                                    tint = Color(0xFF6F9BFF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Text(
                            text = "${monthNames[displayedMonth]} $displayedYear",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            color = GlassTokens.textColor(isDark)
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GlassTokens.chipColor(isDark),
                            border = BorderStroke(1.5.dp, GlassTokens.chipBorderColor(isDark)),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable {
                                    if (displayedMonth == 11) {
                                        displayedMonth = 0
                                        displayedYear += 1
                                    } else {
                                        displayedMonth += 1
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Next Month",
                                    tint = Color(0xFF6F9BFF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Weekdays row
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlassTokens.mutedColor(isDark)
                            )
                        }
                    }

                    // Days Grid
                    val daysInMonth = remember(displayedYear, displayedMonth) {
                        val c = Calendar.getInstance().apply {
                            set(Calendar.YEAR, displayedYear)
                            set(Calendar.MONTH, displayedMonth)
                            set(Calendar.DAY_OF_MONTH, 1)
                        }
                        val firstDayOfWeek = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Mon = 0
                        val maxDays = c.getActualMaximum(Calendar.DAY_OF_MONTH)
                        Pair(firstDayOfWeek, maxDays)
                    }

                    val (startOffset, maxDays) = daysInMonth
                    val totalCells = ((startOffset + maxDays + 6) / 7) * 7

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (row in 0 until (totalCells / 7)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (col in 0 until 7) {
                                    val cellIndex = row * 7 + col
                                    val dayNum = cellIndex - startOffset + 1
                                    if (dayNum in 1..maxDays) {
                                        val dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", displayedYear, displayedMonth + 1, dayNum)
                                        val isSelected = selectedDates.contains(dateStr)
                                        val isSelectable = isSelectableDate(dateStr)
                                        val holidayName = getHolidayName(dateStr)
                                        val isHoliday = holidayName != null

                                        val (bgBrush, textColor, borderColor) = when {
                                            isSelected -> Triple(
                                                Brush.linearGradient(listOf(Color(0xFF17D6A4), Color(0xFF0A9F8D))),
                                                Color.White,
                                                Color.Transparent
                                            )
                                            isHoliday -> Triple(
                                                Brush.linearGradient(listOf(Color(0x33FF3D71), Color(0x33FF3D71))),
                                                Color(0xFFFF5B86),
                                                Color(0x80FF5B86)
                                            )
                                            isSelectable -> Triple(
                                                Brush.linearGradient(listOf(Color(0x1A5082FF), Color(0x1A5082FF))),
                                                GlassTokens.textColor(isDark),
                                                Color(0x335082FF)
                                            )
                                            else -> Triple(
                                                Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                                                GlassTokens.mutedColor(isDark).copy(alpha = 0.42f),
                                                Color.Transparent
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(CircleShape)
                                                .background(bgBrush)
                                                .border(1.2.dp, borderColor, CircleShape)
                                                .clickable(enabled = isSelectable) {
                                                    onDateToggled(dateStr)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$dayNum",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                color = textColor
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                    }
                                }
                            }
                        }
                    }

                    // Legend
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        LegendItem(color = Color(0xFF17D6A4), label = "Selected", isDark = isDark)
                        LegendItem(color = Color(0xFFFF5B86), label = "Holiday", isDark = isDark)
                        LegendItem(color = Color(0xFF6A7FB8), label = "Not Selectable", isDark = isDark)
                    }
                }
            }
        }

        // Plan Type Selection
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Plan Type",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GlassTokens.textColor(isDark)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PlanTypeCard(
                        title = "Whole Day",
                        subtitle = "Mark all classes as leave/bunk",
                        icon = Icons.Default.WbSunny,
                        isSelected = planningMode == WhatIfPlanningMode.WHOLE_DAY,
                        isDark = isDark,
                        onClick = { onModeSelected(WhatIfPlanningMode.WHOLE_DAY) },
                        modifier = Modifier.weight(1f)
                    )

                    PlanTypeCard(
                        title = "Selected Classes",
                        subtitle = "Choose specific classes only",
                        icon = Icons.Default.FormatListBulleted,
                        isSelected = planningMode == WhatIfPlanningMode.SELECTED_CLASSES,
                        isDark = isDark,
                        onClick = { onModeSelected(WhatIfPlanningMode.SELECTED_CLASSES) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Next button dock
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (selectedDates.isNotEmpty()) "${selectedDates.size} date${if (selectedDates.size > 1) "s" else ""} selected" else "Pick at least one date",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GlassTokens.mutedColor(isDark)
                )

                Button(
                    onClick = onNext,
                    enabled = selectedDates.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (selectedDates.isNotEmpty()) GlassTokens.PrimaryGradient else Brush.linearGradient(listOf(Color.Gray, Color.Gray))
                        ),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text(
                        text = "Next",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanTypeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) Color(0xFF6AA6FF) else GlassTokens.borderColor(isDark)
    val bgBrush = if (isSelected) {
        Brush.linearGradient(listOf(Color(0x663C78FF), Color(0x592832A0)))
    } else {
        GlassTokens.surfaceBrush(isDark)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        border = BorderStroke(1.5.dp, borderColor),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .background(bgBrush)
                .padding(14.dp)
        ) {
            Column {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color(0xFF9DBBFF) else GlassTokens.mutedColor(isDark),
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GlassTokens.textColor(isDark)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = GlassTokens.mutedColor(isDark),
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, isDark: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 11.5.sp,
            color = GlassTokens.mutedColor(isDark),
            fontWeight = FontWeight.SemiBold
        )
    }
}

// -------------------------------------------------------------
// Sub-screen 3: Select Classes Screen
// -------------------------------------------------------------
@Composable
fun WhatIfSelectClassesScreen(
    selectedDates: List<String>,
    activeDate: String,
    dayPicks: Map<String, Set<Long>>,
    planningMode: WhatIfPlanningMode,
    isDark: Boolean,
    getClassesForDate: (String) -> List<WhatIfClassItem>,
    onSelectActiveDate: (String) -> Unit,
    onToggleClass: (String, Long) -> Unit,
    onModeChanged: (WhatIfPlanningMode) -> Unit,
    onBack: () -> Unit,
    onCalculate: () -> Unit
) {
    val activeClasses = remember(activeDate) { getClassesForDate(activeDate) }
    val activeMissedSlotIds = dayPicks[activeDate] ?: remember(activeClasses) {
        activeClasses.filter { !it.isAlwaysPresent && !it.isCancelled }.map { it.slotId }.toSet()
    }

    val totalIncludedMissed = selectedDates.sumOf { d ->
        val eligible = getClassesForDate(d).filter { !it.isAlwaysPresent && !it.isCancelled }
        if (planningMode == WhatIfPlanningMode.WHOLE_DAY) {
            eligible.sumOf { it.periodCount }
        } else {
            val picks = dayPicks[d] ?: eligible.map { it.slotId }.toSet()
            eligible.filter { it.slotId in picks }.sumOf { it.periodCount }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            WhatIfTopBar(
                title = "What-If Predictor",
                onBack = onBack,
                isDark = isDark
            )
        }

        item {
            Column {
                Text(
                    text = "Select Classes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GlassTokens.textColor(isDark)
                )
                Text(
                    text = "Choose the classes you want to plan for the selected days.",
                    fontSize = 12.5.sp,
                    color = GlassTokens.mutedColor(isDark)
                )
            }
        }

        // Horizontal Date Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(selectedDates) { d ->
                    val isActive = d == activeDate
                    val cal = Calendar.getInstance().apply {
                        time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(d) ?: Date()
                    }
                    val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
                    val dateFormatted = SimpleDateFormat("dd MMM", Locale.getDefault()).format(cal.time)

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.5.dp, if (isActive) Color(0xFF14D4A0) else GlassTokens.borderColor(isDark)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectActiveDate(d) }
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isActive) Brush.linearGradient(listOf(Color(0x331EDCA4), Color(0x330B5078)))
                                    else GlassTokens.surfaceBrush(isDark)
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dateFormatted,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = GlassTokens.textColor(isDark)
                                )
                                Text(
                                    text = dayName,
                                    fontSize = 11.sp,
                                    color = GlassTokens.mutedColor(isDark),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Segmented toggle: All Classes | Selected Classes
        item {
            WhatIfSegmentedTab(
                selectedTab = if (planningMode == WhatIfPlanningMode.WHOLE_DAY) 0 else 1,
                tabs = listOf("All Classes", "Selected Classes"),
                isDark = isDark,
                onTabSelected = { idx ->
                    onModeChanged(if (idx == 0) WhatIfPlanningMode.WHOLE_DAY else WhatIfPlanningMode.SELECTED_CLASSES)
                }
            )
        }

        // Class items list for active date
        if (activeClasses.isEmpty()) {
            item {
                GlassCard(isDark = isDark) {
                    Text(
                        text = "No scheduled classes on this date.",
                        fontSize = 13.sp,
                        color = GlassTokens.mutedColor(isDark)
                    )
                }
            }
        } else {
            items(activeClasses, key = { "${activeDate}_${it.slotId}_${it.period}" }) { session ->
                val isExcluded = session.isAlwaysPresent || session.isCancelled
                val isChecked = !isExcluded && (planningMode == WhatIfPlanningMode.WHOLE_DAY || activeMissedSlotIds.contains(session.slotId))

                ClassSelectionCard(
                    item = session,
                    isChecked = isChecked,
                    isExcluded = isExcluded,
                    isDark = isDark,
                    onToggle = {
                        if (!isExcluded) {
                            if (planningMode == WhatIfPlanningMode.WHOLE_DAY) {
                                onModeChanged(WhatIfPlanningMode.SELECTED_CLASSES)
                            }
                            onToggleClass(activeDate, session.slotId)
                        }
                    }
                )
            }
        }

        // Summary footer & Calculate Button
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${selectedDates.size} dates",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassTokens.textColor(isDark)
                    )
                    Text(
                        text = "$totalIncludedMissed classes included",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = if (planningMode == WhatIfPlanningMode.WHOLE_DAY) "Whole day" else "Selected classes",
                        fontSize = 12.sp,
                        color = GlassTokens.mutedColor(isDark)
                    )
                }

                Button(
                    onClick = onCalculate,
                    enabled = totalIncludedMissed > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (totalIncludedMissed > 0) GlassTokens.PrimaryGradient else Brush.linearGradient(listOf(Color.Gray, Color.Gray))
                        ),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text(
                        text = "Calculate Prediction",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassSelectionCard(
    item: WhatIfClassItem,
    isChecked: Boolean,
    isExcluded: Boolean,
    isDark: Boolean,
    onToggle: () -> Unit
) {
    val borderColor = if (isChecked) Color(0xFF2EE6B0) else GlassTokens.borderColor(isDark)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        border = BorderStroke(1.5.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = !isExcluded) { onToggle() }
    ) {
        Box(
            modifier = Modifier
                .background(GlassTokens.surfaceBrush(isDark))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox Box
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(
                            if (isChecked) Brush.linearGradient(listOf(Color(0xFF17D6A4), Color(0xFF0A9F8D)))
                            else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .border(
                            1.5.dp,
                            if (isChecked) Color.Transparent else GlassTokens.chipBorderColor(isDark),
                            RoundedCornerShape(9.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isExcluded) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = GlassTokens.mutedColor(isDark),
                            modifier = Modifier.size(15.dp)
                        )
                    } else if (isChecked) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (item.isAlwaysPresent) "${item.subjectName} (Always Present)" else "${item.subjectCode} – ${item.subjectName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = GlassTokens.textColor(isDark),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.periodRoman} • ${item.startTime} - ${item.endTime}",
                        fontSize = 11.5.sp,
                        color = GlassTokens.mutedColor(isDark)
                    )
                    if (item.isAlwaysPresent) {
                        Text(
                            text = "Always present • excluded from leave calculation",
                            fontSize = 10.5.sp,
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.SemiBold
                        )
                    } else if (item.isCancelled) {
                        Text(
                            text = "Cancelled • excluded",
                            fontSize = 10.5.sp,
                            color = Color(0xFFA78BFA),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Sub-screen 4: Results Screen
// -------------------------------------------------------------
@Composable
fun WhatIfResultsScreen(
    result: WhatIfPredictionResult,
    isDark: Boolean,
    onBack: () -> Unit,
    onEditPlan: () -> Unit,
    onToggleKind: (String) -> Unit,
    onViewDetails: () -> Unit,
    onSaveScenario: () -> Unit
) {
    val delta = result.diffPct
    val isDrop = delta < 0
    val target = result.targetPct

    val (statusColor, statusText) = when {
        result.isBelowTarget -> Pair(Color(0xFFFF5B86), "Below target")
        result.isNearTarget -> Pair(Color(0xFFFFB347), "Near target")
        else -> Pair(Color(0xFF2EE6B0), "Above target")
    }

    val warningMessage = when {
        result.currentOverallPct < target ->
            "Your attendance is already below your $target% target. This plan brings it to ${result.projectedOverallPct.roundToOneDecimal()}%."
        result.totalMissedClasses == 0 ->
            "No classes are missed in this plan, so your attendance stays the same."
        result.isBelowTarget ->
            "This plan will drop your attendance below your $target% target (${result.projectedOverallPct.roundToOneDecimal()}%)."
        result.isNearTarget ->
            "You have limited room for more absences. You would stay just above $target% (${result.projectedOverallPct.roundToOneDecimal()}%)."
        else ->
            "Your predicted attendance of ${result.projectedOverallPct.roundToOneDecimal()}% stays safely above your $target% target."
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            WhatIfTopBar(
                title = "What-If Predictor",
                onBack = onBack,
                isDark = isDark
            )
        }

        // Hero Card with Percentage Ring
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Transparent,
                border = BorderStroke(1.5.dp, Color(0xFF4F8DFF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            if (isDark) {
                                Brush.linearGradient(listOf(Color(0xBF2454D6), Color(0xD910226E), Color(0x801896A0)))
                            } else {
                                Brush.linearGradient(listOf(Color(0xFFE3EDFF), Color(0xFFF1EBFF), Color(0xFFDCF7EF)))
                            }
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AnimatedCircularAttendanceRing(
                                percentage = result.projectedOverallPct,
                                target = result.targetPct,
                                size = 104.dp,
                                strokeWidth = 9.dp
                            )

                            Column {
                                Text(
                                    text = "Predicted Overall Attendance",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF9DB0DF) else Color(0xFF5D70A8)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${result.projectedOverallPct.roundToOneDecimal()}%",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GlassTokens.textColor(isDark)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${if (delta >= 0) "▲ +" else "▼ "}${delta.roundToOneDecimal()}%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (delta >= 0) Color(0xFF2EE6B0) else Color(0xFFFF5B86)
                                    )
                                }
                                Text(
                                    text = "(from ${result.currentOverallPct.roundToOneDecimal()}%) • target $target%",
                                    fontSize = 11.5.sp,
                                    color = GlassTokens.mutedColor(isDark),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Target tag
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = statusColor.copy(alpha = 0.18f),
                            border = BorderStroke(1.5.dp, statusColor),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = statusText,
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Context Advice Message Box
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = statusColor.copy(alpha = 0.12f),
                border = BorderStroke(1.5.dp, statusColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (result.isBelowTarget) Icons.Default.Info else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = warningMessage,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassTokens.textColor(isDark),
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Plan Summary Card
        item {
            GlassCard(isDark = isDark, contentPadding = PaddingValues(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Plan Summary",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = GlassTokens.textColor(isDark)
                        )

                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = GlassTokens.chipColor(isDark),
                            border = BorderStroke(1.5.dp, Color(0xFF4F8DFF)),
                            modifier = Modifier
                                .height(26.dp)
                                .clickable { onEditPlan() }
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Edit",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6F9BFF)
                                )
                            }
                        }
                    }

                    // Dates pill
                    val dates = result.dayPlans.map { it.dateStr }
                    val dateRangeFormatted = if (dates.size > 1) {
                        "${formatShortDate(dates.first())} – ${formatShortDate(dates.last())} (${dates.size} Days)"
                    } else if (dates.isNotEmpty()) {
                        "${formatShortDate(dates.first())} (1 Day)"
                    } else {
                        "No dates"
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GlassTokens.chipColor(isDark),
                        border = BorderStroke(1.2.dp, GlassTokens.chipBorderColor(isDark)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color(0xFF5B8CFF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dateRangeFormatted,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlassTokens.textColor(isDark)
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Leave / Bunk Days", fontSize = 11.sp, color = GlassTokens.mutedColor(isDark), fontWeight = FontWeight.SemiBold)
                            Text("${dates.size} days", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GlassTokens.textColor(isDark))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Classes Affected", fontSize = 11.sp, color = GlassTokens.mutedColor(isDark), fontWeight = FontWeight.SemiBold)
                            Text("${result.totalMissedClasses} classes", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5B86))
                        }
                    }
                }
            }
        }

        // Day Wise Details Card
        item {
            GlassCard(isDark = isDark, contentPadding = PaddingValues(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Day Wise Details",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = GlassTokens.textColor(isDark)
                    )

                    result.dayPlans.forEachIndexed { index, day ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${formatShortDate(day.dateStr)} (${day.dayOfWeek.take(3)})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = GlassTokens.textColor(isDark)
                                )
                                Text(
                                    text = "${day.missedCount} missed • ${day.attendedCount} attended",
                                    fontSize = 11.5.sp,
                                    color = GlassTokens.mutedColor(isDark)
                                )
                            }

                            // Leave / Bunk tag toggle
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = if (day.kind == "Leave") Color(0x2614D4A0) else Color(0x268C5CFF),
                                border = BorderStroke(1.2.dp, if (day.kind == "Leave") Color(0xFF14D4A0) else Color(0xFFA78BFA)),
                                modifier = Modifier
                                    .height(26.dp)
                                    .clickable { onToggleKind(day.dateStr) }
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.kind,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (day.kind == "Leave") Color(0xFF14D4A0) else Color(0xFFA78BFA)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "Tap Leave / Bunk to relabel a day. It does not change the calculation.",
                        fontSize = 11.sp,
                        color = GlassTokens.mutedColor(isDark),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onSaveScenario,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassTokens.PrimaryGradient),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Scenario",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = onViewDetails,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF4F8DFF))
                ) {
                    Text(
                        text = "View Details",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassTokens.textColor(isDark)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Sub-screen 5: Detailed Breakdown Screen
// -------------------------------------------------------------
@Composable
fun WhatIfBreakdownScreen(
    result: WhatIfPredictionResult,
    isDark: Boolean,
    onBack: () -> Unit,
    onEditPlan: () -> Unit,
    onSaveScenario: () -> Unit
) {
    val delta = result.diffPct

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            WhatIfTopBar(
                title = "What-If Predictor",
                onBack = onBack,
                isDark = isDark
            )
        }

        item {
            Column {
                Text(
                    text = "Detailed Breakdown",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GlassTokens.textColor(isDark)
                )
                Text(
                    text = "This shows how your attendance changes based on your planned days.",
                    fontSize = 12.5.sp,
                    color = GlassTokens.mutedColor(isDark)
                )
            }
        }

        // Dual Ring Attendance Comparison Card
        item {
            GlassCard(isDark = isDark, contentPadding = PaddingValues(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Attendance Comparison",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = GlassTokens.textColor(isDark)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AnimatedCircularAttendanceRing(
                                percentage = result.currentOverallPct,
                                target = result.targetPct,
                                size = 88.dp,
                                strokeWidth = 8.dp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Current", fontSize = 12.sp, color = GlassTokens.mutedColor(isDark), fontWeight = FontWeight.Bold)
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = GlassTokens.mutedColor(isDark),
                            modifier = Modifier.size(24.dp)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AnimatedCircularAttendanceRing(
                                percentage = result.projectedOverallPct,
                                target = result.targetPct,
                                size = 88.dp,
                                strokeWidth = 8.dp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("After Plan", fontSize = 12.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Subject-wise Impact Table
        item {
            GlassCard(isDark = isDark, contentPadding = PaddingValues(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Subject-wise Impact",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = GlassTokens.textColor(isDark)
                    )

                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subject", fontSize = 11.sp, color = GlassTokens.mutedColor(isDark), fontWeight = FontWeight.Bold)
                        Row {
                            Text("Current", fontSize = 11.sp, color = GlassTokens.mutedColor(isDark), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(28.dp))
                            Text("After Plan", fontSize = 11.sp, color = GlassTokens.mutedColor(isDark), fontWeight = FontWeight.Bold)
                        }
                    }

                    // Rows for planned subjects
                    val affectedSubjects = result.subjectImpacts.filter { it.plannedTotal > 0 }
                    affectedSubjects.forEach { s ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = s.subjectCode,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = GlassTokens.textColor(isDark)
                                )
                                Text(
                                    text = "${s.plannedMissed} of ${s.plannedTotal} missed",
                                    fontSize = 11.sp,
                                    color = GlassTokens.mutedColor(isDark)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${s.currentPct.roundToOneDecimal()}%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GlassTokens.textColor(isDark)
                                )
                                Spacer(modifier = Modifier.width(24.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${s.projectedPct.roundToOneDecimal()}%",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (s.projectedPct >= result.targetPct) Color(0xFF2EE6B0) else Color(0xFFFF5B86)
                                    )
                                    val sDelta = s.diffPct
                                    Text(
                                        text = "${if (sDelta >= 0) "+" else ""}${sDelta.roundToOneDecimal()}%",
                                        fontSize = 10.sp,
                                        color = if (sDelta >= 0) Color(0xFF2EE6B0) else Color(0xFFFF5B86)
                                    )
                                }
                            }
                        }
                    }

                    // Total Overall Row
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(GlassTokens.chipBorderColor(isDark))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overall",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = GlassTokens.textColor(isDark)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${result.currentOverallPct.roundToOneDecimal()}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlassTokens.textColor(isDark)
                            )
                            Spacer(modifier = Modifier.width(24.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${result.projectedOverallPct.roundToOneDecimal()}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (result.projectedOverallPct >= result.targetPct) Color(0xFF2EE6B0) else Color(0xFFFF5B86)
                                )
                                Text(
                                    text = "${if (delta >= 0) "+" else ""}${delta.roundToOneDecimal()}%",
                                    fontSize = 10.5.sp,
                                    color = if (delta >= 0) Color(0xFF2EE6B0) else Color(0xFFFF5B86)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Day-by-Day Breakdown
        item {
            GlassCard(isDark = isDark, contentPadding = PaddingValues(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Day-by-Day Breakdown",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = GlassTokens.textColor(isDark)
                    )

                    result.dayPlans.forEach { day ->
                        var isExpanded by remember { mutableStateOf(false) }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = GlassTokens.chipColor(isDark),
                            border = BorderStroke(1.2.dp, GlassTokens.chipBorderColor(isDark)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { isExpanded = !isExpanded }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${formatShortDate(day.dateStr)} (${day.dayOfWeek})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = GlassTokens.textColor(isDark)
                                        )
                                        Text(
                                            text = "${day.missedCount} missed • ${day.attendedCount} attended",
                                            fontSize = 11.sp,
                                            color = GlassTokens.mutedColor(isDark)
                                        )
                                    }

                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = GlassTokens.mutedColor(isDark)
                                    )
                                }

                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        day.eligibleClasses.forEach { c ->
                                            val isMissed = day.missedSlotIds.contains(c.slotId)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${c.subjectCode} • ${c.startTime}",
                                                    fontSize = 11.5.sp,
                                                    color = GlassTokens.mutedColor(isDark)
                                                )
                                                Text(
                                                    text = if (c.isAlwaysPresent) "Always present" else if (c.isCancelled) "Cancelled" else if (isMissed) "Planned absence" else "Will attend",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (c.isAlwaysPresent) Color(0xFF38BDF8) else if (c.isCancelled) Color(0xFFA78BFA) else if (isMissed) Color(0xFFFF5B86) else Color(0xFF2EE6B0)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onSaveScenario,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassTokens.PrimaryGradient),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Scenario",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditPlan,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF4F8DFF))
                    ) {
                        Text("Edit Plan", fontWeight = FontWeight.Bold, color = GlassTokens.textColor(isDark))
                    }

                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF4F8DFF))
                    ) {
                        Text("Back to Results", fontWeight = FontWeight.Bold, color = GlassTokens.textColor(isDark))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Sub-screen 6: Past Saved Scenarios Screen
// -------------------------------------------------------------
@Composable
fun WhatIfSavedScenariosScreen(
    scenarios: List<WhatIfSavedScenario>,
    targetPct: Int,
    isDark: Boolean,
    onBack: () -> Unit,
    onNewPlan: () -> Unit,
    onOpenScenario: (WhatIfSavedScenario) -> Unit,
    onDuplicateScenario: (WhatIfSavedScenario) -> Unit,
    onDeleteScenario: (WhatIfSavedScenario) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            WhatIfTopBar(
                title = "What-If Predictor",
                onBack = onBack,
                isDark = isDark
            )
        }

        item {
            WhatIfSegmentedTab(
                selectedTab = 1,
                tabs = listOf("Plan", "Past Scenarios"),
                isDark = isDark,
                onTabSelected = { idx ->
                    if (idx == 0) onBack()
                }
            )
        }

        item {
            Column {
                Text(
                    text = "Saved Scenarios",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GlassTokens.textColor(isDark)
                )
                Text(
                    text = "View and manage your previous plans",
                    fontSize = 12.5.sp,
                    color = GlassTokens.mutedColor(isDark)
                )
            }
        }

        if (scenarios.isEmpty()) {
            item {
                GlassCard(isDark = isDark, contentPadding = PaddingValues(24.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = Color(0x334A8DFF),
                            border = BorderStroke(1.5.dp, Color(0x807AA2FF)),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = Color(0xFF6F9BFF),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Text(
                            text = "No Saved Scenarios Yet",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = GlassTokens.textColor(isDark)
                        )
                        Text(
                            text = "Start planning your leaves or bunk days to see your predicted attendance.",
                            fontSize = 12.5.sp,
                            color = GlassTokens.mutedColor(isDark),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = onNewPlan,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .height(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(GlassTokens.PrimaryGradient)
                        ) {
                            Text("Create New Plan", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        } else {
            items(scenarios, key = { it.id }) { s ->
                GlassCard(isDark = isDark, contentPadding = PaddingValues(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0x334A8DFF),
                                border = BorderStroke(1.dp, Color(0x807AA2FF)),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = Color(0xFFA9C2FF),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = s.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = GlassTokens.textColor(isDark)
                                )
                                Text(
                                    text = "${s.dateRangeText} • ${s.missedCount} classes",
                                    fontSize = 11.5.sp,
                                    color = GlassTokens.mutedColor(isDark)
                                )
                            }

                            // Tag
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = if (s.kindSummary == "Leave") Color(0x2614D4A0) else Color(0x268C5CFF),
                                border = BorderStroke(1.dp, if (s.kindSummary == "Leave") Color(0xFF14D4A0) else Color(0xFFA78BFA)),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = s.kindSummary,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (s.kindSummary == "Leave") Color(0xFF14D4A0) else Color(0xFFA78BFA)
                                    )
                                }
                            }
                        }

                        // Bottom action row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Predicted", fontSize = 11.sp, color = GlassTokens.mutedColor(isDark))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${s.predictedPct.roundToOneDecimal()}%",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = GlassTokens.textColor(isDark)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (s.predictedPct >= targetPct) "Meets target" else "Below target",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (s.predictedPct >= targetPct) Color(0xFF2EE6B0) else Color(0xFFFF5B86)
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = GlassTokens.chipColor(isDark),
                                    border = BorderStroke(1.dp, GlassTokens.chipBorderColor(isDark)),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable { onDuplicateScenario(s) }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Duplicate",
                                            tint = GlassTokens.textColor(isDark),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = GlassTokens.chipColor(isDark),
                                    border = BorderStroke(1.dp, GlassTokens.chipBorderColor(isDark)),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable { onDeleteScenario(s) }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFFF5B86),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Button(
                                    onClick = { onOpenScenario(s) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                    modifier = Modifier
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GlassTokens.PrimaryGradient)
                                ) {
                                    Text("View", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Shared Composable Helpers
// -------------------------------------------------------------
@Composable
fun WhatIfTopBar(
    title: String,
    onBack: () -> Unit,
    isDark: Boolean,
    trailingIcon: ImageVector? = null,
    trailingAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(15.dp),
            color = GlassTokens.chipColor(isDark),
            border = BorderStroke(1.5.dp, GlassTokens.chipBorderColor(isDark)),
            modifier = Modifier
                .size(44.dp)
                .clickable { onBack() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = GlassTokens.textColor(isDark),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = title,
            fontSize = 19.sp,
            fontWeight = FontWeight.ExtraBold,
            color = GlassTokens.textColor(isDark),
            letterSpacing = (-0.2).sp
        )

        if (trailingIcon != null && trailingAction != null) {
            Surface(
                shape = RoundedCornerShape(15.dp),
                color = GlassTokens.chipColor(isDark),
                border = BorderStroke(1.5.dp, GlassTokens.chipBorderColor(isDark)),
                modifier = Modifier
                    .size(44.dp)
                    .clickable { trailingAction() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = GlassTokens.textColor(isDark),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.size(44.dp))
        }
    }
}

@Composable
fun WhatIfSegmentedTab(
    selectedTab: Int,
    tabs: List<String>,
    isDark: Boolean,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GlassTokens.chipColor(isDark),
        border = BorderStroke(1.5.dp, GlassTokens.chipBorderColor(isDark)),
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { idx, title ->
                val isSelected = idx == selectedTab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) GlassTokens.PrimaryGradient
                            else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable { onTabSelected(idx) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = if (isSelected) Color.White else GlassTokens.mutedColor(isDark)
                    )
                }
            }
        }
    }
}

private fun formatShortDate(dateStr: String): String {
    return try {
        val d = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr) ?: Date()
        SimpleDateFormat("dd MMM", Locale.getDefault()).format(d)
    } catch (_: Exception) {
        dateStr
    }
}

private fun Float.roundToOneDecimal(): String {
    return String.format(Locale.getDefault(), "%.1f", this)
}

private fun toRoman(p: Int): String {
    return when (p) {
        1 -> "I"
        2 -> "II"
        3 -> "III"
        4 -> "IV"
        5 -> "V"
        6 -> "VI"
        7 -> "VII"
        8 -> "VIII"
        else -> "$p"
    }
}

private fun getSubjectColor(code: String): Long {
    return when {
        code.startsWith("BHT503") -> 0xFF8B5CF6
        code.startsWith("BHT055") -> 0xFF14D4A0
        code.startsWith("BHT051") -> 0xFFFF8A1F
        code.startsWith("BHT501") -> 0xFF4A8DFF
        code.startsWith("HTFT") -> 0xFFFF8A1F
        code.startsWith("BHT502") -> 0xFFFF3D9A
        code.startsWith("BNC501") -> 0xFFA78BFA
        code.startsWith("SWACH") -> 0xFFF5B01A
        code.startsWith("DOUBT") -> 0xFFFB7185
        code.startsWith("MNPM") -> 0xFF22B8FF
        code.startsWith("SEM") -> 0xFF2DD4BF
        else -> 0xFF38BDF8
    }
}
