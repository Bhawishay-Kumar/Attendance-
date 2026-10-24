package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.firestore.FirestoreAttendanceRecord
import com.example.data.firestore.FirestoreAttendanceRepository
import com.example.data.firestore.FirestoreUserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AttendanceRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private val aliceEmail = "alice@example.com"
    private val defaultTimeoutMs = 10000L

    @Test
    fun saveUserProfile_authenticatedUser_success() = runBlocking {
        val uid = signInTestUser(aliceEmail)
        val repository = FirestoreAttendanceRepository(firestore)

        val profile = FirestoreUserProfile(
            userId = uid,
            studentName = "Bhawishay Student",
            college = "IIHT Varanasi",
            branch = "Handloom and Textile Technology",
            semester = "5th Semester",
            academicYear = "2026-27",
            targetPercentage = 75.0,
            selectedBatch = "Batch A",
            themeMode = "system"
        )

        withTimeout(defaultTimeoutMs) {
            repository.saveUserProfile(profile)
            val fetched = repository.observeUserProfile().first()
            assertNotNull(fetched)
            assertEquals("Bhawishay Student", fetched?.studentName)
            assertEquals("Batch A", fetched?.selectedBatch)
        }
    }

    @Test
    fun saveAttendanceRecord_authenticatedUser_success() = runBlocking {
        val uid = signInTestUser(aliceEmail)
        val repository = FirestoreAttendanceRepository(firestore)

        val record = FirestoreAttendanceRecord(
            userId = uid,
            date = "2026-08-17",
            period = 1,
            subjectCode = "BHT503",
            subjectName = "Quality Evaluation of Fibers and Yarns",
            status = "PRESENT"
        )

        withTimeout(defaultTimeoutMs) {
            repository.saveAttendanceRecord(record)
            val records = repository.observeRecords().first()
            assertTrue(records.any { it.period == 1 && it.status == "PRESENT" && it.date == "2026-08-17" })
        }
    }

    @Test
    fun unauthenticatedAccess_throwsIllegalStateException() = runBlocking {
        auth.signOut()
        val repository = FirestoreAttendanceRepository(firestore)

        try {
            repository.saveAttendanceRecord(
                FirestoreAttendanceRecord(
                    userId = "random_user",
                    date = "2026-08-17",
                    period = 1,
                    status = "PRESENT"
                )
            )
            fail("Expected IllegalStateException for unauthenticated write")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("User must be signed in") == true)
        }
    }
}
