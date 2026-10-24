package com.example.data.local

import com.example.data.model.ClassType
import com.example.data.model.PeriodTiming
import com.example.data.model.TimetableSlot

object InitialTimetableData {

    val PERIOD_TIMINGS = listOf(
        PeriodTiming(1, "I", "09:30 AM", "10:20 AM"),
        PeriodTiming(2, "II", "10:20 AM", "11:10 AM"),
        // Water break 11:10 AM - 11:20 AM
        PeriodTiming(3, "III", "11:20 AM", "12:10 PM"),
        PeriodTiming(4, "IV", "12:10 PM", "01:00 PM"),
        // Lunch break 01:00 PM - 02:00 PM
        PeriodTiming(5, "V", "02:00 PM", "02:50 PM"),
        PeriodTiming(6, "VI", "02:50 PM", "03:40 PM"),
        // Water break 03:40 PM - 03:50 PM
        PeriodTiming(7, "VII", "03:50 PM", "04:40 PM"),
        PeriodTiming(8, "VIII", "04:40 PM", "05:30 PM")
    )

    fun getTimingForPeriod(period: Int): Pair<String, String> {
        val pt = PERIOD_TIMINGS.find { it.period == period }
        return if (pt != null) Pair(pt.startTime, pt.endTime) else Pair("09:30 AM", "10:20 AM")
    }

    fun getPeriodRoman(period: Int): String {
        return when (period) {
            1 -> "I"
            2 -> "II"
            3 -> "III"
            4 -> "IV"
            5 -> "V"
            6 -> "VI"
            7 -> "VII"
            8 -> "VIII"
            else -> "$period"
        }
    }

    fun getDefaultSlots(versionId: Int = 1): List<TimetableSlot> {
        val slots = mutableListOf<TimetableSlot>()

        // Helper
        fun add(
            day: String,
            period: Int,
            code: String,
            name: String,
            type: ClassType,
            batch: String = "ALL",
            counts: Boolean = true,
            notes: String = ""
        ) {
            val (st, et) = getTimingForPeriod(period)
            slots.add(
                TimetableSlot(
                    dayOfWeek = day,
                    period = period,
                    startTime = st,
                    endTime = et,
                    subjectCode = code,
                    subjectName = name,
                    classType = type.name,
                    batch = batch,
                    countsInAttendance = counts,
                    versionId = versionId,
                    notes = notes
                )
            )
        }

        // --- MONDAY ---
        add("MONDAY", 1, "BHT503", "Quality Evaluation of Fibers and Yarns", ClassType.LECTURE)
        add("MONDAY", 2, "BHT055", "Knitting and Nonwoven Technology", ClassType.LECTURE)
        add("MONDAY", 3, "BHT051", "Garment Manufacturing Technology", ClassType.LECTURE)
        add("MONDAY", 4, "BHT501", "Woven Fabric Manufacture", ClassType.LECTURE)
        add("MONDAY", 5, "HTFT501", "HTFT501 / MNPM501 / Digital Library — CC / PP&C, MPD / MNC", ClassType.LECTURE)
        add("MONDAY", 6, "BHT502", "Chemical Processing of Textiles-II", ClassType.LECTURE)
        add("MONDAY", 7, "LIB", "Library", ClassType.LIBRARY, counts = true)

        // --- TUESDAY ---
        add("TUESDAY", 1, "BHT051", "Garment Manufacturing Technology", ClassType.LECTURE)
        add("TUESDAY", 2, "LIB", "Library", ClassType.LIBRARY, counts = true)
        add("TUESDAY", 3, "HTFT501", "HTFT501 / MNPM501 / Digital Library — CC / PP&C, MPD / MNC", ClassType.LECTURE)
        add("TUESDAY", 4, "BHT502", "Chemical Processing of Textiles-II", ClassType.LECTURE)
        add("TUESDAY", 5, "BHT055", "Knitting and Nonwoven Technology", ClassType.LECTURE)
        add("TUESDAY", 6, "LIB", "Library", ClassType.LIBRARY, counts = true)
        add("TUESDAY", 7, "BHT503", "Quality Evaluation of Fibers and Yarns", ClassType.LECTURE)

        // --- WEDNESDAY ---
        add("WEDNESDAY", 1, "BNC501", "Constitution of India", ClassType.LECTURE)
        add("WEDNESDAY", 2, "BHT503", "Quality Evaluation of Fibers and Yarns", ClassType.LECTURE)
        add("WEDNESDAY", 3, "BHT501", "Woven Fabric Manufacture", ClassType.LECTURE)
        add("WEDNESDAY", 4, "BHT502", "Chemical Processing of Textiles-II", ClassType.LECTURE)
        add("WEDNESDAY", 5, "BHT051", "Garment Manufacturing Technology", ClassType.LECTURE)
        // VI–VII Labs / Seminar split by batch
        add("WEDNESDAY", 6, "BHT553", "BHT553 Fibre & Yarn Quality Evaluation Lab", ClassType.LAB, batch = "A")
        add("WEDNESDAY", 6, "SEM", "Seminar", ClassType.SEMINAR, batch = "B")
        add("WEDNESDAY", 7, "BHT553", "BHT553 Fibre & Yarn Quality Evaluation Lab", ClassType.LAB, batch = "A")
        add("WEDNESDAY", 7, "SEM", "Seminar", ClassType.SEMINAR, batch = "B")

        // --- THURSDAY ---
        add("THURSDAY", 1, "BHT501", "Woven Fabric Manufacture", ClassType.LECTURE)
        add("THURSDAY", 2, "HTFT501", "HTFT501 / MNPM501 / Digital Library — CC / PP&C, MPD / MNC", ClassType.LECTURE)
        // III–IV Labs split
        add("THURSDAY", 3, "BHT551", "BHT551 Woven Fabric Manufacture Lab", ClassType.LAB, batch = "A")
        add("THURSDAY", 3, "BHT552", "BHT552 Chemical Processing of Textiles-II Lab", ClassType.LAB, batch = "B")
        add("THURSDAY", 4, "BHT551", "BHT551 Woven Fabric Manufacture Lab", ClassType.LAB, batch = "A")
        add("THURSDAY", 4, "BHT552", "BHT552 Chemical Processing of Textiles-II Lab", ClassType.LAB, batch = "B")
        add("THURSDAY", 5, "BHT502-T", "BHT502 — Chemical Processing of Textiles-II Tutorial", ClassType.TUTORIAL)
        // VI–VII Labs / Seminar split
        add("THURSDAY", 6, "SEM", "Seminar", ClassType.SEMINAR, batch = "A")
        add("THURSDAY", 6, "BHT553", "BHT553 Fibre & Yarn Quality Evaluation Lab", ClassType.LAB, batch = "B")
        add("THURSDAY", 7, "SEM", "Seminar", ClassType.SEMINAR, batch = "A")
        add("THURSDAY", 7, "BHT553", "BHT553 Fibre & Yarn Quality Evaluation Lab", ClassType.LAB, batch = "B")

        // --- FRIDAY ---
        add("FRIDAY", 1, "BNC501", "Constitution of India", ClassType.LECTURE)
        add("FRIDAY", 2, "HTFT501-T", "HTFT501 / MNPM501 / Digital Library — Tutorial, CC / PP&C, MPD / MNC", ClassType.TUTORIAL)
        // III–IV Labs split
        add("FRIDAY", 3, "BHT552", "BHT552 Chemical Processing of Textiles-II Lab", ClassType.LAB, batch = "A")
        add("FRIDAY", 3, "BHT551", "BHT551 Woven Fabric Manufacture Lab", ClassType.LAB, batch = "B")
        add("FRIDAY", 4, "BHT552", "BHT552 Chemical Processing of Textiles-II Lab", ClassType.LAB, batch = "A")
        add("FRIDAY", 4, "BHT551", "BHT551 Woven Fabric Manufacture Lab", ClassType.LAB, batch = "B")
        add("FRIDAY", 5, "BHT501-T", "BHT501 — Woven Fabric Manufacture Tutorial", ClassType.TUTORIAL)
        add("FRIDAY", 6, "BHT503-T", "BHT503 — Quality Evaluation of Fibers and Yarns Tutorial", ClassType.TUTORIAL)
        add("FRIDAY", 7, "BHT055", "Knitting and Nonwoven Technology", ClassType.LECTURE)

        return slots
    }
}
