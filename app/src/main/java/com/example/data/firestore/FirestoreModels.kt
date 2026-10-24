package com.example.data.firestore

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(exception: Exception, operationType: OperationType, path: String?): String {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", exception.message ?: exception.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

@IgnoreExtraProperties
data class FirestoreUserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val studentName: String = "",
    val rollNumber: String? = null,
    val college: String? = null,
    val branch: String? = null,
    val semester: String? = null,
    val academicYear: String? = null,
    val targetPercentage: Double = 75.0,
    val selectedBatch: String = "Batch A",
    val electiveChoice: String? = null,
    val themeMode: String = "system", // "system", "light", "dark"
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreAttendanceRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val date: String = "",
    val period: Int = 1,
    val subjectCode: String? = null,
    val subjectName: String? = null,
    val status: String = "PRESENT", // PRESENT, ABSENT, CANCELLED, UNMARKED
    val topic: String? = null,
    val note: String? = null,
    val markedAt: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreTimetableSlot(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dayOfWeek: Int = 1,
    val period: Int = 1,
    val subjectCode: String = "",
    val subjectName: String? = null,
    val batch: String? = null,
    val room: String? = null,
    val classType: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreDateOverride(
    @DocumentId val id: String = "",
    val userId: String = "",
    val date: String = "",
    val isHoliday: Boolean = false,
    val holidayReason: String? = null,
    val isCustomSchedule: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
