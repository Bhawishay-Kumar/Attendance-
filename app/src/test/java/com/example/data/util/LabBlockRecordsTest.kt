package com.example.data.util

import com.example.data.model.AttendanceRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class LabBlockRecordsTest {

    private fun r(date: String, period: Int, code: String, status: String, name: String = code) =
        AttendanceRecord(date = date, period = period, subjectCode = code, subjectName = name, status = status)

    @Test fun two_lab_periods_with_same_status_become_one_entry() {
        val e = mergeLabRecords(listOf(
            r("2026-10-08", 1, "BHT501", "PRESENT"),
            r("2026-10-08", 3, "BHT551", "ABSENT", "Woven Fabric Lab"),
            r("2026-10-08", 4, "BHT551", "ABSENT", "Woven Fabric Lab")))
        assertEquals(2, e.size)
        val lab = e.first { it.subjectCode == "BHT551" }
        assertEquals("Period 3–4", lab.periodLabel); assertEquals("ABSENT", lab.status)
        assertEquals("Period 1", e.first { it.subjectCode == "BHT501" }.periodLabel)
    }

    @Test fun different_status_other_subject_or_other_day_stay_separate() {
        assertEquals(2, mergeLabRecords(listOf(r("d", 3, "BHT551", "PRESENT"), r("d", 4, "BHT551", "ABSENT"))).size)
        assertEquals(2, mergeLabRecords(listOf(r("d", 3, "BHT551", "PRESENT"), r("d", 4, "BHT552", "PRESENT"))).size)
        assertEquals(2, mergeLabRecords(listOf(r("d1", 3, "BHT551", "PRESENT"), r("d2", 4, "BHT551", "PRESENT"))).size)
    }

    @Test fun lectures_are_never_merged() {
        assertEquals(2, mergeLabRecords(listOf(r("d", 1, "BHT501", "PRESENT"), r("d", 2, "BHT501", "PRESENT"))).size)
    }

    @Test fun merging_does_not_change_the_calculation_input() {
        val recs = listOf(r("d", 3, "BHT551", "PRESENT"), r("d", 4, "BHT551", "PRESENT"))
        mergeLabRecords(recs)
        assertEquals(2.0, AttendanceCalculationEngine.weightedTotals(recs).second, 0.0)
    }
}
