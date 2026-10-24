package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ScreenTab
import com.example.data.model.NotificationActionType
import com.example.data.model.NotificationCategory
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationPreferences
import com.example.ui.components.ScenicBackground
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.isAppDarkTheme
import com.example.ui.viewmodel.AttendanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class NotificationFilter(val label: String) {
    ALL("All"),
    UNREAD("Unread"),
    REPORTS("Reports"),
    REMINDERS("Reminders"),
    WARNINGS("Alerts")
}

@Composable
fun NotificationCenterScreen(
    viewModel: AttendanceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTab: (ScreenTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()
    val context = LocalContext.current
    val notifications by viewModel.allNotifications.collectAsState()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()
    val preferences by viewModel.notificationPreferences.collectAsState()

    var selectedFilter by remember { mutableStateOf(NotificationFilter.ALL) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val filteredNotifications = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            NotificationFilter.ALL -> notifications
            NotificationFilter.UNREAD -> notifications.filter { !it.isRead }
            NotificationFilter.REPORTS -> notifications.filter {
                it.category == NotificationCategory.WEEKLY_REPORT.name ||
                it.category == NotificationCategory.MONTHLY_REPORT.name
            }
            NotificationFilter.REMINDERS -> notifications.filter {
                it.category == NotificationCategory.ATTENDANCE_REMINDER.name ||
                it.category == NotificationCategory.TIMETABLE_REMINDER.name
            }
            NotificationFilter.WARNINGS -> notifications.filter {
                it.category == NotificationCategory.LOW_ATTENDANCE_WARNING.name
            }
        }
    }

    ScenicBackground(isDark = isDark) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_close_notifications")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isDark) Color.White else Color(0xFF102653)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Notifications",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF102653)
                            )
                            if (unreadCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF1769FF),
                                    modifier = Modifier.testTag("unread_count_badge")
                                ) {
                                    Text(
                                        text = "$unreadCount New",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Class reminders, weekly reports & alerts",
                            fontSize = 11.5.sp,
                            color = if (isDark) Color(0xFF8EA5C8) else Color(0xFF64748B)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (unreadCount > 0) {
                        IconButton(
                            onClick = { viewModel.markAllNotificationsAsRead() },
                            modifier = Modifier.testTag("btn_mark_all_read")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Mark all as read",
                                tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1769FF)
                            )
                        }
                    }

                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("btn_notification_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Notification Settings",
                            tint = if (isDark) Color(0xFF8EA5C8) else Color(0xFF64748B)
                        )
                    }
                }
            }

            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(NotificationFilter.values()) { filter ->
                    val isSelected = selectedFilter == filter
                    val count = when (filter) {
                        NotificationFilter.ALL -> notifications.size
                        NotificationFilter.UNREAD -> unreadCount
                        NotificationFilter.REPORTS -> notifications.count {
                            it.category == NotificationCategory.WEEKLY_REPORT.name ||
                            it.category == NotificationCategory.MONTHLY_REPORT.name
                        }
                        NotificationFilter.REMINDERS -> notifications.count {
                            it.category == NotificationCategory.ATTENDANCE_REMINDER.name ||
                            it.category == NotificationCategory.TIMETABLE_REMINDER.name
                        }
                        NotificationFilter.WARNINGS -> notifications.count {
                            it.category == NotificationCategory.LOW_ATTENDANCE_WARNING.name
                        }
                    }

                    Surface(
                        onClick = { selectedFilter = filter },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) {
                            Color(0xFF1769FF)
                        } else {
                            if (isDark) Color(0xFF10264A) else Color(0xFFFFFFFF)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF1769FF)
                            else if (isDark) Color(0xFF1B3B6B) else Color(0xFFE0EDFB)
                        ),
                        modifier = Modifier.testTag("filter_${filter.name.lowercase()}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = filter.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else (if (isDark) Color(0xFF8EA5C8) else Color(0xFF64748B))
                            )
                            if (count > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) Color.White.copy(alpha = 0.25f)
                                            else (if (isDark) Color(0xFF1A3868) else Color(0xFFEFF6FF))
                                        )
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "$count",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else (if (isDark) Color(0xFF38BDF8) else Color(0xFF1769FF))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Notification Cards or Empty State
            if (filteredNotifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF10264A) else Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1769FF),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (selectedFilter == NotificationFilter.UNREAD) "No Unread Notifications" else "No Notifications Yet",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF102653)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "You're all caught up! As classes take place, attendance reminders and weekly summaries will appear here.",
                            fontSize = 12.5.sp,
                            color = if (isDark) Color(0xFF8EA5C8) else Color(0xFF64748B),
                            modifier = Modifier.padding(horizontal = 24.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        FilledTonalButton(
                            onClick = { viewModel.generateSampleNotifications() },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("btn_seed_sample_notifications")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate Sample Alerts", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredNotifications, key = { it.id }) { item ->
                        NotificationCard(
                            item = item,
                            isDark = isDark,
                            onCardClick = {
                                viewModel.markNotificationAsRead(item.id)
                            },
                            onActionClick = {
                                viewModel.markNotificationAsRead(item.id)
                                when (item.actionType) {
                                    NotificationActionType.NAVIGATE_DAILY.name -> {
                                        if (item.actionPayload.isNotBlank()) {
                                            viewModel.setSelectedDate(item.actionPayload)
                                        }
                                        onNavigateToTab(ScreenTab.DAILY)
                                    }
                                    NotificationActionType.NAVIGATE_REPORTS.name -> {
                                        onNavigateToTab(ScreenTab.REPORTS)
                                    }
                                    NotificationActionType.NAVIGATE_TIMETABLE.name -> {
                                        onNavigateToTab(ScreenTab.TIMETABLE)
                                    }
                                    NotificationActionType.NAVIGATE_SETTINGS.name -> {
                                        onNavigateToTab(ScreenTab.SETTINGS)
                                    }
                                }
                            },
                            onDelete = {
                                viewModel.deleteNotification(item.id)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(
                                onClick = { viewModel.clearAllNotifications() },
                                modifier = Modifier.testTag("btn_clear_all_notifications")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear Notification History", fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }

    // Notification Preferences Settings Dialog
    if (showSettingsDialog) {
        NotificationSettingsDialog(
            preferences = preferences ?: NotificationPreferences(),
            isDark = isDark,
            onSave = { updated ->
                viewModel.updateNotificationPreferences(updated)
                showSettingsDialog = false
            },
            onDismiss = { showSettingsDialog = false }
        )
    }
}

@Composable
fun NotificationCard(
    item: NotificationItem,
    isDark: Boolean,
    onCardClick: () -> Unit,
    onActionClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, iconBgColor, categoryLabel) = when (item.category) {
        NotificationCategory.ATTENDANCE_REMINDER.name -> Triple(
            Icons.Default.EventNote,
            Color(0xFF1769FF),
            "Attendance Reminder"
        )
        NotificationCategory.WEEKLY_REPORT.name -> Triple(
            Icons.Default.BarChart,
            Color(0xFF0284C7),
            "Weekly Report"
        )
        NotificationCategory.MONTHLY_REPORT.name -> Triple(
            Icons.Default.Assessment,
            Color(0xFF7C3AED),
            "Monthly Report"
        )
        NotificationCategory.LOW_ATTENDANCE_WARNING.name -> Triple(
            Icons.Default.Warning,
            Color(0xFFEF4444),
            "Attendance Alert"
        )
        NotificationCategory.TIMETABLE_REMINDER.name -> Triple(
            Icons.Default.Schedule,
            Color(0xFF0D9488),
            "Upcoming Class"
        )
        else -> Triple(
            Icons.Default.Info,
            Color(0xFF6366F1),
            "System Notification"
        )
    }

    val timeFormatted = remember(item.timestamp) {
        val diff = System.currentTimeMillis() - item.timestamp
        val minutes = diff / (1000 * 60)
        val hours = minutes / 60
        val days = hours / 24
        when {
            minutes < 2 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days == 1L -> "Yesterday"
            days < 7 -> "${days}d ago"
            else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(item.timestamp))
        }
    }

    Surface(
        onClick = onCardClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isDark) {
            if (!item.isRead) Color(0xFF112952) else Color(0xFF0B1B36)
        } else {
            if (!item.isRead) Color(0xFFF0F6FF) else Color(0xFFFFFFFF)
        },
        border = BorderStroke(
            1.dp,
            if (!item.isRead) {
                if (isDark) Color(0xFF2563EB) else Color(0xFFBFDBFE)
            } else {
                if (isDark) Color(0xFF1B3B6B) else Color(0xFFE0EDFB)
            }
        ),
        shadowElevation = if (!item.isRead) 3.dp else 1.5.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("notification_card_${item.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Category & Time Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(iconBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = categoryLabel,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.category == NotificationCategory.LOW_ATTENDANCE_WARNING.name) {
                            Color(0xFFEF4444)
                        } else {
                            if (isDark) Color(0xFF38BDF8) else Color(0xFF1769FF)
                        }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeFormatted,
                        fontSize = 11.sp,
                        color = if (isDark) Color(0xFF8EA5C8) else Color(0xFF64748B)
                    )

                    if (!item.isRead) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1769FF))
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Notification Title
            Text(
                text = item.title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFF5F8FF) else Color(0xFF102653)
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Notification Message
            Text(
                text = item.message,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
            )

            // Action Button if applicable
            if (item.actionLabel.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        onClick = onActionClick,
                        shape = RoundedCornerShape(10.dp),
                        color = if (item.category == NotificationCategory.LOW_ATTENDANCE_WARNING.name) {
                            if (isDark) Color(0x33EF4444) else Color(0xFFFEE2E2)
                        } else {
                            if (isDark) Color(0x2E1769FF) else Color(0xFFEFF6FF)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (item.category == NotificationCategory.LOW_ATTENDANCE_WARNING.name) {
                                Color(0xFFEF4444).copy(alpha = 0.4f)
                            } else {
                                Color(0xFF1769FF).copy(alpha = 0.35f)
                            }
                        )
                    ) {
                        Text(
                            text = item.actionLabel,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.category == NotificationCategory.LOW_ATTENDANCE_WARNING.name) {
                                if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
                            } else {
                                if (isDark) Color(0xFF38BDF8) else Color(0xFF1769FF)
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationSettingsDialog(
    preferences: NotificationPreferences,
    isDark: Boolean,
    onSave: (NotificationPreferences) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var masterEnabled by remember { mutableStateOf(preferences.masterEnabled) }
    var markReminders by remember { mutableStateOf(preferences.markRemindersEnabled) }
    var weeklyReports by remember { mutableStateOf(preferences.weeklyReportsEnabled) }
    var monthlyReports by remember { mutableStateOf(preferences.monthlyReportsEnabled) }
    var lowAttendanceWarnings by remember { mutableStateOf(preferences.lowAttendanceWarningsEnabled) }
    var upcomingReminders by remember { mutableStateOf(preferences.upcomingClassRemindersEnabled) }
    var leadTime by remember { mutableStateOf(preferences.leadTimeMinutes) }
    var quietHours by remember { mutableStateOf(preferences.quietHoursEnabled) }
    var preferredDay by remember { mutableStateOf(preferences.preferredWeeklyDay) }

    // System notification permission check
    val hasSystemPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    var permissionGranted by remember { mutableStateOf(hasSystemPermission) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Notification Preferences", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // System Permission Status Row
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (permissionGranted) {
                            if (isDark) Color(0x2810B981) else Color(0xFFDCFCE7)
                        } else {
                            if (isDark) Color(0x2EF59E0B) else Color(0xFFFEF3C7)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (permissionGranted) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFF59E0B).copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = if (permissionGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (permissionGranted) Color(0xFF10B981) else Color(0xFFD97706),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (permissionGranted) "System Alerts Enabled" else "Android Permission Needed",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (permissionGranted) {
                                        if (isDark) Color(0xFF86EFAC) else Color(0xFF166534)
                                    } else {
                                        if (isDark) Color(0xFFFDE68A) else Color(0xFF78350F)
                                    }
                                )
                            }

                            if (!permissionGranted) {
                                FilledTonalButton(
                                    onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("btn_grant_notification_permission")
                                ) {
                                    Text("Grant", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Master Toggle
                SettingToggleRow(
                    title = "Allow All Notifications",
                    subtitle = "Master switch for in-app and background alerts",
                    checked = masterEnabled,
                    onCheckedChange = { masterEnabled = it }
                )

                if (masterEnabled) {
                    // Attendance Reminders
                    SettingToggleRow(
                        title = "Class Marking Reminders",
                        subtitle = "Alert when a class finishes without being marked",
                        checked = markReminders,
                        onCheckedChange = { markReminders = it }
                    )

                    // Weekly Reports
                    SettingToggleRow(
                        title = "Weekly Attendance Summary",
                        subtitle = "Delivered on $preferredDay at 06:00 PM",
                        checked = weeklyReports,
                        onCheckedChange = { weeklyReports = it }
                    )

                    // Monthly Reports
                    SettingToggleRow(
                        title = "Monthly Performance Breakdown",
                        subtitle = "Delivered on the 1st of every month",
                        checked = monthlyReports,
                        onCheckedChange = { monthlyReports = it }
                    )

                    // Low Attendance Warning
                    SettingToggleRow(
                        title = "Low Attendance Warnings (<75%)",
                        subtitle = "Calculates recovery classes required",
                        checked = lowAttendanceWarnings,
                        onCheckedChange = { lowAttendanceWarnings = it }
                    )

                    // Upcoming Class Reminders
                    SettingToggleRow(
                        title = "Upcoming Class Alerts",
                        subtitle = "$leadTime minutes before class starts",
                        checked = upcomingReminders,
                        onCheckedChange = { upcomingReminders = it }
                    )

                    // Lead time selector
                    if (upcomingReminders) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Reminder Lead Time", fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(5, 10, 15).forEach { mins ->
                                    Surface(
                                        onClick = { leadTime = mins },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (leadTime == mins) Color(0xFF1769FF) else (if (isDark) Color(0xFF10264A) else Color(0xFFEFF6FF)),
                                        border = BorderStroke(1.dp, if (leadTime == mins) Color(0xFF1769FF) else Color(0xFFDBEAFE))
                                    ) {
                                        Text(
                                            text = "${mins}m",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (leadTime == mins) Color.White else (if (isDark) Color(0xFF38BDF8) else Color(0xFF1769FF)),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Quiet Hours
                    SettingToggleRow(
                        title = "Quiet Hours (10:00 PM – 07:00 AM)",
                        subtitle = "Mute audio & vibrations during study or sleep",
                        checked = quietHours,
                        onCheckedChange = { quietHours = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = preferences.copy(
                        masterEnabled = masterEnabled,
                        markRemindersEnabled = markReminders,
                        weeklyReportsEnabled = weeklyReports,
                        monthlyReportsEnabled = monthlyReports,
                        lowAttendanceWarningsEnabled = lowAttendanceWarnings,
                        upcomingClassRemindersEnabled = upcomingReminders,
                        leadTimeMinutes = leadTime,
                        quietHoursEnabled = quietHours,
                        preferredWeeklyDay = preferredDay
                    )
                    onSave(updated)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_save_notification_preferences")
            ) {
                Text("Save Preferences", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val isDark = isAppDarkTheme()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFF5F8FF) else Color(0xFF102653)
            )
            Text(
                text = subtitle,
                fontSize = 10.5.sp,
                color = if (isDark) Color(0xFF8EA5C8) else Color(0xFF64748B)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF1769FF),
                uncheckedThumbColor = if (isDark) Color(0xFF8EA5C8) else Color(0xFF94A3B8),
                uncheckedTrackColor = if (isDark) Color(0xFF10264A) else Color(0xFFE2E8F0)
            )
        )
    }
}
