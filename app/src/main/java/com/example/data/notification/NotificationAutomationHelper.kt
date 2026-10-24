package com.example.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.data.local.InitialTimetableData
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.NotificationActionType
import com.example.data.model.NotificationCategory
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationPreferences
import com.example.data.model.OverallAttendanceStat
import com.example.data.model.StudentProfile
import com.example.data.repository.AttendanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object NotificationAutomationHelper {

    const val CHANNEL_REMINDERS = "channel_attendance_reminders"
    const val CHANNEL_REPORTS = "channel_attendance_reports"
    const val CHANNEL_ALERTS = "channel_attendance_alerts"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Attendance Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to mark attendance for scheduled classes"
                enableVibration(true)
            }

            val reportsChannel = NotificationChannel(
                CHANNEL_REPORTS,
                "Weekly & Monthly Reports",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Periodic attendance summaries and analytics"
            }

            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "Low Attendance Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent alerts when attendance drops below minimum threshold"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(remindersChannel)
            notificationManager.createNotificationChannel(reportsChannel)
            notificationManager.createNotificationChannel(alertsChannel)
        }
    }

    /**
     * Scans real attendance data, timetable, and settings to generate appropriate in-app notifications
     */
    suspend fun evaluateAndGenerateNotifications(
        context: Context,
        repository: AttendanceRepository,
        records: List<AttendanceRecord>,
        profile: StudentProfile,
        stats: OverallAttendanceStat
    ) = withContext(Dispatchers.IO) {
        val prefs = repository.getNotificationPreferencesSync()
        if (!prefs.masterEnabled) return@withContext

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())
        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentMinute = cal.get(Calendar.MINUTE)
        val currentTotalMinutes = currentHour * 60 + currentMinute

        // 1. Attendance Marking Reminders
        if (prefs.markRemindersEnabled) {
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

            val defaultSlots = InitialTimetableData.getDefaultSlots(1)
                .filter { it.dayOfWeek == dayOfWeek && (it.batch == "ALL" || it.batch.equals(profile.batch, ignoreCase = true)) }

            for (slot in defaultSlots) {
                // Parse slot end time, e.g. "10:20 AM"
                val endMinutes = parseTimeToMinutes(slot.endTime)
                val isSlotEnded = currentTotalMinutes >= endMinutes

                // If class has ended (or it's afternoon/evening) and there's no attendance marked for it
                val hasRecord = records.any { it.date == todayStr && it.period == slot.period }
                if (isSlotEnded && !hasRecord && slot.countsInAttendance) {
                    val key = "reminder_${todayStr}_period_${slot.period}"
                    val notification = NotificationItem(
                        notificationKey = key,
                        title = "Mark Attendance: ${slot.subjectName}",
                        message = "Period ${slot.period} (${slot.startTime}–${slot.endTime}) has completed. Please update your status to keep records accurate.",
                        category = NotificationCategory.ATTENDANCE_REMINDER.name,
                        actionType = NotificationActionType.NAVIGATE_DAILY.name,
                        actionPayload = todayStr,
                        actionLabel = "Mark Attendance",
                        priority = "HIGH"
                    )
                    repository.insertNotification(notification)
                }
            }
        }

        // 2. Low-Attendance Warning
        if (prefs.lowAttendanceWarningsEnabled && stats.conducted > 0) {
            val pctStr = String.format(Locale.US, "%.1f", stats.percentage)
            if (stats.statusTier == com.example.data.model.AttendanceStatusTier.WARNING ||
                stats.statusTier == com.example.data.model.AttendanceStatusTier.SHORTFALL) {
                val key = "warning_low_${todayStr}"
                val notification = NotificationItem(
                    notificationKey = key,
                    title = "Attendance Alert: $pctStr% (Below ${profile.targetAttendancePct}%)",
                    message = "Your overall attendance is below the mandatory ${profile.targetAttendancePct}% threshold. You need to attend the next ${stats.classesToTarget} session(s) consecutively to recover.",
                    category = NotificationCategory.LOW_ATTENDANCE_WARNING.name,
                    actionType = NotificationActionType.NAVIGATE_REPORTS.name,
                    actionPayload = "reports",
                    actionLabel = "View Subject Details",
                    priority = "HIGH"
                )
                repository.insertNotification(notification)
            } else if (stats.percentage <= prefs.warningLeadThresholdPct) {
                // Warning approaching target
                val key = "warning_approaching_${todayStr}"
                val notification = NotificationItem(
                    notificationKey = key,
                    title = "Caution: Attendance at $pctStr%",
                    message = "You are approaching the ${profile.targetAttendancePct}% limit. You can safely miss up to ${stats.classesCanMiss} session(s) before dropping below target.",
                    category = NotificationCategory.LOW_ATTENDANCE_WARNING.name,
                    actionType = NotificationActionType.NAVIGATE_REPORTS.name,
                    actionPayload = "reports",
                    actionLabel = "View Margin",
                    priority = "NORMAL"
                )
                repository.insertNotification(notification)
            }
        }

        // 3. Weekly Attendance Report
        if (prefs.weeklyReportsEnabled && stats.conducted > 0) {
            val weekOfYear = cal.get(Calendar.WEEK_OF_YEAR)
            val year = cal.get(Calendar.YEAR)
            val key = "weekly_report_${year}_w${weekOfYear}"
            val pctStr = String.format(Locale.US, "%.1f", stats.percentage)
            val remark = if (stats.percentageExact >= profile.targetAttendancePct) "You are on track!" else "Attention needed in low subjects."
            val notification = NotificationItem(
                notificationKey = key,
                title = "Weekly Attendance Report (Week $weekOfYear)",
                message = "Overall performance: $pctStr% (${stats.present}/${stats.conducted} sessions attended). $remark",
                category = NotificationCategory.WEEKLY_REPORT.name,
                actionType = NotificationActionType.NAVIGATE_REPORTS.name,
                actionPayload = "reports",
                actionLabel = "View Weekly Report",
                priority = "NORMAL"
            )
            repository.insertNotification(notification)
        }

        // 4. Monthly Attendance Report
        if (prefs.monthlyReportsEnabled && stats.conducted > 0) {
            val month = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())
            val year = cal.get(Calendar.YEAR)
            val key = "monthly_report_${year}_${month.lowercase()}"
            val pctStr = String.format(Locale.US, "%.1f", stats.percentage)
            val notification = NotificationItem(
                notificationKey = key,
                title = "$month Attendance Summary Ready",
                message = "Complete monthly breakdown: $pctStr% overall attendance with ${stats.present} classes attended out of ${stats.conducted} held.",
                category = NotificationCategory.MONTHLY_REPORT.name,
                actionType = NotificationActionType.NAVIGATE_REPORTS.name,
                actionPayload = "reports",
                actionLabel = "View Monthly Report",
                priority = "NORMAL"
            )
            repository.insertNotification(notification)
        }
    }

    /**
     * Seeds initial representative notifications so that the user immediately has
     * active, functioning notifications across all categories to inspect and interact with.
     */
    suspend fun seedSampleNotificationsIfEmpty(
        repository: AttendanceRepository,
        stats: OverallAttendanceStat,
        profile: StudentProfile
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val pctStr = String.format(Locale.US, "%.1f", stats.percentage)
        val statusRemark = if (stats.percentageExact >= profile.targetAttendancePct) "On Track" else "Below Target"
        val sampleItems = listOf(
            NotificationItem(
                notificationKey = "welcome_smart_tracker",
                title = "Batch ${profile.batch} Smart Tracker Active",
                message = "IIHT Handloom & Textile 5th Semester attendance tracker is configured with your timetable. Tap any card to get started.",
                category = NotificationCategory.SYSTEM_ALERT.name,
                timestamp = now - 1000 * 60 * 15, // 15 mins ago
                isRead = false,
                actionType = NotificationActionType.NAVIGATE_DAILY.name,
                actionLabel = "Go to Attendance",
                priority = "NORMAL"
            ),
            NotificationItem(
                notificationKey = "demo_attendance_reminder",
                title = "Pending Attendance: Quality Evaluation of Fibers",
                message = "Period 1 (09:30 AM–10:20 AM) concluded. Tap to mark your attendance for today.",
                category = NotificationCategory.ATTENDANCE_REMINDER.name,
                timestamp = now - 1000 * 60 * 45, // 45 mins ago
                isRead = false,
                actionType = NotificationActionType.NAVIGATE_DAILY.name,
                actionLabel = "Mark Attendance",
                priority = "HIGH"
            ),
            NotificationItem(
                notificationKey = "demo_weekly_report",
                title = "Weekly Attendance Summary",
                message = "Weekly target is ${profile.targetAttendancePct}%. Overall standing: $pctStr% with ${stats.present} classes attended. Subject forecasts updated.",
                category = NotificationCategory.WEEKLY_REPORT.name,
                timestamp = now - 1000 * 60 * 60 * 6, // 6 hours ago
                isRead = false,
                actionType = NotificationActionType.NAVIGATE_REPORTS.name,
                actionLabel = "View Weekly Report",
                priority = "NORMAL"
            ),
            NotificationItem(
                notificationKey = "demo_upcoming_class",
                title = "Upcoming: Knitting and Nonwoven Technology",
                message = "Class starts in 10 minutes at 10:20 AM in Room 102. Lecture session.",
                category = NotificationCategory.TIMETABLE_REMINDER.name,
                timestamp = now - 1000 * 60 * 120, // 2 hours ago
                isRead = true,
                actionType = NotificationActionType.NAVIGATE_TIMETABLE.name,
                actionLabel = "Check Schedule",
                priority = "NORMAL"
            ),
            NotificationItem(
                notificationKey = "demo_monthly_report",
                title = "Monthly Attendance Breakdown",
                message = "Overall status: $statusRemark. Safe margin allows up to ${stats.classesCanMiss} missed sessions while maintaining eligibility.",
                category = NotificationCategory.MONTHLY_REPORT.name,
                timestamp = now - 1000 * 60 * 60 * 24, // 1 day ago
                isRead = true,
                actionType = NotificationActionType.NAVIGATE_REPORTS.name,
                actionLabel = "View Monthly Report",
                priority = "NORMAL"
            )
        )

        for (item in sampleItems) {
            repository.insertNotification(item)
        }
    }

    private fun parseTimeToMinutes(timeStr: String): Int {
        return try {
            val trimmed = timeStr.trim().uppercase()
            val isPM = trimmed.endsWith("PM")
            val isAM = trimmed.endsWith("AM")
            val clean = trimmed.replace("AM", "").replace("PM", "").trim()
            val parts = clean.split(":")
            var hour = parts[0].toInt()
            val min = parts[1].toInt()
            if (isPM && hour < 12) hour += 12
            if (isAM && hour == 12) hour = 0
            hour * 60 + min
        } catch (_: Exception) {
            10 * 60 // default 10:00 AM
        }
    }
}
