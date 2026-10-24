package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import com.example.data.local.AktuHolidays
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.SubjectAttendanceStat
import com.example.data.util.AttendanceLogEntry
import com.example.data.util.mergeLabRecords
import com.example.ui.components.AttendanceHeroCard
import com.example.ui.components.AnimatedBarChart
import com.example.ui.components.AnimatedHorizontalProgressBar
import androidx.compose.foundation.BorderStroke
import com.example.ui.theme.isAppDarkTheme
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.StatusAbsentBgDark
import com.example.ui.theme.StatusAbsentBgLight
import com.example.ui.theme.StatusAbsentDark
import com.example.ui.theme.StatusAbsentLight
import com.example.ui.theme.StatusCancelledBgDark
import com.example.ui.theme.StatusCancelledBgLight
import com.example.ui.theme.StatusCancelledDark
import com.example.ui.theme.StatusCancelledLight
import com.example.ui.theme.StatusIncompleteBgDark
import com.example.ui.theme.StatusIncompleteBgLight
import com.example.ui.theme.StatusIncompleteDark
import com.example.ui.theme.StatusIncompleteLight
import com.example.ui.theme.StatusIncompleteTextDark
import com.example.ui.theme.StatusIncompleteTextLight
import com.example.ui.theme.StatusPresentBgDark
import com.example.ui.theme.StatusPresentBgLight
import com.example.ui.theme.StatusPresentDark
import com.example.ui.theme.StatusPresentLight
import com.example.ui.theme.StatusPresentTextDark
import com.example.ui.theme.StatusPresentTextLight
import com.example.ui.theme.StatusAbsentTextDark
import com.example.ui.theme.StatusAbsentTextLight
import com.example.ui.theme.StatusUnmarkedBgDark
import com.example.ui.theme.StatusUnmarkedBgLight
import com.example.ui.theme.StatusUnmarkedDark
import com.example.ui.theme.StatusUnmarkedLight
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BrightBlue
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.theme.StatusIncomplete
import com.example.ui.theme.StatusIncompleteBg
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.StatusPresentBg
import com.example.ui.theme.StatusUnmarked
import com.example.ui.theme.StatusUnmarkedBg
import com.example.ui.viewmodel.AttendanceViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class CalendarDayStatus {
    FULLY_PRESENT, // Green: All conducted sessions attended
    FULLY_ABSENT,  // Red: All conducted sessions absent
    INCOMPLETE,    // Yellow: Partial attendance (some attended, some missed)
    NO_CLASSES     // Grey: Weekend, holiday, or no classes recorded
}

@Composable
fun ReportsScreen(
    viewModel: AttendanceViewModel,
    onNavigateToDate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val stats by viewModel.overallStats.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val records by viewModel.allAttendanceRecords.collectAsState()
    val overrides by viewModel.allOverrides.collectAsState()

    val isDark = isAppDarkTheme()
    var selectedTabIndex by remember { mutableStateOf(0) } // 0: Subjects, 1: Monthly Calendar, 2: History
    var historyFilter by remember { mutableStateOf("ALL") }

    // Calendar state: Year and Month
    val currentCal = remember { Calendar.getInstance() }
    var calendarYear by remember { mutableStateOf(currentCal.get(Calendar.YEAR)) }
    var calendarMonth by remember { mutableStateOf(currentCal.get(Calendar.MONTH)) } // 0-based

    // Date inspection dialog
    var inspectDate by remember { mutableStateOf<String?>(null) }

    // A two-period lab is listed once (for example "Period 3–4")
    val logEntries = remember(records) { mergeLabRecords(records) }

    val filteredRecords = remember(logEntries, historyFilter) {
        val sorted = logEntries.sortedWith(compareByDescending<AttendanceLogEntry> { it.date }.thenByDescending { it.firstPeriod })
        if (historyFilter == "ALL") sorted else sorted.filter { it.status == historyFilter }
    }

    fun shareReport() {
        coroutineScope.launch {
            val csv = viewModel.exportCsv()
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, csv)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Export Attendance Report"))
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Header with CSV Export
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Attendance Reports",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFF5F8FF) else Color(0xFF102653)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "IIHT Varanasi • 5th Semester BHTT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color(0xFF8EA5C8) else Color(0xFF64748B)
                    )
                }

                Surface(
                    onClick = { shareReport() },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color(0xFF10264A) else Color(0xFFFFFFFF),
                    border = BorderStroke(
                        1.2.dp,
                        if (isDark) Color(0xFF2563EB) else Color(0xFF1769FF)
                    ),
                    shadowElevation = if (isDark) 0.dp else 1.5.dp,
                    modifier = Modifier.testTag("btn_export_csv")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1769FF),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Export CSV",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1769FF)
                        )
                    }
                }
            }
        }

        // Summary Hero Card
        item {
            AttendanceHeroCard(
                percentage = stats.percentage,
                target = stats.targetPercentage,
                present = stats.present,
                conducted = stats.conducted,
                absent = stats.absent,
                cancelled = stats.cancelled,
                classesToTarget = stats.classesToTarget,
                classesCanMiss = stats.classesCanMiss,
                exactPercentage = stats.percentageExact,
                isEstimate = stats.isEstimate
            )
        }

        // Segmented Tabs: Subject-wise vs Monthly Calendar vs History Log
        item {
            Surface(
                shape = RoundedCornerShape(23.dp),
                color = if (isDark) Color(0xFF0B1B36) else Color(0xFFF1F6FC),
                border = BorderStroke(
                    1.dp,
                    if (isDark) Color(0xFF1B3B6B) else Color(0xFFE0EDFB)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabs = listOf(
                        "Subjects (${if (stats.conducted == 0) 0 else stats.subjectStats.size})",
                        "Calendar",
                        "History (${records.size})"
                    )
                    tabs.forEachIndexed { index, tabTitle ->
                        val isSelected = selectedTabIndex == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { selectedTabIndex = index }
                                .testTag("reports_tab_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = tabTitle,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) {
                                        if (isDark) Color(0xFF38BDF8) else Color(0xFF1769FF)
                                    } else {
                                        if (isDark) Color(0xFF8EA5C8) else Color(0xFF64748B)
                                    }
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(46.dp)
                                            .height(3.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.horizontalGradient(
                                                    if (isDark) {
                                                        listOf(Color(0xFF06B6D4), Color(0xFF38BDF8))
                                                    } else {
                                                        listOf(Color(0xFF1769FF), Color(0xFF2563EB))
                                                    }
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedTabIndex == 0) {
            // --- SUBJECT-WISE VIEW ---
            if (stats.conducted == 0 || stats.subjectStats.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            com.example.ui.components.CalendarEmptyIllustration(isDark = isDark)
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "No Attendance Records Yet",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF102653)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "As you mark attendance in the Daily tab, individual subject stats and forecasts will appear here.",
                                fontSize = 12.sp,
                                color = if (isDark) Color(0xFF8EA5C8) else Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.widthIn(max = 280.dp)
                            )
                        }
                    }
                }
            } else {
                item {
                    val chartItems = remember(stats.subjectStats) {
                        stats.subjectStats
                            .filter { stat ->
                                val code = stat.subjectCode.uppercase()
                                val name = stat.subjectName.lowercase()
                                !code.contains("LIB") &&
                                !code.contains("DLIB") &&
                                !code.contains("SEM") &&
                                !code.contains("SWACH") &&
                                !name.contains("library") &&
                                !name.contains("seminar") &&
                                !name.contains("swachh")
                            }
                            .map { it.subjectCode to it.percentage }
                    }
                    AnimatedBarChart(
                        items = chartItems,
                        target = stats.targetPercentage,
                        height = 150.dp
                    )
                }

                items(stats.subjectStats, key = { it.subjectCode }) { subStat ->
                    SubjectReportCard(
                        stat = subStat,
                        target = stats.targetPercentage
                    )
                }
            }
        } else if (selectedTabIndex == 1) {
            // --- MONTHLY CALENDAR VIEW ---
            item {
                MonthlyCalendarComponent(
                    year = calendarYear,
                    month = calendarMonth,
                    records = records,
                    onPrevMonth = {
                        if (calendarMonth == 0) {
                            calendarMonth = 11
                            calendarYear -= 1
                        } else {
                            calendarMonth -= 1
                        }
                    },
                    onNextMonth = {
                        if (calendarMonth == 11) {
                            calendarMonth = 0
                            calendarYear += 1
                        } else {
                            calendarMonth += 1
                        }
                    },
                    onSelectDate = { dateStr ->
                        inspectDate = dateStr
                    }
                )
            }
        } else {
            // --- ATTENDANCE HISTORY LOG VIEW ---
            item {
                // Filter chips row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        "ALL" to "All (${records.size})",
                        "PRESENT" to "Present",
                        "ABSENT" to "Absent",
                        "CANCELLED" to "Cancelled"
                    ).forEach { (flt, label) ->
                        val isSelected = historyFilter == flt
                        val chipColor = when (flt) {
                            "PRESENT" -> if (isSelected) StatusPresent else StatusPresentBg
                            "ABSENT" -> if (isSelected) StatusAbsent else StatusAbsentBg
                            "CANCELLED" -> if (isSelected) AmberAccent else AmberAccent.copy(alpha = 0.15f)
                            else -> if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        }
                        val textCol = when (flt) {
                            "PRESENT" -> if (isSelected) Color.White else StatusPresent
                            "ABSENT" -> if (isSelected) Color.White else StatusAbsent
                            "CANCELLED" -> if (isSelected) Color.White else AmberAccent
                            else -> if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Surface(
                            onClick = { historyFilter = flt },
                            shape = RoundedCornerShape(10.dp),
                            color = chipColor,
                            modifier = Modifier.testTag("history_filter_$flt")
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textCol,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            if (filteredRecords.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().testTag("empty_history_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Records Found",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (historyFilter == "ALL") "Mark attendance in the Attendance tab to start building your log history." else "No records match filter: $historyFilter.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredRecords, key = { "${it.date}_${it.firstPeriod}" }) { rec ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, if (isDark) GlassTokens.GlassBorderDark else GlassTokens.GlassBorderLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 2.5.dp),
                        modifier = Modifier.fillMaxWidth().testTag("history_item_${rec.date}_${rec.firstPeriod}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = rec.date,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${rec.periodLabel}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${rec.subjectCode} - ${rec.subjectName}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                if (rec.notes.isNotBlank()) {
                                    Text(
                                        text = "Note: ${rec.notes}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val (bgCol, fgCol, text) = when (rec.status) {
                                    AttendanceStatus.PRESENT.name -> Triple(
                                        if (isDark) StatusPresentBgDark else StatusPresentBgLight,
                                        if (isDark) StatusPresentDark else StatusPresentLight,
                                        "Present"
                                    )
                                    AttendanceStatus.ABSENT.name -> Triple(
                                        if (isDark) StatusAbsentBgDark else StatusAbsentBgLight,
                                        if (isDark) StatusAbsentDark else StatusAbsentLight,
                                        "Absent"
                                    )
                                    AttendanceStatus.CANCELLED.name -> Triple(
                                        if (isDark) StatusCancelledBgDark else StatusCancelledBgLight,
                                        if (isDark) StatusCancelledDark else StatusCancelledLight,
                                        "Cancelled"
                                    )
                                    else -> Triple(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                        rec.status
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = bgCol,
                                    border = BorderStroke(1.dp, fgCol.copy(alpha = 0.25f)),
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = text,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = fgCol,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onNavigateToDate(rec.date) },
                                    modifier = Modifier.size(28.dp).testTag("btn_view_date_${rec.date}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Jump to date",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Inspect date dialog
    if (inspectDate != null) {
        val dt = inspectDate!!
        val officialHoliday = AktuHolidays.getHoliday(dt)

        if (officialHoliday != null) {
            AlertDialog(
                onDismissRequest = { inspectDate = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AKTU Official Holiday",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = officialHoliday.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
                        )
                        Text(
                            text = "Date: ${officialHoliday.date}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "As per the official AKTU academic calendar, this day is an authorized university holiday. Classes are suspended and attendance marking is disabled.",
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    OutlinedButton(onClick = { inspectDate = null }) {
                        Text("Close")
                    }
                }
            )
        } else {
            val dateRecords = logEntries.filter { it.date == dt }
            val presentCount = dateRecords.count { it.status == AttendanceStatus.PRESENT.name }
            val absentCount = dateRecords.count { it.status == AttendanceStatus.ABSENT.name }
            val cancelledCount = dateRecords.count { it.status == AttendanceStatus.CANCELLED.name }

            AlertDialog(
                onDismissRequest = { inspectDate = null },
                title = {
                    Column {
                        Text(
                            text = "Attendance for $dt",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (dateRecords.isNotEmpty()) {
                            Text(
                                text = "$presentCount Present • $absentCount Absent • $cancelledCount Cancelled",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (dateRecords.isEmpty()) {
                            Text(
                                text = "No recorded attendance for this date. Tap below to mark attendance.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        } else {
                            dateRecords.sortedBy { it.firstPeriod }.forEach { r ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${r.periodLabel}: ${r.subjectName}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(text = r.subjectCode, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Surface(
                                        color = when (r.status) {
                                            AttendanceStatus.PRESENT.name -> if (isDark) StatusPresentBgDark else StatusPresentBgLight
                                            AttendanceStatus.ABSENT.name -> if (isDark) StatusAbsentBgDark else StatusAbsentBgLight
                                            AttendanceStatus.CANCELLED.name -> if (isDark) StatusCancelledBgDark else StatusCancelledBgLight
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = r.status,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (r.status) {
                                                AttendanceStatus.PRESENT.name -> if (isDark) StatusPresentDark else StatusPresentLight
                                                AttendanceStatus.ABSENT.name -> if (isDark) StatusAbsentDark else StatusAbsentLight
                                                AttendanceStatus.CANCELLED.name -> if (isDark) StatusCancelledDark else StatusCancelledLight
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.setSelectedDate(dt)
                        inspectDate = null
                        onNavigateToDate(dt)
                    }) {
                        Text("Open Daily View")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { inspectDate = null }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun SubjectReportCard(
    stat: SubjectAttendanceStat,
    target: Int,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()
    val isBelow = stat.isBelowTarget
    val progress = if (stat.conducted > 0) (stat.percentage / 100f).coerceIn(0f, 1f) else 0f

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 2.5.dp),
        border = BorderStroke(1.dp, if (isDark) GlassTokens.GlassBorderDark else GlassTokens.GlassBorderLight),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stat.subjectName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stat.subjectCode,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "%.1f%%".format(stat.percentage),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isBelow) (if (isDark) StatusAbsentDark else StatusAbsentLight) else (if (isDark) StatusPresentDark else StatusPresentLight)
                    )
                    Text(
                        text = "${stat.present} / ${stat.conducted} sessions",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedHorizontalProgressBar(
                percentage = stat.percentage,
                target = target,
                height = 6.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                customGradient = com.example.ui.components.SubjectGradients.getHorizontalGradient(stat.subjectCode)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Guidance tip for this subject
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isBelow) {
                    val alertColor = if (isDark) StatusAbsentDark else StatusAbsentLight
                    Icon(Icons.Default.Warning, contentDescription = null, tint = alertColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Below target ($target%). Attend ${stat.classesToTarget} more sessions to reach target.",
                        fontSize = 11.sp,
                        color = alertColor,
                        fontWeight = FontWeight.Medium
                    )
                } else if (stat.classesCanMiss > 0) {
                    Text(
                        text = "Eligible. You can afford to miss ${stat.classesCanMiss} upcoming sessions.",
                        fontSize = 11.sp,
                        color = if (isDark) StatusPresentDark else StatusPresentLight,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "Eligible (Meets minimum $target% target requirement).",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyCalendarComponent(
    year: Int,
    month: Int,
    records: List<AttendanceRecord>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (String) -> Unit
) {
    val cal = remember(year, month) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val monthName = remember(year, month) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday

    // Map of records by date
    val recordsByDate = remember(records) {
        records.groupBy { it.date }
    }

    val isDark = isAppDarkTheme()
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isDark) GlassTokens.GlassBorderDark else GlassTokens.GlassBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 2.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month Header Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevMonth) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                }

                Text(
                    text = monthName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Day of week labels (S M T W T F S)
            val weekDayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
            Row(modifier = Modifier.fillMaxWidth()) {
                weekDayLabels.forEach { label ->
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Days Grid (6 rows of 7)
            val startOffset = firstDayOfWeek - 1 // 0-based offset for Sunday

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (row in 0 until 6) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNum = cellIndex - startOffset + 1

                            if (dayNum in 1..daysInMonth) {
                                val dateStr = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayNum)
                                val dayRecords = recordsByDate[dateStr] ?: emptyList()

                                val dayStatus = if (dayRecords.isEmpty()) {
                                    CalendarDayStatus.NO_CLASSES
                                } else {
                                    val academicRecords = dayRecords.filter { r ->
                                        val code = r.subjectCode.uppercase()
                                        val name = r.subjectName.lowercase()
                                        !code.contains("LIB") && !code.contains("DLIB") && !code.contains("SWACH") &&
                                        !name.contains("library") && !name.contains("swachh") &&
                                        !code.contains("DOUBT") && !name.contains("doubt")
                                    }
                                    val effectiveRecords = if (academicRecords.isNotEmpty()) academicRecords else dayRecords
                                    val presentCount = effectiveRecords.count { it.status == AttendanceStatus.PRESENT.name }
                                    val absentCount = effectiveRecords.count { it.status == AttendanceStatus.ABSENT.name }

                                    when {
                                        absentCount > 0 -> CalendarDayStatus.FULLY_ABSENT  // Red when absent on this day
                                        presentCount > 0 -> CalendarDayStatus.FULLY_PRESENT // Green when present
                                        else -> CalendarDayStatus.NO_CLASSES                // Grey
                                    }
                                }

                                val isOfficialHoliday = AktuHolidays.isOfficialHoliday(dateStr)

                                val (cellBg, cellText) = if (isOfficialHoliday) {
                                    Pair(
                                        if (isDark) Color(0x40F59E0B) else Color(0x33F59E0B),
                                        if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
                                    )
                                } else when (dayStatus) {
                                    CalendarDayStatus.FULLY_PRESENT -> Pair(
                                        if (isDark) StatusPresentBgDark else StatusPresentBgLight,
                                        if (isDark) StatusPresentTextDark else StatusPresentTextLight
                                    )
                                    CalendarDayStatus.FULLY_ABSENT -> Pair(
                                        if (isDark) Color(0x3DF87171) else Color(0x2EFEE2E2),
                                        if (isDark) Color(0xFFF87171) else Color(0xFFB91C1C)
                                    )
                                    CalendarDayStatus.INCOMPLETE -> Pair(
                                        if (isDark) StatusIncompleteBgDark else StatusIncompleteBgLight,
                                        if (isDark) StatusIncompleteTextDark else StatusIncompleteTextLight
                                    )
                                    CalendarDayStatus.NO_CLASSES -> Pair(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    onClick = { onSelectDate(dateStr) },
                                    shape = CircleShape,
                                    color = cellBg,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isOfficialHoliday) Color(0xFFF59E0B).copy(alpha = 0.6f) else cellText.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "$dayNum",
                                                fontSize = if (isOfficialHoliday) 11.sp else 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = cellText
                                            )
                                            if (isOfficialHoliday) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(3.5.dp)
                                                        .clip(CircleShape)
                                                        .background(cellText)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Calendar Visual Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                LegendItem(color = if (isDark) StatusPresentDark else StatusPresentLight, label = "Present")
                LegendItem(color = if (isDark) StatusAbsentDark else StatusAbsentLight, label = "Absent")
                LegendItem(color = if (isDark) StatusIncompleteDark else StatusIncompleteLight, label = "Incomplete")
                LegendItem(color = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706), label = "AKTU Holiday")
                LegendItem(color = MaterialTheme.colorScheme.onSurfaceVariant, label = "No Class")
            }

            // AKTU Official Holidays (This Month) - Sleek Pill-Shaped Background in Normal Size
            val holidaysThisMonth = remember(year, month) {
                AktuHolidays.OFFICIAL_HOLIDAYS.filter { h ->
                    try {
                        val parts = h.date.split("-")
                        val hYear = parts[0].toInt()
                        val hMonth = parts[1].toInt() - 1
                        hYear == year && hMonth == month
                    } catch (_: Exception) {
                        false
                    }
                }
            }

            if (holidaysThisMonth.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "AKTU Official Holidays (This Month)",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    holidaysThisMonth.forEach { hol ->
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (isDark) Color(0x2EF59E0B) else Color(0x1DF59E0B),
                            border = BorderStroke(1.dp, if (isDark) Color(0x66F59E0B) else Color(0x40F59E0B))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${hol.date.takeLast(2)} ${SimpleDateFormat("MMM", Locale.US).format(cal.time)} • ${hol.name}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Holiday",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
