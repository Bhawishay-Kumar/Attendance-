package com.example.ui.screens.whatif

import org.junit.Assert.assertEquals
import org.junit.Test

class WhatIfLabMergeTest {

    private fun c(slot: Long, period: Int, code: String, type: String, always: Boolean = false) =
        WhatIfClassItem(slotId = slot, period = period, periodRoman = "P$period", startTime = "s$period",
            endTime = "e$period", subjectCode = code, subjectName = code, classType = type, isAlwaysPresent = always)

    @Test fun lab_block_becomes_one_selectable_class_worth_two_periods() {
        val m = mergeWhatIfLabBlocks(listOf(c(1, 1, "BHT501", "LECTURE"), c(7, 3, "BHT551", "LAB"), c(8, 4, "BHT551", "LAB")))
        assertEquals(2, m.size)
        val lab = m[1]
        assertEquals(7L, lab.slotId); assertEquals(2, lab.periodCount)
        assertEquals("P3–P4", lab.periodRoman); assertEquals("s3", lab.startTime); assertEquals("e4", lab.endTime)
    }

    @Test fun other_classes_are_untouched() {
        assertEquals(2, mergeWhatIfLabBlocks(listOf(c(1, 3, "BHT551", "LAB"), c(2, 4, "BHT552", "LAB"))).size)
        assertEquals(2, mergeWhatIfLabBlocks(listOf(c(1, 1, "BHT501", "LECTURE"), c(2, 2, "BHT501", "LECTURE"))).size)
    }

    @Test fun skipping_a_lab_block_counts_two_periods() {
        val m = mergeWhatIfLabBlocks(listOf(c(7, 3, "BHT551", "LAB"), c(8, 4, "BHT551", "LAB"), c(9, 5, "BHT502-T", "TUTORIAL")))
        val plan = WhatIfDayPlan("2026-10-08", "Thursday", m, missedSlotIds = setOf(7L))
        assertEquals(2, plan.missedCount); assertEquals(1, plan.attendedCount)
    }
}
