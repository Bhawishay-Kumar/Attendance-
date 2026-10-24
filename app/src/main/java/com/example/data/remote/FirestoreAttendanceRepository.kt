package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.firestore.OperationType
import com.example.data.firestore.handleFirestoreError
import com.example.data.model.AttendanceRecord
import com.example.data.model.StudentProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreAttendanceRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing cloud Firestore.")
    }

    suspend fun saveProfile(profile: StudentProfile) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val existing = docRef.get().await()
            val data = hashMapOf<String, Any>(
                "userId" to uid,
                "studentName" to profile.studentName,
                "rollNumber" to profile.rollNo,
                "college" to profile.college,
                "branch" to profile.branch,
                "semester" to profile.semester,
                "academicYear" to profile.academicYear,
                "selectedBatch" to if (profile.batch.startsWith("Batch")) profile.batch else "Batch ${profile.batch}",
                "academicCategory" to profile.academicCategory,
                "targetPercentage" to profile.targetAttendancePct.toDouble(),
                "themeMode" to when (profile.themePreference.uppercase()) {
                    "DARK" -> "dark"
                    "LIGHT" -> "light"
                    else -> "system"
                },
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!existing.exists()) {
                data["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(data).await()
            } else {
                docRef.set(data, SetOptions.merge()).await()
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
        }
    }

    fun observeProfile(uid: String): Flow<StudentProfile?> =
        db.collection("users").document(uid)
            .snapshots()
            .map { snap ->
                if (!snap.exists()) return@map null
                StudentProfile(
                    studentName = snap.getString("studentName") ?: "Student",
                    rollNo = snap.getString("rollNumber") ?: "",
                    college = snap.getString("college") ?: "Indian Institute of Handloom Technology, Varanasi",
                    course = "B.Tech.",
                    branch = snap.getString("branch") ?: "Handloom and Textile Technology",
                    semester = snap.getString("semester") ?: "5th Semester",
                    academicYear = snap.getString("academicYear") ?: "2026–27",
                    batch = (snap.getString("selectedBatch") ?: "Batch A").replace("Batch ", "").trim(),
                    academicCategory = snap.getString("academicCategory") ?: "Major",
                    targetAttendancePct = (snap.getDouble("targetPercentage") ?: 75.0).toInt(),
                    themePreference = (snap.getString("themeMode") ?: "system").uppercase()
                )
            }.catch { e ->
                Log.e("FirestoreRepo", "Error observing user profile", e)
                emit(null)
            }

    suspend fun saveAttendanceRecord(record: AttendanceRecord) {
        val uid = requireUserId()
        val recordId = "${record.date}_period_${record.period}"
        val docRef = db.collection("users").document(uid).collection("records").document(recordId)
        try {
            val existing = docRef.get().await()
            val data = hashMapOf<String, Any>(
                "userId" to uid,
                "date" to record.date,
                "period" to record.period,
                "subjectCode" to record.subjectCode,
                "subjectName" to record.subjectName,
                "status" to record.status,
                "note" to record.notes,
                "weight" to record.weight.toDouble(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!existing.exists()) {
                data["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(data).await()
            } else {
                docRef.set(data, SetOptions.merge()).await()
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
        }
    }

    suspend fun deleteAttendanceRecord(date: String, period: Int) {
        val uid = requireUserId()
        val recordId = "${date}_period_${period}"
        val docRef = db.collection("users").document(uid).collection("records").document(recordId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
        }
    }

    fun observeAttendanceRecords(uid: String): Flow<List<AttendanceRecord>> =
        db.collection("users").document(uid).collection("records")
            .snapshots()
            .map { snap ->
                snap.documents.mapNotNull { doc ->
                    val date = doc.getString("date") ?: return@mapNotNull null
                    val period = doc.getLong("period")?.toInt() ?: return@mapNotNull null
                    val subjectCode = doc.getString("subjectCode") ?: return@mapNotNull null
                    val status = doc.getString("status") ?: return@mapNotNull null
                    val weight = (doc.getDouble("weight") ?: 1.0).toFloat()
                    AttendanceRecord(
                        date = date,
                        period = period,
                        subjectCode = subjectCode,
                        subjectName = doc.getString("subjectName") ?: "",
                        status = status,
                        notes = doc.getString("note") ?: "",
                        weight = weight
                    )
                }
            }.catch { e ->
                Log.e("FirestoreRepo", "Error observing attendance records", e)
                emit(emptyList())
            }

    suspend fun syncAllLocalToCloud(localRecords: List<AttendanceRecord>, localProfile: StudentProfile) {
        val uid = auth.currentUser?.uid ?: return
        saveProfile(localProfile)
        for (rec in localRecords) {
            saveAttendanceRecord(rec)
        }
    }
}
