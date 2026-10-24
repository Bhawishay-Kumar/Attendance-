package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Tune
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.geometry.Offset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.ui.theme.LoraFontFamily
import com.example.ui.theme.Outfit
import com.example.ui.theme.WorkSans
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.ui.auth.GoogleAuthHelper
import com.example.ui.components.AppConfirmationDialog
import com.example.ui.components.DeveloperInfoSection
import com.example.ui.components.HapticManager
import com.example.ui.components.rememberHaptic
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.StatusPresentBg
import com.example.ui.theme.isAppDarkTheme
import com.example.ui.viewmodel.AttendanceViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AttendanceViewModel,
    onNavigateToTimetable: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val profile by viewModel.profile.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isDark = isAppDarkTheme()
    val haptic = rememberHaptic()

    var targetAttendance by remember(profile) { mutableFloatStateOf(profile.targetAttendancePct.toFloat()) }
    var targetInputText by remember(profile.targetAttendancePct) { mutableStateOf(profile.targetAttendancePct.toString()) }
    var targetInputError by remember { mutableStateOf<String?>(null) }
    var isAuthLoading by remember { mutableStateOf(false) }

    // Dialog state
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showResetAllConfirm by remember { mutableStateOf(false) }
    var showRestoreDefaultTimetableConfirm by remember { mutableStateOf(false) }
    var showRestoreJsonDialog by remember { mutableStateOf(false) }
    var restoreJsonText by remember { mutableStateOf("") }
    var restoreJsonError by remember { mutableStateOf<String?>(null) }
    var pendingBatchToSwitch by remember { mutableStateOf<String?>(null) }

    val credentialManager = remember { CredentialManager.create(context) }

    fun shareBackupJson() {
        coroutineScope.launch {
            val json = viewModel.exportJson()
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, json)
                type = "application/json"
            }
            context.startActivity(Intent.createChooser(intent, "Share Attendance Backup"))
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Page Title & Subtitle Header
        item {
            Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                Text(
                    text = "Settings & Preferences",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GlassTokens.textColor(isDark),
                    letterSpacing = (-0.3).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Customize student profile, timetable, themes, and cloud persistence",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlassTokens.mutedColor(isDark)
                )
            }
        }

        // =====================================================================
        // 1. Student Profile Card (HTML .pcard identity card specification)
        // =====================================================================
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xB3101E4A) else Color(0xCCFFFFFF)
                ),
                border = BorderStroke(
                    0.8.dp,
                    if (isDark) Color(0x663E70EB) else Color(0x66B0C4FF)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_profile_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Top Cover: Smooth gradient transition blending into card background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(104.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF2F6BFF),
                                        Color(0xFF7A4DFF),
                                        if (isDark) Color(0xB3101E4A) else Color(0xCCFFFFFF)
                                    )
                                )
                            )
                    ) {
                        // Glow circle 1 (top right)
                        Canvas(
                            modifier = Modifier
                                .size(170.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(Color.White.copy(alpha = 0.38f), Color.Transparent)
                                ),
                                radius = size.width / 2f,
                                center = Offset(size.width * 0.9f, 0f)
                            )
                        }
                        // Glow circle 2 (bottom middle-left)
                        Canvas(
                            modifier = Modifier
                                .size(130.dp)
                                .align(Alignment.BottomStart)
                        ) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(Color.White.copy(alpha = 0.35f), Color.Transparent)
                                ),
                                radius = size.width / 2f,
                                center = Offset(size.width * 0.7f, size.height)
                            )
                        }
                    }

                    // Overlapping Avatar: 86dp rounded square (offset -43dp)
                    Box(
                        modifier = Modifier
                            .padding(start = 18.dp)
                            .offset(y = (-43).dp)
                            .size(86.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color.White, Color(0xFFE6EEFF))
                                )
                            )
                            .padding(4.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF3B6BFF), Color(0xFF8B5CF6))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getStudentInitials(profile.studentName),
                            fontFamily = Outfit,
                            fontSize = 29.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    // Body content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                            .offset(y = (-30).dp)
                    ) {
                        // Student Name in Lora 25sp bold
                        Text(
                            text = profile.studentName.ifBlank { "Bhawishya Kumar" },
                            fontFamily = LoraFontFamily,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.2).sp,
                            color = if (isDark) Color(0xFFF2F5FF) else Color(0xFF0C1B4A),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        // Roll Number Pill
                        Surface(
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = BorderStroke(0.8.dp, if (isDark) Color(0x665A87EB) else Color(0x403B6BFF)),
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (isDark) {
                                        Brush.horizontalGradient(listOf(Color(0x33244096), Color(0x1F182F70)))
                                    } else {
                                        Brush.horizontalGradient(listOf(Color(0x1F3B6BFF), Color(0x12DBEAFE)))
                                    }
                                )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF7AA2FF) else Color(0xFF2563EB),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = profile.rollNo.ifBlank { "IIHT/2024/BHTT/01" },
                                    fontFamily = WorkSans,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = if (isDark) Color(0xFF7AA2FF) else Color(0xFF2563EB)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Chips: Course, Semester, Academic Year, Lab Batch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProfileTagChip(
                                icon = Icons.Default.MenuBook,
                                text = profile.course.ifBlank { "B.Tech." },
                                isDark = isDark,
                                modifier = Modifier.weight(1f)
                            )
                            ProfileTagChip(
                                icon = Icons.Default.DateRange,
                                text = profile.semester.ifBlank { "5th Sem" },
                                isDark = isDark,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProfileTagChip(
                                icon = Icons.Default.Schedule,
                                text = profile.academicYear.ifBlank { "2026–27" },
                                isDark = isDark,
                                modifier = Modifier.weight(1f)
                            )
                            ProfileTagChip(
                                icon = Icons.Default.Group,
                                text = "Batch ${profile.batch}",
                                isDark = isDark,
                                isHighlight = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2 List items: College and Branch
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ProfileDetailRow(
                                icon = Icons.Default.AccountBalance,
                                label = "COLLEGE",
                                value = profile.college.ifBlank { "Indian Institute of Handloom Technology, Varanasi" },
                                isDark = isDark
                            )
                            ProfileDetailRow(
                                icon = Icons.Default.Layers,
                                label = "BRANCH",
                                value = profile.branch.ifBlank { "Handloom and Textile Technology" },
                                isDark = isDark
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Full-width primary gradient "Edit Profile" Button
                        Surface(
                            onClick = {
                                haptic.select()
                                showEditProfileDialog = true
                            },
                            shape = RoundedCornerShape(18.dp),
                            color = Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF3B6BFF), Color(0xFF7A4DFF))
                                    )
                                )
                                .testTag("btn_edit_profile")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Edit Profile",
                                    fontFamily = Outfit,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 2. Appearance & Theme (Segmented Sliding Glass Control, No Accent Picker)
        // =====================================================================
        item {
            SettingsGlassCard(isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingsIconBox(
                            icon = Icons.Default.SettingsBrightness,
                            isDark = isDark
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Appearance & Theme",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = GlassTokens.textColor(isDark)
                            )
                            Text(
                                text = "Synchronized with day/night atmospheric scenery",
                                fontSize = 11.sp,
                                color = GlassTokens.mutedColor(isDark)
                            )
                        }
                    }

                    // Sliding Glass Segmented Theme Selector (Light, Dark, System)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent,
                        border = BorderStroke(0.8.dp, if (isDark) Color(0x334E7BFF) else Color(0x40FFFFFF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isDark) {
                                    Brush.horizontalGradient(listOf(Color(0x40081232), Color(0x30101E4A)))
                                } else {
                                    Brush.horizontalGradient(listOf(Color(0x66E2E8F5), Color(0x40F1F5F9)))
                                }
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val currentPref = profile.themePreference.uppercase()
                            listOf(
                                Triple("LIGHT", "Light", Icons.Default.LightMode),
                                Triple("DARK", "Dark", Icons.Default.DarkMode),
                                Triple("SYSTEM", "System", Icons.Default.SettingsBrightness)
                            ).forEach { (mode, label, icon) ->
                                val isSelected = currentPref == mode
                                Surface(
                                    onClick = {
                                        haptic.select()
                                        viewModel.setThemePreference(mode)
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) {
                                        if (isDark) Color(0xFF2855D8) else Color.White
                                    } else {
                                        Color.Transparent
                                    },
                                    shadowElevation = if (isSelected) (if (isDark) 3.dp else 1.5.dp) else 0.dp,
                                    border = if (isSelected) {
                                        BorderStroke(0.8.dp, if (isDark) Color(0x805A8CFF) else Color(0x80D0E0FF))
                                    } else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("theme_btn_${mode.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) {
                                                if (isDark) Color.White else Color(0xFF1D5FE8)
                                            } else {
                                                GlassTokens.mutedColor(isDark)
                                            },
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) {
                                                if (isDark) Color.White else Color(0xFF0F172A)
                                            } else {
                                                GlassTokens.mutedColor(isDark)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 3. My Lab Batch & Academic Category
        // =====================================================================
        item {
            SettingsGlassCard(isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingsIconBox(
                            icon = Icons.Default.Tune,
                            isDark = isDark
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "My Lab Batch & Category",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = GlassTokens.textColor(isDark)
                            )
                            Text(
                                text = "Determines schedule slots and elective attendance",
                                fontSize = 11.sp,
                                color = GlassTokens.mutedColor(isDark)
                            )
                        }
                    }

                    // Lab Batch Selector: Batch A vs Batch B
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isBatchA = profile.batch.equals("A", ignoreCase = true)
                        val isBatchB = profile.batch.equals("B", ignoreCase = true)

                        // Choice 1: Batch A
                        Surface(
                            onClick = {
                                if (!isBatchA) {
                                    haptic.select()
                                    pendingBatchToSwitch = "A"
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Transparent,
                            border = BorderStroke(
                                width = 0.8.dp,
                                color = if (isBatchA) Color(0x803B82F6) else (if (isDark) Color(0x2E4E7BFF) else Color(0x33CBD5E1))
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isBatchA) {
                                        if (isDark) {
                                            Brush.horizontalGradient(listOf(Color(0x4A1E40AF), Color(0x302563EB)))
                                        } else {
                                            Brush.horizontalGradient(listOf(Color(0xFFDBEAFE), Color(0xFFEFF6FF)))
                                        }
                                    } else {
                                        if (isDark) {
                                            Brush.horizontalGradient(listOf(Color(0x2E0A1430), Color(0x20101E4A)))
                                        } else {
                                            Brush.horizontalGradient(listOf(Color(0x38E2E8F0), Color(0x25F1F5F9)))
                                        }
                                    }
                                )
                                .testTag("btn_select_batch_a")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Batch A",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isBatchA) (if (isDark) Color(0xFF93C5FD) else Color(0xFF1E3A8A)) else GlassTokens.textColor(isDark)
                                    )
                                    Text(
                                        text = if (isBatchA) "Active Lab Batch" else "Tap to Switch",
                                        fontSize = 10.5.sp,
                                        color = if (isBatchA) Color(0xFF3B82F6) else GlassTokens.mutedColor(isDark)
                                    )
                                }
                                if (isBatchA) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Active",
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Choice 2: Batch B
                        Surface(
                            onClick = {
                                if (!isBatchB) {
                                    haptic.select()
                                    pendingBatchToSwitch = "B"
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Transparent,
                            border = BorderStroke(
                                width = 0.8.dp,
                                color = if (isBatchB) Color(0x803B82F6) else (if (isDark) Color(0x2E4E7BFF) else Color(0x33CBD5E1))
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isBatchB) {
                                        if (isDark) {
                                            Brush.horizontalGradient(listOf(Color(0x4A1E40AF), Color(0x302563EB)))
                                        } else {
                                            Brush.horizontalGradient(listOf(Color(0xFFDBEAFE), Color(0xFFEFF6FF)))
                                        }
                                    } else {
                                        if (isDark) {
                                            Brush.horizontalGradient(listOf(Color(0x2E0A1430), Color(0x20101E4A)))
                                        } else {
                                            Brush.horizontalGradient(listOf(Color(0x38E2E8F0), Color(0x25F1F5F9)))
                                        }
                                    }
                                )
                                .testTag("btn_select_batch_b")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Batch B",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isBatchB) (if (isDark) Color(0xFF93C5FD) else Color(0xFF1E3A8A)) else GlassTokens.textColor(isDark)
                                    )
                                    Text(
                                        text = if (isBatchB) "Active Lab Batch" else "Tap to Switch",
                                        fontSize = 10.5.sp,
                                        color = if (isBatchB) Color(0xFF3B82F6) else GlassTokens.mutedColor(isDark)
                                    )
                                }
                                if (isBatchB) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Active",
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Academic Category Selector
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Academic Curriculum Track",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GlassTokens.mutedColor(isDark)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple("Major", "Regular B.Tech", "btn_category_major"),
                                Triple("Minor", "Interdisciplinary", "btn_category_minor"),
                                Triple("Library", "Always Present", "btn_category_library")
                            ).forEach { (catName, subtitle, testTag) ->
                                val isSelected = profile.academicCategory.equals(catName, ignoreCase = true)
                                Surface(
                                    onClick = {
                                        if (!isSelected) {
                                            haptic.select()
                                            viewModel.updateAcademicCategory(catName)
                                        }
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.Transparent,
                                    border = BorderStroke(
                                        width = 0.8.dp,
                                        color = if (isSelected) Color(0x803B82F6) else (if (isDark) Color(0x204E7BFF) else Color(0x26CBD5E1))
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSelected) {
                                                if (isDark) {
                                                    Brush.verticalGradient(listOf(Color(0x451E40AF), Color(0x2E2563EB)))
                                                } else {
                                                    Brush.verticalGradient(listOf(Color(0xFFDBEAFE), Color(0xFFEFF6FF)))
                                                }
                                            } else {
                                                if (isDark) {
                                                    Brush.verticalGradient(listOf(Color(0x240A1430), Color(0x18101E4A)))
                                                } else {
                                                    Brush.verticalGradient(listOf(Color(0x2EEDF2F7), Color(0x1AF8FAFC)))
                                                }
                                            }
                                        )
                                        .testTag(testTag)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(4.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = catName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            color = if (isSelected) Color(0xFF3B82F6) else GlassTokens.textColor(isDark)
                                        )
                                        Text(
                                            text = subtitle,
                                            fontSize = 9.5.sp,
                                            color = GlassTokens.mutedColor(isDark),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 4. Attendance Target Threshold (Slider + Increment buttons + Input)
        // =====================================================================
        item {
            SettingsGlassCard(isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingsIconBox(
                                icon = Icons.Default.School,
                                isDark = isDark
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Attendance Target",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = GlassTokens.textColor(isDark)
                                )
                                Text(
                                    text = "Target attendance percentage threshold",
                                    fontSize = 11.sp,
                                    color = GlassTokens.mutedColor(isDark)
                                )
                            }
                        }

                        // Target percentage badge with stepper controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    val currentVal = targetAttendance.roundToInt()
                                    if (currentVal > 50) {
                                        haptic.tap()
                                        val newVal = (currentVal - 1).toFloat()
                                        targetAttendance = newVal
                                        targetInputText = newVal.toInt().toString()
                                        targetInputError = null
                                        viewModel.updateProfile(profile.copy(targetAttendancePct = newVal.toInt()))
                                    }
                                },
                                modifier = Modifier
                                    .size(30.dp)
                                    .testTag("btn_target_decrease")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease 1%",
                                    tint = if (isDark) Color(0xFF6AA6FF) else Color(0xFF2563EB),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Transparent,
                                border = BorderStroke(0.8.dp, if (isDark) Color(0x803B82F6) else Color(0x6693C5FD)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isDark) {
                                            Brush.horizontalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF2563EB)))
                                        } else {
                                            Brush.horizontalGradient(listOf(Color(0xFFDBEAFE), Color(0xFFEFF6FF)))
                                        }
                                    )
                            ) {
                                Text(
                                    text = "${targetAttendance.roundToInt()}%",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1E3A8A),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    val currentVal = targetAttendance.roundToInt()
                                    if (currentVal < 100) {
                                        haptic.tap()
                                        val newVal = (currentVal + 1).toFloat()
                                        targetAttendance = newVal
                                        targetInputText = newVal.toInt().toString()
                                        targetInputError = null
                                        viewModel.updateProfile(profile.copy(targetAttendancePct = newVal.toInt()))
                                    }
                                },
                                modifier = Modifier
                                    .size(30.dp)
                                    .testTag("btn_target_increase")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase 1%",
                                    tint = if (isDark) Color(0xFF6AA6FF) else Color(0xFF2563EB),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Slider Control
                    Slider(
                        value = targetAttendance,
                        onValueChange = { newVal ->
                            val rounded = kotlin.math.round(newVal)
                            targetAttendance = rounded
                            val intVal = rounded.toInt()
                            targetInputText = intVal.toString()
                            targetInputError = null
                            viewModel.updateProfile(profile.copy(targetAttendancePct = intVal))
                        },
                        valueRange = 50f..100f,
                        steps = 49,
                        colors = SliderDefaults.colors(
                            thumbColor = if (isDark) Color(0xFF6AA6FF) else Color(0xFF2563EB),
                            activeTrackColor = if (isDark) Color(0xFF3B82F6) else Color(0xFF2563EB),
                            inactiveTrackColor = if (isDark) Color(0x334E7BFF) else Color(0x40CBD5E1)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("slider_attendance_target")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("50%", fontSize = 11.sp, color = GlassTokens.mutedColor(isDark))
                        Text("75% (Mandatory Target)", fontSize = 11.sp, color = if (isDark) Color(0xFF6AA6FF) else Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                        Text("100%", fontSize = 11.sp, color = GlassTokens.mutedColor(isDark))
                    }
                }
            }
        }

        // =====================================================================
        // 5. Customize Dashboard Cards
        // =====================================================================
        item {
            SettingsGlassCard(isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingsIconBox(
                            icon = Icons.Default.DashboardCustomize,
                            isDark = isDark
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Customize Dashboard Cards",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = GlassTokens.textColor(isDark)
                            )
                            Text(
                                text = "Toggle which cards appear on the home screen",
                                fontSize = 11.sp,
                                color = GlassTokens.mutedColor(isDark)
                            )
                        }
                    }

                    DashboardCardToggleRow(
                        title = "Overall Attendance Hero Card",
                        subtitle = "Target percentage, conducted vs attended sessions",
                        checked = profile.showOverviewCard,
                        onCheckedChange = { viewModel.toggleDashboardCard("overview", it) },
                        isDark = isDark
                    )
                    DashboardCardToggleRow(
                        title = "Monthly Attendance Summary",
                        subtitle = "Monthly progress bar and statistics",
                        checked = profile.showMonthlyCard,
                        onCheckedChange = { viewModel.toggleDashboardCard("monthly", it) },
                        isDark = isDark
                    )
                    DashboardCardToggleRow(
                        title = "Today's Schedule Card",
                        subtitle = "Daily timetable with one-tap status shortcuts",
                        checked = profile.showTodayCard,
                        onCheckedChange = { viewModel.toggleDashboardCard("today", it) },
                        isDark = isDark
                    )
                    DashboardCardToggleRow(
                        title = "Recent Attendance Logs",
                        subtitle = "Recent attendance events with undo capability",
                        checked = profile.showRecentCard,
                        onCheckedChange = { viewModel.toggleDashboardCard("recent", it) },
                        isDark = isDark
                    )
                    DashboardCardToggleRow(
                        title = "Weekly Timetable Preview",
                        subtitle = "Weekly recurring timetable carousel",
                        checked = profile.showWeeklyCard,
                        onCheckedChange = { viewModel.toggleDashboardCard("weekly", it) },
                        isDark = isDark
                    )
                }
            }
        }

        // =====================================================================
        // 6. Haptics & Visual Effects
        // =====================================================================
        item {
            SettingsGlassCard(isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingsIconBox(
                            icon = Icons.Default.Vibration,
                            isDark = isDark
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Haptics & Tactile Effects",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = GlassTokens.textColor(isDark)
                            )
                            Text(
                                text = "Tactile spring response and micro-animations",
                                fontSize = 11.sp,
                                color = GlassTokens.mutedColor(isDark)
                            )
                        }
                    }

                    // Haptic Feedback Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Haptic Vibration",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp,
                                color = GlassTokens.textColor(isDark)
                            )
                            Text(
                                text = "Tactile clicks on attendance marking and switches",
                                fontSize = 11.sp,
                                color = GlassTokens.mutedColor(isDark)
                            )
                        }
                        Switch(
                            checked = HapticManager.hapticsEnabled.value,
                            onCheckedChange = {
                                HapticManager.setHapticsEnabled(it)
                                if (it) haptic.tap()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = if (isDark) Color(0xFF3B82F6) else Color(0xFF2563EB)
                            )
                        )
                    }

                    // Intensity Selector (Light, Medium, Strong)
                    if (HapticManager.hapticsEnabled.value) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Vibration Strength",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GlassTokens.mutedColor(isDark)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val currentStrength = HapticManager.hapticStrength.value.lowercase()
                                listOf("Light", "Medium", "Strong").forEach { strength ->
                                    val isSelected = currentStrength == strength.lowercase()
                                    Surface(
                                        onClick = {
                                            HapticManager.setHapticStrength(strength.lowercase())
                                            haptic.tap()
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color.Transparent,
                                        border = BorderStroke(
                                            0.8.dp,
                                            if (isSelected) Color(0x803B82F6) else (if (isDark) Color(0x204E7BFF) else Color(0x26CBD5E1))
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                if (isSelected) {
                                                    if (isDark) {
                                                        Brush.horizontalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF2563EB)))
                                                    } else {
                                                        Brush.horizontalGradient(listOf(Color(0xFFDBEAFE), Color(0xFFEFF6FF)))
                                                    }
                                                } else {
                                                    if (isDark) {
                                                        Brush.horizontalGradient(listOf(Color(0x2B081232), Color(0x1C101E4A)))
                                                    } else {
                                                        Brush.horizontalGradient(listOf(Color(0x33E2E8F0), Color(0x20F1F5F9)))
                                                    }
                                                }
                                            )
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = strength,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) (if (isDark) Color(0xFF93C5FD) else Color(0xFF1E3A8A)) else GlassTokens.mutedColor(isDark)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Test Haptic Button
                        OutlinedButton(
                            onClick = {
                                haptic.success()
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(0.8.dp, if (isDark) Color(0x404E7BFF) else Color(0x40CBD5E1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Haptic Vibration", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Particle Effects Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Celebration Particles",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp,
                                color = GlassTokens.textColor(isDark)
                            )
                            Text(
                                text = "Confetti effects on reaching attendance goals",
                                fontSize = 11.sp,
                                color = GlassTokens.mutedColor(isDark)
                            )
                        }
                        Switch(
                            checked = HapticManager.particlesEnabled.value,
                            onCheckedChange = { HapticManager.setParticlesEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = if (isDark) Color(0xFF3B82F6) else Color(0xFF2563EB)
                            )
                        )
                    }
                }
            }
        }

        // =====================================================================
        // 7. Cloud Backup & Sync (Google Sign-In via Credential Manager)
        // =====================================================================
        item {
            SettingsGlassCard(isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingsIconBox(
                                icon = Icons.Default.CloudSync,
                                isDark = isDark
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Cloud Backup & Sync",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = GlassTokens.textColor(isDark)
                                )
                                Text(
                                    text = "Google Account synchronization",
                                    fontSize = 11.sp,
                                    color = GlassTokens.mutedColor(isDark)
                                )
                            }
                        }

                        if (currentUser != null) {
                            Surface(
                                color = StatusPresentBg,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = StatusPresent, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Synced", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusPresent)
                                }
                            }
                        }
                    }

                    if (currentUser != null) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.Transparent,
                            border = BorderStroke(0.8.dp, if (isDark) Color(0x334E7BFF) else Color(0x40CBD5E1)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isDark) {
                                        Brush.horizontalGradient(listOf(Color(0x33081232), Color(0x22101E4A)))
                                    } else {
                                        Brush.horizontalGradient(listOf(Color(0x66F1F5F9), Color(0x40FFFFFF)))
                                    }
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = if (isDark) Color(0xFF6AA6FF) else Color(0xFF2563EB), modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentUser?.displayName ?: profile.studentName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = GlassTokens.textColor(isDark)
                                    )
                                    Text(
                                        text = currentUser?.email ?: "Signed in with Google",
                                        fontSize = 11.5.sp,
                                        color = GlassTokens.mutedColor(isDark)
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        GoogleAuthHelper.signOut(
                                            context = context,
                                            credentialManager = credentialManager,
                                            onSignOutComplete = {
                                                coroutineScope.launch {
                                                    viewModel.updateProfile(profile)
                                                }
                                            },
                                            scope = coroutineScope
                                        )
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(0.8.dp, if (isDark) Color(0x404E7BFF) else Color(0x40CBD5E1))
                                ) {
                                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sign Out", fontSize = 11.sp)
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Sign in with Google to securely synchronize attendance records and timetable overrides across devices via Cloud Firestore.",
                            fontSize = 12.sp,
                            color = GlassTokens.mutedColor(isDark),
                            lineHeight = 16.sp
                        )

                        Button(
                            onClick = {
                                isAuthLoading = true
                                GoogleAuthHelper.onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        isAuthLoading = false
                                        coroutineScope.launch {
                                            val localRecords = viewModel.allAttendanceRecords.value
                                            viewModel.firestoreRepo.syncAllLocalToCloud(localRecords, profile)
                                        }
                                    },
                                    onAuthError = { isAuthLoading = false },
                                    scope = coroutineScope,
                                    onAuthCancelled = { isAuthLoading = false }
                                )
                            },
                            enabled = !isAuthLoading,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Color(0xFF2563EB) else Color(0xFF1D4ED8)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("btn_google_signin")
                        ) {
                            if (isAuthLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sign In with Google", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 8. Timetable Editor & Data Backup / Clear
        // =====================================================================
        item {
            SettingsGlassCard(isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingsIconBox(
                            icon = Icons.Default.Backup,
                            isDark = isDark
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Data Management & Timetable",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = GlassTokens.textColor(isDark)
                            )
                            Text(
                                text = "JSON backup, official restore, and reset",
                                fontSize = 11.sp,
                                color = GlassTokens.mutedColor(isDark)
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = onNavigateToTimetable,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Full Timetable Editor", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { shareBackupJson() },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(0.8.dp, if (isDark) Color(0x404E7BFF) else Color(0x40CBD5E1)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export Backup", fontSize = 11.5.sp)
                        }

                        OutlinedButton(
                            onClick = { showRestoreJsonDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(0.8.dp, if (isDark) Color(0x404E7BFF) else Color(0x40CBD5E1)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore Backup", fontSize = 11.5.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = { showRestoreDefaultTimetableConfirm = true },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(0.8.dp, if (isDark) Color(0x404E7BFF) else Color(0x40CBD5E1)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Restore Official Handloom & Textile Timetable", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { showResetAllConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusAbsent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear All Attendance Records", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // =====================================================================
        // 9. Developer & Message Section (Bhawishya Kumar Card)
        // =====================================================================
        item {
            DeveloperInfoSection(
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }

    // =========================================================================
    // Dialogs
    // =========================================================================

    // Edit Profile Sheet (HTML profileSheet specification)
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(profile.studentName) }
        var editRollNo by remember { mutableStateOf(profile.rollNo) }
        var editCollege by remember { mutableStateOf(profile.college) }
        var editCourse by remember { mutableStateOf(profile.course) }
        var editBranch by remember { mutableStateOf(profile.branch) }
        var editSemester by remember { mutableStateOf(profile.semester) }
        var editAcademicYear by remember { mutableStateOf(profile.academicYear) }
        var nameError by remember { mutableStateOf<String?>(null) }

        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { showEditProfileDialog = false },
            sheetState = sheetState,
            containerColor = if (isDark) Color(0xFF0F1E4A) else Color(0xFFF8FAFF),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = Modifier.testTag("edit_profile_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .padding(bottom = 24.dp)
            ) {
                // Header with live avatar preview
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(62.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF3B6BFF), Color(0xFF8B5CF6))
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
                    ) {
                        Text(
                            text = getStudentInitials(editName),
                            fontFamily = Outfit,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Column {
                        Text(
                            text = "Edit Profile",
                            fontFamily = Outfit,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFF2F5FF) else Color(0xFF0C1B4A)
                        )
                        Text(
                            text = editName.ifBlank { "Your name" },
                            fontSize = 13.sp,
                            color = if (isDark) Color(0xFF9DB0DF) else Color(0xFF5B70A6)
                        )
                    }
                }

                // Section: PERSONAL
                EditProfileSectionHeader(title = "PERSONAL", isDark = isDark)
                Spacer(modifier = Modifier.height(6.dp))

                EditProfileInputField(
                    label = "Full Name",
                    value = editName,
                    onValueChange = {
                        editName = it
                        if (it.isNotBlank()) nameError = null
                    },
                    icon = Icons.Default.Person,
                    placeholder = "Your name",
                    isDark = isDark
                )

                EditProfileInputField(
                    label = "Roll Number",
                    value = editRollNo,
                    onValueChange = { editRollNo = it },
                    icon = Icons.Default.Badge,
                    placeholder = "IIHT/2024/...",
                    isDark = isDark
                )

                // Section: ACADEMIC
                Spacer(modifier = Modifier.height(10.dp))
                EditProfileSectionHeader(title = "ACADEMIC", isDark = isDark)
                Spacer(modifier = Modifier.height(6.dp))

                EditProfileInputField(
                    label = "College",
                    value = editCollege,
                    onValueChange = { editCollege = it },
                    icon = Icons.Default.AccountBalance,
                    placeholder = "College name",
                    isDark = isDark
                )

                EditProfileInputField(
                    label = "Branch",
                    value = editBranch,
                    onValueChange = { editBranch = it },
                    icon = Icons.Default.Layers,
                    placeholder = "Branch",
                    isDark = isDark
                )

                EditProfileInputField(
                    label = "Course",
                    value = editCourse,
                    onValueChange = { editCourse = it },
                    icon = Icons.Default.MenuBook,
                    placeholder = "B.Tech.",
                    isDark = isDark
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        EditProfileInputField(
                            label = "Semester",
                            value = editSemester,
                            onValueChange = { editSemester = it },
                            icon = Icons.Default.DateRange,
                            placeholder = "5th Semester",
                            isDark = isDark
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        EditProfileInputField(
                            label = "Academic Year",
                            value = editAcademicYear,
                            onValueChange = { editAcademicYear = it },
                            icon = Icons.Default.Schedule,
                            placeholder = "2026–27",
                            isDark = isDark
                        )
                    }
                }

                // Error message
                if (nameError != null) {
                    Text(
                        text = nameError ?: "",
                        color = Color(0xFFFF3B5C),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sticky Action Bar: Cancel + Save Changes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            haptic.tap()
                            showEditProfileDialog = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (isDark) Color(0x663E70EB) else Color(0x66B0C4FF)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontFamily = Outfit,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFF2F5FF) else Color(0xFF0C1B4A)
                        )
                    }

                    Button(
                        onClick = {
                            if (editName.isBlank()) {
                                nameError = "Name cannot be empty."
                                haptic.warn()
                                return@Button
                            }
                            haptic.success()
                            viewModel.updateProfile(
                                profile.copy(
                                    studentName = editName.trim(),
                                    rollNo = editRollNo.trim(),
                                    college = editCollege.trim(),
                                    course = editCourse.trim(),
                                    branch = editBranch.trim(),
                                    semester = editSemester.trim(),
                                    academicYear = editAcademicYear.trim()
                                )
                            )
                            showEditProfileDialog = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF3B6BFF), Color(0xFF7A4DFF))
                                )
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Save Changes",
                                fontFamily = Outfit,
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

    // Confirmation for Clear All Records
    if (showResetAllConfirm) {
        AppConfirmationDialog(
            title = "Clear All Attendance Records?",
            message = "This will permanently delete all saved attendance logs. Your timetable slots and profile configuration will remain intact.",
            confirmButtonText = "Delete Everything",
            isDestructive = true,
            onConfirm = {
                viewModel.clearAllAttendance()
                showResetAllConfirm = false
            },
            onDismiss = { showResetAllConfirm = false }
        )
    }

    // Confirmation for Restore Official Timetable
    if (showRestoreDefaultTimetableConfirm) {
        AppConfirmationDialog(
            title = "Restore Official IIHT Timetable?",
            message = "This will reset all weekly slots to the official Handloom & Textile Technology curriculum. Your past recorded attendance will remain safe.",
            confirmButtonText = "Restore Timetable",
            isDestructive = false,
            onConfirm = {
                viewModel.restoreTimetableVersion(1)
                showRestoreDefaultTimetableConfirm = false
            },
            onDismiss = { showRestoreDefaultTimetableConfirm = false }
        )
    }

    // Restore JSON dialog
    if (showRestoreJsonDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreJsonDialog = false },
            title = { Text("Restore Attendance Backup", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Paste the JSON backup string below to restore attendance records and settings:",
                        fontSize = 12.sp,
                        color = GlassTokens.mutedColor(isDark)
                    )
                    OutlinedTextField(
                        value = restoreJsonText,
                        onValueChange = {
                            restoreJsonText = it
                            restoreJsonError = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        placeholder = { Text("{\"profile\": ...}") },
                        isError = restoreJsonError != null
                    )
                    if (restoreJsonError != null) {
                        Text(
                            text = restoreJsonError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = viewModel.restoreJson(restoreJsonText)
                            if (success) {
                                showRestoreJsonDialog = false
                                restoreJsonText = ""
                                restoreJsonError = null
                            } else {
                                restoreJsonError = "Failed to restore backup. Invalid JSON format."
                            }
                        }
                    },
                    enabled = restoreJsonText.isNotBlank()
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRestoreJsonDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation for Switching Lab Batch
    if (pendingBatchToSwitch != null) {
        val targetBatch = pendingBatchToSwitch!!
        AppConfirmationDialog(
            title = "Switch Lab Batch to Batch $targetBatch?",
            message = "Switching your lab batch will immediately update your timetable and attendance views to display Batch $targetBatch laboratory sessions and the alternate seminar practicals.\n\nAll your historical attendance records remain completely preserved.",
            confirmButtonText = "Confirm Switch",
            cancelButtonText = "Cancel",
            isDestructive = false,
            onConfirm = {
                viewModel.updateBatch(targetBatch)
                pendingBatchToSwitch = null
            },
            onDismiss = { pendingBatchToSwitch = null }
        )
    }
}

// =============================================================================
// Helper Composables for Consistent Glassmorphic Cards & Rows
// =============================================================================

@Composable
private fun SettingsGlassCard(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val borderBrush = Brush.linearGradient(
        if (isDark) listOf(Color(0x454A6EFF), Color(0x1A4A6EFF))
        else listOf(Color(0x332563EB), Color(0x142563EB))
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(GlassTokens.surfaceBrush(isDark))
            .border(BorderStroke(0.dp, borderBrush), shape)
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
private fun SettingsIconBox(
    icon: ImageVector,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        border = BorderStroke(0.8.dp, if (isDark) Color(0x334E7BFF) else Color(0x403B6BFF)),
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDark) {
                        Brush.linearGradient(listOf(Color(0x4D3B6BFF), Color(0x331E3A8A)))
                    } else {
                        Brush.linearGradient(listOf(Color(0xFFE8F0FE), Color(0xFFDBEAFE)))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDark) Color(0xFF6AA6FF) else Color(0xFF2563EB),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = GlassTokens.mutedColor(isDark)
        )
        Text(
            text = value.ifBlank { "—" },
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = GlassTokens.textColor(isDark),
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 220.dp)
        )
    }
}

@Composable
private fun DashboardCardToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = GlassTokens.textColor(isDark)
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = GlassTokens.mutedColor(isDark)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = if (isDark) Color(0xFF3B82F6) else Color(0xFF2563EB)
            )
        )
    }
}

private fun getStudentInitials(name: String): String {
    val words = name.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
    return when {
        words.isEmpty() -> "?"
        words.size == 1 -> words[0].take(2).uppercase()
        else -> "${words[0].first()}${words[1].first()}".uppercase()
    }
}

@Composable
private fun ProfileTagChip(
    icon: ImageVector,
    text: String,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    isHighlight: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        border = BorderStroke(
            0.8.dp,
            if (isHighlight) {
                if (isDark) Color(0x665A87EB) else Color(0x403B6BFF)
            } else {
                if (isDark) Color(0x335A87EB) else Color(0x203B6BFF)
            }
        ),
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isHighlight) {
                    if (isDark) {
                        Brush.horizontalGradient(listOf(Color(0x383B6BFF), Color(0x206366F1)))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0x263B6BFF), Color(0x18818CF8)))
                    }
                } else {
                    if (isDark) {
                        Brush.horizontalGradient(listOf(Color(0x24244096), Color(0x18101E4A)))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0x183B6BFF), Color(0x0FDBEAFE)))
                    }
                }
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDark) Color(0xFF7AA2FF) else Color(0xFF2563EB),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                fontFamily = WorkSans,
                fontSize = 12.sp,
                fontWeight = if (isHighlight) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (isDark) Color(0xFFB9C9F2) else Color(0xFF1E3A8A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProfileDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    isDark: Boolean
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        border = BorderStroke(0.8.dp, if (isDark) Color(0x335A87EB) else Color(0x263B6BFF)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isDark) {
                    Brush.horizontalGradient(
                        listOf(Color(0x2E244096), Color(0x1A14245A), Color(0x120C1738))
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(Color(0x1F3B6BFF), Color(0x14EEF4FF), Color(0x0CDBEAFE))
                    )
                }
            )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isDark) {
                            Brush.linearGradient(listOf(Color(0x335B8CFF), Color(0x203B6BFF)))
                        } else {
                            Brush.linearGradient(listOf(Color(0x265B8CFF), Color(0x183B6BFF)))
                        }
                    )
                    .border(0.8.dp, if (isDark) Color(0x335A87EB) else Color(0x263B6BFF), RoundedCornerShape(14.dp))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isDark) Color(0xFF7AA2FF) else Color(0xFF2563EB),
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = label,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = if (isDark) Color(0xFF9DB0DF) else Color(0xFF5B70A6)
                )
                Text(
                    text = value,
                    fontFamily = Outfit,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFF2F5FF) else Color(0xFF0C1B4A),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun EditProfileSectionHeader(
    title: String,
    isDark: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontFamily = WorkSans,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            color = if (isDark) Color(0xFF9DB0DF) else Color(0xFF5B70A6)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(if (isDark) Color(0x405A87EB) else Color(0x40B0C4FF))
        )
    }
}

@Composable
private fun EditProfileInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color(0xFF9DB0DF) else Color(0xFF5B70A6)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 13.5.sp) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF7AA2FF) else Color(0xFF2563EB),
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF3B6BFF),
                unfocusedBorderColor = if (isDark) Color(0x4D5A87EB) else Color(0x4DB0C4FF),
                focusedContainerColor = if (isDark) Color(0x33101E4A) else Color(0x66FFFFFF),
                unfocusedContainerColor = if (isDark) Color(0x26101E4A) else Color(0x33FFFFFF)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
