package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AktuHolidays
import com.example.data.model.AttendanceStatus
import com.example.data.model.ClassType
import com.example.data.model.TimetableSlot
import com.example.ui.components.AppConfirmationDialog
import com.example.ui.components.BulkAttendanceCard
import com.example.ui.components.ClassSessionCard
import com.example.ui.components.GlassCard
import com.example.ui.components.StaggeredEntrance
import com.example.ui.components.rememberHaptic
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.isAppDarkTheme
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.ui.viewmodel.TimetableScope
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DailyScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()
    val context = LocalContext.current
    val haptic = rememberHaptic()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dailyItems by viewModel.dailyAttendanceItems.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val overrides by viewModel.allOverrides.collectAsState()

    var showMarkAllPresentDialog by remember { mutableStateOf(false) }
    var showMarkAllAbsentDialog by remember { mutableStateOf(false) }
    var showMarkAllNoClassDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showAddExtraClassDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayDate = remember(selectedDate) {
        try {
            val d = dateFormat.parse(selectedDate) ?: Date()
            SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(d)
        } catch (_: Exception) {
            selectedDate
        }
    }

    val officialHoliday = AktuHolidays.getHoliday(selectedDate)
    val customHoliday = overrides.find { it.date == selectedDate && it.isHoliday }
    val isHoliday = officialHoliday != null || customHoliday != null
    val holidayName = officialHoliday?.name ?: customHoliday?.holidayName ?: "Official Holiday"

    fun openDatePicker() {
        val cal = Calendar.getInstance().apply {
            time = dateFormat.parse(selectedDate) ?: Date()
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                viewModel.setSelectedDate(dateFormat.format(newCal.time))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Date Navigation Card (.date matching HTML prototype)
        item {
            StaggeredEntrance(index = 0) {
                GlassCard(isDark = isDark, contentPadding = PaddingValues(14.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Row 1: < Arrow | Date Chip | > Arrow
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Previous Day Arrow
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GlassTokens.chipColor(isDark))
                                    .border(1.5.dp, GlassTokens.chipBorderColor(isDark), RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptic.select()
                                        viewModel.stepDay(-1)
                                    }
                                    .testTag("btn_prev_day"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "Previous Day",
                                    tint = Color(0xFF6F9BFF),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Big Date Chip (Click opens DatePicker)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GlassTokens.chipColor(isDark))
                                    .border(1.5.dp, GlassTokens.chipBorderColor(isDark), RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptic.select()
                                        openDatePicker()
                                    }
                                    .testTag("btn_date_picker"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = Color(0xFF5B8CFF),
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = displayDate,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GlassTokens.textColor(isDark),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Next Day Arrow
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GlassTokens.chipColor(isDark))
                                    .border(1.5.dp, GlassTokens.chipBorderColor(isDark), RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptic.select()
                                        viewModel.stepDay(1)
                                    }
                                    .testTag("btn_next_day"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Next Day",
                                    tint = Color(0xFF6F9BFF),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Row 2: Today Chip | Batch Chip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Today Button Chip
                            Box(
                                modifier = Modifier
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GlassTokens.chipColor(isDark))
                                    .border(1.5.dp, GlassTokens.chipBorderColor(isDark), RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptic.select()
                                        viewModel.setToday()
                                    }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Today,
                                        contentDescription = null,
                                        tint = Color(0xFF5B8CFF),
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Today",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GlassTokens.chipFgColor(isDark)
                                    )
                                }
                            }

                            // Batch Chip
                            Box(
                                modifier = Modifier
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GlassTokens.chipColor(isDark))
                                    .border(1.5.dp, GlassTokens.chipBorderColor(isDark), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Group,
                                        contentDescription = null,
                                        tint = Color(0xFF5B8CFF),
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Batch ${profile.batch}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GlassTokens.chipFgColor(isDark)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Holiday Banner if date is marked holiday
        // 2. Holiday Banner if date is marked holiday
        if (isHoliday) {
            item {
                StaggeredEntrance(index = 1) {
                    GlassCard(isDark = isDark) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x33FFAA3C)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EventBusy,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFFFFB347) else Color(0xFFD97706),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = holidayName,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.5.sp,
                                    color = GlassTokens.textColor(isDark)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Official Holiday • Classes are suspended. Attendance marking is disabled.",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GlassTokens.mutedColor(isDark)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Bulk Attendance Actions Card (.bulk matching HTML prototype)
        if (!isHoliday && dailyItems.isNotEmpty()) {
            item {
                StaggeredEntrance(index = 2) {
                    BulkAttendanceCard(
                        items = dailyItems,
                        onBulkMark = { status ->
                            when (status) {
                                AttendanceStatus.PRESENT -> showMarkAllPresentDialog = true
                                AttendanceStatus.ABSENT -> showMarkAllAbsentDialog = true
                                AttendanceStatus.CANCELLED -> showMarkAllNoClassDialog = true
                                AttendanceStatus.UNMARKED -> showResetDialog = true
                            }
                        },
                        onUndo = {
                            viewModel.undoBulkMark()
                            haptic.tap()
                        }
                    )
                }
            }
        }

        // 4. Session Cards List (.sc matching HTML prototype)
        if (!isHoliday && dailyItems.isEmpty()) {
            item {
                GlassCard(isDark = isDark) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Classes Scheduled Today",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = GlassTokens.textColor(isDark)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "There are no regular sessions. You can add an exceptional class below.",
                            fontSize = 13.sp,
                            color = GlassTokens.mutedColor(isDark)
                        )
                    }
                }
            }
        } else if (!isHoliday) {
            itemsIndexed(
                items = dailyItems,
                key = { _, item -> "${selectedDate}_${item.slotId}_${item.period}" }
            ) { index, session ->
                StaggeredEntrance(index = index + 3) {
                    ClassSessionCard(
                        item = session,
                        onMarkStatus = { status ->
                            viewModel.markAttendance(
                                slotId = session.slotId,
                                date = selectedDate,
                                status = status,
                                subjectCode = session.subjectCode,
                                subjectName = session.subjectName,
                                classType = session.classType,
                                period = session.period,
                                counts = session.countsInAttendance
                            )
                        }
                    )
                }
            }
        }

        // 5. Add Exceptional Class Button (.exc matching dashed button from HTML prototype)
        if (!isHoliday) {
            item {
                StaggeredEntrance(index = dailyItems.size + 4) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x1A2FB5FF))
                            .border(
                                width = 1.5.dp,
                                color = Color(0xB33C96E6),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                haptic.select()
                                showAddExtraClassDialog = true
                            }
                            .testTag("btn_add_exceptional_class"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color(0xFF2FB5FF),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add Exceptional Class to $selectedDate",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2FB5FF)
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialogs matching HTML confirmDlg
    if (showMarkAllPresentDialog) {
        val count = dailyItems.count { !it.isAlwaysPresent }
        AppConfirmationDialog(
            title = "Mark All Present?",
            message = "All $count session${if (count > 1) "s" else ""} for $displayDate will be marked as Present. You can undo this action.",
            confirmButtonText = "Mark All Present",
            dismissButtonText = "Cancel",
            onConfirm = {
                showMarkAllPresentDialog = false
                viewModel.bulkMarkAttendance(selectedDate, AttendanceStatus.PRESENT)
                haptic.success()
            },
            onDismiss = { showMarkAllPresentDialog = false },
            isDark = isDark
        )
    }

    if (showMarkAllAbsentDialog) {
        val count = dailyItems.count { !it.isAlwaysPresent }
        AppConfirmationDialog(
            title = "Mark All Absent?",
            message = "All $count session${if (count > 1) "s" else ""} for $displayDate will be marked as Absent. You can undo this action.",
            confirmButtonText = "Mark All Absent",
            dismissButtonText = "Cancel",
            isDestructive = true,
            onConfirm = {
                showMarkAllAbsentDialog = false
                viewModel.bulkMarkAttendance(selectedDate, AttendanceStatus.ABSENT)
                haptic.warn()
            },
            onDismiss = { showMarkAllAbsentDialog = false },
            isDark = isDark
        )
    }

    if (showMarkAllNoClassDialog) {
        val count = dailyItems.count { !it.isAlwaysPresent }
        AppConfirmationDialog(
            title = "Mark All No Class?",
            message = "All $count session${if (count > 1) "s" else ""} for $displayDate will be marked as No Class / Cancelled.",
            confirmButtonText = "Mark No Class",
            dismissButtonText = "Cancel",
            onConfirm = {
                showMarkAllNoClassDialog = false
                viewModel.markAllCancelledForDate()
                haptic.success()
            },
            onDismiss = { showMarkAllNoClassDialog = false },
            isDark = isDark
        )
    }

    if (showResetDialog) {
        AppConfirmationDialog(
            title = "Reset Attendance?",
            message = "All marks for $displayDate will be cleared back to unmarked.",
            confirmButtonText = "Reset",
            dismissButtonText = "Cancel",
            isDestructive = true,
            onConfirm = {
                showResetDialog = false
                viewModel.resetAttendanceForDate()
                haptic.warn()
            },
            onDismiss = { showResetDialog = false },
            isDark = isDark
        )
    }

    if (showAddExtraClassDialog) {
        AddExtraClassDialog(
            date = selectedDate,
            isDark = isDark,
            onDismiss = { showAddExtraClassDialog = false },
            onSave = { slot ->
                viewModel.addOrUpdateSlot(slot, TimetableScope.THIS_DATE_ONLY, selectedDate)
                showAddExtraClassDialog = false
                haptic.success()
            }
        )
    }
}

@Composable
fun AddExtraClassDialog(
    date: String,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSave: (TimetableSlot) -> Unit
) {
    var subjectCode by remember { mutableStateOf("BHT-EXTRA") }
    var subjectName by remember { mutableStateOf("Extra Makeup Lecture") }
    var period by remember { mutableStateOf("1") }
    var startTime by remember { mutableStateOf("04:40 PM") }
    var endTime by remember { mutableStateOf("05:30 PM") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Exceptional Class",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = GlassTokens.textColor(isDark)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "$date • one-off or makeup session",
                    fontSize = 12.sp,
                    color = GlassTokens.mutedColor(isDark)
                )
                OutlinedTextField(
                    value = subjectName,
                    onValueChange = { subjectName = it },
                    label = { Text("Subject Name") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = subjectCode,
                        onValueChange = { subjectCode = it },
                        label = { Text("Code") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = period,
                        onValueChange = { period = it },
                        label = { Text("Period (1-8)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = period.toIntOrNull() ?: 1
                    onSave(
                        TimetableSlot(
                            dayOfWeek = "EXTRA",
                            period = p,
                            startTime = startTime,
                            endTime = endTime,
                            subjectCode = subjectCode.trim().uppercase(),
                            subjectName = subjectName.trim(),
                            classType = ClassType.LECTURE.name,
                            batch = "ALL",
                            countsInAttendance = true
                        )
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GlassTokens.PrimaryGradient)
            ) {
                Text("Add Class", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text("Cancel", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
