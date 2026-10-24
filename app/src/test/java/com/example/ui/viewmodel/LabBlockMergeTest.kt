package com.example.ui.viewmodel

import com.example.data.model.AttendanceStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LabBlockMergeTest {

    private fun item(period: Int, code: String, type: String, status: AttendanceStatus = AttendanceStatus.UNMARKED,
                     override: Boolean = false) =
        DailyAttendanceUiItem(
            slotId = period.toLong(), period = period, periodRoman = "P$period",
            startTime = "s$period", endTime = "e$period", subjectCode = code, subjectName = code,
            classType = type, batch = "A", countsInAttendance = true, currentStatus = status, isOverride = override
        )

    @Test fun two_consecutive_lab_periods_become_one_item() {
        val day = listOf(item(1, "BHT501", "LECTURE"), item(3, "BHT551", "LAB"), item(4, "BHT551", "LAB"), item(5, "BHT502-T", "TUTORIAL"))
        val m = mergeLabBlocks(day)
        assertEquals(listOf(1, 3, 5), m.map { it.period })
        val lab = m[1]
        assertEquals(4, lab.pairedPeriod); assertEquals("III–IV", lab.periodRoman)
        assertEquals("s3", lab.startTime); assertEquals("e4", lab.endTime)
        assertNull(m[0].pairedPeriod)
    }

    @Test fun different_lab_subjects_or_gaps_are_not_merged() {
        assertEquals(2, mergeLabBlocks(listOf(item(3, "BHT551", "LAB"), item(4, "BHT552", "LAB"))).size)
        assertEquals(2, mergeLabBlocks(listOf(item(3, "BHT551", "LAB"), item(5, "BHT551", "LAB"))).size)
        assertEquals(2, mergeLabBlocks(listOf(item(3, "BHT551", "LAB"), item(4, "BHT551", "LAB", override = true))).size)
    }

    @Test fun status_follows_both_periods() {
        val both = mergeLabBlocks(listOf(item(6, "BHT553", "LAB", AttendanceStatus.PRESENT), item(7, "BHT553", "LAB", AttendanceStatus.PRESENT)))
        assertEquals(AttendanceStatus.PRESENT, both.single().currentStatus)
        val mixed = mergeLabBlocks(listOf(item(6, "BHT553", "LAB", AttendanceStatus.PRESENT), item(7, "BHT553", "LAB", AttendanceStatus.UNMARKED)))
        assertEquals(AttendanceStatus.UNMARKED, mixed.single().currentStatus)
    }

    @Test fun expand_restores_one_item_per_period() {
        val day = listOf(item(1, "BHT501", "LECTURE"), item(3, "BHT551", "LAB"), item(4, "BHT551", "LAB"))
        val round = expandLabBlocks(mergeLabBlocks(day))
        assertEquals(listOf(1, 3, 4), round.map { it.period })
        assertEquals(true, round.all { it.pairedPeriod == null })
    }
}
