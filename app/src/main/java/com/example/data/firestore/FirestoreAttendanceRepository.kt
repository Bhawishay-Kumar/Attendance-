package com.example.data.firestore

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreAttendanceRepository(private val db: FirebaseFirestore) {

    // CRITICAL: Always initialize with R.string.firestore_database_id
    constructor(context: Context) : this(getFirestoreInstance(context))

    companion object {
        private fun getFirestoreInstance(context: Context): FirebaseFirestore {
            val appContext = context.applicationContext
            if (com.google.firebase.FirebaseApp.getApps(appContext).isEmpty()) {
                try {
                    com.google.firebase.FirebaseApp.initializeApp(
                        appContext,
                        com.google.firebase.FirebaseOptions.Builder()
                            .setApplicationId(appContext.packageName)
                            .setProjectId("attendancetracker-app")
                            .setApiKey("local-api-key")
                            .build()
                    )
                } catch (_: Exception) {
                    try {
                        com.google.firebase.FirebaseApp.initializeApp(appContext)
                    } catch (_: Exception) {}
                }
            }
            val dbId = try {
                appContext.getString(R.string.firestore_database_id)
            } catch (_: Exception) {
                "(default)"
            }
            return try {
                FirebaseFirestore.getInstance(dbId)
            } catch (_: Exception) {
                FirebaseFirestore.getInstance()
            }
        }
    }

    private val auth: FirebaseAuth get() = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    val currentUserId: String? get() = auth.currentUser?.uid

    // --- Profile Operations ---
    suspend fun saveUserProfile(profile: FirestoreUserProfile) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val path = docRef.path
        try {
            val existingDoc = docRef.get().await()
            val payload = mutableMapOf<String, Any?>(
                "userId" to uid,
                "studentName" to profile.studentName,
                "rollNumber" to profile.rollNumber,
                "college" to profile.college,
                "branch" to profile.branch,
                "semester" to profile.semester,
                "academicYear" to profile.academicYear,
                "targetPercentage" to profile.targetPercentage,
                "selectedBatch" to profile.selectedBatch,
                "electiveChoice" to profile.electiveChoice,
                "themeMode" to profile.themeMode,
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            if (!existingDoc.exists()) {
                val createPayload = payload + mapOf("createdAt" to FieldValue.serverTimestamp())
                docRef.set(createPayload).await()
            } else {
                docRef.set(payload, SetOptions.merge()).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    fun observeUserProfile(): Flow<FirestoreUserProfile?> = flow {
        val uid = requireUserId()
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) snapshot.toObject(FirestoreUserProfile::class.java) else null
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    throw error
                }
        )
    }

    // --- Attendance Records Operations ---
    suspend fun saveAttendanceRecord(record: FirestoreAttendanceRecord) {
        val uid = requireUserId()
        val recordId = "${record.date}_period_${record.period}"
        val docRef = db.collection("users").document(uid).collection("records").document(recordId)
        val path = docRef.path
        try {
            val existingDoc = docRef.get().await()
            val payload = mutableMapOf<String, Any?>(
                "userId" to uid,
                "date" to record.date,
                "period" to record.period,
                "subjectCode" to record.subjectCode,
                "subjectName" to record.subjectName,
                "status" to record.status,
                "topic" to record.topic,
                "note" to record.note,
                "markedAt" to record.markedAt,
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            if (!existingDoc.exists()) {
                val createPayload = payload + mapOf("createdAt" to FieldValue.serverTimestamp())
                docRef.set(createPayload).await()
            } else {
                docRef.set(payload, SetOptions.merge()).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    suspend fun deleteAttendanceRecord(date: String, period: Int) {
        val uid = requireUserId()
        val recordId = "${date}_period_${period}"
        val docRef = db.collection("users").document(uid).collection("records").document(recordId)
        val path = docRef.path
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    fun observeRecords(): Flow<List<FirestoreAttendanceRecord>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/records"
        emitAll(
            db.collection("users").document(uid).collection("records")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(FirestoreAttendanceRecord::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    // --- Timetable Slots Operations ---
    suspend fun saveTimetableSlot(slot: FirestoreTimetableSlot) {
        val uid = requireUserId()
        val slotId = "slot_${slot.dayOfWeek}_${slot.period}"
        val docRef = db.collection("users").document(uid).collection("timetable").document(slotId)
        val path = docRef.path
        try {
            val existingDoc = docRef.get().await()
            val payload = mutableMapOf<String, Any?>(
                "userId" to uid,
                "dayOfWeek" to slot.dayOfWeek,
                "period" to slot.period,
                "subjectCode" to slot.subjectCode,
                "subjectName" to slot.subjectName,
                "batch" to slot.batch,
                "room" to slot.room,
                "classType" to slot.classType,
                "startTime" to slot.startTime,
                "endTime" to slot.endTime,
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            if (!existingDoc.exists()) {
                val createPayload = payload + mapOf("createdAt" to FieldValue.serverTimestamp())
                docRef.set(createPayload).await()
            } else {
                docRef.set(payload, SetOptions.merge()).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    fun observeTimetableSlots(): Flow<List<FirestoreTimetableSlot>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/timetable"
        emitAll(
            db.collection("users").document(uid).collection("timetable")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(FirestoreTimetableSlot::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    // --- Date Override Operations ---
    suspend fun saveDateOverride(override: FirestoreDateOverride) {
        val uid = requireUserId()
        val dateId = override.date
        val docRef = db.collection("users").document(uid).collection("overrides").document(dateId)
        val path = docRef.path
        try {
            val existingDoc = docRef.get().await()
            val payload = mutableMapOf<String, Any?>(
                "userId" to uid,
                "date" to override.date,
                "isHoliday" to override.isHoliday,
                "holidayReason" to override.holidayReason,
                "isCustomSchedule" to override.isCustomSchedule,
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            if (!existingDoc.exists()) {
                val createPayload = payload + mapOf("createdAt" to FieldValue.serverTimestamp())
                docRef.set(createPayload).await()
            } else {
                docRef.set(payload, SetOptions.merge()).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    fun observeDateOverrides(): Flow<List<FirestoreDateOverride>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/overrides"
        emitAll(
            db.collection("users").document(uid).collection("overrides")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(FirestoreDateOverride::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }
}
