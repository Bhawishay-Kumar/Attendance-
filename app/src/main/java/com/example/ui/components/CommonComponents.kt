package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.draw.scale
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.remember
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.MenuBook
import com.example.ui.theme.GlassTokens
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Texture
import androidx.compose.material.icons.filled.ViewCompact
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Locale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BrightBlue
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.theme.StatusAbsentText
import com.example.ui.theme.StatusCancelled
import com.example.ui.theme.StatusCancelledBg
import com.example.ui.theme.StatusCancelledText
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.StatusPresentBg
import com.example.ui.theme.StatusPresentText
import com.example.ui.theme.StatusUnmarked
import com.example.ui.theme.StatusUnmarkedBg
import com.example.ui.theme.StatusUnmarkedText
import com.example.ui.viewmodel.DailyAttendanceUiItem

import com.example.ui.theme.isAppDarkTheme
import com.example.ui.theme.StatusAbsentBgDark
import com.example.ui.theme.StatusAbsentBgLight
import com.example.ui.theme.StatusAbsentDark
import com.example.ui.theme.StatusAbsentLight
import com.example.ui.theme.StatusAbsentTextDark
import com.example.ui.theme.StatusAbsentTextLight
import com.example.ui.theme.StatusCancelledBgDark
import com.example.ui.theme.StatusCancelledBgLight
import com.example.ui.theme.StatusCancelledDark
import com.example.ui.theme.StatusCancelledLight
import com.example.ui.theme.StatusCancelledTextDark
import com.example.ui.theme.StatusCancelledTextLight
import com.example.ui.theme.StatusPresentBgDark
import com.example.ui.theme.StatusPresentBgLight
import com.example.ui.theme.StatusPresentDark
import com.example.ui.theme.StatusPresentLight
import com.example.ui.theme.StatusPresentTextDark
import com.example.ui.theme.StatusPresentTextLight
import com.example.ui.theme.StatusUnmarkedBgDark
import com.example.ui.theme.StatusUnmarkedBgLight
import com.example.ui.theme.StatusUnmarkedDark
import com.example.ui.theme.StatusUnmarkedLight
import com.example.ui.theme.StatusUnmarkedTextDark
import com.example.ui.theme.StatusUnmarkedTextLight

@Composable
fun AttendanceStatusBadge(
    status: AttendanceStatus,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()

    val (bgColor, textColor, label, icon) = when (status) {
        AttendanceStatus.PRESENT -> if (isDark) {
            Quad(StatusPresentBgDark, StatusPresentTextDark, "Present", Icons.Default.Check)
        } else {
            Quad(StatusPresentBgLight, StatusPresentTextLight, "Present", Icons.Default.Check)
        }
        AttendanceStatus.ABSENT -> if (isDark) {
            Quad(StatusAbsentBgDark, StatusAbsentTextDark, "Absent", Icons.Default.Close)
        } else {
            Quad(StatusAbsentBgLight, StatusAbsentTextLight, "Absent", Icons.Default.Close)
        }
        AttendanceStatus.CANCELLED -> if (isDark) {
            Quad(StatusCancelledBgDark, StatusCancelledTextDark, "Cancelled", Icons.Default.Block)
        } else {
            Quad(StatusCancelledBgLight, StatusCancelledTextLight, "Cancelled", Icons.Default.Block)
        }
        AttendanceStatus.UNMARKED -> if (isDark) {
            Quad(StatusUnmarkedBgDark, StatusUnmarkedTextDark, "Not Marked", Icons.Default.HelpOutline)
        } else {
            Quad(StatusUnmarkedBgLight, StatusUnmarkedTextLight, "Not Marked", Icons.Default.HelpOutline)
        }
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (isDark) textColor.copy(alpha = 0.35f) else textColor.copy(alpha = 0.18f)
        ),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun AttendanceHeroCard(
    percentage: Float,
    target: Int,
    present: Int,
    conducted: Int,
    absent: Int,
    cancelled: Int,
    classesToTarget: Int,
    classesCanMiss: Int,
    modifier: Modifier = Modifier,
    exactPercentage: Double = percentage.toDouble(),
    isEstimate: Boolean = false
) {
    val isDark = isAppDarkTheme()
    val hasData = conducted > 0
    // Status compares the unrounded percentage against the user's target
    val isEligible = exactPercentage >= target

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = if (isDark) Color(0xFF10264A) else Color(0xFFFFFFFF),
        border = BorderStroke(
            1.dp,
            if (isDark) Color(0xFF1B3B6B) else Color(0xFFE0EDFB)
        ),
        shadowElevation = if (isDark) 2.dp else 3.5.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .testTag("attendance_hero_card")
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .animateContentSize()
        ) {
            // Header Row: Calendar Icon + "Overall Attendance" on left, Status Pill on right
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
                            .background(Color(0xFF2563EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Overall Attendance",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color(0xFFF5F8FF) else Color(0xFF102653)
                    )
                }

                // Status Badge Pill with glow effect when On Track
                val infiniteGlow = rememberInfiniteTransition(label = "hero_on_track_glow")
                val heroGlowAlpha by infiniteGlow.animateFloat(
                    initialValue = 0.35f,
                    targetValue = 0.95f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "hero_glow_alpha"
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isEligible) {
                        if (isDark) Color(0xFF062E1F) else Color(0xFFECFDF5)
                    } else {
                        if (isDark) Color(0xFF2D1220) else Color(0xFFFFF1F2)
                    },
                    border = BorderStroke(
                        width = if (isEligible) 1.5.dp else 1.dp,
                        brush = if (isEligible) {
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF34D399).copy(alpha = heroGlowAlpha),
                                    Color(0xFF10B981).copy(alpha = heroGlowAlpha * 0.9f),
                                    Color(0xFF059669).copy(alpha = heroGlowAlpha)
                                )
                            )
                        } else {
                            SolidColor(if (isDark) Color(0x66EF4444) else Color(0xFFFECDD3))
                        }
                    ),
                    modifier = Modifier.then(
                        if (isEligible) {
                            Modifier.drawBehind {
                                drawRoundRect(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF10B981).copy(alpha = heroGlowAlpha * 0.4f),
                                            Color(0xFF34D399).copy(alpha = heroGlowAlpha * 0.15f),
                                            Color.Transparent
                                        ),
                                        radius = maxOf(size.width, size.height) * 1.3f
                                    ),
                                    cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                                )
                            }
                        } else Modifier
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isEligible) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isEligible) {
                                if (isDark) Color(0xFF4ADE80) else Color(0xFF16A34A)
                            } else {
                                if (isDark) Color(0xFFFB7185) else Color(0xFFE11D48)
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = if (isEligible) "On Track" else "Below Target",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isEligible) {
                                    if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D)
                                } else {
                                    if (isDark) Color(0xFFFB7185) else Color(0xFFE11D48)
                                }
                            )
                            Text(
                                text = if (isEligible) "Target: $target%" else "Req: $target%",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isEligible) {
                                    if (isDark) Color(0xFF86EFAC) else Color(0xFF166534)
                                } else {
                                    if (isDark) Color(0xFFFCA5A5) else Color(0xFFBE123C)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prominent animated percentage figure
            if (hasData) {
                AnimatedPercentageCounter(
                    targetPercentage = percentage,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Color(0xFF102653)
                )
            } else {
                Text(
                    text = "No Data",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Color(0xFF102653)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Progress Bar with Target 75% tick and 0%, Target 75%, 100% labels
            MockupAttendanceProgressBar(
                percentage = percentage,
                target = target,
                height = 8.dp,
                trackColor = if (isDark) Color(0xFF162E56) else Color(0xFFE6EFF9)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Four Equal Statistic Sub-Cards: Attended, Missed, Conducted, Cancelled (matching Image 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. Attended
                HeroStatSquare(
                    title = "Attended",
                    count = present,
                    icon = Icons.Default.Check,
                    iconColor = Color(0xFF10B981),
                    iconBgColor = if (isDark) Color(0x33064E3B) else Color(0xFFDCFCE7),
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                // 2. Missed
                HeroStatSquare(
                    title = "Missed",
                    count = absent,
                    icon = Icons.Default.Close,
                    iconColor = if (isDark) Color(0xFFFB7185) else Color(0xFFEF4444),
                    iconBgColor = if (isDark) Color(0x334C0519) else Color(0xFFFFE4E6),
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                // 3. Conducted
                HeroStatSquare(
                    title = "Conducted",
                    count = conducted,
                    icon = Icons.Default.DateRange,
                    iconColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB),
                    iconBgColor = if (isDark) Color(0x331E3A8A) else Color(0xFFEFF6FF),
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                // 4. Cancelled
                HeroStatSquare(
                    title = "Cancelled",
                    count = cancelled,
                    icon = Icons.Default.Block,
                    iconColor = if (isDark) Color(0xFFFBBF24) else Color(0xFFF59E0B),
                    iconBgColor = if (isDark) Color(0x3378350F) else Color(0xFFFEF3C7),
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            // Safe margin info row (shown when classes have been conducted)
            if (conducted > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF0D2244) else Color(0xFFEFF6FF),
                    border = BorderStroke(
                        1.dp,
                        if (isDark) Color(0xFF1B3B6B) else Color(0xFFDBEAFE)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1769FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val est = if (isEstimate) " (estimate)" else ""
                        val message = if (!hasData) {
                            "No classes conducted yet."
                        } else if (isEligible && classesCanMiss > 0) {
                            "Safe margin: You can safely miss up to $classesCanMiss sessions$est."
                        } else if (!isEligible && classesToTarget > 0) {
                            "Attend $classesToTarget more consecutive sessions to reach $target% target$est."
                        } else {
                            "You are currently on track with the $target% requirement."
                        }
                        Text(
                            text = message,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1E40AF)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MockupAttendanceProgressBar(
    percentage: Float,
    target: Int = 75,
    height: Dp = 8.dp,
    trackColor: Color = if (isAppDarkTheme()) Color(0xFF162E56) else Color(0xFFE6EFF9),
    modifier: Modifier = Modifier
) {
    val progress = (percentage / 100f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "hero_progress_anim"
    )
    val isDark = isAppDarkTheme()

    Column(modifier = modifier.fillMaxWidth()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val totalWidth = maxWidth
            // Base track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(CircleShape)
                    .background(trackColor)
            ) {
                // Filled progress with cyan-to-blue-to-purple gradient
                if (animatedProgress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF06B6D4),
                                        Color(0xFF1769FF),
                                        Color(0xFF8B5CF6)
                                    )
                                )
                            )
                    )
                }
            }

            // Target marker tick at target% (e.g. 75%)
            val targetFraction = (target / 100f).coerceIn(0f, 1f)
            val markerOffset = totalWidth * targetFraction
            Box(
                modifier = Modifier
                    .offset(x = (markerOffset - 1.5.dp).coerceAtLeast(0.dp))
                    .width(3.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(Color(0xFFF59E0B))
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Labels row: 0%, Target 75%, 100%
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val totalWidth = maxWidth
            Text(
                text = "0%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xFF8EA5C8) else Color(0xFF64748B),
                modifier = Modifier.align(Alignment.CenterStart)
            )

            val targetFraction = (target / 100f).coerceIn(0.25f, 0.85f)
            Text(
                text = "Target $target%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                modifier = Modifier
                    .offset(x = (totalWidth * targetFraction - 32.dp).coerceAtLeast(0.dp))
            )

            Text(
                text = "100%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xFF8EA5C8) else Color(0xFF64748B),
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
private fun OverallStatSubCard(
    title: String,
    count: Int,
    countColor: Color,
    badgeBgColor: Color,
    badgeIconColor: Color,
    badgeTextColor: Color,
    badgeIcon: ImageVector,
    subLabel: String,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Color(0xFF0D2244) else Color(0xFFF8FAFD),
        border = BorderStroke(
            1.dp,
            if (isDark) Color(0xFF1A3660) else Color(0xFFE2EDF9)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(badgeBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = null,
                        tint = badgeIconColor,
                        modifier = Modifier.size(10.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeTextColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$count",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = countColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subLabel,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xFF8EA5C8) else Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun HeroStatSquare(
    title: String,
    count: Int,
    icon: ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Color(0xFF0D2244) else Color(0xFFF8FAFD),
        border = BorderStroke(
            1.dp,
            if (isDark) Color(0xFF1A3660) else Color(0xFFE2EDF9)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(13.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$count",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF102653)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xFF8EA5C8) else Color(0xFF64748B)
            )
        }
    }
}


@Composable
fun CalendarEmptyIllustration(
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        // Subtle Sparkle stars
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.7f) else Color(0xFF60A5FA).copy(alpha = 0.8f),
            modifier = Modifier
                .size(13.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = 4.dp)
        )
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.5f) else Color(0xFF93C5FD).copy(alpha = 0.6f),
            modifier = Modifier
                .size(9.dp)
                .align(Alignment.BottomStart)
                .offset(x = 6.dp, y = (-8).dp)
        )
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = if (isDark) Color(0xFF818CF8).copy(alpha = 0.6f) else Color(0xFF93C5FD).copy(alpha = 0.7f),
            modifier = Modifier
                .size(10.dp)
                .align(Alignment.TopStart)
                .offset(x = 8.dp, y = 10.dp)
        )

        // Central glowing disc
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    if (isDark) {
                        Brush.radialGradient(
                            listOf(Color(0xFF1E3A8A), Color(0xFF0F244A))
                        )
                    } else {
                        Brush.radialGradient(
                            listOf(Color(0xFFE0EDFB), Color(0xFFD0E4FA))
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(42.dp),
                contentAlignment = Alignment.Center
            ) {
                // Calendar vector icon
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1769FF),
                    modifier = Modifier.size(36.dp)
                )

                // Small clock overlay badge on bottom right corner of calendar
                Box(
                    modifier = Modifier
                        .size(17.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF10264A) else Color.White)
                        .border(1.2.dp, if (isDark) Color(0xFF38BDF8) else Color(0xFF1769FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1769FF),
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = value,
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun BatchToggleBadge(
    currentBatch: String,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier
    ) {
        Row(modifier = Modifier.padding(3.dp)) {
            val isA = currentBatch.equals("A", ignoreCase = true) || currentBatch.equals("Batch A", ignoreCase = true)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isA) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onToggle("A") }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Batch A",
                    color = if (isA) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.5.sp,
                    fontWeight = if (isA) FontWeight.Bold else FontWeight.Medium
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (!isA) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onToggle("B") }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Batch B",
                    color = if (!isA) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.5.sp,
                    fontWeight = if (!isA) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ClassSessionCard(
    item: DailyAttendanceUiItem,
    onMarkPresent: () -> Unit,
    onMarkAbsent: () -> Unit,
    onMarkCancelled: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    ClassSessionCard(
        item = item,
        onMarkStatus = { status ->
            when (status) {
                AttendanceStatus.PRESENT -> onMarkPresent()
                AttendanceStatus.ABSENT -> onMarkAbsent()
                AttendanceStatus.CANCELLED -> onMarkCancelled()
                AttendanceStatus.UNMARKED -> onReset()
            }
        },
        modifier = modifier
    )
}

/**
 * Interactive tactile bounce compression when pressing buttons,
 * reproducing the CSS active scale animation from the HTML design.
 */
@Composable
fun Modifier.tactilePress(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.94f
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tactile_scale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Redesigned Attendance Session Card based on Claude HTML/CSS tokens and reference mockup.
 * Features glassmorphic backdrop, custom Canvas waves, period capsule, subject icon box,
 * and dynamic action slot (Present/Absent/No-Class -> Result Banner with Undo).
 */
@Composable
fun ClassSessionCard(
    item: DailyAttendanceUiItem,
    onMarkStatus: (AttendanceStatus) -> Unit,
    onEditNote: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()

    val haptic = rememberHaptic()

    // Smooth animated perimeter glow and border colors
    val targetBorderColor = when (item.currentStatus) {
        AttendanceStatus.PRESENT -> if (isDark) Color(0xFF1EE7A5).copy(alpha = 0.85f) else Color(0xFF19CF9C).copy(alpha = 0.75f)
        AttendanceStatus.ABSENT -> if (isDark) Color(0xFFFF5B86).copy(alpha = 0.85f) else Color(0xFFF0305A).copy(alpha = 0.75f)
        AttendanceStatus.CANCELLED -> if (isDark) Color(0xFFA99AFF).copy(alpha = 0.85f) else Color(0xFF7A5CFF).copy(alpha = 0.7f)
        AttendanceStatus.UNMARKED -> if (isDark) Color(0x805A8CFF) else Color.White.copy(alpha = 0.95f)
    }
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "card_border_${item.period}"
    )
    val cardBorder = BorderStroke(1.5.dp, animatedBorderColor)
    val isDoubt = item.subjectCode.equals("DOUBT", ignoreCase = true) ||
                  item.subjectName.contains("Doubt", ignoreCase = true)
    val isElective = item.subjectCode.contains("HTFT", ignoreCase = true) ||
                     item.subjectCode.contains("MNPM", ignoreCase = true) ||
                     item.subjectName.contains("Comfort", ignoreCase = true) ||
                     item.subjectName.contains("Planning", ignoreCase = true)
    val isAlwaysPresent = !isElective && (
        item.subjectCode.equals("LIB", ignoreCase = true) ||
        item.subjectCode.equals("DLIB", ignoreCase = true) ||
        item.subjectCode.equals("SWACH", ignoreCase = true) ||
        item.subjectName.contains("Library", ignoreCase = true) ||
        item.subjectName.contains("Swachh", ignoreCase = true) ||
        item.classType.equals("LIBRARY", ignoreCase = true)
    )

    val cardBackgroundBrush = if (isDark) {
        Brush.linearGradient(
            listOf(
                Color(0xFF0B2260),
                Color(0xFF07163F)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0xF7FFFFFF),
                Color(0xEBEEF5FF)
            )
        )
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        border = cardBorder,
        shadowElevation = if (isDark) 2.5.dp else 1.5.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBackgroundBrush)
        ) {
            // Content column sized compactly to match the reference mockup image
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                // Top Row: Period Chip | Divider | Time | Status Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Period Chip (Gradient capsule)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                        shadowElevation = if (isDark) 0.dp else 1.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF2F6BFF), Color(0xFF7A4DFF))
                                    )
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.5.dp))
                                Text(
                                    text = "Period ${item.periodRoman}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(7.dp))

                    // Subtle vertical divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(13.dp)
                            .background(
                                if (isDark) Color(0x66648CE6) else Color(0x59788CBE)
                            )
                    )

                    Spacer(modifier = Modifier.width(7.dp))

                    // Class Timing
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF6A98EE) else Color(0xFF6B7AA6),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.5.dp))
                        Text(
                            text = "${item.startTime} – ${item.endTime}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFF6A98EE) else Color(0xFF6B7AA6),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (isDoubt) {
                        Surface(
                            shape = CircleShape,
                            color = if (isDark) Color(0x661E293B) else Color(0xCCE2E8F0),
                            border = BorderStroke(1.2.dp, if (isDark) Color(0x8064748B) else Color(0x8094A3B8))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.5.dp))
                                Text(
                                    text = "Doubt Session",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                                )
                            }
                        }
                    } else if (isAlwaysPresent) {
                        Surface(
                            shape = CircleShape,
                            color = if (isDark) Color(0x800A555F) else Color(0xBFCDF6E2),
                            border = BorderStroke(1.2.dp, if (isDark) Color(0xB328E6B4) else Color(0x8C28BE8C))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF3DF0B8) else Color(0xFF0A8758),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.5.dp))
                                Text(
                                    text = "Always Present",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF3DF0B8) else Color(0xFF0A8758)
                                )
                            }
                        }
                    } else {
                        CardStatusPill(
                            status = item.currentStatus,
                            isDark = isDark
                        )
                    }
                }

                // Row 2: Subject Row (Icon Box + Subject Name & Metadata)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Subject Icon Box in rounded square (38dp with 20dp icon)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent,
                        border = BorderStroke(
                            1.dp,
                            if (isDark) Color(0x405A8CFF) else Color.White.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (isDark) {
                                        Brush.linearGradient(
                                            listOf(Color(0x66466ED7), Color(0x661E3C8C))
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            listOf(Color(0xFFF4F7FF), Color(0xFFE1EAFF))
                                        )
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF86B6FF) else Color(0xFF2F5BEA),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(9.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.5.dp)
                    ) {
                        Text(
                            text = item.subjectName,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFF3F6FF) else Color(0xFF0E1250),
                            lineHeight = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.5.dp)
                        ) {
                            Text(
                                text = item.subjectCode,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF22D3EE) else Color(0xFF1D6BFF)
                            )

                            Box(
                                modifier = Modifier
                                    .size(3.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDark) Color(0xFF6A8FE0) else Color(0xFF7E8BB3)
                                    )
                            )

                            Text(
                                text = item.classType + (if (item.batch != "ALL") " • Batch ${item.batch}" else ""),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) Color(0xFF6A8FE0) else Color(0xFF7E8BB3),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Row 3: Action Slot (Present / Absent / No-Class Buttons -> Result Banner with Undo)
                if (isDoubt) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) Color(0x331E293B) else Color(0x26334155),
                        border = BorderStroke(1.2.dp, if (isDark) Color(0x66475569) else Color(0x4D64748B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Doubt Clearing Session • Attendance not counted",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
                            )
                        }
                    }
                } else if (isAlwaysPresent) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) Color(0x3310B981) else Color(0x2610B981),
                        border = BorderStroke(1.2.dp, if (isDark) Color(0x6610B981) else Color(0x4D10B981)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF34D399) else Color(0xFF059669),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Always Present • ${if (item.subjectName.contains("Swachh", ignoreCase = true) || item.subjectCode.equals("SWACH", ignoreCase = true)) "Swachhata Abhiyan" else "Library Session"}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color(0xFF34D399) else Color(0xFF059669)
                            )
                        }
                    }
                } else {
                    AnimatedContent(
                        targetState = item.currentStatus,
                        transitionSpec = {
                            (fadeIn(tween(240, easing = FastOutSlowInEasing)) +
                             scaleIn(initialScale = 0.94f, animationSpec = tween(240, easing = FastOutSlowInEasing)))
                                .togetherWith(
                                    fadeOut(tween(160, easing = FastOutSlowInEasing)) +
                                    scaleOut(targetScale = 0.94f, animationSpec = tween(160, easing = FastOutSlowInEasing))
                                )
                        },
                        label = "card_action_slot_${item.period}"
                    ) { currentStatus ->
                        if (currentStatus == AttendanceStatus.UNMARKED) {
                        // Unmarked: 3 Distinct Interactive Buttons (38dp sleek height)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val presentInteractionSource = remember { MutableInteractionSource() }
                            val absentInteractionSource = remember { MutableInteractionSource() }
                            val cancelInteractionSource = remember { MutableInteractionSource() }

                            // 1. Present Button
                            Surface(
                                onClick = {
                                    haptic.success()
                                    onMarkStatus(AttendanceStatus.PRESENT)
                                },
                                interactionSource = presentInteractionSource,
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Transparent,
                                shadowElevation = if (isDark) 0.dp else 1.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .tactilePress(presentInteractionSource)
                                    .testTag("btn_present_${item.period}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF19CF9C), Color(0xFF07A8B8))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Present",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Present",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // 2. Absent Button
                            Surface(
                                onClick = {
                                    haptic.warn()
                                    onMarkStatus(AttendanceStatus.ABSENT)
                                },
                                interactionSource = absentInteractionSource,
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDark) Color(0xB30A1946) else Color(0xE6FFF3F7),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (isDark) Color(0xCCFF5A8C) else Color(0x80F05078)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .tactilePress(absentInteractionSource)
                                    .testTag("btn_absent_${item.period}")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Absent",
                                        tint = if (isDark) Color(0xFFFF5B86) else Color(0xFFF0305A),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Absent",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFFF5B86) else Color(0xFFF0305A)
                                    )
                                }
                            }

                            // 3. No Class / Cancelled Button (Corrected icon from HTML: Block / Ban icon)
                            Surface(
                                onClick = {
                                    haptic.tap()
                                    onMarkStatus(AttendanceStatus.CANCELLED)
                                },
                                interactionSource = cancelInteractionSource,
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDark) Color(0xBF0F235F) else Color(0xF2EEF4FF),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (isDark) Color(0x805A82E6) else Color(0x8CAA7BEB)
                                ),
                                modifier = Modifier
                                    .size(38.dp)
                                    .tactilePress(cancelInteractionSource)
                                    .testTag("btn_cancel_${item.period}")
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = "Mark No Class",
                                        tint = if (isDark) Color(0xFFA99AFF) else Color(0xFF5A49D8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Redesigned marked attendance button: sleek full-width button only showing "Attendance Marked"
                        val (resultBg, resultBorder, resultFg) = when (currentStatus) {
                            AttendanceStatus.PRESENT -> Triple(
                                if (isDark) Color(0x800A555F) else Color(0xBFCDF6E2),
                                if (isDark) Color(0xB328E6B4) else Color(0x8C28BE8C),
                                if (isDark) Color(0xFF3DF0B8) else Color(0xFF0A8758)
                            )
                            AttendanceStatus.ABSENT -> Triple(
                                if (isDark) Color(0x805F143C) else Color(0xD9FFE4EC),
                                if (isDark) Color(0xB3FF5A8C) else Color(0x80F05078),
                                if (isDark) Color(0xFFFF7B9F) else Color(0xFFD41F48)
                            )
                            AttendanceStatus.CANCELLED -> Triple(
                                if (isDark) Color(0x733C2896) else Color(0xE6ECE9FF),
                                if (isDark) Color(0xB39682FF) else Color(0x808C78F0),
                                if (isDark) Color(0xFFBCAEFF) else Color(0xFF5F45DC)
                            )
                            AttendanceStatus.UNMARKED -> Triple(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Transparent
                            )
                        }

                        val iconPopScale = remember { Animatable(0.4f) }
                        LaunchedEffect(item.currentStatus) {
                            iconPopScale.snapTo(0.4f)
                            iconPopScale.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }

                        val markedInteractionSource = remember { MutableInteractionSource() }
                        Surface(
                            onClick = {
                                haptic.select()
                                onMarkStatus(AttendanceStatus.UNMARKED)
                            },
                            interactionSource = markedInteractionSource,
                            shape = RoundedCornerShape(12.dp),
                            color = resultBg,
                            border = BorderStroke(1.2.dp, resultBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .tactilePress(markedInteractionSource)
                                .testTag("btn_marked_${item.period}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = resultFg,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .scale(iconPopScale.value)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Attendance Marked",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = resultFg,
                                    letterSpacing = 0.2.sp
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

/**
 * Bulk Attendance Action Card styled consistently with ClassSessionCard
 * based on Claude HTML/CSS tokens and reference mockup.
 */
@Composable
fun BulkAttendanceCard(
    totalSessions: Int,
    markedSessions: Int,
    allPresent: Boolean,
    allAbsent: Boolean,
    allCancelled: Boolean = false,
    onMarkAllPresent: () -> Unit,
    onMarkAllAbsent: () -> Unit,
    onMarkAllNoClass: () -> Unit = {},
    onResetDay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()
    val haptic = rememberHaptic()

    val targetBorderColor = when {
        allPresent -> if (isDark) Color(0xFF1EE7A5).copy(alpha = 0.85f) else Color(0xFF19CF9C).copy(alpha = 0.75f)
        allAbsent -> if (isDark) Color(0xFFFF5B86).copy(alpha = 0.85f) else Color(0xFFF0305A).copy(alpha = 0.75f)
        allCancelled -> if (isDark) Color(0xFFA99AFF).copy(alpha = 0.85f) else Color(0xFF7A5CFF).copy(alpha = 0.7f)
        else -> if (isDark) Color(0x805A8CFF) else Color.White.copy(alpha = 0.95f)
    }
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "bulk_card_border"
    )
    val cardBorder = BorderStroke(1.5.dp, animatedBorderColor)

    val cardBackgroundBrush = if (isDark) {
        Brush.linearGradient(
            listOf(
                Color(0xFF0B2260),
                Color(0xFF07163F)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0xF7FFFFFF),
                Color(0xEBEEF5FF)
            )
        )
    }

    val isBulkMarked = allPresent || allAbsent || allCancelled || (markedSessions == totalSessions && totalSessions > 0)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        border = cardBorder,
        shadowElevation = if (isDark) 2.5.dp else 1.5.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBackgroundBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                // Row 1: Header Row with Gradient Capsule | Divider | Session count | Status Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Capsule Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                        shadowElevation = if (isDark) 0.dp else 1.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF2F6BFF), Color(0xFF7A4DFF))
                                    )
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.5.dp))
                                Text(
                                    text = "Bulk Attendance",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(7.dp))

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(13.dp)
                            .background(
                                if (isDark) Color(0x66648CE6) else Color(0x59788CBE)
                            )
                    )

                    Spacer(modifier = Modifier.width(7.dp))

                    // Class Count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF6A98EE) else Color(0xFF6B7AA6),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.5.dp))
                        Text(
                            text = "$totalSessions Classes",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFF6A98EE) else Color(0xFF6B7AA6),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Status Pill
                    val (pillBg, pillBorder, pillFg, pillText, pillIcon) = when {
                        allPresent -> Quintuple(
                            if (isDark) Color(0x800A555F) else Color(0xBFCDF6E2),
                            if (isDark) Color(0xB328E6B4) else Color(0x8C28BE8C),
                            if (isDark) Color(0xFF3DF0B8) else Color(0xFF0A8758),
                            "All Present",
                            Icons.Default.CheckCircle
                        )
                        allAbsent -> Quintuple(
                            if (isDark) Color(0x805F143C) else Color(0xD9FFE4EC),
                            if (isDark) Color(0xB3FF5A8C) else Color(0x80F05078),
                            if (isDark) Color(0xFFFF7B9F) else Color(0xFFD41F48),
                            "All Absent",
                            Icons.Default.Cancel
                        )
                        allCancelled -> Quintuple(
                            if (isDark) Color(0x733C2896) else Color(0xE6ECE9FF),
                            if (isDark) Color(0xB39682FF) else Color(0x808C78F0),
                            if (isDark) Color(0xFFBCAEFF) else Color(0xFF5F45DC),
                            "All No Class",
                            Icons.Default.Block
                        )
                        markedSessions > 0 -> Quintuple(
                            if (isDark) Color(0x7314466E) else Color(0x99CDF6E2),
                            if (isDark) Color(0x9928D2AA) else Color(0x8C6ED2A0),
                            if (isDark) Color(0xFF3DF0B8) else Color(0xFF0B8A5C),
                            "$markedSessions/$totalSessions Marked",
                            Icons.Default.CheckCircle
                        )
                        else -> Quintuple(
                            if (isDark) Color(0x66192850) else Color(0xE6E8EFFC),
                            if (isDark) Color(0x80466EC8) else Color(0x807896D8),
                            if (isDark) Color(0xFF90B4FE) else Color(0xFF285AD8),
                            "0/$totalSessions Marked",
                            Icons.Default.Schedule
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = pillBg,
                        border = BorderStroke(1.2.dp, pillBorder)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = pillIcon,
                                contentDescription = null,
                                tint = pillFg,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.5.dp))
                            Text(
                                text = pillText,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = pillFg
                            )
                        }
                    }
                }

                // Row 2: Subject/Action Info Row (Icon Box + Title + Subtitle)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent,
                        border = BorderStroke(
                            1.dp,
                            if (isDark) Color(0x405A8CFF) else Color.White.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (isDark) {
                                        Brush.linearGradient(
                                            listOf(Color(0x66466ED7), Color(0x661E3C8C))
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            listOf(Color(0xFFF4F7FF), Color(0xFFE1EAFF))
                                        )
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF86B6FF) else Color(0xFF2F5BEA),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(9.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.5.dp)
                    ) {
                        Text(
                            text = "Mark All Today's Classes",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFF3F6FF) else Color(0xFF0E1250),
                            lineHeight = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (markedSessions > 0)
                                "$markedSessions of $totalSessions periods marked • One-tap bulk action"
                            else
                                "One-tap attendance for all $totalSessions scheduled periods",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color(0xFF6A8FE0) else Color(0xFF7E8BB3),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Animated horizontal line progress bar reaching final position
                val lineProgress = if (totalSessions > 0) {
                    if (allPresent || allAbsent || allCancelled) 1f
                    else (markedSessions.toFloat() / totalSessions.toFloat()).coerceIn(0f, 1f)
                } else 0f

                val animatedLineProgress by animateFloatAsState(
                    targetValue = lineProgress,
                    animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
                    label = "bulk_line_progress"
                )

                val lineBrush = when {
                    allPresent -> Brush.horizontalGradient(listOf(Color(0xFF19CF9C), Color(0xFF07A8B8)))
                    allAbsent -> Brush.horizontalGradient(listOf(Color(0xFFFF5B86), Color(0xFFF0305A)))
                    allCancelled -> Brush.horizontalGradient(listOf(Color(0xFFA99AFF), Color(0xFF7A5CFF)))
                    else -> Brush.horizontalGradient(listOf(Color(0xFF3B82F6), Color(0xFF06B6D4)))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isDark) Color(0x334E75DC) else Color(0x293B82F6))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedLineProgress)
                            .clip(RoundedCornerShape(2.dp))
                            .background(lineBrush)
                    )
                }

                // Row 3: Action Slot with AnimatedContent (3 Buttons <-> Result Banner with Undo)
                AnimatedContent(
                    targetState = isBulkMarked,
                    transitionSpec = {
                        (fadeIn(tween(240, easing = FastOutSlowInEasing)) +
                         scaleIn(initialScale = 0.94f, animationSpec = tween(240, easing = FastOutSlowInEasing)))
                            .togetherWith(
                                fadeOut(tween(160, easing = FastOutSlowInEasing)) +
                                scaleOut(targetScale = 0.94f, animationSpec = tween(160, easing = FastOutSlowInEasing))
                            )
                    },
                    label = "bulk_action_slot"
                ) { marked ->
                    if (!marked) {
                        // Unmarked / In-progress: 3 Interactive Action Buttons (38dp height)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val presentInteractionSource = remember { MutableInteractionSource() }
                            val absentInteractionSource = remember { MutableInteractionSource() }
                            val cancelInteractionSource = remember { MutableInteractionSource() }

                            // 1. All Present Button
                            Surface(
                                onClick = {
                                    haptic.success()
                                    onMarkAllPresent()
                                },
                                interactionSource = presentInteractionSource,
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Transparent,
                                shadowElevation = if (isDark) 0.dp else 1.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .tactilePress(presentInteractionSource)
                                    .testTag("btn_bulk_present")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF19CF9C), Color(0xFF07A8B8))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "All Present",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "All Present",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // 2. All Absent Button
                            Surface(
                                onClick = {
                                    haptic.warn()
                                    onMarkAllAbsent()
                                },
                                interactionSource = absentInteractionSource,
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDark) Color(0xB30A1946) else Color(0xE6FFF3F7),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (isDark) Color(0xCCFF5A8C) else Color(0x80F05078)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .tactilePress(absentInteractionSource)
                                    .testTag("btn_bulk_absent")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "All Absent",
                                        tint = if (isDark) Color(0xFFFF5B86) else Color(0xFFF0305A),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "All Absent",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFFF5B86) else Color(0xFFF0305A)
                                    )
                                }
                            }

                            // 3. No Class Button on right side of Absent (HTML ban/block icon)
                            Surface(
                                onClick = {
                                    haptic.select()
                                    onMarkAllNoClass()
                                },
                                interactionSource = cancelInteractionSource,
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDark) Color(0xBF0F235F) else Color(0xF2EEF4FF),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (isDark) Color(0x805A82E6) else Color(0x8CAA7BEB)
                                ),
                                modifier = Modifier
                                    .size(38.dp)
                                    .tactilePress(cancelInteractionSource)
                                    .testTag("btn_bulk_cancel")
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = "Mark All No Class",
                                        tint = if (isDark) Color(0xFFA99AFF) else Color(0xFF5A49D8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Bulk Marked: Compact Result Banner with Undo Button (38dp height)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val (resultBg, resultBorder, resultFg, resultMsg, resultSub, resultIcon) = when {
                                allPresent -> ResultBarSpec(
                                    bg = if (isDark) Color(0x800A555F) else Color(0xBFCDF6E2),
                                    border = if (isDark) Color(0xB328E6B4) else Color(0x8C28BE8C),
                                    fg = if (isDark) Color(0xFF3DF0B8) else Color(0xFF0A8758),
                                    msg = "All classes marked present",
                                    sub = "", // Only show message "All classes marked present" only
                                    icon = Icons.Default.CheckCircle
                                )
                                allAbsent -> ResultBarSpec(
                                    bg = if (isDark) Color(0x805F143C) else Color(0xD9FFE4EC),
                                    border = if (isDark) Color(0xB3FF5A8C) else Color(0x80F05078),
                                    fg = if (isDark) Color(0xFFFF7B9F) else Color(0xFFD41F48),
                                    msg = "All classes marked absent",
                                    sub = "All $totalSessions sessions recorded",
                                    icon = Icons.Default.Cancel
                                )
                                allCancelled -> ResultBarSpec(
                                    bg = if (isDark) Color(0x733C2896) else Color(0xE6ECE9FF),
                                    border = if (isDark) Color(0xB39682FF) else Color(0x808C78F0),
                                    fg = if (isDark) Color(0xFFBCAEFF) else Color(0xFF5F45DC),
                                    msg = "All classes cancelled",
                                    sub = "No sessions held today",
                                    icon = Icons.Default.Block
                                )
                                else -> ResultBarSpec(
                                    bg = if (isDark) Color(0x800A555F) else Color(0xBFCDF6E2),
                                    border = if (isDark) Color(0xB328E6B4) else Color(0x8C28BE8C),
                                    fg = if (isDark) Color(0xFF3DF0B8) else Color(0xFF0A8758),
                                    msg = "$markedSessions of $totalSessions classes marked",
                                    sub = "Attendance recorded",
                                    icon = Icons.Default.CheckCircle
                                )
                            }

                            // Result Status Display Box
                            val bulkIconPopScale = remember { Animatable(0.4f) }
                            LaunchedEffect(allPresent, allAbsent, allCancelled, markedSessions) {
                                bulkIconPopScale.snapTo(0.4f)
                                bulkIconPopScale.animateTo(
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = resultBg,
                                border = BorderStroke(1.2.dp, resultBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = resultIcon,
                                        contentDescription = null,
                                        tint = resultFg,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .scale(bulkIconPopScale.value)
                                    )
                                    Spacer(modifier = Modifier.width(7.dp))
                                    Column {
                                        Text(
                                            text = resultMsg,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            color = resultFg
                                        )
                                        if (resultSub.isNotBlank()) {
                                            Text(
                                                text = resultSub,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 9.5.sp,
                                                color = resultFg.copy(alpha = 0.85f)
                                            )
                                        }
                                    }
                                }
                            }

                            // Undo Button with Tactile Bounce
                            val undoInteractionSource = remember { MutableInteractionSource() }
                            Surface(
                                onClick = {
                                    haptic.select()
                                    onResetDay()
                                },
                                interactionSource = undoInteractionSource,
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDark) Color(0xBF0F235F) else Color(0xF2EEF4FF),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (isDark) Color(0x805A82E6) else Color(0x8CAA7BEB)
                                ),
                                modifier = Modifier
                                    .height(38.dp)
                                    .tactilePress(undoInteractionSource)
                                    .testTag("btn_bulk_undo")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(horizontal = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Undo,
                                        contentDescription = "Undo",
                                        tint = if (isDark) Color(0xFFA99AFF) else Color(0xFF5A49D8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Undo",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isDark) Color(0xFFA99AFF) else Color(0xFF5A49D8)
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

private data class ResultBarSpec(
    val bg: Color,
    val border: Color,
    val fg: Color,
    val msg: String,
    val sub: String,
    val icon: ImageVector
)

/**
 * Non-actionable Status Indicator Pill matching the Claude HTML design tokens (compact).
 */
@Composable
fun CardStatusPill(
    status: AttendanceStatus,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val (pillBg, pillBorder, pillFg, label, icon) = when (status) {
        AttendanceStatus.PRESENT -> Quintuple(
            if (isDark) Color(0x800A555F) else Color(0xBFCDF6E2),
            if (isDark) Color(0xB328E6B4) else Color(0x8C28BE8C),
            if (isDark) Color(0xFF3DF0B8) else Color(0xFF0A8758),
            "Present",
            Icons.Default.CheckCircle
        )
        AttendanceStatus.ABSENT -> Quintuple(
            if (isDark) Color(0x805F143C) else Color(0xD9FFE4EC),
            if (isDark) Color(0xB3FF5A8C) else Color(0x80F05078),
            if (isDark) Color(0xFFFF7B9F) else Color(0xFFD41F48),
            "Absent",
            Icons.Default.Cancel
        )
        AttendanceStatus.CANCELLED -> Quintuple(
            if (isDark) Color(0x733C2896) else Color(0xE6ECE9FF),
            if (isDark) Color(0xB39682FF) else Color(0x808C78F0),
            if (isDark) Color(0xFFBCAEFF) else Color(0xFF5F45DC),
            "No Class",
            Icons.Default.Block
        )
        AttendanceStatus.UNMARKED -> Quintuple(
            if (isDark) Color(0x66192850) else Color(0xE6E8EFFC),
            if (isDark) Color(0x80466EC8) else Color(0x807896D8),
            if (isDark) Color(0xFF90B4FE) else Color(0xFF285AD8),
            "Not Marked",
            Icons.Default.Schedule
        )
    }

    Surface(
        shape = CircleShape,
        color = pillBg,
        border = BorderStroke(1.2.dp, pillBorder),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = pillFg,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.5.dp))
            Text(
                text = label,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = pillFg
            )
        }
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

/**
 * CardWaveBackground: Replaced with no-op per user requirement to remove wavy design.
 */
@Composable
fun CardWaveBackground(
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    // Intentionally no-op to remove wavy curves from cards
}

@Composable
fun AppConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    confirmButtonText: String = confirmText,
    cancelButtonText: String = cancelText,
    dismissButtonText: String = cancelButtonText,
    isDestructive: Boolean = false,
    isDark: Boolean = isAppDarkTheme(),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val finalConfirm = if (confirmButtonText != "Confirm") confirmButtonText else confirmText
    val finalCancel = if (dismissButtonText != "Cancel") dismissButtonText else if (cancelButtonText != "Cancel") cancelButtonText else cancelText
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = { Text(text = message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) StatusAbsent else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(finalConfirm)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(finalCancel)
            }
        }
    )
}

@Composable
fun BulkAttendanceCard(
    items: List<com.example.ui.viewmodel.DailyAttendanceUiItem>,
    onBulkMark: (AttendanceStatus) -> Unit,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Only count 7 classes in bulk attendance marker not 8, excluding the doubt clearing session
    val filteredItems = items.filter { item ->
        val isDoubt = item.subjectCode.equals("DOUBT", ignoreCase = true) ||
                      item.subjectName.contains("Doubt", ignoreCase = true) ||
                      item.classType.contains("Doubt", ignoreCase = true)
        !isDoubt
    }
    val effectiveItems = if (filteredItems.isNotEmpty()) filteredItems else items
    val totalCount = effectiveItems.size
    val markedCount = effectiveItems.count { it.currentStatus != AttendanceStatus.UNMARKED }
    val allPresent = effectiveItems.isNotEmpty() && effectiveItems.all { it.currentStatus == AttendanceStatus.PRESENT }
    val allAbsent = effectiveItems.isNotEmpty() && effectiveItems.all { it.currentStatus == AttendanceStatus.ABSENT }
    val allCancelled = effectiveItems.isNotEmpty() && effectiveItems.all { it.currentStatus == AttendanceStatus.CANCELLED }

    BulkAttendanceCard(
        totalSessions = totalCount,
        markedSessions = markedCount,
        allPresent = allPresent,
        allAbsent = allAbsent,
        allCancelled = allCancelled,
        onMarkAllPresent = { onBulkMark(AttendanceStatus.PRESENT) },
        onMarkAllAbsent = { onBulkMark(AttendanceStatus.ABSENT) },
        onMarkAllNoClass = { onBulkMark(AttendanceStatus.CANCELLED) },
        onResetDay = { onBulkMark(AttendanceStatus.UNMARKED) },
        modifier = modifier
    )
}

