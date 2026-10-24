package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.isAppDarkTheme
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Sequential entrance wrapper providing smooth fade-in and subtle slide-up
 * based on index stagger.
 */
@Composable
fun StaggeredEntrance(
    index: Int,
    modifier: Modifier = Modifier,
    baseDelayMs: Long = 45L,
    durationMs: Int = 320,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay((index * baseDelayMs).coerceAtLeast(0L))
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
        label = "stagger_alpha_$index"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 20f,
        animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
        label = "stagger_offset_$index"
    )

    Box(
        modifier = modifier
            .alpha(alpha)
            .offset(y = offsetY.dp)
    ) {
        content()
    }
}

/**
 * Animated numerical counter that smoothly counts from 0 (or previous value)
 * to target percentage with 1 decimal place.
 */
@Composable
fun AnimatedPercentageCounter(
    targetPercentage: Float,
    fontSize: androidx.compose.ui.unit.TextUnit = 38.sp,
    color: Color = Color.White,
    fontWeight: FontWeight = FontWeight.Bold,
    durationMs: Int = 850,
    modifier: Modifier = Modifier
) {
    val animatedValue by animateFloatAsState(
        targetValue = targetPercentage.coerceIn(0f, 100f),
        animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
        label = "counter_anim"
    )

    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = modifier
    ) {
        Text(
            text = String.format(java.util.Locale.US, "%.1f", animatedValue),
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color
        )
        Text(
            text = "%",
            fontSize = (fontSize.value * 0.55f).sp,
            fontWeight = FontWeight.SemiBold,
            color = color.copy(alpha = 0.85f),
            modifier = Modifier.padding(bottom = (fontSize.value * 0.12f).dp, start = 2.dp)
        )
    }
}

/**
 * Animated Circular Attendance Ring showing attendance percentage and comparison with target.
 */
@Composable
fun AnimatedCircularAttendanceRing(
    percentage: Float,
    target: Int = 75,
    size: Dp = 130.dp,
    strokeWidth: Dp = 10.dp,
    modifier: Modifier = Modifier
) {
    val progress = (percentage / 100f).coerceIn(0f, 1f)
    val targetProgress = (target / 100f).coerceIn(0f, 1f)
    val isEligible = percentage >= target

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 950, easing = FastOutSlowInEasing),
        label = "ring_progress"
    )

    val ringColor = when {
        percentage >= target -> Color(0xFF10B981) // Emerald green
        percentage >= target - 5f -> Color(0xFFF59E0B) // Amber warning
        else -> Color(0xFFEF4444) // Coral red
    }

    val gradientBrush = Brush.sweepGradient(
        listOf(
            ringColor.copy(alpha = 0.8f),
            ringColor,
            Color(0xFF38BDF8),
            ringColor
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(size.toPx() - strokePx, size.toPx() - strokePx)
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            // Background Track
            drawArc(
                color = Color.White.copy(alpha = 0.18f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Target marker line (small subtle notch at target threshold)
            val targetAngle = -90f + (targetProgress * 360f)
            val rad = Math.toRadians(targetAngle.toDouble())
            val centerX = size.toPx() / 2f
            val centerY = size.toPx() / 2f
            val radius = (size.toPx() - strokePx) / 2f
            val markerStart = Offset(
                x = (centerX + (radius - strokePx * 0.6f) * Math.cos(rad)).toFloat(),
                y = (centerY + (radius - strokePx * 0.6f) * Math.sin(rad)).toFloat()
            )
            val markerEnd = Offset(
                x = (centerX + (radius + strokePx * 0.6f) * Math.cos(rad)).toFloat(),
                y = (centerY + (radius + strokePx * 0.6f) * Math.sin(rad)).toFloat()
            )
            drawLine(
                color = Color(0xFFFCD34D),
                start = markerStart,
                end = markerEnd,
                strokeWidth = 2.dp.toPx()
            )

            // Foreground Progress Arc
            if (animatedProgress > 0f) {
                drawArc(
                    brush = gradientBrush,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Central Animated Counter
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedPercentageCounter(
                targetPercentage = percentage,
                fontSize = (size.value * 0.22f).sp,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = if (isEligible) "Target Met" else "Req: $target%",
                fontSize = (size.value * 0.085f).sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isEligible) Color(0xFF6EE7B7) else Color(0xFFFDE68A)
            )
        }
    }
}

/**
 * Distinct gradients mapped per subject so that each subject has a unique visual signature.
 * The same gradient is shared between the Subject Performance Distribution bar chart
 * and the horizontal progress line in SubjectReportCard.
 */
object SubjectGradients {
    val palette = listOf(
        listOf(Color(0xFFF43F5E), Color(0xFFFB923C)), // Coral -> Peach (Clothing Comfort)
        listOf(Color(0xFF3B82F6), Color(0xFF06B6D4)), // Blue -> Cyan (Textile Testing)
        listOf(Color(0xFF8B5CF6), Color(0xFFC084FC)), // Violet -> Purple (Fabric Structure)
        listOf(Color(0xFF10B981), Color(0xFF14B8A6)), // Emerald -> Teal (PPC / MNPM)
        listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)), // Amber -> Gold (MPD)
        listOf(Color(0xFFEC4899), Color(0xFFF43F5E)), // Pink -> Rose
        listOf(Color(0xFF6366F1), Color(0xFF818CF8)), // Indigo -> Soft Indigo
        listOf(Color(0xFF0284C7), Color(0xFF38BDF8))  // Ocean -> Sky
    )

    fun getGradientColors(subjectCode: String): List<Color> {
        val code = subjectCode.trim().uppercase()
        return when {
            code.contains("501") || code.contains("CC") -> listOf(Color(0xFFF43F5E), Color(0xFFFB923C)) // CC: Coral -> Peach
            code.contains("502") || code.contains("TT") || code.contains("TEST") -> listOf(Color(0xFF3B82F6), Color(0xFF06B6D4)) // TT: Blue -> Cyan
            code.contains("503") || code.contains("FABRIC") || code.contains("WEAV") -> listOf(Color(0xFF8B5CF6), Color(0xFFC084FC)) // Fabric: Violet -> Purple
            code.contains("MNPM") || code.contains("PPC") -> listOf(Color(0xFF10B981), Color(0xFF14B8A6)) // PPC: Emerald -> Teal
            code.contains("MPD") || code.contains("MNC") -> listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)) // MPD: Amber -> Gold
            else -> {
                val idx = kotlin.math.abs(subjectCode.hashCode()) % palette.size
                palette[idx]
            }
        }
    }

    fun getHorizontalGradient(subjectCode: String): Brush {
        return Brush.horizontalGradient(getGradientColors(subjectCode))
    }

    fun getVerticalGradient(subjectCode: String): Brush {
        return Brush.verticalGradient(getGradientColors(subjectCode))
    }
}

/**
 * Custom Animated Horizontal Progress Bar that fills smoothly from 0 to target value
 * with a glowing gradient cap, custom target indicator notch, and dual-theme adaptation.
 */
@Composable
fun AnimatedHorizontalProgressBar(
    percentage: Float,
    target: Int = 75,
    height: Dp = 8.dp,
    showTargetIndicator: Boolean = false,
    trackColor: Color? = null,
    customGradient: Brush? = null,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()
    val progress = (percentage / 100f).coerceIn(0f, 1f)
    val targetFraction = (target / 100f).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "linear_bar_anim"
    )

    val barGradient = customGradient ?: when {
        percentage >= target -> Brush.horizontalGradient(
            listOf(Color(0xFF10B981), Color(0xFF06B6D4))
        )
        percentage >= target - 5f -> Brush.horizontalGradient(
            listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))
        )
        else -> Brush.horizontalGradient(
            listOf(Color(0xFFEF4444), Color(0xFFF43F5E))
        )
    }

    val defaultTrackColor = trackColor ?: if (isDark) {
        Color.White.copy(alpha = 0.14f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val infiniteTransition = rememberInfiniteTransition(label = "bar_shimmer_trans")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bar_shimmer_offset"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            val totalWidth = maxWidth

            // 1. Background Track
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(height / 2f))
                    .background(defaultTrackColor)
            )

            // 2. Animated Progress Fill with Glow Shimmer
            if (animatedProgress > 0f) {
                val currentWidth = totalWidth * animatedProgress
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(currentWidth)
                        .clip(RoundedCornerShape(height / 2f))
                        .background(barGradient)
                ) {
                    // Shimmer sweep highlight
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = if (isDark) 0.35f else 0.45f),
                                        Color.Transparent
                                    ),
                                    startX = currentWidth.value * shimmerOffset,
                                    endX = currentWidth.value * (shimmerOffset + 0.45f)
                                )
                            )
                    )
                }
            }

            // 3. Target Threshold Marker Notch
            if (showTargetIndicator && target in 1..99) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .offset(x = (totalWidth * targetFraction) - 1.dp)
                        .width(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(
                            if (isDark) Color(0xFFFCD34D) else Color(0xFFD97706)
                        )
                )
            }
        }

        // Sub-row showing Current % and Target % if requested
        if (showTargetIndicator) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "0%",
                    fontSize = 9.5.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Target $target%",
                    fontSize = 10.sp,
                    color = if (isDark) Color(0xFFFCD34D) else Color(0xFFD97706),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "100%",
                    fontSize = 9.5.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Interactive Animated Bar Chart for Subject / Monthly Attendance.
 * Bars grow upward sequentially from baseline with smooth tap feedback.
 */
@Composable
fun AnimatedBarChart(
    items: List<Pair<String, Float>>, // Label, Percentage
    target: Int = 75,
    height: Dp = 160.dp,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isDark) GlassTokens.GlassBorderDark else GlassTokens.GlassBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 2.5.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Subject Performance Distribution",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap a bar to inspect detailed percentage",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (selectedIndex != null && selectedIndex in items.indices) {
                    val sel = items[selectedIndex!!]
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${sel.first}: ${String.format(java.util.Locale.US, "%.1f", sel.second)}%",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bars container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
            ) {
                // Target baseline dashed line
                val targetFraction = (target / 100f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(targetFraction)
                        .align(Alignment.BottomStart)
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(1.dp).align(Alignment.TopStart)) {
                        drawLine(
                            color = Color(0xFFF59E0B).copy(alpha = 0.6f),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }

                val filteredItems = remember(items) {
                    items.filter { pair ->
                        val code = pair.first.uppercase()
                        !code.contains("LIB") &&
                        !code.contains("DLIB") &&
                        !code.contains("SEM") &&
                        !code.contains("SWACH")
                    }
                }

                // Sequential Bars with individual subject gradient signatures
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    filteredItems.forEachIndexed { idx, pair ->
                        val pct = pair.second.coerceIn(0f, 100f)
                        val fraction = (pct / 100f).coerceIn(0.04f, 1f)
                        val isSelected = selectedIndex == idx
                        val subjectGradColors = SubjectGradients.getGradientColors(pair.first)

                        // Sequential animation
                        val animFraction by animateFloatAsState(
                            targetValue = fraction,
                            animationSpec = tween(
                                durationMillis = 700 + (idx * 50),
                                delayMillis = idx * 60,
                                easing = FastOutSlowInEasing
                            ),
                            label = "bar_rise_$idx"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable {
                                    selectedIndex = if (selectedIndex == idx) null else idx
                                }
                        ) {
                            if (isSelected) {
                                Text(
                                    text = "${pct.toInt()}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = subjectGradColors.first()
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .width(if (isSelected) 22.dp else 16.dp)
                                    .fillMaxHeight(animFraction)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        if (isSelected) Brush.verticalGradient(
                                            listOf(Color(0xFF38BDF8), subjectGradColors.first(), subjectGradColors.last())
                                        ) else SubjectGradients.getVerticalGradient(pair.first)
                                    )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Distinct gradient dot indicator instead of subject name text per user specification
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(subjectGradColors.first())
                            )
                        }
                    }
                }
            }
        }
    }
}
