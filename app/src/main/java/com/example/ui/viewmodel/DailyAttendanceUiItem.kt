package com.example.ui.viewmodel

import com.example.data.model.AttendanceStatus
import com.example.data.model.ClassType

data class DailyAttendanceUiItem(
    val slotId: Long = 0L,
    val period: Int,
    val periodRoman: String,
    val startTime: String,
    val endTime: String,
    val subjectCode: String,
    val subjectName: String,
    val classType: String,
    val batch: String,
    val countsInAttendance: Boolean,
    val currentStatus: AttendanceStatus,
    val isOverride: Boolean = false,
    val notes: String = "",
    val isAlwaysPresent: Boolean = false,
    /** Second period of a merged two-period lab block, or null for an ordinary single-period item. */
    val pairedPeriod: Int? = null
)

private val ROMAN = listOf("", "I", "II", "III", "IV", "V", "VI", "VII", "VIII")

/**
 * Merges back-to-back lab periods of the same subject (for example BHT551 in periods III and IV)
 * into one item, so the student marks the lab once. The merged item keeps the first period and
 * records the second in [DailyAttendanceUiItem.pairedPeriod]. If the two periods hold different
 * saved statuses (older data), the item shows as unmarked until it is marked once.
 */
fun mergeLabBlocks(items: List<DailyAttendanceUiItem>): List<DailyAttendanceUiItem> {
    val sorted = items.sortedBy { it.period }
    val out = mutableListOf<DailyAttendanceUiItem>()
    var i = 0
    while (i < sorted.size) {
        val a = sorted[i]
        val b = sorted.getOrNull(i + 1)
        val mergeable = b != null &&
            !a.isOverride && !b.isOverride &&
            a.pairedPeriod == null &&
            a.classType.equals(ClassType.LAB.name, ignoreCase = true) &&
            b.classType.equals(ClassType.LAB.name, ignoreCase = true) &&
            a.subjectCode.equals(b.subjectCode, ignoreCase = true) &&
            b.period == a.period + 1
        if (mergeable && b != null) {
            val first = ROMAN.getOrElse(a.period) { "${a.period}" }
            val second = ROMAN.getOrElse(b.period) { "${b.period}" }
            out.add(
                a.copy(
                    periodRoman = "$first–$second",
                    endTime = b.endTime,
                    currentStatus = if (a.currentStatus == b.currentStatus) a.currentStatus else AttendanceStatus.UNMARKED,
                    countsInAttendance = a.countsInAttendance || b.countsInAttendance,
                    pairedPeriod = b.period
                )
            )
            i += 2
        } else {
            out.add(a)
            i += 1
        }
    }
    return out
}

/** Reverse of [mergeLabBlocks]: one item per period, used by the "mark all" actions. */
fun expandLabBlocks(items: List<DailyAttendanceUiItem>): List<DailyAttendanceUiItem> =
    items.flatMap { item ->
        val second = item.pairedPeriod
        if (second == null) listOf(item)
        else listOf(
            item.copy(pairedPeriod = null),
            item.copy(period = second, periodRoman = ROMAN.getOrElse(second) { "$second" }, pairedPeriod = null)
        )
    }
