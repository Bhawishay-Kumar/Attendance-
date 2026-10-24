package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Attendance status enum:
 * PRESENT, ABSENT, CANCELLED (not held / teacher absent), UNMARKED
 */
enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    CANCELLED,
    UNMARKED
}

/**
 * Class type classification
 */
enum class ClassType(val displayName: String) {
    LECTURE("Lecture"),
    TUTORIAL("Tutorial"),
    LAB("Lab / Practical"),
    SEMINAR("Seminar"),
    LIBRARY("Library"),
    ACTIVITY("Activity / Swachhta"),
    DOUBT_CLEARING("Doubt Clearing"),
    OTHER("Other")
}

/**
 * Institutional attendance status tier.
 * Safe: Attendance is at or above selected target.
 * Warning: Below target but within 5 percentage points.
 * Shortfall: More than 5 percentage points below target.
 * No Data: Zero conducted sessions.
 */
enum class AttendanceStatusTier(val label: String) {
    SAFE("Safe"),
    WARNING("Warning"),
    SHORTFALL("Shortfall"),
    NO_DATA("No Data")
}

/**
 * Single recorded attendance item for a specific date and period.
 * Unique index on (date, period) prevents duplicate attendance records for the same session.
 */
@Entity(
    tableName = "attendance_records",
    indices = [Index(value = ["date", "period"], unique = true)]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,             // Format: "YYYY-MM-DD"
    val period: Int,              // 1 to 8
    val subjectCode: String,      // e.g. "BHT503"
    val subjectName: String,      // e.g. "Quality Evaluation of Fibers and Yarns"
    val status: String,           // PRESENT, ABSENT, CANCELLED
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val weight: Float = 1.0f      // Session weight: 1.0 for theory, 2.0 for lab, etc.
)

/**
 * Scheduled slot in a weekly recurring timetable.
 */
@Entity(tableName = "timetable_slots")
data class TimetableSlot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayOfWeek: String,         // MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    val period: Int,              // 1 to 8
    val startTime: String,         // "09:30 AM"
    val endTime: String,           // "10:20 AM"
    val subjectCode: String,       // "BHT503"
    val subjectName: String,       // "Quality Evaluation of Fibers and Yarns"
    val classType: String = ClassType.LECTURE.name,
    val batch: String = "ALL",     // "ALL", "A", "B"
    val countsInAttendance: Boolean = true,
    val versionId: Int = 1,
    val notes: String = ""
)

/**
 * Timetable version for history & rollback.
 */
@Entity(tableName = "timetable_versions")
data class TimetableVersion(
    @PrimaryKey(autoGenerate = true)
    val versionId: Int = 0,
    val versionName: String,
    val effectiveDate: String,    // "YYYY-MM-DD"
    val createdAt: Long = System.currentTimeMillis(),
    val isCurrent: Boolean = true
)

/**
 * Date overrides: Single-day exceptions, holidays, or extra makeup classes.
 */
@Entity(tableName = "date_overrides")
data class DateOverride(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,              // "YYYY-MM-DD"
    val isHoliday: Boolean = false,
    val holidayName: String = "",
    val isExtraClass: Boolean = false,
    val period: Int = 1,
    val startTime: String = "",
    val endTime: String = "",
    val subjectCode: String = "",
    val subjectName: String = "",
    val classType: String = ClassType.LECTURE.name,
    val batch: String = "ALL",
    val countsInAttendance: Boolean = true
)

/**
 * User student profile & preferences
 */
@Entity(tableName = "student_profile")
data class StudentProfile(
    @PrimaryKey
    val id: Int = 1,
    val studentName: String = "B.Tech Student",
    val rollNo: String = "IIHT/2024/BHTT/01",
    val college: String = "Indian Institute of Handloom Technology, Varanasi",
    val course: String = "B.Tech.",
    val branch: String = "Handloom and Textile Technology",
    val semester: String = "5th Semester",
    val academicYear: String = "2026–27",
    val batch: String = "A",               // "A" or "B"
    val academicCategory: String = "Major", // "Major", "Minor", "Library"
    val targetAttendancePct: Int = 75,
    val labTwoPeriodCounting: Boolean = true, // If true, 2-period lab counts as 2 sessions; if false, 1 session
    val timetableEffectiveDate: String = "2026-08-17",
    val themePreference: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val accentTheme: String = "IIHT_BLUE", // "IIHT_BLUE", "WEAVE_TEAL", "VARANASI_AMBER", "ROYAL_PURPLE"
    val showOverviewCard: Boolean = true,
    val showTodayCard: Boolean = true,
    val showWeeklyCard: Boolean = true,
    val showRecentCard: Boolean = true,
    val showMonthlyCard: Boolean = true,
    val profilePhotoUri: String = ""
)

/**
 * Aggregated statistics for reporting
 */
data class SubjectAttendanceStat(
    val subjectCode: String,
    val subjectName: String,
    val conducted: Int,
    val present: Int,
    val absent: Int,
    val cancelled: Int,
    val percentage: Float,
    val classesToTarget: Int,
    val classesCanMiss: Int,
    val isBelowTarget: Boolean,
    val weightedConducted: Double = conducted.toDouble(),
    val weightedPresent: Double = present.toDouble(),
    val weightedAbsent: Double = absent.toDouble(),
    val sessionWeight: Float = 1.0f,
    val statusTier: AttendanceStatusTier = AttendanceStatusTier.NO_DATA,
    val percentageExact: Double = percentage.toDouble(),
    /** True when recovery / safe-miss used an assumed class weight instead of the real upcoming timetable. */
    val isEstimate: Boolean = false
)

data class OverallAttendanceStat(
    val conducted: Int,
    val present: Int,
    val absent: Int,
    val cancelled: Int,
    val percentage: Float,
    val targetPercentage: Int,
    val classesToTarget: Int,
    val classesCanMiss: Int,
    val subjectStats: List<SubjectAttendanceStat>,
    val weightedConducted: Double = conducted.toDouble(),
    val weightedPresent: Double = present.toDouble(),
    val weightedAbsent: Double = absent.toDouble(),
    val statusTier: AttendanceStatusTier = AttendanceStatusTier.NO_DATA,
    val percentageExact: Double = percentage.toDouble(),
    /** True when recovery / safe-miss used an assumed class weight instead of the real upcoming timetable. */
    val isEstimate: Boolean = false
)

/**
 * Period Timing helper
 */
data class PeriodTiming(
    val period: Int,
    val roman: String,
    val startTime: String,
    val endTime: String
)

data class DashboardCardVisibility(
    val showOverallAttendance: Boolean = true,
    val showTodaySchedule: Boolean = true,
    val showWeeklyTrends: Boolean = true,
    val showRecentActivity: Boolean = true,
    val showAttendanceForecast: Boolean = true,
    val showWeeklyTimetable: Boolean = true,
    val showMonthlySummary: Boolean = true,
    val showRecentLogs: Boolean = true
)

val StudentProfile.dashboardCardVisibility: DashboardCardVisibility
    get() = DashboardCardVisibility(
        showOverallAttendance = showOverviewCard,
        showTodaySchedule = showTodayCard,
        showWeeklyTrends = showWeeklyCard,
        showRecentActivity = showRecentCard,
        showAttendanceForecast = showMonthlyCard,
        showWeeklyTimetable = showWeeklyCard,
        showMonthlySummary = showMonthlyCard,
        showRecentLogs = showRecentCard
    )

val OverallAttendanceStat.overallPercentage: Float get() = percentage
val OverallAttendanceStat.totalAttended: Int get() = present
val OverallAttendanceStat.totalConducted: Int get() = conducted
val OverallAttendanceStat.totalMissed: Int get() = absent
val OverallAttendanceStat.totalCancelled: Int get() = cancelled
val OverallAttendanceStat.isBelowTarget: Boolean get() = statusTier == AttendanceStatusTier.WARNING || statusTier == AttendanceStatusTier.SHORTFALL
val OverallAttendanceStat.hasData: Boolean get() = statusTier != AttendanceStatusTier.NO_DATA
val OverallAttendanceStat.consecutiveClassesNeeded: Int get() = classesToTarget

val SubjectAttendanceStat.category: String get() = if (subjectCode.contains("LAB", ignoreCase = true) || subjectName.contains("Lab", ignoreCase = true)) "Practical" else "Theory"

