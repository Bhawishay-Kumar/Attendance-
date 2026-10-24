package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Search
import com.example.data.local.AktuHoliday
import com.example.data.local.AktuHolidays
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Surface
import com.example.ui.components.tactilePress
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.local.InitialTimetableData
import com.example.data.model.ClassType
import com.example.data.model.TimetableSlot
import com.example.ui.components.CardSubjectIcon
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isAppDarkTheme()
    val haptic = rememberHaptic()
    val profile by viewModel.profile.collectAsState()
    val selectedDay by viewModel.selectedTimetableDay.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val currentVersion by viewModel.currentVersion.collectAsState()
    val slotsForDay by viewModel.timetableSlotsForDay.collectAsState()
    val overrides by viewModel.allOverrides.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var slotToEdit by remember { mutableStateOf<TimetableSlot?>(null) }
    var slotToDelete by remember { mutableStateOf<TimetableSlot?>(null) }

    // Official Timetable: Monday–Friday ONLY (NO Saturday or Sunday!)
    val daysOfWeek = remember { listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY") }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayDate = remember(selectedDate) {
        try {
            val d = dateFormat.parse(selectedDate) ?: Date()
            SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(d)
        } catch (_: Exception) {
            selectedDate
        }
    }

    val holiday = overrides.find { it.date == selectedDate && it.isHoliday }
    var holidayName by remember(holiday) { mutableStateOf(holiday?.holidayName ?: "") }
    var showAllHolidaysDialog by remember { mutableStateOf(false) }
    var holidaySearchQuery by remember { mutableStateOf("") }

    // Filter current day's slots strictly according to active batch (Batch A / Batch B)
    val displaySlots = remember(slotsForDay, selectedDay, currentVersion, profile.batch) {
        val userBatch = profile.batch
        val rawSlots = if (slotsForDay.isNotEmpty()) slotsForDay
        else InitialTimetableData.getDefaultSlots(currentVersion?.versionId ?: 1).filter { it.dayOfWeek == selectedDay }
        rawSlots.filter { it.batch == "ALL" || it.batch.equals(userBatch, ignoreCase = true) }
    }

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
                val newDate = dateFormat.format(newCal.time)
                viewModel.setSelectedDate(newDate)
                val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(newCal.time).uppercase()
                if (daysOfWeek.contains(dayName)) {
                    viewModel.setSelectedTimetableDay(dayName)
                }
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Date Navigation & Monday–Friday Day Segmented Control (.date matching HTML)
            item {
                StaggeredEntrance(index = 0) {
                    GlassCard(isDark = isDark, contentPadding = PaddingValues(14.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Date Picker Row: < | Date | >
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GlassTokens.chipColor(isDark))
                                        .border(1.5.dp, GlassTokens.chipBorderColor(isDark), RoundedCornerShape(12.dp))
                                        .clickable {
                                            haptic.select()
                                            viewModel.stepDay(-1)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowLeft,
                                        contentDescription = "Previous Day",
                                        tint = Color(0xFF6F9BFF),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

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
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
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

                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GlassTokens.chipColor(isDark))
                                        .border(1.5.dp, GlassTokens.chipBorderColor(isDark), RoundedCornerShape(12.dp))
                                        .clickable {
                                            haptic.select()
                                            viewModel.stepDay(1)
                                        },
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

                            // Mon–Fri Day Selector Bar (Monday through Friday only!)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(GlassTokens.chipColor(isDark))
                                    .border(1.5.dp, GlassTokens.chipBorderColor(isDark), RoundedCornerShape(16.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                daysOfWeek.forEach { day ->
                                    val isSelected = selectedDay.equals(day, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .then(
                                                if (isSelected) Modifier.background(GlassTokens.PrimaryGradient)
                                                else Modifier
                                            )
                                            .clickable {
                                                haptic.select()
                                                viewModel.setSelectedTimetableDay(day)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day.take(3),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else GlassTokens.mutedColor(isDark)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. View All Holidays Card
            item {
                StaggeredEntrance(index = 1) {
                    GlassCard(isDark = isDark, contentPadding = PaddingValues(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.select()
                                    showAllHolidaysDialog = true
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isDark) Color(0x33F59E0B) else Color(0x26F59E0B),
                                    border = BorderStroke(1.2.dp, if (isDark) Color(0x80F59E0B) else Color(0x66D97706)),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Celebration,
                                            contentDescription = null,
                                            tint = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "View All Holidays",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = GlassTokens.textColor(isDark)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isDark) Color(0x3310B981) else Color(0x2610B981)
                                        ) {
                                            Text(
                                                text = "AKTU",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDark) Color(0xFF34D399) else Color(0xFF059669),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Official university holiday list • Valid leaves without permission",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = GlassTokens.mutedColor(isDark),
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = CircleShape,
                                color = GlassTokens.chipColor(isDark),
                                border = BorderStroke(1.dp, GlassTokens.chipBorderColor(isDark)),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = "Open Holidays",
                                        tint = if (isDark) Color(0xFF93C5FD) else Color(0xFF2563EB),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Edit or Modify Holidays Card (Replaces Institute Holiday)
            item {
                StaggeredEntrance(index = 2) {
                    val officialHoliday = AktuHolidays.getHoliday(selectedDate)
                    val isMarkedHoliday = holiday != null

                    GlassCard(isDark = isDark, contentPadding = PaddingValues(14.dp)) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isDark) Color(0x336366F1) else Color(0x266366F1),
                                        border = BorderStroke(1.2.dp, if (isDark) Color(0x80818CF8) else Color(0x666366F1)),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                tint = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = "Edit or Modify Holidays",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = GlassTokens.textColor(isDark)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Set, edit, or customize holiday status for $displayDate to exempt attendance.",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = GlassTokens.mutedColor(isDark),
                                            lineHeight = 16.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Switch(
                                    checked = isMarkedHoliday,
                                    onCheckedChange = { checked ->
                                        haptic.select()
                                        val defaultName = officialHoliday?.name ?: holidayName.ifBlank { "Holiday" }
                                        viewModel.markDateAsHoliday(selectedDate, checked, defaultName)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF13DBA2),
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = GlassTokens.chipColor(isDark)
                                    )
                                )
                            }

                            if (officialHoliday != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isDark) Color(0x33F59E0B) else Color(0x26F59E0B),
                                    border = BorderStroke(1.dp, if (isDark) Color(0x80F59E0B) else Color(0x66D97706)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Celebration,
                                            contentDescription = null,
                                            tint = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "AKTU Official Holiday",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
                                            )
                                            Text(
                                                text = officialHoliday.name,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = GlassTokens.textColor(isDark)
                                            )
                                        }
                                    }
                                }
                            }

                            if (isMarkedHoliday) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = holidayName,
                                    onValueChange = {
                                        holidayName = it
                                        viewModel.markDateAsHoliday(selectedDate, true, it)
                                    },
                                    label = { Text("Holiday Title / Reason") },
                                    placeholder = { Text(officialHoliday?.name ?: "e.g. Cultural Fest / Diwali") },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }

            // 3. Section Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${selectedDay.lowercase().replaceFirstChar { it.uppercase() }} Schedule",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GlassTokens.textColor(isDark)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Batch ${profile.batch}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GlassTokens.mutedColor(isDark)
                        )
                    }
                }
            }

            // 4. Classes List (.tt matching HTML prototype)
            if (displaySlots.isEmpty()) {
                item {
                    GlassCard(isDark = isDark) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF7AA2FF),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Classes Scheduled",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = GlassTokens.textColor(isDark)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap 'Add Class' to create a session for $selectedDay.",
                                fontSize = 12.5.sp,
                                color = GlassTokens.mutedColor(isDark)
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(
                    items = displaySlots,
                    key = { _, slot -> "${slot.id}_${slot.period}_${slot.subjectCode}" }
                ) { index, slot ->
                    StaggeredEntrance(index = index + 2) {
                        TimetableSessionItem(
                            slot = slot,
                            isDark = isDark,
                            onEdit = {
                                slotToEdit = slot
                                showAddEditDialog = true
                            },
                            onDelete = {
                                slotToDelete = slot
                            }
                        )
                    }
                }
            }

            // Bottom "Add Class" button placed at the bottom of the page, similar to "Add Exceptional Class" in Attendance section
            item {
                StaggeredEntrance(index = displaySlots.size + 4) {
                    val addBtnInteraction = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    if (isDark) listOf(Color(0x263B82F6), Color(0x266366F1), Color(0x268B5CF6))
                                    else listOf(Color(0x1A2563EB), Color(0x1A4F46E5), Color(0x1A7C3AED))
                                )
                            )
                            .border(
                                width = 1.5.dp,
                                brush = Brush.horizontalGradient(
                                    if (isDark) listOf(Color(0x8060A5FA), Color(0x80818CF8), Color(0x80A78BFA))
                                    else listOf(Color(0x992563EB), Color(0x994F46E5), Color(0x997C3AED))
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable(interactionSource = addBtnInteraction, indication = null) {
                                haptic.select()
                                slotToEdit = null
                                showAddEditDialog = true
                            }
                            .tactilePress(addBtnInteraction)
                            .testTag("btn_add_class_bottom"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (isDark) Color(0x3360A5FA) else Color(0x332563EB),
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFF93C5FD) else Color(0xFF2563EB),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Add Class to $selectedDay",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF93C5FD) else Color(0xFF2563EB)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    // View All Holidays Dialog
    if (showAllHolidaysDialog) {
        AllHolidaysDialog(
            isDark = isDark,
            searchQuery = holidaySearchQuery,
            onSearchQueryChange = { holidaySearchQuery = it },
            onDismiss = {
                showAllHolidaysDialog = false
                holidaySearchQuery = ""
            }
        )
    }

    // Add / Edit Class Bottom Sheet / Dialog
    if (showAddEditDialog) {
        AddEditClassDialog(
            slot = slotToEdit,
            selectedDay = selectedDay,
            selectedDate = selectedDate,
            isDark = isDark,
            onDismiss = { showAddEditDialog = false },
            onSave = { updatedSlot, scope ->
                viewModel.addOrUpdateSlot(updatedSlot, scope, selectedDate)
                showAddEditDialog = false
                haptic.success()
            }
        )
    }

    // Delete Session Confirmation Dialog (matching exact HTML prototype wording)
    if (slotToDelete != null) {
        val s = slotToDelete!!
        AlertDialog(
            onDismissRequest = { slotToDelete = null },
            title = {
                Text(
                    text = "Delete Session",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = GlassTokens.textColor(isDark)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete ${s.subjectName} (Period ${InitialTimetableData.getPeriodRoman(s.period)}) from the timetable? This will not delete your past attendance history.",
                    fontSize = 13.5.sp,
                    color = GlassTokens.mutedColor(isDark),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Option 1: Skip this date only
                    Button(
                        onClick = {
                            viewModel.deleteSlot(s, TimetableScope.THIS_DATE_ONLY, selectedDate)
                            slotToDelete = null
                            haptic.warn()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GlassTokens.chipColor(isDark)),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("Skip this date only", color = GlassTokens.textColor(isDark), fontWeight = FontWeight.Bold)
                    }

                    // Option 2: Delete permanently
                    Button(
                        onClick = {
                            viewModel.deleteSlot(s, TimetableScope.ENTIRE_TIMETABLE, selectedDate)
                            slotToDelete = null
                            haptic.heavy()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("Delete permanently", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    // Cancel
                    OutlinedButton(
                        onClick = { slotToDelete = null },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            dismissButton = null
        )
    }
}

@Composable
private fun TimetableSessionItem(
    slot: TimetableSlot,
    isDark: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val haptic = rememberHaptic()

    GlassCard(isDark = isDark, contentPadding = PaddingValues(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject Icon Tile
            CardSubjectIcon(
                subjectCode = slot.subjectCode,
                classType = slot.classType,
                size = 44.dp,
                isDark = isDark
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Period chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GlassTokens.PrimaryGradient)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Period ${InitialTimetableData.getPeriodRoman(slot.period)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = slot.subjectName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlassTokens.textColor(isDark),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${slot.startTime} – ${slot.endTime} • ${slot.subjectCode} • ${slot.classType}${if (slot.batch != "ALL") " • Batch ${slot.batch}" else ""}",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlassTokens.mutedColor(isDark),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Buttons: Edit and Delete
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Edit Icon Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassTokens.chipColor(isDark))
                        .border(1.5.dp, GlassTokens.chipBorderColor(isDark), RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.select()
                            onEdit()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = GlassTokens.chipFgColor(isDark),
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Delete Icon Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassTokens.chipColor(isDark))
                        .border(1.5.dp, GlassTokens.chipBorderColor(isDark), RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.warn()
                            onDelete()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFFF4F81),
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditClassDialog(
    slot: TimetableSlot?,
    selectedDay: String,
    selectedDate: String,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSave: (TimetableSlot, TimetableScope) -> Unit
) {
    val isEditing = slot != null
    var name by remember(slot) { mutableStateOf(slot?.subjectName ?: "") }
    var code by remember(slot) { mutableStateOf(slot?.subjectCode ?: "") }
    var classType by remember(slot) { mutableStateOf(slot?.classType ?: "LECTURE") }
    var period by remember(slot) { mutableStateOf(slot?.period?.toString() ?: "1") }
    var startTime by remember(slot) { mutableStateOf(slot?.startTime ?: "09:30 AM") }
    var endTime by remember(slot) { mutableStateOf(slot?.endTime ?: "10:20 AM") }
    var batch by remember(slot) { mutableStateOf(slot?.batch ?: "ALL") }
    var countsInAttendance by remember(slot) { mutableStateOf(slot?.countsInAttendance ?: true) }
    var notes by remember(slot) { mutableStateOf(slot?.notes ?: "") }
    var scope by remember { mutableStateOf(TimetableScope.THIS_DAY_ONWARDS) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Edit Session" else "Add New Session",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = GlassTokens.textColor(isDark)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Code") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = period,
                        onValueChange = {
                            period = it
                            val pInt = it.toIntOrNull()
                            if (pInt != null && pInt in 1..8) {
                                val (st, et) = InitialTimetableData.getTimingForPeriod(pInt)
                                startTime = st
                                endTime = et
                            }
                        },
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

                // Applicable Batch (All / A / B)
                Text(
                    text = "Applicable Batch",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlassTokens.mutedColor(isDark)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ALL" to "All Batches", "A" to "Batch A", "B" to "Batch B").forEach { (bVal, bLabel) ->
                        val isSelected = batch == bVal
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .then(
                                    if (isSelected) Modifier.background(GlassTokens.PrimaryGradient)
                                    else Modifier.background(GlassTokens.chipColor(isDark))
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color.Transparent else GlassTokens.chipBorderColor(isDark),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { batch = bVal },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = bLabel,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else GlassTokens.chipFgColor(isDark)
                            )
                        }
                    }
                }

                // Scope (This date only / This day onwards)
                Text(
                    text = "Apply Scope",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlassTokens.mutedColor(isDark)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        TimetableScope.THIS_DATE_ONLY to "This date only",
                        TimetableScope.THIS_DAY_ONWARDS to "This day onwards"
                    ).forEach { (sVal, sLabel) ->
                        val isSelected = scope == sVal
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .then(
                                    if (isSelected) Modifier.background(GlassTokens.PrimaryGradient)
                                    else Modifier.background(GlassTokens.chipColor(isDark))
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color.Transparent else GlassTokens.chipBorderColor(isDark),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { scope = sVal },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sLabel,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else GlassTokens.chipFgColor(isDark)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = period.toIntOrNull() ?: 1
                    val newSlot = (slot ?: TimetableSlot(
                        dayOfWeek = selectedDay,
                        period = p,
                        startTime = startTime,
                        endTime = endTime,
                        subjectCode = code.trim().uppercase(),
                        subjectName = name.trim(),
                        classType = classType,
                        batch = batch,
                        countsInAttendance = countsInAttendance,
                        notes = notes
                    )).copy(
                        period = p,
                        startTime = startTime,
                        endTime = endTime,
                        subjectCode = code.trim().uppercase(),
                        subjectName = name.trim(),
                        classType = classType,
                        batch = batch,
                        countsInAttendance = countsInAttendance,
                        notes = notes
                    )
                    onSave(newSlot, scope)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GlassTokens.PrimaryGradient)
            ) {
                Text(if (isEditing) "Save Changes" else "Add Session", fontWeight = FontWeight.Bold, color = Color.White)
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

@Composable
private fun AllHolidaysDialog(
    isDark: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedYear by remember { mutableStateOf("2026") }

    val allHolidays = remember { AktuHolidays.OFFICIAL_HOLIDAYS }
    val filteredHolidays = remember(searchQuery, selectedYear) {
        allHolidays.filter { h ->
            val matchesYear = if (selectedYear == "All") true else h.date.startsWith(selectedYear)
            val matchesQuery = searchQuery.isBlank() ||
                h.name.contains(searchQuery, ignoreCase = true) ||
                h.date.contains(searchQuery, ignoreCase = true) ||
                h.dayOfWeek.contains(searchQuery, ignoreCase = true) ||
                h.description.contains(searchQuery, ignoreCase = true)
            matchesYear && matchesQuery
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) Color(0x33F59E0B) else Color(0x26F59E0B),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Celebration,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "University Holidays",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = GlassTokens.textColor(isDark)
                            )
                            Text(
                                text = "Official AKTU Circular • Valid Leaves",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = GlassTokens.mutedColor(isDark)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = GlassTokens.mutedColor(isDark)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Notice Banner explaining that only valid leave without permission is included
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) Color(0x2610B981) else Color(0x1A10B981),
                    border = BorderStroke(1.dp, if (isDark) Color(0x6610B981) else Color(0x4D10B981)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF34D399) else Color(0xFF059669),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Only valid public leaves that can be taken without permission are included.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFF34D399) else Color(0xFF059669),
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search holidays by name or month...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = GlassTokens.mutedColor(isDark),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Year selector chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("2026", "2025", "All").forEach { yearLabel ->
                        val isSel = selectedYear == yearLabel
                        Surface(
                            onClick = { selectedYear = yearLabel },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) {
                                if (isDark) Color(0xFF2563EB) else Color(0xFF1D4ED8)
                            } else {
                                GlassTokens.chipColor(isDark)
                            },
                            border = BorderStroke(1.dp, if (isSel) Color.Transparent else GlassTokens.chipBorderColor(isDark)),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (yearLabel == "All") "All Years" else "Year $yearLabel",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) Color.White else GlassTokens.mutedColor(isDark)
                                )
                            }
                        }
                    }
                }

                // Holidays Count
                Text(
                    text = "${filteredHolidays.size} Holidays Found",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlassTokens.mutedColor(isDark)
                )

                // Holiday items list
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filteredHolidays.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No holidays found matching \"$searchQuery\"",
                                    fontSize = 13.sp,
                                    color = GlassTokens.mutedColor(isDark)
                                )
                            }
                        }
                    } else {
                        items(filteredHolidays) { item ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) Color(0x331E293B) else Color(0x40F1F5F9),
                                border = BorderStroke(1.dp, if (isDark) Color(0x40334155) else Color(0x3394A3B8)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Date badge
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isDark) Color(0x4DFBBF24) else Color(0x33F59E0B),
                                        modifier = Modifier.size(width = 54.dp, height = 48.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            val parts = item.date.split("-")
                                            val dayStr = parts.getOrNull(2) ?: ""
                                            val monthStr = when (parts.getOrNull(1)) {
                                                "01" -> "JAN"
                                                "02" -> "FEB"
                                                "03" -> "MAR"
                                                "04" -> "APR"
                                                "05" -> "MAY"
                                                "06" -> "JUN"
                                                "07" -> "JUL"
                                                "08" -> "AUG"
                                                "09" -> "SEP"
                                                "10" -> "OCT"
                                                "11" -> "NOV"
                                                "12" -> "DEC"
                                                else -> ""
                                            }
                                            Text(
                                                text = dayStr,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp,
                                                color = if (isDark) Color(0xFFFDE68A) else Color(0xFFB45309)
                                            )
                                            Text(
                                                text = monthStr,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = GlassTokens.textColor(isDark),
                                            lineHeight = 17.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.dayOfWeek,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = GlassTokens.mutedColor(isDark)
                                            )
                                            Text(
                                                text = " • ",
                                                fontSize = 11.sp,
                                                color = GlassTokens.mutedColor(isDark)
                                            )
                                            Text(
                                                text = item.description,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = GlassTokens.mutedColor(isDark)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color(0xFF2563EB) else Color(0xFF1D4ED8)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    )
}
