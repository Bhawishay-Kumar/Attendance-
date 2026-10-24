package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.theme.GlassTokens
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.ui.auth.GoogleAuthHelper
import com.example.ui.screens.DailyScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TimetableScreen
import com.example.ui.theme.AccentTheme
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.isAppDarkTheme
import com.example.ui.viewmodel.AttendanceViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.layout.WindowInsets
import com.example.ui.components.BkMonogramLogo
import com.example.ui.components.AppLogo
import com.example.ui.components.DayNightSlider
import com.example.ui.components.ScenicBackground
import com.example.ui.components.FloatingBottomNavBar
import com.example.ui.screens.NotificationCenterScreen
import com.example.ui.screens.whatif.WhatIfPredictorScreen

enum class ScreenTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    DAILY("Attendance", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
    TIMETABLE("Timetable", Icons.Filled.Schedule, Icons.Outlined.Schedule),
    REPORTS("Reports", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: AttendanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            val profile by viewModel.profile.collectAsState()
            val accent = AccentTheme.values().find { it.name.equals(profile.accentTheme, ignoreCase = true) } ?: AccentTheme.IIHT_BLUE

            MyApplicationTheme(
                themePreference = profile.themePreference,
                accentTheme = accent
            ) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: AttendanceViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val profile by viewModel.profile.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var currentTab by remember { mutableStateOf(ScreenTab.HOME) }
    var showNotificationsScreen by remember { mutableStateOf(false) }
    var showWhatIfPredictorScreen by remember { mutableStateOf(false) }
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsState()

    // Attempt silent auto sign-in after initial frame renders
    LaunchedEffect(Unit) {
        delay(600)
        val credentialManager = CredentialManager.create(context)
        GoogleAuthHelper.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                coroutineScope.launch {
                    val localRecords = viewModel.allAttendanceRecords.value
                    viewModel.firestoreRepo.syncAllLocalToCloud(localRecords, profile)
                }
            },
            onUnauthenticated = { /* Runs offline */ },
            scope = coroutineScope
        )
    }

    // BackHandler: If in Notification Center, close it. If in What-If, close it. If on another tab, back navigates to HOME
    BackHandler(enabled = showNotificationsScreen) {
        showNotificationsScreen = false
    }

    BackHandler(enabled = !showNotificationsScreen && showWhatIfPredictorScreen) {
        showWhatIfPredictorScreen = false
    }

    BackHandler(enabled = !showNotificationsScreen && !showWhatIfPredictorScreen && currentTab != ScreenTab.HOME) {
        currentTab = ScreenTab.HOME
    }

    val isDark = isAppDarkTheme()

    ScenicBackground(isDark = isDark) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWide = maxWidth >= 600.dp

            Row(modifier = Modifier.fillMaxSize()) {
                if (isWide && !showNotificationsScreen && !showWhatIfPredictorScreen) {
                    NavigationRail(
                        containerColor = if (isDark) Color(0xEB0A162D) else Color(0xF5FFFFFF),
                        modifier = Modifier
                            .fillMaxHeight()
                            .testTag("navigation_rail"),
                        header = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                            ) {
                                AppLogo(
                                    size = 42.dp,
                                    showGlow = true,
                                    modifier = Modifier.clickable { currentTab = ScreenTab.HOME }.testTag("rail_bk_logo")
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Attendance",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isDark) Color.White else Color(0xFF102653)
                                )
                            }
                        }
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        ScreenTab.values().forEach { tab ->
                            val isSelected = currentTab == tab
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = Color(0xFF3B6BFF),
                                    selectedTextColor = Color(0xFF3B6BFF),
                                    unselectedIconColor = if (isDark) Color(0xFF8EA5C8) else Color(0xFF64748B),
                                    unselectedTextColor = if (isDark) Color(0xFF8EA5C8) else Color(0xFF64748B),
                                    indicatorColor = if (isDark) Color(0x333B6BFF) else Color(0xFFDBEAFE)
                                ),
                                modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.weight(1f),
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    topBar = {
                        if (!showNotificationsScreen && !showWhatIfPredictorScreen) {
                            AppHeader(
                                title = currentTab.title,
                                batch = profile.batch,
                                isDark = isDark,
                                unreadCount = unreadNotificationsCount,
                                onToggleTheme = { dark ->
                                    val nextTheme = if (dark) "DARK" else "LIGHT"
                                    viewModel.setThemePreference(nextTheme)
                                },
                                onLogoClick = { currentTab = ScreenTab.HOME },
                                onNotificationsClick = {
                                    showNotificationsScreen = true
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = innerPadding.calculateTopPadding())
                                .widthIn(max = 840.dp)
                        ) {
                            if (showNotificationsScreen) {
                                NotificationCenterScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { showNotificationsScreen = false },
                                    onNavigateToTab = { tab ->
                                        currentTab = tab
                                        showNotificationsScreen = false
                                    }
                                )
                            } else if (showWhatIfPredictorScreen) {
                                WhatIfPredictorScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { showWhatIfPredictorScreen = false }
                                )
                            } else {
                                AnimatedContent(
                                    targetState = currentTab,
                                    transitionSpec = {
                                        val forward = targetState.ordinal >= initialState.ordinal
                                        (slideInHorizontally(
                                            initialOffsetX = { fullWidth -> if (forward) fullWidth / 4 else -fullWidth / 4 },
                                            animationSpec = tween(420, easing = FastOutSlowInEasing)
                                        ) + fadeIn(animationSpec = tween(300))) togetherWith
                                        (slideOutHorizontally(
                                            targetOffsetX = { fullWidth -> if (forward) -fullWidth / 4 else fullWidth / 4 },
                                            animationSpec = tween(420, easing = FastOutSlowInEasing)
                                        ) + fadeOut(animationSpec = tween(250)))
                                    },
                                    label = "ScreenTransition"
                                ) { targetScreen ->
                                    when (targetScreen) {
                                        ScreenTab.HOME -> {
                                            HomeScreen(
                                                viewModel = viewModel,
                                                onNavigateToTimetable = { currentTab = ScreenTab.TIMETABLE },
                                                onNavigateToDaily = { currentTab = ScreenTab.DAILY },
                                                onNavigateToReports = { currentTab = ScreenTab.REPORTS },
                                                onNavigateToSettings = { currentTab = ScreenTab.SETTINGS },
                                                onNavigateToWhatIf = { showWhatIfPredictorScreen = true }
                                            )
                                        }
                                        ScreenTab.DAILY -> {
                                            DailyScreen(viewModel = viewModel)
                                        }
                                        ScreenTab.TIMETABLE -> {
                                            TimetableScreen(viewModel = viewModel)
                                        }
                                        ScreenTab.REPORTS -> {
                                            ReportsScreen(
                                                viewModel = viewModel,
                                                onNavigateToDate = { date ->
                                                    viewModel.setSelectedDate(date)
                                                    currentTab = ScreenTab.DAILY
                                                }
                                            )
                                        }
                                        ScreenTab.SETTINGS -> {
                                            SettingsScreen(
                                                viewModel = viewModel,
                                                onNavigateToTimetable = { currentTab = ScreenTab.TIMETABLE }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Floating bottom navigation bar matching HTML (.dockwrap fixed at bottom, transparent background outside dock)
                        if (!isWide && !showNotificationsScreen && !showWhatIfPredictorScreen) {
                            FloatingBottomNavBar(
                                currentTab = currentTab,
                                onTabSelected = { currentTab = it },
                                isDark = isDark,
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppHeader(
    title: String = "Attendance",
    batch: String,
    isDark: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onLogoClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    unreadCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Logo + App Title & Batch Subtitle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onLogoClick() }
        ) {
            AppLogo(
                size = 42.dp,
                showGlow = true,
                modifier = Modifier.testTag("header_bk_logo")
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                AnimatedContent(
                    targetState = title,
                    transitionSpec = {
                        (slideInVertically { height -> height / 2 } + fadeIn()) togetherWith
                        (slideOutVertically { height -> -height / 2 } + fadeOut())
                    },
                    label = "header_title"
                ) { targetTitle ->
                    Text(
                        text = targetTitle,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color(0xFFF2F5FF) else Color(0xFF0E1A52),
                        letterSpacing = (-0.3).sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Batch $batch • Smart Tracker",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color(0xFF9DB0DF) else Color(0xFF6578A8)
                )
            }
        }

        // Right: Day/Night Slider Toggle + Notification Bell
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Theme Toggle Slider (spring-driven glass switch)
            DayNightSlider(
                isDark = isDark,
                onToggle = onToggleTheme
            )

            // Notification Bell with unclipped badge
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .testTag("header_notification_bell"),
                contentAlignment = Alignment.TopEnd
            ) {
                // Circular Bell Icon Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDark) Color(0x80244096) else Color(0xE6E8F0FF))
                        .border(
                            1.5.dp,
                            if (isDark) Color(0x805A87EB) else Color(0x99AAC3F0),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onNotificationsClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = if (isDark) Color(0xFFF2F5FF) else Color(0xFF0E1A52),
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Notification Badge
                if (unreadCount > 0) {
                    val countText = when {
                        unreadCount <= 99 -> "$unreadCount"
                        else -> "99+"
                    }
                    val isSingleDigit = unreadCount in 1..9

                    Box(
                        modifier = Modifier
                            .offset(x = 4.dp, y = (-4).dp)
                            .defaultMinSize(minWidth = 19.dp, minHeight = 19.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF4D6A))
                            .border(
                                width = 1.5.dp,
                                color = if (isDark) Color(0xFF040B22) else Color.White,
                                shape = CircleShape
                            )
                            .padding(horizontal = if (isSingleDigit) 3.dp else 5.dp, vertical = 1.dp)
                            .clickable { onNotificationsClick() }
                            .testTag("notification_badge_counter"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = countText,
                            fontSize = if (countText.length >= 3) 8.sp else 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            lineHeight = 11.sp
                        )
                    }
                }
            }
        }
    }
}

