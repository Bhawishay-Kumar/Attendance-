package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.AppConfirmationDialog
import com.example.ui.components.CardSubjectIcon
import com.example.ui.components.GlassCard
import com.example.ui.components.StaggeredEntrance
import com.example.ui.components.rememberHaptic
import com.example.ui.components.tactilePress
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.LoraFontFamily
import com.example.ui.theme.Outfit
import com.example.ui.theme.WorkSans
import com.example.ui.theme.isAppDarkTheme
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.ui.viewmodel.DailyAttendanceUiItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: AttendanceViewModel,
    onNavigateToTimetable: () -> Unit,
    onNavigateToDaily: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToWhatIf: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()
    val haptic = rememberHaptic()
    val profile by viewModel.profile.collectAsState()
    val stats by viewModel.overallStats.collectAsState()
    val dailyItems by viewModel.dailyAttendanceItems.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val pendingUnmarked by viewModel.pendingUnmarkedCount.collectAsState()
    val monthlySummary by viewModel.monthlyAttendanceSummary.collectAsState()
    val recentActivity by viewModel.recentActivityList.collectAsState()
    val currentQuote by viewModel.currentMotivationalQuote.collectAsState()
    val allVersions by viewModel.allVersions.collectAsState()
    val timetableSlots by viewModel.timetableSlotsForDay.collectAsState()

    var showMarkAllConfirmDialog by remember { mutableStateOf(false) }

    val todayFullDate = remember {
        val d = Date()
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(d)
    }

    val todayShortDate = remember {
        val d = Date()
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(d)
    }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    val firstName = remember(profile.studentName) {
        val trimmed = profile.studentName.trim()
        if (trimmed.isEmpty()) "Student" else trimmed.split(" ").first()
    }

    val initials = remember(profile.studentName) {
        val parts = profile.studentName.trim().split(" ").filter { it.isNotEmpty() }
        when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "ST"
        }
    }

    // Bell animation for pending banner
    val infiniteTransition = rememberInfiniteTransition(label = "banner_bell")
    val bellRotation by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bell_rotation"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Greeting Card with Initials Monogram
        item {
            StaggeredEntrance(index = 0) {
                GlassCard(isDark = isDark) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Monogram box
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF3B6BFF), Color(0xFF8B5CF6))
                                    )
                                )
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.40f),
                                    RoundedCornerShape(18.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$greeting, $firstName",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GlassTokens.textColor(isDark),
                                letterSpacing = (-0.4).sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = todayFullDate,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GlassTokens.mutedColor(isDark)
                            )
                        }
                    }
                }
            }
        }

        // 2. Pending Attendance Reminder Banner
        if (pendingUnmarked > 0) {
            item {
                StaggeredEntrance(index = 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0x38FFAA3C),
                                        Color(0x28FF6464)
                                    )
                                )
                            )
                            .border(1.5.dp, Color(0x8CFFB347), RoundedCornerShape(20.dp))
                            .padding(14.dp)
                            .testTag("pending_attendance_banner")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Ringing Bell Icon Tile
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(Color(0x33FFAA3C)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFFFFB347) else Color(0xFFD97706),
                                    modifier = Modifier
                                        .size(22.dp)
                                        .rotate(bellRotation)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pending Attendance Reminder",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GlassTokens.textColor(isDark)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "You have $pendingUnmarked unmarked session${if (pendingUnmarked > 1) "s" else ""} today",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GlassTokens.mutedColor(isDark)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Mark Now Button
                            Button(
                                onClick = {
                                    haptic.select()
                                    onNavigateToDaily()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GlassTokens.PrimaryGradient)
                                    .testTag("btn_mark_pending_action")
                            ) {
                                Text(
                                    text = "Mark Now",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Overall Attendance Hero Card
        if (profile.dashboardCardVisibility.showOverallAttendance) {
            item {
                StaggeredEntrance(index = 2) {
                    GlassCard(isDark = isDark) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Animated Circular Progress Ring
                                val pct = stats.overallPercentage.coerceIn(0f, 100f)
                                val sweepAnim = remember { Animatable(0f) }
                                LaunchedEffect(pct) {
                                    sweepAnim.animateTo(
                                        targetValue = pct / 100f,
                                        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                                    )
                                }

                                Box(
                                    modifier = Modifier.size(112.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.size(108.dp)) {
                                        val strokeW = 10.dp.toPx()
                                        val arcSize = size.width - strokeW
                                        // Background track
                                        drawArc(
                                            color = if (isDark) Color(0x403E70EB) else Color(0x33A0BEF5),
                                            startAngle = -90f,
                                            sweepAngle = 360f,
                                            useCenter = false,
                                            topLeft = Offset(strokeW / 2, strokeW / 2),
                                            size = Size(arcSize, arcSize),
                                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                                        )
                                        // Foreground animated gradient arc
                                        drawArc(
                                            brush = Brush.sweepGradient(
                                                listOf(
                                                    Color(0xFF3B6BFF),
                                                    Color(0xFF8B5CF6),
                                                    Color(0xFF14D4A0),
                                                    Color(0xFF3B6BFF)
                                                )
                                            ),
                                            startAngle = -90f,
                                            sweepAngle = 360f * sweepAnim.value,
                                            useCenter = false,
                                            topLeft = Offset(strokeW / 2, strokeW / 2),
                                            size = Size(arcSize, arcSize),
                                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                                        )
                                    }

                                    Text(
                                        text = if (stats.totalConducted == 0) "No Data" else "${pct.toInt()}%",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GlassTokens.textColor(isDark)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Overall Attendance",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GlassTokens.textColor(isDark)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "${stats.totalAttended} attended of ${stats.totalConducted} conducted classes",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = GlassTokens.mutedColor(isDark)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Chips Row
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Target chip
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(999.dp))
                                                .background(GlassTokens.chipColor(isDark))
                                                .border(1.dp, GlassTokens.chipBorderColor(isDark), RoundedCornerShape(999.dp))
                                                .padding(horizontal = 9.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "Target ${profile.targetAttendancePct}%",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GlassTokens.chipFgColor(isDark)
                                            )
                                        }

                                        // Status Chip
                                        val isBelow = stats.isBelowTarget
                                        val statusBg = if (isBelow) {
                                            if (isDark) GlassTokens.DarkNoPillBg else GlassTokens.LightNoPillBg
                                        } else {
                                            if (isDark) GlassTokens.DarkOkPillBg else GlassTokens.LightOkPillBg
                                        }
                                        val statusBorder = if (isBelow) {
                                            if (isDark) GlassTokens.DarkNoPillBorder else GlassTokens.LightNoPillBorder
                                        } else {
                                            if (isDark) GlassTokens.DarkOkPillBorder else GlassTokens.LightOkPillBorder
                                        }
                                        val statusFg = if (isBelow) {
                                            if (isDark) GlassTokens.DarkNoPillFg else GlassTokens.LightNoPillFg
                                        } else {
                                            if (isDark) GlassTokens.DarkOkPillFg else GlassTokens.LightOkPillFg
                                        }

                                        val infiniteTransition = rememberInfiniteTransition(label = "on_track_glow")
                                        val glowAlpha by infiniteTransition.animateFloat(
                                            initialValue = 0.35f,
                                            targetValue = 0.95f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(1200, easing = FastOutSlowInEasing),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "on_track_glow_anim"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .then(
                                                    if (!isBelow) {
                                                        Modifier.drawBehind {
                                                            drawRoundRect(
                                                                brush = Brush.radialGradient(
                                                                    colors = listOf(
                                                                        Color(0xFF10B981).copy(alpha = glowAlpha * 0.45f),
                                                                        Color(0xFF34D399).copy(alpha = glowAlpha * 0.2f),
                                                                        Color.Transparent
                                                                    ),
                                                                    radius = maxOf(size.width, size.height) * 1.3f
                                                                ),
                                                                cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                                                            )
                                                        }
                                                    } else Modifier
                                                )
                                                .clip(RoundedCornerShape(999.dp))
                                                .background(statusBg)
                                                .border(
                                                    width = if (!isBelow) 1.5.dp else 1.dp,
                                                    brush = if (!isBelow) {
                                                        Brush.horizontalGradient(
                                                            listOf(
                                                                Color(0xFF34D399).copy(alpha = glowAlpha),
                                                                Color(0xFF10B981).copy(alpha = glowAlpha * 0.9f),
                                                                Color(0xFF059669).copy(alpha = glowAlpha)
                                                            )
                                                        )
                                                    } else {
                                                        androidx.compose.ui.graphics.SolidColor(statusBorder)
                                                    },
                                                    shape = RoundedCornerShape(999.dp)
                                                )
                                                .padding(horizontal = 11.dp, vertical = 4.5.dp)
                                        ) {
                                            Text(
                                                text = if (isBelow) "Below Target" else "On Track",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = statusFg
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Forecast Note Box
                            val noteText = remember(stats) {
                                when {
                                    stats.totalConducted == 0 -> "Start marking attendance to see your safe margin and forecast."
                                    stats.isBelowTarget -> "Attend ${stats.consecutiveClassesNeeded} more consecutive session(s) to reach ${profile.targetAttendancePct}%${if (stats.isEstimate) " (estimate)" else ""}."
                                    else -> "You can safely miss up to ${stats.classesCanMiss} more session(s) and stay above ${profile.targetAttendancePct}%${if (stats.isEstimate) " (estimate)" else ""}."
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(GlassTokens.chipColor(isDark))
                                    .border(1.dp, GlassTokens.chipBorderColor(isDark), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 13.dp, vertical = 11.dp)
                            ) {
                                Text(
                                    text = noteText,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GlassTokens.textColor(isDark),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Quick Actions
        item {
            StaggeredEntrance(index = 3) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Quick Actions",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GlassTokens.textColor(isDark),
                        modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Action 1: Daily
                            QuickActionTile(
                                title = "Daily",
                                subtitle = "Update attendance",
                                icon = Icons.Default.CheckCircle,
                                accentColor = Color(0xFF13DBA2),
                                isDark = isDark,
                                onClick = {
                                    haptic.select()
                                    onNavigateToDaily()
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Action 2: Timetable
                            QuickActionTile(
                                title = "Timetable",
                                subtitle = "Check schedule",
                                icon = Icons.Default.Schedule,
                                accentColor = Color(0xFF3B6BFF),
                                isDark = isDark,
                                onClick = {
                                    haptic.select()
                                    onNavigateToTimetable()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Action 3: Calendar
                            QuickActionTile(
                                title = "Calendar",
                                subtitle = "View calendar",
                                icon = Icons.Default.CalendarMonth,
                                accentColor = Color(0xFF8B5CF6),
                                isDark = isDark,
                                onClick = {
                                    haptic.select()
                                    onNavigateToReports()
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Action 4: What-If Predictor
                            QuickActionTile(
                                title = "What-If",
                                subtitle = "Predict leave/bunk",
                                icon = Icons.Default.Speed,
                                accentColor = Color(0xFF22D3EE),
                                isDark = isDark,
                                onClick = {
                                    haptic.select()
                                    onNavigateToWhatIf()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 5. Today's Schedule Rows
        if (profile.dashboardCardVisibility.showTodaySchedule) {
            item {
                StaggeredEntrance(index = 4) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Today's Schedule",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GlassTokens.textColor(isDark)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = todayShortDate,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GlassTokens.mutedColor(isDark)
                                )
                            }

                            Text(
                                text = "View All",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6F9BFF),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        haptic.select()
                                        onNavigateToDaily()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        if (dailyItems.isEmpty()) {
                            GlassCard(isDark = isDark) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No Classes Scheduled Today",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = GlassTokens.textColor(isDark)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Enjoy your day or add an exceptional class in Daily Attendance.",
                                        fontSize = 12.5.sp,
                                        color = GlassTokens.mutedColor(isDark)
                                    )
                                }
                            }
                        } else {
                            GlassCard(isDark = isDark, contentPadding = PaddingValues(12.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    dailyItems.forEach { session ->
                                        TodayScheduleRow(
                                            session = session,
                                            isDark = isDark,
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

                                    // Mark All Present Button
                                    val countableItems = dailyItems.filter { !it.isAlwaysPresent }
                                    if (countableItems.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Button(
                                            onClick = {
                                                haptic.select()
                                                showMarkAllConfirmDialog = true
                                            },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                            contentPadding = PaddingValues(0.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(GlassTokens.PrimaryGradient)
                                                .testTag("btn_mark_all_present_home")
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Mark All Present",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Weekly Timetable Carousel (Monday-Friday only)
        if (profile.dashboardCardVisibility.showWeeklyTimetable) {
            item {
                StaggeredEntrance(index = 5) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Weekly Timetable",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GlassTokens.textColor(isDark)
                            )
                            Text(
                                text = "View Full",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6F9BFF),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        haptic.select()
                                        onNavigateToTimetable()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Scrollable row of Monday through Friday
                        val weekDays = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")
                        val currentDayName = remember {
                            SimpleDateFormat("EEEE", Locale.getDefault()).format(Date()).uppercase()
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            weekDays.forEach { day ->
                                val isToday = day == currentDayName
                                Box(
                                    modifier = Modifier
                                        .width(220.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(GlassTokens.surfaceBrush(isDark))
                                        .border(
                                            width = if (isToday) 2.dp else 1.5.dp,
                                            color = if (isToday) Color(0xFF5B8CFF) else GlassTokens.borderColor(isDark),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(14.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = day.take(3),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = GlassTokens.textColor(isDark)
                                            )
                                            if (isToday) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(999.dp))
                                                        .background(if (isDark) GlassTokens.DarkOkPillBg else GlassTokens.LightOkPillBg)
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "Today",
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isDark) GlassTokens.DarkOkPillFg else GlassTokens.LightOkPillFg
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Summary of classes for that day
                                        val daySchedule = remember(day) {
                                            when (day) {
                                                "MONDAY" -> listOf("BHT503 Fiber Quality", "BHT055 Knitting", "BHT051 Garment", "BHT501 Weaving")
                                                "TUESDAY" -> listOf("BHT051 Garment", "Library", "HTFT501 Comfort", "BHT502 Wet Processing")
                                                "WEDNESDAY" -> listOf("BNC501 Constitution", "BHT503 Fiber Quality", "BHT501 Weaving", "BHT502 Wet Processing")
                                                "THURSDAY" -> listOf("BHT501 Weaving", "HTFT501 Comfort", "BHT551/552 Lab", "BHT502-T Tutorial")
                                                else -> listOf("BNC501 Constitution", "HTFT501-T Tutorial", "BHT552/551 Lab", "BHT501-T Weaving")
                                            }
                                        }

                                        daySchedule.forEach { sub ->
                                            Text(
                                                text = sub,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = GlassTokens.mutedColor(isDark),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                lineHeight = 17.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Monthly Overview
        if (profile.dashboardCardVisibility.showMonthlySummary) {
            item {
                StaggeredEntrance(index = 6) {
                    val currentMonthName = remember {
                        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
                    }
                    GlassCard(isDark = isDark) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$currentMonthName Overview",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GlassTokens.textColor(isDark)
                                )
                                Text(
                                    text = if (stats.totalConducted == 0) "No Data" else "${stats.overallPercentage.toInt()}%",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GlassTokens.codeColor(isDark)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Horizontal gradient progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(GlassTokens.chipColor(isDark))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(stats.overallPercentage.coerceIn(0f, 100f) / 100f)
                                        .height(6.dp)
                                        .background(GlassTokens.PrimaryGradient)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Stats breakdown (Present / Absent / No class)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MiniStatTile(
                                    label = "Present",
                                    value = "${stats.totalAttended}",
                                    color = Color(0xFF13DBA2),
                                    isDark = isDark,
                                    modifier = Modifier.weight(1f)
                                )
                                MiniStatTile(
                                    label = "Absent",
                                    value = "${stats.totalMissed}",
                                    color = Color(0xFFFF2F75),
                                    isDark = isDark,
                                    modifier = Modifier.weight(1f)
                                )
                                MiniStatTile(
                                    label = "No Class",
                                    value = "${stats.totalCancelled}",
                                    color = Color(0xFF8B5CF6),
                                    isDark = isDark,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 8. Recent Attendance Logs (Last 5)
        if (profile.dashboardCardVisibility.showRecentLogs && recentActivity.isNotEmpty()) {
            item {
                StaggeredEntrance(index = 7) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Recent Attendance Logs",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GlassTokens.textColor(isDark),
                            modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
                        )

                        GlassCard(isDark = isDark, contentPadding = PaddingValues(12.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                recentActivity.take(5).forEach { record ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Status dot
                                        val dotColor = when (record.status.uppercase()) {
                                            AttendanceStatus.PRESENT.name -> Color(0xFF13DBA2)
                                            AttendanceStatus.ABSENT.name -> Color(0xFFFF2F75)
                                            AttendanceStatus.CANCELLED.name -> Color(0xFF8B5CF6)
                                            else -> Color(0xFFF59E0B)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(dotColor)
                                        )

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = record.subjectName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GlassTokens.textColor(isDark),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${record.status.lowercase().replaceFirstChar { it.uppercase() }} • ${record.date} • Period ${record.period}",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium,
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
        }

        // 9. Motivational Quote Card
        item {
            StaggeredEntrance(index = 8) {
                GlassCard(
                    isDark = isDark,
                    onClick = {
                        haptic.tap()
                        viewModel.cycleQuote()
                    }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                tint = Color(0xFF3B6BFF).copy(alpha = 0.45f),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "“${currentQuote.quote}”",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GlassTokens.textColor(isDark),
                                lineHeight = 21.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "— ${currentQuote.author} • ${currentQuote.category}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassTokens.mutedColor(isDark),
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }

    // Confirmation dialog for Mark All Present Home
    if (showMarkAllConfirmDialog) {
        val count = dailyItems.count { !it.isAlwaysPresent }
        AppConfirmationDialog(
            title = "Mark All Present for Today?",
            message = "All $count scheduled sessions for $todayShortDate will be marked as Present. You can undo this action.",
            confirmButtonText = "Mark All Present",
            dismissButtonText = "Cancel",
            onConfirm = {
                showMarkAllConfirmDialog = false
                viewModel.bulkMarkAttendance(selectedDate, AttendanceStatus.PRESENT)
                haptic.success()
            },
            onDismiss = { showMarkAllConfirmDialog = false },
            isDark = isDark
        )
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(GlassTokens.surfaceBrush(isDark))
            .border(1.5.dp, GlassTokens.borderColor(isDark), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(accentColor.copy(alpha = if (isDark) 0.22f else 0.15f))
                    .border(1.5.dp, accentColor.copy(alpha = 0.40f), RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GlassTokens.textColor(isDark)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                color = GlassTokens.mutedColor(isDark),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TodayScheduleRow(
    session: DailyAttendanceUiItem,
    isDark: Boolean,
    onMarkStatus: (AttendanceStatus) -> Unit
) {
    val haptic = rememberHaptic()

    val statusBorder = when (session.currentStatus) {
        AttendanceStatus.PRESENT -> Color(0xFF13DBA2).copy(alpha = 0.6f)
        AttendanceStatus.ABSENT -> Color(0xFFFF2F75).copy(alpha = 0.6f)
        AttendanceStatus.CANCELLED -> Color(0xFF8B5CF6).copy(alpha = 0.6f)
        AttendanceStatus.UNMARKED -> GlassTokens.chipBorderColor(isDark)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GlassTokens.chipColor(isDark))
            .border(1.5.dp, statusBorder, RoundedCornerShape(18.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject Icon Tile
            CardSubjectIcon(
                subjectCode = session.subjectCode,
                classType = session.classType,
                size = 38.dp,
                isDark = isDark
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.subjectName,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlassTokens.textColor(isDark),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${session.periodRoman} • ${session.startTime}",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlassTokens.mutedColor(isDark)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (session.isAlwaysPresent) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isDark) GlassTokens.DarkOkPillBg else GlassTokens.LightOkPillBg)
                        .border(
                            1.dp,
                            if (isDark) GlassTokens.DarkOkPillBorder else GlassTokens.LightOkPillBorder,
                            RoundedCornerShape(999.dp)
                        )
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Always Present",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) GlassTokens.DarkOkPillFg else GlassTokens.LightOkPillFg
                    )
                }
            } else {
                // 3 Quick Action buttons: Present, Absent, Cancelled
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Present Button
                    val isP = session.currentStatus == AttendanceStatus.PRESENT
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (isP) Color(0xFF13DBA2) else if (isDark) Color(0x6614286E) else Color(0xEEFFFFFF))
                            .border(
                                1.5.dp,
                                if (isP) Color.Transparent else if (isDark) Color(0x805F8CF0) else Color(0x80AAC3F0),
                                RoundedCornerShape(11.dp)
                            )
                            .clickable {
                                haptic.success()
                                onMarkStatus(AttendanceStatus.PRESENT)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Present",
                            tint = if (isP) Color.White else if (isDark) Color(0xFFA9BDF0) else Color(0xFF4B5FA3),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Absent Button
                    val isA = session.currentStatus == AttendanceStatus.ABSENT
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (isA) Color(0xFFFF2F75) else if (isDark) Color(0x6614286E) else Color(0xEEFFFFFF))
                            .border(
                                1.5.dp,
                                if (isA) Color.Transparent else if (isDark) Color(0x805F8CF0) else Color(0x80AAC3F0),
                                RoundedCornerShape(11.dp)
                            )
                            .clickable {
                                haptic.warn()
                                onMarkStatus(AttendanceStatus.ABSENT)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Absent",
                            tint = if (isA) Color.White else if (isDark) Color(0xFFA9BDF0) else Color(0xFF4B5FA3),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Cancelled / No Class Button
                    val isC = session.currentStatus == AttendanceStatus.CANCELLED
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (isC) Color(0xFF4A4FA8) else if (isDark) Color(0x6614286E) else Color(0xEEFFFFFF))
                            .border(
                                1.5.dp,
                                if (isC) Color.Transparent else if (isDark) Color(0x805F8CF0) else Color(0x80AAC3F0),
                                RoundedCornerShape(11.dp)
                            )
                            .clickable {
                                haptic.select()
                                onMarkStatus(AttendanceStatus.CANCELLED)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "No class held",
                            tint = if (isC) Color.White else if (isDark) Color(0xFFA9BDF0) else Color(0xFF4B5FA3),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStatTile(
    label: String,
    value: String,
    color: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GlassTokens.chipColor(isDark))
            .border(1.5.dp, GlassTokens.chipBorderColor(isDark), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        Column {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = GlassTokens.mutedColor(isDark)
            )
        }
    }
}
