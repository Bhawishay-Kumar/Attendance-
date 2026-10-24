package com.example.data.util

import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatusTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AttendanceCalculationEngineTest {

    private var seq = 0
    private fun rec(code: String, status: String, name: String = code, weight: Float = 1.0f) =
        AttendanceRecord(date = "2026-09-%02d".format(1 + seq / 8), period = 1 + seq++ % 8,
            subjectCode = code, subjectName = name, status = status, weight = weight)

    private fun recs(code: String, present: Int, absent: Int, name: String = code) =
        List(present) { rec(code, "PRESENT", name) } + List(absent) { rec(code, "ABSENT", name) }

    @Test fun theory_weight_one() {
        val s = AttendanceCalculationEngine.calculateOverallStats(recs("HTFT501", 3, 1), 75)
        assertEquals(4.0, s.weightedConducted, 0.0); assertEquals(3.0, s.weightedPresent, 0.0)
        assertEquals(75.0, s.percentageExact, 1e-9); assertEquals(AttendanceStatusTier.SAFE, s.statusTier)
    }

    @Test fun lab_block_of_two_periods_is_two_credits() {
        // A lab block is two periods; each period is one normal class, so the block is 2 credits.
        val s = AttendanceCalculationEngine.calculateOverallStats(
            recs("BHT551", 2, 0, "Weaving Lab") + recs("HTFT501", 0, 1), 75)
        assertEquals(3.0, s.weightedConducted, 0.0); assertEquals(2.0, s.weightedPresent, 0.0)
        assertEquals(200.0 / 3.0, s.percentageExact, 1e-9)
    }

    @Test fun legacy_stored_lab_weight_of_two_is_ignored() {
        val old = listOf(rec("BHT553", "PRESENT", "Fibre Lab", weight = 2.0f),
                         rec("BHT553", "PRESENT", "Fibre Lab", weight = 2.0f))
        val s = AttendanceCalculationEngine.calculateOverallStats(old, 75)
        assertEquals(2.0, s.weightedConducted, 0.0)
        assertEquals(1.0f, AttendanceCalculationEngine.resolveSessionWeight("BHT551", "", "LAB", 2.0f), 0f)
    }

    @Test fun absent_lab_block_costs_two_credits() {
        val s = AttendanceCalculationEngine.calculateOverallStats(
            recs("HTFT501", 6, 0) + recs("BHT552", 0, 2, "Chemical Lab"), 75)
        assertEquals(8.0, s.weightedConducted, 0.0); assertEquals(6.0, s.weightedPresent, 0.0)
        assertEquals(AttendanceStatusTier.SAFE, s.statusTier)
    }

    @Test fun lab_word_match_is_not_substring() {
        assertFalse(AttendanceCalculationEngine.isLabSession("HTFT501", "Collaborative Design"))
        assertTrue(AttendanceCalculationEngine.isLabSession("XYZ1", "Dyeing Laboratory"))
    }

    @Test fun auto_credit_adds_equally_and_never_absent() {
        val s = AttendanceCalculationEngine.calculateOverallStats(
            listOf(rec("LIB", "ABSENT", "Library"), rec("SEM", "PRESENT", "Seminar")) + recs("HTFT501", 1, 1), 75)
        assertEquals(4.0, s.weightedConducted, 0.0); assertEquals(3.0, s.weightedPresent, 0.0)
        assertEquals(1, s.absent)
    }

    @Test fun swachhta_and_doubt_sessions_never_count() {
        val s = AttendanceCalculationEngine.calculateOverallStats(
            listOf(rec("SWACH", "PRESENT", "Swachhta Abhiyan"), rec("SWACH", "ABSENT", "Swachhta Abhiyan"),
                   rec("DOUBT", "ABSENT", "Doubt Clearing Session")) + recs("HTFT501", 1, 1), 75)
        assertEquals(2.0, s.weightedConducted, 0.0); assertEquals(1.0, s.weightedPresent, 0.0)
        assertTrue(AttendanceCalculationEngine.isExcludedFromAttendance("SWACH", "Swachhta Abhiyan"))
        assertFalse(AttendanceCalculationEngine.isAutoCredited("SWACH", "Swachhta Abhiyan"))
        assertEquals(0f, AttendanceCalculationEngine.resolveSessionWeight("SWACH", "Swachhta Abhiyan"), 0f)
    }

    @Test fun cancelled_and_unmarked_excluded() {
        val s = AttendanceCalculationEngine.calculateOverallStats(
            recs("HTFT501", 1, 1) + rec("HTFT501", "CANCELLED") + rec("HTFT501", "UNMARKED"), 75)
        assertEquals(2.0, s.weightedConducted, 0.0); assertEquals(1, s.cancelled); assertEquals(1, s.absent)
    }

    @Test fun zero_classes_is_no_data() {
        val s = AttendanceCalculationEngine.calculateOverallStats(emptyList(), 75)
        assertEquals(AttendanceStatusTier.NO_DATA, s.statusTier)
        assertEquals(0, s.classesToTarget); assertEquals(0, s.classesCanMiss)
    }

    @Test fun tutorial_merges_with_lecture_subject() {
        val s = AttendanceCalculationEngine.calculateOverallStats(
            recs("BHT502", 2, 0) + recs("BHT502-T", 0, 1), 75)
        assertEquals(1, s.subjectStats.size)
        assertEquals(3.0, s.subjectStats[0].weightedConducted, 0.0)
        assertEquals("BHT502", s.subjectStats[0].subjectCode)
    }

    @Test fun overall_is_pooled_not_average_of_subjects() {
        val s = AttendanceCalculationEngine.calculateOverallStats(recs("A1", 1, 0) + recs("B1", 3, 6), 75)
        assertEquals(40.0, s.percentageExact, 1e-9) // 4/10, not (100+33.3)/2
    }

    @Test fun tier_boundaries_use_unrounded_values() {
        val t = AttendanceCalculationEngine::determineStatusTier
        assertEquals(AttendanceStatusTier.SAFE, t(75.0, 4.0, 75.0))
        assertEquals(AttendanceStatusTier.WARNING, t(74.999, 4.0, 75.0))
        assertEquals(AttendanceStatusTier.WARNING, t(70.0, 4.0, 75.0))
        assertEquals(AttendanceStatusTier.SHORTFALL, t(69.999, 4.0, 75.0))
        assertEquals(AttendanceStatusTier.SHORTFALL, t(84.9, 4.0, 90.0))
        assertEquals(AttendanceStatusTier.WARNING, t(85.0, 4.0, 90.0))
    }

    @Test fun recovery_equal_weight() {
        // A=6, C=10, T=.75 -> ceil((7.5-6)/.25)=6 ; check (12/16)=.75
        assertEquals(6, AttendanceCalculationEngine.calculateRecoveryClasses(6.0, 10.0, 75.0))
        assertEquals(0, AttendanceCalculationEngine.calculateRecoveryClasses(15.0, 20.0, 75.0))
    }

    @Test fun recovery_mixed_weights_uses_timetable_order() {
        // A=6, C=10: +2 (lab) -> 8/12=.667 ; +1 -> 9/13=.692 ; +2 -> 11/15=.733 ; +1 -> 12/16=.75
        assertEquals(4, AttendanceCalculationEngine.calculateRecoveryClasses(
            6.0, 10.0, 75.0, upcomingWeights = listOf(2f, 1f, 2f, 1f)))
    }

    @Test fun safe_miss_equal_weight_and_boundary() {
        assertEquals(4, AttendanceCalculationEngine.calculateSafeMissClasses(18.0, 20.0, 75.0))
        // exactly at target: cannot miss any
        assertEquals(0, AttendanceCalculationEngine.calculateSafeMissClasses(15.0, 20.0, 75.0))
        assertEquals(0, AttendanceCalculationEngine.calculateSafeMissClasses(27.0, 30.0, 90.0))
        assertEquals(0, AttendanceCalculationEngine.calculateSafeMissClasses(14.0, 20.0, 75.0)) // below target
    }

    @Test fun safe_miss_never_exceeds_affordable() {
        val a = 18.0; val c = 20.0
        val m = AttendanceCalculationEngine.calculateSafeMissClasses(a, c, 75.0)
        assertTrue(a / (c + m) >= 0.75); assertTrue(a / (c + m + 1) < 0.75)
        // mixed weights: labs weigh 2 so fewer can be missed
        assertEquals(2, AttendanceCalculationEngine.calculateSafeMissClasses(
            18.0, 20.0, 75.0, upcomingWeights = listOf(2f, 2f, 2f, 1f, 1f)))
    }

    @Test fun estimate_flag_when_no_upcoming_timetable() {
        assertTrue(AttendanceCalculationEngine.calculateOverallStats(recs("A1", 1, 1), 75).isEstimate)
        assertFalse(AttendanceCalculationEngine.calculateOverallStats(recs("A1", 1, 1), 75, listOf(1f)).isEstimate)
    }

    @Test fun weightedTotals_matches_master_stats() {
        val r = recs("BHT551", 1, 1, "Lab") + recs("HTFT501", 2, 1) + rec("LIB", "ABSENT", "Library")
        val s = AttendanceCalculationEngine.calculateOverallStats(r, 75)
        val (a, c) = AttendanceCalculationEngine.weightedTotals(r)
        assertEquals(s.weightedPresent, a, 0.0); assertEquals(s.weightedConducted, c, 0.0)
    }
}
