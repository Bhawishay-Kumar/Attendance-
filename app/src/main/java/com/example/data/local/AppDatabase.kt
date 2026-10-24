package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.DateOverride
import com.example.data.model.StudentProfile
import com.example.data.model.TimetableSlot
import com.example.data.model.TimetableVersion

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

import com.example.data.model.NotificationItem
import com.example.data.model.NotificationPreferences

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE student_profile ADD COLUMN profilePhotoUri TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE student_profile ADD COLUMN academicCategory TEXT NOT NULL DEFAULT 'Major'")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE attendance_records ADD COLUMN weight REAL NOT NULL DEFAULT 1.0")
    }
}

@Database(
    entities = [
        AttendanceRecord::class,
        TimetableSlot::class,
        TimetableVersion::class,
        DateOverride::class,
        StudentProfile::class,
        NotificationItem::class,
        NotificationPreferences::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun attendanceDao(): AttendanceDao
    abstract fun timetableDao(): TimetableDao
    abstract fun overrideDao(): OverrideDao
    abstract fun profileDao(): ProfileDao
    abstract fun notificationDao(): NotificationDao
    abstract fun notificationPreferencesDao(): NotificationPreferencesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "iiht_attendance_tracker.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
