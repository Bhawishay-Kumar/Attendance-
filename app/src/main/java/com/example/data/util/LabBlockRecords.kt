package com.example.data.util

import com.example.data.model.AttendanceRecord

/**
 * One line in a history or day view. A two-period lab block with the same saved status in both
 * periods is shown as a single entry, for example "Period III–IV".
 */
data class AttendanceLogEntry(
    val date: String,
    val firstPeriod: Int,
    val lastPeriod: Int,
    val subjectCode: String,
    val subjectName: String,
    val status: String,
    val notes: String
) {
    val periodLabel: String
        get() = if (lastPeriod == firstPeriod) "Period $firstPeriod" else "Period $firstPeriod–$lastPeriod"
}

/**
 * Merges back-to-back lab records of the same subject, date and status into one entry.
 * Nothing is deleted or changed in the database; this only affects how the records are listed.
 * Records whose two lab periods hold different statuses stay as separate entries.
 */
fun mergeLabRecords(records: List<AttendanceRecord>): List<AttendanceLogEntry> {
    val out = mutableListOf<AttendanceLogEntry>()
    for ((_, dayRecords) in records.groupBy { it.date }) {
        val sorted = dayRecords.sortedBy { it.period }
        var i = 0
        while (i < sorted.size) {
            val a = sorted[i]
            val b = sorted.getOrNull(i + 1)
            val merge = b != null &&
                b.period == a.period + 1 &&
                a.subjectCode.equals(b.subjectCode, ignoreCase = true) &&
                a.status == b.status &&
                AttendanceCalculationEngine.isLabSession(a.subjectCode, a.subjectName)
            val last = if (merge && b != null) b.period else a.period
            out.add(AttendanceLogEntry(a.date, a.period, last, a.subjectCode, a.subjectName, a.status, a.notes))
            i += if (merge) 2 else 1
        }
    }
    return out
}
