package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassTokens

/**
 * Modern glassmorphic empty state card adhering to Material Design 3 and
 * seamless in both Light and Dark themes.
 */
@Composable
fun ModernEmptyStateCard(
    title: String,
    subtitle: String,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.CalendarToday,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    testTag: String = "modern_empty_state_card"
) {
    GlassCard(
        isDark = isDark,
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glowing decorative icon disc
            Box(
                modifier = Modifier.size(76.dp),
                contentAlignment = Alignment.Center
            ) {
                // Subtle sparkle indicator
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.8f) else Color(0xFF2563EB).copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 2.dp)
                )

                // Outer ambient glow ring
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDark) {
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFF1E3A8A).copy(alpha = 0.6f),
                                        Color(0xFF0F172A).copy(alpha = 0.2f)
                                    )
                                )
                            } else {
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFFDBEAFE).copy(alpha = 0.9f),
                                        Color(0xFFEFF6FF).copy(alpha = 0.3f)
                                    )
                                )
                            }
                        )
                        .border(
                            width = 1.dp,
                            brush = if (isDark) {
                                Brush.linearGradient(
                                    listOf(Color(0xFF38BDF8).copy(alpha = 0.4f), Color.Transparent)
                                )
                            } else {
                                Brush.linearGradient(
                                    listOf(Color(0xFF93C5FD).copy(alpha = 0.6f), Color(0xFFBFDBFE).copy(alpha = 0.3f))
                                )
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1D4ED8),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GlassTokens.textColor(isDark),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Normal,
                color = GlassTokens.mutedColor(isDark),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.widthIn(max = 320.dp)
            )

            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF),
                        contentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1D4ED8)
                    ),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text(
                        text = actionLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Compact pill-styled badge for inline "No Data" stats.
 */
@Composable
fun NoDataBadge(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    label: String = "No Data"
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isDark) Color(0xFF1E293B).copy(alpha = 0.7f) else Color(0xFFE2E8F0).copy(alpha = 0.8f)
            )
            .border(
                1.dp,
                if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFCBD5E1).copy(alpha = 0.8f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = GlassTokens.mutedColor(isDark)
        )
    }
}
