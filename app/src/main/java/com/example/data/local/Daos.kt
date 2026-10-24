package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceRecord
import com.example.data.model.DateOverride
import com.example.data.model.StudentProfile
import com.example.data.model.TimetableSlot
import com.example.data.model.TimetableVersion
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationPreferences
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records WHERE date = :date ORDER BY period ASC")
    fun getRecordsForDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records ORDER BY date DESC, period ASC")
    fun getAllRecords(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records")
    suspend fun getAllRecordsList(): List<AttendanceRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<AttendanceRecord>)

    @Query("DELETE FROM attendance_records WHERE date = :date AND period = :period")
    suspend fun deleteRecord(date: String, period: Int)

    @Query("DELETE FROM attendance_records WHERE date = :date")
    suspend fun deleteRecordsForDate(date: String)

    @Query("DELETE FROM attendance_records")
    suspend fun clearAll()
}

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable_slots WHERE versionId = :versionId ORDER BY period ASC")
    fun getSlotsForVersion(versionId: Int): Flow<List<TimetableSlot>>

    @Query("SELECT * FROM timetable_slots WHERE dayOfWeek = :dayOfWeek AND versionId = :versionId ORDER BY period ASC")
    fun getSlotsForDay(dayOfWeek: String, versionId: Int): Flow<List<TimetableSlot>>

    @Query("SELECT * FROM timetable_slots")
    suspend fun getAllSlotsSync(): List<TimetableSlot>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: TimetableSlot): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlots(slots: List<TimetableSlot>)

    @Update
    suspend fun updateSlot(slot: TimetableSlot)

    @Query("DELETE FROM timetable_slots WHERE id = :id")
    suspend fun deleteSlot(id: Long)

    @Query("DELETE FROM timetable_slots WHERE versionId = :versionId")
    suspend fun deleteSlotsForVersion(versionId: Int)

    /** Removes Swachhta Abhiyan and doubt-clearing slots left in an existing timetable. Attendance history is untouched. */
    @Query("DELETE FROM timetable_slots WHERE UPPER(subjectCode) IN ('SWACH', 'DOUBT')")
    suspend fun deleteRemovedSessionSlots()

    @Query("DELETE FROM timetable_slots")
    suspend fun clearAllSlots()

    // Versions
    @Query("SELECT * FROM timetable_versions ORDER BY versionId DESC")
    fun getAllVersions(): Flow<List<TimetableVersion>>

    @Query("SELECT * FROM timetable_versions WHERE isCurrent = 1 LIMIT 1")
    fun getCurrentVersion(): Flow<TimetableVersion?>

    @Query("SELECT * FROM timetable_versions WHERE isCurrent = 1 LIMIT 1")
    suspend fun getCurrentVersionSync(): TimetableVersion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: TimetableVersion): Long

    @Update
    suspend fun updateVersion(version: TimetableVersion)

    @Query("UPDATE timetable_versions SET isCurrent = 0")
    suspend fun unmarkAllCurrentVersions()

    @Query("UPDATE timetable_versions SET isCurrent = 1 WHERE versionId = :versionId")
    suspend fun setCurrentVersion(versionId: Int)
}

@Dao
interface OverrideDao {
    @Query("SELECT * FROM date_overrides WHERE date = :date")
    fun getOverridesForDate(date: String): Flow<List<DateOverride>>

    @Query("SELECT * FROM date_overrides ORDER BY date ASC")
    fun getAllOverrides(): Flow<List<DateOverride>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOverride(override: DateOverride): Long

    @Query("DELETE FROM date_overrides WHERE id = :id")
    suspend fun deleteOverride(id: Long)

    @Query("DELETE FROM date_overrides WHERE date = :date")
    suspend fun deleteOverridesForDate(date: String)

    @Query("DELETE FROM date_overrides")
    suspend fun clearAll()
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM student_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<StudentProfile?>

    @Query("SELECT * FROM student_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): StudentProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: StudentProfile)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationItem>>

    @Query("SELECT * FROM notifications WHERE isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadNotifications(): Flow<List<NotificationItem>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("SELECT * FROM notifications WHERE category = :category ORDER BY timestamp DESC")
    fun getNotificationsByCategory(category: String): Flow<List<NotificationItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotification(notification: NotificationItem): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(notifications: List<NotificationItem>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: Long)

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()

    @Query("SELECT EXISTS(SELECT 1 FROM notifications WHERE notificationKey = :key)")
    suspend fun existsKey(key: String): Boolean
}

@Dao
interface NotificationPreferencesDao {
    @Query("SELECT * FROM notification_preferences WHERE id = 1 LIMIT 1")
    fun getPreferences(): Flow<NotificationPreferences?>

    @Query("SELECT * FROM notification_preferences WHERE id = 1 LIMIT 1")
    suspend fun getPreferencesSync(): NotificationPreferences?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePreferences(preferences: NotificationPreferences)
}

