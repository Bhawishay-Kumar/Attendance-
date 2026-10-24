package com.example.data.local

import com.example.data.util.AttendanceCalculationEngine
import java.time.DayOfWeek
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class InitialTimetableDataTest {

    private val slots = InitialTimetableData.getDefaultSlots()

    @Test fun swachhta_and_doubt_are_not_in_the_timetable() {
        assertFalse(slots.any { it.subjectCode.equals("SWACH", true) || it.subjectCode.equals("DOUBT", true) })
        assertFalse(slots.any { it.subjectName.contains("Swachh", true) || it.subjectName.contains("Doubt", true) })
    }

    @Test fun every_batch_has_seven_credits_a_day_from_monday_to_friday() {
        for (batch in listOf("A", "B")) {
            for (day in DayOfWeek.values().filter { it.value <= 5 }) {
                val credits = slots
                    .filter { it.dayOfWeek == day.name && (it.batch == "ALL" || it.batch == batch) }
                    .filter { AttendanceCalculationEngine.resolveSessionWeight(it.subjectCode, it.subjectName, it.classType) > 0f }
                    .sumOf { AttendanceCalculationEngine.resolveSessionWeight(it.subjectCode, it.subjectName, it.classType).toDouble() }
                assertEquals("$batch $day", 7.0, credits, 0.0)
            }
        }
    }

    @Test fun october_2026_has_140_credits_over_20_teaching_days() {
        val holidays = setOf(LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 20))
        for (batch in listOf("A", "B")) {
            var days = 0; var credits = 0.0
            var d = LocalDate.of(2026, 10, 1)
            while (d.monthValue == 10) {
                if (d.dayOfWeek.value <= 5 && d !in holidays) {
                    days++
                    credits += slots.filter { it.dayOfWeek == d.dayOfWeek.name && (it.batch == "ALL" || it.batch == batch) }
                        .sumOf { AttendanceCalculationEngine.resolveSessionWeight(it.subjectCode, it.subjectName, it.classType).toDouble() }
                }
                d = d.plusDays(1)
            }
            assertEquals(20, days); assertEquals("batch $batch", 140.0, credits, 0.0)
        }
    }
}
