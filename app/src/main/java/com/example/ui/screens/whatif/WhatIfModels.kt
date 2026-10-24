package com.example.ui.screens.whatif

import com.example.data.model.ClassType
import com.example.data.util.AttendanceCalculationEngine
import java.util.UUID

enum class WhatIfScreenStep {
    INTRO,
    PLAN_CALENDAR,
    SELECT_CLASSES,
    RESULTS,
    BREAKDOWN,
    SAVED_SCENARIOS
}

enum class WhatIfPlanningMode {
    WHOLE_DAY,
    SELECTED_CLASSES
}

data class WhatIfClassItem(
    val slotId: Long,
    val period: Int,
    val periodRoman: String,
    val startTime: String,
    val endTime: String,
    val subjectCode: String,
    val subjectName: String,
    val classType: String,
    val isAlwaysPresent: Boolean = false,
    val isCancelled: Boolean = false,
    val accentColorHex: Long = 0xFF4A8DFF,
    /** 2 for a merged two-period lab block, otherwise 1. A lab block is worth 2 credits. */
    val periodCount: Int = 1
)

/**
 * Merges back-to-back lab periods of the same subject into one selectable class, so a lab can be
 * skipped with one tap. The merged item keeps the first slot id and counts as two periods.
 */
fun mergeWhatIfLabBlocks(items: List<WhatIfClassItem>): List<WhatIfClassItem> {
    val sorted = items.sortedBy { it.period }
    val out = mutableListOf<WhatIfClassItem>()
    var i = 0
    while (i < sorted.size) {
        val a = sorted[i]
        val b = sorted.getOrNull(i + 1)
        val merge = b != null &&
            a.periodCount == 1 && b.periodCount == 1 &&
            a.classType.equals(ClassType.LAB.name, ignoreCase = true) &&
            b.classType.equals(ClassType.LAB.name, ignoreCase = true) &&
            a.subjectCode.equals(b.subjectCode, ignoreCase = true) &&
            b.period == a.period + 1 &&
            !a.isAlwaysPresent && !b.isAlwaysPresent && !a.isCancelled && !b.isCancelled
        if (merge && b != null) {
            out.add(a.copy(periodRoman = "${a.periodRoman}–${b.periodRoman}", endTime = b.endTime, periodCount = 2))
            i += 2
        } else {
            out.add(a)
            i += 1
        }
    }
    return out
}

data class WhatIfDayPlan(
    val dateStr: String, // "YYYY-MM-DD"
    val dayOfWeek: String, // "Monday", "Tuesday", etc.
    val eligibleClasses: List<WhatIfClassItem>,
    val missedSlotIds: Set<Long> = emptySet(),
    val kind: String = "Leave" // "Leave" or "Bunk"
) {
    val missedCount: Int get() = eligibleClasses.filter { it.slotId in missedSlotIds && !it.isAlwaysPresent && !it.isCancelled }.sumOf { it.periodCount }
    val attendedCount: Int get() = eligibleClasses.filter { it.slotId !in missedSlotIds && !it.isAlwaysPresent && !it.isCancelled }.sumOf { it.periodCount }
}

data class WhatIfSubjectImpact(
    val subjectCode: String,
    val subjectName: String,
    val currentAttended: Double,
    val currentConducted: Double,
    val projectedAttended: Double,
    val projectedConducted: Double,
    val plannedMissed: Int,
    val plannedTotal: Int
) {
    val currentPct: Float get() = AttendanceCalculationEngine.calculatePercentage(currentAttended, currentConducted).toFloat()
    val projectedPct: Float get() = AttendanceCalculationEngine.calculatePercentage(projectedAttended, projectedConducted).toFloat()
    val diffPct: Float get() = projectedPct - currentPct
}

data class WhatIfPredictionResult(
    val currentOverallPct: Float,
    val projectedOverallPct: Float,
    val targetPct: Int,
    val totalMissedClasses: Int,
    val dayPlans: List<WhatIfDayPlan>,
    val subjectImpacts: List<WhatIfSubjectImpact>
) {
    val diffPct: Float get() = projectedOverallPct - currentOverallPct
    val isBelowTarget: Boolean get() = projectedOverallPct < targetPct
    val isNearTarget: Boolean get() = projectedOverallPct >= targetPct && projectedOverallPct < (targetPct + 3)
}

data class WhatIfSavedScenario(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dateRangeText: String,
    val dates: List<String>,
    val mode: WhatIfPlanningMode,
    val missedCount: Int,
    val predictedPct: Float,
    val targetPct: Int,
    val kindSummary: String, // "Leave", "Bunk", or "Mixed"
    val dayKinds: Map<String, String> = emptyMap(),
    val dayPicks: Map<String, List<Long>> = emptyMap()
)
