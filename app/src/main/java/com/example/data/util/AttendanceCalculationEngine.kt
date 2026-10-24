package com.example.data.util

import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.AttendanceStatusTier
import com.example.data.model.ClassType
import com.example.data.model.OverallAttendanceStat
import com.example.data.model.SubjectAttendanceStat
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

/**
 * Central Weighted Attendance Calculation Engine.
 *
 * Implements institutional attendance rules:
 * 1. Theory classes: Weight = 1.0
 * 2. Lab/practical classes: a lab block spans two periods and is worth 2 credits in total, i.e. each
 *    lab period counts as one normal class (weight 1.0 per period). Applies to BHT551, BHT552, BHT553
 *    or ClassType.LAB. A stored legacy weight of 2.0 on a lab period is ignored.
 * 3. Auto-credited activities: Library (LIB, DLIB), Seminar (SEM),
 *    and General Elective slots categorized as Digital Library receive 100% attendance automatically
 *    (both attended and conducted weighted totals receive the session weight, default 1.0).
 * 4. Cancelled / No Class: Excluded completely from conducted and attended totals.
 * 5. Unmarked classes: Kept distinct from Present and Absent (never silently converted to absences).
 * 6. Doubt clearing (DOUBT) and Swachhta Abhiyan (SWACH) sessions: removed from the timetable and
 *    excluded completely from attendance counts, including any older saved records.
 * 7. Weighted Attendance % = (Weighted Attended / Weighted Conducted) * 100.
 * 8. Status Tiers:
 *    - Safe: Attendance >= Target
 *    - Warning: Target - 5 <= Attendance < Target
 *    - Shortfall: Attendance < Target - 5
 *    - No Data: 0 conducted sessions
 * 9. Recovery Calculator: Smallest non-negative integer number of future classes to reach target with actual weights.
 * 10. Safe Miss Calculator: Maximum number of upcoming classes that can be missed while remaining >= target.
 */
object AttendanceCalculationEngine {

    const val DEFAULT_THEORY_WEIGHT = 1.0f
    /** Credit per lab PERIOD. A two-period lab block therefore equals 2 credits (two normal classes). */
    const val DEFAULT_LAB_PERIOD_WEIGHT = 1.0f
    const val LAB_BLOCK_CREDITS = 2.0f
    const val DEFAULT_AUTO_CREDIT_WEIGHT = 1.0f

    /**
     * Resolves the session weight for a subject and class type.
     * Prioritizes verified metadata and handles backward compatibility.
     */
    fun resolveSessionWeight(
        subjectCode: String,
        subjectName: String = "",
        classType: String? = null,
        existingWeight: Float? = null
    ): Float {
        // Labs: one credit per period (two periods = 2 credits). Checked first so records saved
        // earlier with a weight of 2.0 per period are not counted twice.
        if (isLabSession(subjectCode, subjectName, classType)) {
            return DEFAULT_LAB_PERIOD_WEIGHT
        }

        // If an explicit non-default weight is already stored, honor it
        if (existingWeight != null && existingWeight > 0f && existingWeight != DEFAULT_THEORY_WEIGHT) {
            return existingWeight
        }

        if (isExcludedFromAttendance(subjectCode, subjectName, classType)) {
            return 0.0f
        }

        if (isAutoCredited(subjectCode, subjectName, classType)) {
            return DEFAULT_AUTO_CREDIT_WEIGHT
        }

        return DEFAULT_THEORY_WEIGHT
    }

    private val TUTORIAL_SUFFIX = Regex("^(.*\\d)\\s*-\\s*T$")
    private val LAB_WORD = Regex("\\b(LAB|LABS|LABORATORY|PRACTICAL|PRACTICALS)\\b")
    private val LAB_CODE = Regex("(^|[^A-Z])(LAB|PRAC)")

    /**
     * Canonical subject key. A tutorial belongs to the same subject as its lecture, so
     * "BHT502-T" and "HTFT 501-T" fold into "BHT502" and "HTFT501". Spaces are ignored.
     */
    fun canonicalSubjectCode(subjectCode: String): String {
        val compact = subjectCode.uppercase().trim().replace(" ", "")
        val m = TUTORIAL_SUFFIX.matchEntire(compact)
        return m?.groupValues?.get(1) ?: compact
    }

    fun isTutorialSession(subjectCode: String, subjectName: String = "", classType: String? = null): Boolean {
        if (classType != null && classType.equals(ClassType.TUTORIAL.name, ignoreCase = true)) return true
        val compact = subjectCode.uppercase().trim().replace(" ", "")
        return TUTORIAL_SUFFIX.matches(compact) || subjectName.uppercase().contains("TUTORIAL")
    }

    /**
     * Identifies if a session is a laboratory or practical class.
     */
    fun isLabSession(
        subjectCode: String,
        subjectName: String = "",
        classType: String? = null
    ): Boolean {
        if (classType != null && classType.equals(ClassType.LAB.name, ignoreCase = true)) {
            return true
        }
        if (isTutorialSession(subjectCode, subjectName, classType)) return false
        val upperCode = subjectCode.uppercase().trim()
        val upperName = subjectName.uppercase().trim()

        if (upperCode.contains("BHT551") || upperCode.contains("BHT 551") ||
            upperCode.contains("BHT552") || upperCode.contains("BHT 552") ||
            upperCode.contains("BHT553") || upperCode.contains("BHT 553") ||
            upperCode.contains("BHT-551") || upperCode.contains("BHT-552") || upperCode.contains("BHT-553")
        ) {
            return true
        }

        if (LAB_CODE.containsMatchIn(upperCode)) {
            return true
        }

        if (LAB_WORD.containsMatchIn(upperName)) {
            return true
        }

        return false
    }

    /**
     * Identifies if a session is auto-credited (Library, Seminar, etc.)
     */
    fun isAutoCredited(
        subjectCode: String,
        subjectName: String = "",
        classType: String? = null
    ): Boolean {
        if (classType != null) {
            if (classType.equals(ClassType.LIBRARY.name, ignoreCase = true) ||
                classType.equals(ClassType.SEMINAR.name, ignoreCase = true) ||
                classType.equals(ClassType.ACTIVITY.name, ignoreCase = true)
            ) {
                return true
            }
        }
        val upperCode = subjectCode.uppercase().trim()
        val upperName = subjectName.uppercase().trim()

        if (upperCode == "LIB" || upperCode == "DLIB" || upperCode == "SEM" ||
            upperCode.contains("LIBRARY") || upperCode.contains("SEMINAR")
        ) {
            return true
        }

        if (upperName.contains("DIGITAL LIBRARY") || upperName.contains("LIBRARY") ||
            upperName.contains("SEMINAR")
        ) {
            return true
        }

        return false
    }

    /**
     * Identifies if a session is excluded from attendance calculation (Doubt Clearing, Swachhta Abhiyan).
     */
    fun isExcludedFromAttendance(
        subjectCode: String,
        subjectName: String = "",
        classType: String? = null
    ): Boolean {
        if (classType != null && classType.equals(ClassType.DOUBT_CLEARING.name, ignoreCase = true)) {
            return true
        }
        val upperCode = subjectCode.uppercase().trim()
        val upperName = subjectName.uppercase().trim()

        return upperCode == "DOUBT" || upperCode.contains("DOUBT") || upperName.contains("DOUBT") ||
            upperCode == "SWACH" || upperCode.contains("SWACHH") || upperName.contains("SWACHH")
    }

    /**
     * Computes attendance percentage using full floating-point precision.
     * Safely returns 0.0 when conducted <= 0 (avoiding NaN/Infinity).
     */
    fun calculatePercentage(weightedAttended: Double, weightedConducted: Double): Double {
        return if (weightedConducted > 0.0) {
            (weightedAttended / weightedConducted) * 100.0
        } else {
            0.0
        }
    }

    /**
     * Assigns attendance status tier using unrounded attendance values.
     */
    fun determineStatusTier(
        percentage: Double,
        conductedWeight: Double,
        targetPercentage: Double
    ): AttendanceStatusTier {
        if (conductedWeight <= 0.0) {
            return AttendanceStatusTier.NO_DATA
        }
        val diff = percentage - targetPercentage
        return when {
            diff >= 0.0 -> AttendanceStatusTier.SAFE
            diff >= -5.0 -> AttendanceStatusTier.WARNING
            else -> AttendanceStatusTier.SHORTFALL
        }
    }

    /**
     * Calculates the minimum number of consecutive future classes a student must attend
     * to reach the target attendance percentage.
     *
     * Respects actual upcoming session weights if provided.
     * When weights are uniform or estimated:
     * n = ceil((Target * Conducted - Attended) / (w * (1 - Target)))
     */
    fun calculateRecoveryClasses(
        weightedAttended: Double,
        weightedConducted: Double,
        targetPercentage: Double,
        upcomingWeights: List<Float> = emptyList(),
        assumedClassWeight: Float = DEFAULT_THEORY_WEIGHT
    ): Int {
        if (targetPercentage <= 0.0 || targetPercentage >= 100.0) return 0
        if (weightedConducted <= 0.0) return 0

        val targetDecimal = targetPercentage / 100.0
        val currentPct = calculatePercentage(weightedAttended, weightedConducted)

        // Already at or above target
        if (currentPct >= targetPercentage) return 0

        // If specific upcoming sequence is provided, step through it
        if (upcomingWeights.isNotEmpty()) {
            var simAttended = weightedAttended
            var simConducted = weightedConducted
            var count = 0

            for (w in upcomingWeights) {
                if (w <= 0f) continue
                simAttended += w
                simConducted += w
                count++
                if ((simAttended / simConducted) >= targetDecimal) {
                    return count
                }
            }

            // If sequence ended and still below target, continue with assumed weight
            val remainingNum = targetDecimal * simConducted - simAttended
            val remainingDen = assumedClassWeight.toDouble() * (1.0 - targetDecimal)
            if (remainingDen > 0.0) {
                val extra = ceil(remainingNum / remainingDen).toInt()
                return count + max(0, extra)
            }
            return count
        }

        // Standard formula using assumed class weight
        val weight = if (assumedClassWeight > 0f) assumedClassWeight.toDouble() else 1.0
        val numerator = targetDecimal * weightedConducted - weightedAttended
        val denominator = weight * (1.0 - targetDecimal)

        if (denominator <= 0.0) return 0
        val needed = ceil(numerator / denominator).toInt()
        return max(0, needed)
    }

    /**
     * Calculates the maximum number of consecutive future classes a student can safely miss
     * while maintaining attendance at or above the target percentage.
     *
     * Respects actual upcoming session weights if provided.
     * When weights are uniform or estimated:
     * m = floor((Attended - Target * Conducted) / (Target * w))
     */
    fun calculateSafeMissClasses(
        weightedAttended: Double,
        weightedConducted: Double,
        targetPercentage: Double,
        upcomingWeights: List<Float> = emptyList(),
        assumedClassWeight: Float = DEFAULT_THEORY_WEIGHT
    ): Int {
        if (targetPercentage <= 0.0 || weightedConducted <= 0.0 || weightedAttended <= 0.0) return 0

        val targetDecimal = targetPercentage / 100.0
        val currentPct = calculatePercentage(weightedAttended, weightedConducted)

        // If current attendance is below target, cannot miss any classes safely
        if (currentPct < targetPercentage) return 0

        if (upcomingWeights.isNotEmpty()) {
            var simConducted = weightedConducted
            var count = 0

            for (w in upcomingWeights) {
                if (w <= 0f) continue
                val nextConducted = simConducted + w
                if ((weightedAttended / nextConducted) >= targetDecimal) {
                    simConducted = nextConducted
                    count++
                } else {
                    break
                }
            }
            return count
        }

        val weight = if (assumedClassWeight > 0f) assumedClassWeight.toDouble() else 1.0
        val numerator = weightedAttended - targetDecimal * weightedConducted
        val denominator = targetDecimal * weight

        if (denominator <= 0.0) return 0
        val missable = floor(numerator / denominator).toInt()
        return max(0, missable)
    }

    /**
     * Master statistics calculation used consistently across the entire application.
     */
    fun calculateOverallStats(
        records: List<AttendanceRecord>,
        targetPercentage: Int = 75,
        upcomingWeights: List<Float> = emptyList(),
        upcomingWeightsBySubject: Map<String, List<Float>> = emptyMap()
    ): OverallAttendanceStat {
        val targetDouble = targetPercentage.toDouble()

        var totalWeightedConducted = 0.0
        var totalWeightedPresent = 0.0
        var totalWeightedAbsent = 0.0

        var totalRawConducted = 0
        var totalRawPresent = 0
        var totalRawAbsent = 0
        var totalRawCancelled = 0

        val subjectMap = mutableMapOf<String, SubjectAccumulator>()

        for (rec in records) {
            val status = rec.status

            if (status == AttendanceStatus.CANCELLED.name) {
                totalRawCancelled++
                continue
            }

            if (isExcludedFromAttendance(rec.subjectCode, rec.subjectName)) {
                continue
            }

            // Unmarked records are neither present nor absent nor conducted
            if (status == AttendanceStatus.UNMARKED.name) {
                continue
            }

            val sessionWeight = resolveSessionWeight(
                subjectCode = rec.subjectCode,
                subjectName = rec.subjectName,
                existingWeight = rec.weight
            ).toDouble()

            val isAutoCredit = isAutoCredited(rec.subjectCode, rec.subjectName)
            val effectiveStatus = if (isAutoCredit) {
                AttendanceStatus.PRESENT.name
            } else {
                status
            }

            val isTutorial = isTutorialSession(rec.subjectCode, rec.subjectName)
            val canonicalCode = canonicalSubjectCode(rec.subjectCode)
            val accumulator = subjectMap.getOrPut(canonicalCode) {
                SubjectAccumulator(
                    code = if (isTutorial) canonicalCode else rec.subjectCode,
                    name = rec.subjectName,
                    sessionWeight = sessionWeight.toFloat()
                )
            }
            // Prefer the lecture's code/name over a tutorial's when both exist for one subject.
            if (!isTutorial && accumulator.fromTutorial) {
                accumulator.code = rec.subjectCode
                accumulator.name = rec.subjectName
                accumulator.fromTutorial = false
            } else if (isTutorial && accumulator.rawConducted == 0 && accumulator.weightedConducted == 0.0) {
                accumulator.fromTutorial = true
            }

            when (effectiveStatus) {
                AttendanceStatus.PRESENT.name -> {
                    totalRawConducted++
                    totalRawPresent++
                    totalWeightedConducted += sessionWeight
                    totalWeightedPresent += sessionWeight

                    accumulator.rawConducted++
                    accumulator.rawPresent++
                    accumulator.weightedConducted += sessionWeight
                    accumulator.weightedPresent += sessionWeight
                }
                AttendanceStatus.ABSENT.name -> {
                    totalRawConducted++
                    totalRawAbsent++
                    totalWeightedConducted += sessionWeight
                    totalWeightedAbsent += sessionWeight

                    accumulator.rawConducted++
                    accumulator.rawAbsent++
                    accumulator.weightedConducted += sessionWeight
                    accumulator.weightedAbsent += sessionWeight
                }
            }
        }

        val overallPercentageDouble = calculatePercentage(totalWeightedPresent, totalWeightedConducted)
        val overallStatusTier = determineStatusTier(
            percentage = overallPercentageDouble,
            conductedWeight = totalWeightedConducted,
            targetPercentage = targetDouble
        )

        val overallRecoveryNeeded = calculateRecoveryClasses(
            weightedAttended = totalWeightedPresent,
            weightedConducted = totalWeightedConducted,
            targetPercentage = targetDouble,
            upcomingWeights = upcomingWeights
        )

        val overallSafeMiss = calculateSafeMissClasses(
            weightedAttended = totalWeightedPresent,
            weightedConducted = totalWeightedConducted,
            targetPercentage = targetDouble,
            upcomingWeights = upcomingWeights
        )

        val subjectStats = subjectMap.values.map { acc ->
            val subPct = calculatePercentage(acc.weightedPresent, acc.weightedConducted)
            val subTier = determineStatusTier(subPct, acc.weightedConducted, targetDouble)
            val subUpcoming = upcomingWeightsBySubject[canonicalSubjectCode(acc.code)].orEmpty()
            val subRecovery = calculateRecoveryClasses(
                weightedAttended = acc.weightedPresent,
                weightedConducted = acc.weightedConducted,
                targetPercentage = targetDouble,
                upcomingWeights = subUpcoming,
                assumedClassWeight = acc.sessionWeight
            )
            val subSafeMiss = if (isAutoCredited(acc.code, acc.name)) 0 else calculateSafeMissClasses(
                weightedAttended = acc.weightedPresent,
                weightedConducted = acc.weightedConducted,
                targetPercentage = targetDouble,
                upcomingWeights = subUpcoming,
                assumedClassWeight = acc.sessionWeight
            )

            SubjectAttendanceStat(
                subjectCode = acc.code,
                subjectName = acc.name,
                conducted = acc.rawConducted,
                present = acc.rawPresent,
                absent = acc.rawAbsent,
                cancelled = 0,
                percentage = subPct.toFloat(),
                classesToTarget = subRecovery,
                classesCanMiss = subSafeMiss,
                isBelowTarget = subTier == AttendanceStatusTier.WARNING || subTier == AttendanceStatusTier.SHORTFALL,
                weightedConducted = acc.weightedConducted,
                weightedPresent = acc.weightedPresent,
                weightedAbsent = acc.weightedAbsent,
                sessionWeight = acc.sessionWeight,
                statusTier = subTier,
                percentageExact = subPct,
                isEstimate = false
            )
        }.sortedBy { it.subjectCode }

        return OverallAttendanceStat(
            conducted = totalRawConducted,
            present = totalRawPresent,
            absent = totalRawAbsent,
            cancelled = totalRawCancelled,
            percentage = overallPercentageDouble.toFloat(),
            targetPercentage = targetPercentage,
            classesToTarget = overallRecoveryNeeded,
            classesCanMiss = overallSafeMiss,
            subjectStats = subjectStats,
            weightedConducted = totalWeightedConducted,
            weightedPresent = totalWeightedPresent,
            weightedAbsent = totalWeightedAbsent,
            statusTier = overallStatusTier,
            percentageExact = overallPercentageDouble,
            isEstimate = upcomingWeights.isEmpty()
        )
    }

    /** Weighted attended / conducted for any subset of records, using the same rules as the master stats. */
    fun weightedTotals(records: List<AttendanceRecord>): Pair<Double, Double> {
        var attended = 0.0
        var conducted = 0.0
        for (rec in records) {
            if (rec.status == AttendanceStatus.CANCELLED.name || rec.status == AttendanceStatus.UNMARKED.name) continue
            if (isExcludedFromAttendance(rec.subjectCode, rec.subjectName)) continue
            val w = resolveSessionWeight(rec.subjectCode, rec.subjectName, null, rec.weight).toDouble()
            val present = isAutoCredited(rec.subjectCode, rec.subjectName) || rec.status == AttendanceStatus.PRESENT.name
            if (present) attended += w
            if (present || rec.status == AttendanceStatus.ABSENT.name) conducted += w
        }
        return attended to conducted
    }

    /**
     * Weights of upcoming avoidable classes in timetable order (auto-credited, doubt-clearing and
     * non-counting slots are skipped: they can't be missed). Pass the already batch/category-filtered
     * slots per day, keyed by uppercase day name. Returns the overall sequence and a per-subject map.
     */
    fun upcomingWeights(
        slotsByDay: Map<String, List<com.example.data.model.TimetableSlot>>,
        startDate: java.time.LocalDate,
        holidayDates: Set<String>,
        horizonDays: Int = 120
    ): Pair<List<Float>, Map<String, List<Float>>> {
        val overall = mutableListOf<Float>()
        val bySubject = mutableMapOf<String, MutableList<Float>>()
        for (offset in 0 until horizonDays) {
            val d = startDate.plusDays(offset.toLong())
            if (d.toString() in holidayDates) continue
            val slots = slotsByDay[d.dayOfWeek.name].orEmpty().sortedBy { it.period }
            for (s in slots) {
                if (!s.countsInAttendance) continue
                if (isExcludedFromAttendance(s.subjectCode, s.subjectName, s.classType)) continue
                if (isAutoCredited(s.subjectCode, s.subjectName, s.classType)) continue
                val w = resolveSessionWeight(s.subjectCode, s.subjectName, s.classType)
                if (w <= 0f) continue
                overall.add(w)
                bySubject.getOrPut(canonicalSubjectCode(s.subjectCode)) { mutableListOf() }.add(w)
            }
        }
        return overall to bySubject
    }

    private class SubjectAccumulator(
        var code: String,
        var name: String,
        var fromTutorial: Boolean = false,
        val sessionWeight: Float,
        var rawConducted: Int = 0,
        var rawPresent: Int = 0,
        var rawAbsent: Int = 0,
        var weightedConducted: Double = 0.0,
        var weightedPresent: Double = 0.0,
        var weightedAbsent: Double = 0.0
    )
}
