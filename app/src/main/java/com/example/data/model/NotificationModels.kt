package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Categories of notifications in the app
 */
enum class NotificationCategory(val displayName: String) {
    ATTENDANCE_REMINDER("Attendance Reminder"),
    WEEKLY_REPORT("Weekly Report"),
    MONTHLY_REPORT("Monthly Report"),
    LOW_ATTENDANCE_WARNING("Attendance Alert"),
    TIMETABLE_REMINDER("Upcoming Class"),
    SYSTEM_ALERT("System")
}

/**
 * Action type to take when a notification is tapped or action button clicked
 */
enum class NotificationActionType {
    NAVIGATE_DAILY,
    NAVIGATE_REPORTS,
    NAVIGATE_TIMETABLE,
    NAVIGATE_SETTINGS,
    NONE
}

/**
 * Persistent Notification Item stored in Room
 */
@Entity(
    tableName = "notifications",
    indices = [Index(value = ["notificationKey"], unique = true)]
)
data class NotificationItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val notificationKey: String,          // Idempotency key (e.g. reminder_2026-10-17_p2)
    val title: String,
    val message: String,
    val category: String = NotificationCategory.ATTENDANCE_REMINDER.name,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val actionType: String = NotificationActionType.NONE.name,
    val actionPayload: String = "",       // e.g. date string, subject code
    val actionLabel: String = "",         // e.g. "Mark Attendance", "View Report"
    val priority: String = "NORMAL"       // "HIGH", "NORMAL", "LOW"
)

/**
 * User Notification Preferences
 */
@Entity(tableName = "notification_preferences")
data class NotificationPreferences(
    @PrimaryKey
    val id: Int = 1,
    val masterEnabled: Boolean = true,
    val markRemindersEnabled: Boolean = true,
    val weeklyReportsEnabled: Boolean = true,
    val monthlyReportsEnabled: Boolean = true,
    val lowAttendanceWarningsEnabled: Boolean = true,
    val upcomingClassRemindersEnabled: Boolean = true,
    val leadTimeMinutes: Int = 10,                 // 5, 10, or 15 minutes before class
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",          // 10:00 PM
    val quietHoursEnd: String = "07:00",            // 07:00 AM
    val targetThresholdPct: Int = 75,
    val warningLeadThresholdPct: Int = 80,
    val preferredWeeklyDay: String = "SUNDAY",      // Day to deliver weekly summary
    val preferredWeeklyTime: String = "18:00",      // 06:00 PM
    val preferredMonthlyDate: Int = 1,              // 1st of every month
    val preferredMonthlyTime: String = "10:00"      // 10:00 AM
)
