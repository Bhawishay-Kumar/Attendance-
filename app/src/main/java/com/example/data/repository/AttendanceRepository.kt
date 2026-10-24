package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.InitialTimetableData
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.DateOverride
import com.example.data.model.OverallAttendanceStat
import com.example.data.model.StudentProfile
import com.example.data.model.SubjectAttendanceStat
import com.example.data.model.TimetableSlot
import com.example.data.model.TimetableVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil
import com.example.data.util.AttendanceCalculationEngine

class AttendanceRepository(private val database: AppDatabase) {

    private val attendanceDao = database.attendanceDao()
    private val timetableDao = database.timetableDao()
    private val overrideDao = database.overrideDao()
    private val profileDao = database.profileDao()
    private val notificationDao = database.notificationDao()
    private val notificationPreferencesDao = database.notificationPreferencesDao()

    val profileFlow: Flow<StudentProfile?> = profileDao.getProfile()
    val allAttendanceFlow: Flow<List<AttendanceRecord>> = attendanceDao.getAllRecords()
    val allVersionsFlow: Flow<List<TimetableVersion>> = timetableDao.getAllVersions()
    val allOverridesFlow: Flow<List<DateOverride>> = overrideDao.getAllOverrides()
    val allNotificationsFlow: Flow<List<com.example.data.model.NotificationItem>> = notificationDao.getAllNotifications()
    val unreadNotificationsCountFlow: Flow<Int> = notificationDao.getUnreadCount()
    val notificationPreferencesFlow: Flow<com.example.data.model.NotificationPreferences?> = notificationPreferencesDao.getPreferences()

    /**
     * Seeds initial database state if not present
     */
    suspend fun initializeDefaultsIfNeeded() = withContext(Dispatchers.IO) {
        // Seed Profile
        val existingProfile = profileDao.getProfileSync()
        if (existingProfile == null) {
            profileDao.insertOrUpdateProfile(StudentProfile())
        }

        // Seed Timetable Version & Slots
        val existingVersion = timetableDao.getCurrentVersionSync()
        if (existingVersion == null) {
            val vId = timetableDao.insertVersion(
                TimetableVersion(
                    versionId = 1,
                    versionName = "Revised BHTT 5th Sem (AY 2026-27)",
                    effectiveDate = "2026-08-17",
                    isCurrent = true
                )
            ).toInt()

            val defaultSlots = InitialTimetableData.getDefaultSlots(versionId = vId)
            timetableDao.insertSlots(defaultSlots)
        }

        // Swachhta Abhiyan and doubt-clearing are no longer classes: drop them from saved timetables
        timetableDao.deleteRemovedSessionSlots()

        // Seed Notification Preferences
        val existingPrefs = notificationPreferencesDao.getPreferencesSync()
        if (existingPrefs == null) {
            notificationPreferencesDao.insertOrUpdatePreferences(com.example.data.model.NotificationPreferences())
        }
    }

    suspend fun insertNotification(notification: com.example.data.model.NotificationItem) = withContext(Dispatchers.IO) {
        notificationDao.insertNotification(notification)
    }

    suspend fun markNotificationAsRead(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead()
    }

    suspend fun deleteNotification(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.deleteNotification(id)
    }

    suspend fun clearAllNotifications() = withContext(Dispatchers.IO) {
        notificationDao.clearAllNotifications()
    }

    suspend fun saveNotificationPreferences(preferences: com.example.data.model.NotificationPreferences) = withContext(Dispatchers.IO) {
        notificationPreferencesDao.insertOrUpdatePreferences(preferences)
    }

    suspend fun getNotificationPreferencesSync(): com.example.data.model.NotificationPreferences {
        return withContext(Dispatchers.IO) {
            notificationPreferencesDao.getPreferencesSync() ?: com.example.data.model.NotificationPreferences()
        }
    }

    fun getRecordsForDate(date: String): Flow<List<AttendanceRecord>> {
        return attendanceDao.getRecordsForDate(date)
    }

    fun getSlotsForDay(dayOfWeek: String, versionId: Int = 1): Flow<List<TimetableSlot>> {
        return timetableDao.getSlotsForDay(dayOfWeek, versionId)
    }

    fun getSlotsForVersion(versionId: Int): Flow<List<TimetableSlot>> {
        return timetableDao.getSlotsForVersion(versionId)
    }

    fun getOverridesForDate(date: String): Flow<List<DateOverride>> {
        return overrideDao.getOverridesForDate(date)
    }

    suspend fun saveProfile(profile: StudentProfile) = withContext(Dispatchers.IO) {
        profileDao.insertOrUpdateProfile(profile)
    }

    suspend fun markAttendance(record: AttendanceRecord) = withContext(Dispatchers.IO) {
        attendanceDao.insertOrUpdate(record)
    }

    suspend fun resetAttendanceForSlot(date: String, period: Int) = withContext(Dispatchers.IO) {
        attendanceDao.deleteRecord(date, period)
    }

    suspend fun resetAttendanceForDate(date: String) = withContext(Dispatchers.IO) {
        attendanceDao.deleteRecordsForDate(date)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        attendanceDao.clearAll()
    }

    /**
     * Bulk Mark All Present for a date
     */
    suspend fun markAllPresent(
        date: String,
        slots: List<TimetableSlot>,
        batch: String
    ): Int = withContext(Dispatchers.IO) {
        val eligibleSlots = slots.filter { s ->
            s.batch == "ALL" || s.batch == batch
        }
        val records = eligibleSlots.map { slot ->
            val weight = AttendanceCalculationEngine.resolveSessionWeight(
                subjectCode = slot.subjectCode,
                subjectName = slot.subjectName,
                classType = slot.classType
            )
            AttendanceRecord(
                date = date,
                period = slot.period,
                subjectCode = slot.subjectCode,
                subjectName = slot.subjectName,
                status = AttendanceStatus.PRESENT.name,
                weight = weight
            )
        }
        attendanceDao.insertAll(records)
        eligibleSlots.size
    }

    /**
     * Bulk Mark All Absent for a date
     */
    suspend fun markAllAbsent(
        date: String,
        slots: List<TimetableSlot>,
        batch: String
    ): Int = withContext(Dispatchers.IO) {
        val eligibleSlots = slots.filter { s ->
            s.batch == "ALL" || s.batch == batch
        }
        val records = eligibleSlots.map { slot ->
            val weight = AttendanceCalculationEngine.resolveSessionWeight(
                subjectCode = slot.subjectCode,
                subjectName = slot.subjectName,
                classType = slot.classType
            )
            AttendanceRecord(
                date = date,
                period = slot.period,
                subjectCode = slot.subjectCode,
                subjectName = slot.subjectName,
                status = AttendanceStatus.ABSENT.name,
                weight = weight
            )
        }
        attendanceDao.insertAll(records)
        eligibleSlots.size
    }

    /**
     * Bulk Mark All Cancelled / No Class for a date
     */
    suspend fun markAllCancelled(
        date: String,
        slots: List<TimetableSlot>,
        batch: String
    ): Int = withContext(Dispatchers.IO) {
        val eligibleSlots = slots.filter { s ->
            s.batch == "ALL" || s.batch == batch
        }
        val records = eligibleSlots.map { slot ->
            val weight = AttendanceCalculationEngine.resolveSessionWeight(
                subjectCode = slot.subjectCode,
                subjectName = slot.subjectName,
                classType = slot.classType
            )
            AttendanceRecord(
                date = date,
                period = slot.period,
                subjectCode = slot.subjectCode,
                subjectName = slot.subjectName,
                status = AttendanceStatus.CANCELLED.name,
                weight = weight
            )
        }
        attendanceDao.insertAll(records)
        eligibleSlots.size
    }

    // Timetable slot management
    suspend fun addSlot(slot: TimetableSlot) = withContext(Dispatchers.IO) {
        timetableDao.insertSlot(slot)
    }

    suspend fun updateSlot(slot: TimetableSlot) = withContext(Dispatchers.IO) {
        timetableDao.updateSlot(slot)
    }

    suspend fun deleteSlot(slotId: Long) = withContext(Dispatchers.IO) {
        timetableDao.deleteSlot(slotId)
    }

    // Timetable versioning
    suspend fun createNewTimetableVersion(
        versionName: String,
        effectiveDate: String,
        copyFromVersionId: Int
    ): Int = withContext(Dispatchers.IO) {
        timetableDao.unmarkAllCurrentVersions()
        val newVersionId = timetableDao.insertVersion(
            TimetableVersion(
                versionName = versionName,
                effectiveDate = effectiveDate,
                isCurrent = true
            )
        ).toInt()

        val oldSlots = timetableDao.getAllSlotsSync().filter { it.versionId == copyFromVersionId }
        val newSlots = oldSlots.map { it.copy(id = 0, versionId = newVersionId) }
        timetableDao.insertSlots(newSlots)
        newVersionId
    }

    suspend fun restoreTimetableVersion(versionId: Int) = withContext(Dispatchers.IO) {
        timetableDao.unmarkAllCurrentVersions()
        timetableDao.setCurrentVersion(versionId)
    }

    // Overrides / Holiday
    suspend fun addDateOverride(override: DateOverride) = withContext(Dispatchers.IO) {
        overrideDao.insertOverride(override)
    }

    suspend fun removeDateOverride(overrideId: Long) = withContext(Dispatchers.IO) {
        overrideDao.deleteOverride(overrideId)
    }

    suspend fun clearDateOverrides(date: String) = withContext(Dispatchers.IO) {
        overrideDao.deleteOverridesForDate(date)
    }

    /**
     * Compute comprehensive attendance statistics based on records and user target.
     * Uses AttendanceCalculationEngine:
     * - Theory: weight 1.0
     * - Lab: 1.0 per period, so a two-period lab block = 2 credits (BHT551, BHT552, BHT553, ClassType.LAB)
     * - Auto-credited (Library, Seminar): 100% attendance automatically
     * - Cancelled, Doubt clearing and Swachhta Abhiyan sessions: excluded
     * - Unmarked: kept distinct
     * - Full precision percentage, recovery, safe miss, and status tiers
     */
    fun calculateStatistics(
        records: List<AttendanceRecord>,
        targetPercentage: Int = 75,
        upcomingWeights: List<Float> = emptyList(),
        upcomingWeightsBySubject: Map<String, List<Float>> = emptyMap()
    ): OverallAttendanceStat {
        return AttendanceCalculationEngine.calculateOverallStats(
            records, targetPercentage, upcomingWeights, upcomingWeightsBySubject
        )
    }

    /**
     * CSV Export Generator
     */
    suspend fun generateCsvReport(profile: StudentProfile): String = withContext(Dispatchers.IO) {
        val records = attendanceDao.getAllRecordsList()
        val stats = calculateStatistics(records, profile.targetAttendancePct)

        val sb = StringBuilder()
        sb.append("=====================================================\n")
        sb.append("ATTENDANCE REPORT - ${profile.college}\n")
        sb.append("Student: ${profile.studentName} | Roll: ${profile.rollNo}\n")
        sb.append("Course: ${profile.course} | Branch: ${profile.branch}\n")
        sb.append("Semester: ${profile.semester} | Academic Year: ${profile.academicYear} | Batch: ${profile.batch}\n")
        sb.append("Target Attendance: ${profile.targetAttendancePct}%\n")
        sb.append("Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().time)}\n")
        sb.append("=====================================================\n\n")

        sb.append("SUMMARY STATISTICS\n")
        sb.append("Total Classes Conducted,${stats.conducted}\n")
        sb.append("Total Attended (Present),${stats.present}\n")
        sb.append("Total Missed (Absent),${stats.absent}\n")
        sb.append("Total Cancelled,${stats.cancelled}\n")
        sb.append("Weighted Conducted,${String.format(Locale.US, "%.1f", stats.weightedConducted)}\n")
        sb.append("Weighted Attended,${String.format(Locale.US, "%.1f", stats.weightedPresent)}\n")
        sb.append("Overall Attendance Percentage,${String.format(Locale.US, "%.2f", stats.percentage)}%\n")
        sb.append("Status Tier,${stats.statusTier.label}\n")
        sb.append("Status,${when (stats.statusTier) { com.example.data.model.AttendanceStatusTier.SAFE -> "ELIGIBLE (>= ${stats.targetPercentage}%)"; com.example.data.model.AttendanceStatusTier.NO_DATA -> "NO DATA"; else -> "SHORTAGE (< ${stats.targetPercentage}%)" }}\n")
        if (stats.classesToTarget > 0) {
            sb.append("Consecutive Classes Needed to Reach Target,${stats.classesToTarget}\n")
        } else if (stats.classesCanMiss > 0) {
            sb.append("Safe Missable Classes,${stats.classesCanMiss}\n")
        }
        sb.append("\n")

        sb.append("SUBJECT-WISE BREAKDOWN\n")
        sb.append("Subject Code,Subject Name,Session Weight,Conducted,Present,Absent,Weighted Conducted,Weighted Attended,Percentage (%),Status Tier,Status\n")
        for (sub in stats.subjectStats) {
            val statusStr = when (sub.statusTier) { com.example.data.model.AttendanceStatusTier.SAFE -> "OK"; com.example.data.model.AttendanceStatusTier.NO_DATA -> "NO DATA"; else -> "LOW ATTENDANCE" }
            sb.append("\"${sub.subjectCode}\",\"${sub.subjectName}\",${sub.sessionWeight},${sub.conducted},${sub.present},${sub.absent},${String.format(Locale.US, "%.1f", sub.weightedConducted)},${String.format(Locale.US, "%.1f", sub.weightedPresent)},${String.format(Locale.US, "%.1f", sub.percentage)}%,\"${sub.statusTier.label}\",\"$statusStr\"\n")
        }
        sb.append("\n")

        sb.append("DETAILED ATTENDANCE LOG\n")
        sb.append("Date,Period,Subject Code,Subject Name,Status,Notes\n")
        for (rec in records.sortedWith(compareBy({ it.date }, { it.period }))) {
            sb.append("${rec.date},Period ${rec.period},\"${rec.subjectCode}\",\"${rec.subjectName}\",${rec.status},\"${rec.notes}\"\n")
        }

        sb.toString()
    }

    /**
     * Backup to JSON string
     */
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val profile = profileDao.getProfileSync() ?: StudentProfile()
        val records = attendanceDao.getAllRecordsList()
        val slots = timetableDao.getAllSlotsSync()

        root.put("version", 1)
        root.put("appName", "IIHT Attendance Tracker")

        val profObj = JSONObject().apply {
            put("studentName", profile.studentName)
            put("rollNo", profile.rollNo)
            put("college", profile.college)
            put("course", profile.course)
            put("branch", profile.branch)
            put("semester", profile.semester)
            put("academicYear", profile.academicYear)
            put("batch", profile.batch)
            put("academicCategory", profile.academicCategory)
            put("targetAttendancePct", profile.targetAttendancePct)
            put("labTwoPeriodCounting", profile.labTwoPeriodCounting)
            put("timetableEffectiveDate", profile.timetableEffectiveDate)
            put("profilePhotoUri", profile.profilePhotoUri)
        }
        root.put("profile", profObj)

        val recArray = JSONArray()
        for (r in records) {
            val o = JSONObject().apply {
                put("date", r.date)
                put("period", r.period)
                put("subjectCode", r.subjectCode)
                put("subjectName", r.subjectName)
                put("status", r.status)
                put("notes", r.notes)
                put("timestamp", r.timestamp)
            }
            recArray.put(o)
        }
        root.put("records", recArray)

        val slotArray = JSONArray()
        for (s in slots) {
            val o = JSONObject().apply {
                put("dayOfWeek", s.dayOfWeek)
                put("period", s.period)
                put("startTime", s.startTime)
                put("endTime", s.endTime)
                put("subjectCode", s.subjectCode)
                put("subjectName", s.subjectName)
                put("classType", s.classType)
                put("batch", s.batch)
                put("countsInAttendance", s.countsInAttendance)
                put("versionId", s.versionId)
                put("notes", s.notes)
            }
            slotArray.put(o)
        }
        root.put("slots", slotArray)

        root.toString(2)
    }

    /**
     * Restore from JSON string
     */
    suspend fun restoreBackupJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (root.has("profile")) {
                val p = root.getJSONObject("profile")
                val restoredProfile = StudentProfile(
                    studentName = p.optString("studentName", "Student"),
                    rollNo = p.optString("rollNo", "IIHT/01"),
                    college = p.optString("college", "IIHT Varanasi"),
                    course = p.optString("course", "B.Tech."),
                    branch = p.optString("branch", "Handloom and Textile Technology"),
                    semester = p.optString("semester", "5th Semester"),
                    academicYear = p.optString("academicYear", "2026–27"),
                    batch = p.optString("batch", "A"),
                    academicCategory = p.optString("academicCategory", "Major"),
                    targetAttendancePct = p.optInt("targetAttendancePct", 75),
                    labTwoPeriodCounting = p.optBoolean("labTwoPeriodCounting", true),
                    timetableEffectiveDate = p.optString("timetableEffectiveDate", "2026-08-17"),
                    profilePhotoUri = p.optString("profilePhotoUri", "")
                )
                profileDao.insertOrUpdateProfile(restoredProfile)
            }

            if (root.has("records")) {
                val recArray = root.getJSONArray("records")
                val records = mutableListOf<AttendanceRecord>()
                for (i in 0 until recArray.length()) {
                    val o = recArray.getJSONObject(i)
                    records.add(
                        AttendanceRecord(
                            date = o.getString("date"),
                            period = o.getInt("period"),
                            subjectCode = o.getString("subjectCode"),
                            subjectName = o.getString("subjectName"),
                            status = o.getString("status"),
                            notes = o.optString("notes", ""),
                            timestamp = o.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                attendanceDao.insertAll(records)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Factory / Singleton holder
     */
    companion object {
        @Volatile
        private var instance: AttendanceRepository? = null

        fun getInstance(database: AppDatabase): AttendanceRepository {
            return instance ?: synchronized(this) {
                instance ?: AttendanceRepository(database).also { instance = it }
            }
        }
    }
}
