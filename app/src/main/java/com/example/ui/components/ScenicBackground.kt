package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.ui.theme.isAppDarkTheme

/**
 * CompositionLocal providing current animated theme progress (0f = Day, 1f = Night).
 */
val LocalThemeProgress = compositionLocalOf { 0f }

/**
 * Redesigned Atmospheric Scenic Background for College Attendance Tracker:
 * - NO Sun, NO Moon (per user specification)
 * - NO heavy wavy hills or clutter
 * - Dark Theme: Luxurious midnight navy-indigo-violet gradient with undulating celestial aurora
 *   glows and gently drifting twinkling stars.
 * - Light Theme: Crisp, soft icy-blue & cloud-sky gradient with delicate ambient lighting.
 * - Super smooth 60fps performance with zero frame drops or lag.
 * - Soft contrast overlay ensures razor-sharp foreground text and pristine glassmorphism.
 */
@Composable
fun ScenicBackground(
    modifier: Modifier = Modifier,
    isDark: Boolean = isAppDarkTheme(),
    content: @Composable BoxScope.() -> Unit
) {
    // Single Animatable progress driven smoothly between 0f (Day) and 1f (Night)
    val progress = remember { Animatable(if (isDark) 1f else 0f) }

    LaunchedEffect(isDark) {
        val target = if (isDark) 1f else 0f
        if (progress.value != target) {
            progress.animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "scenic_drift")

    // Star twinkle 1 (3.2s frequency)
    val starTwinkle1 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_twinkle_1"
    )

    // Star twinkle 2 (4.4s frequency with offset)
    val starTwinkle2 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(4400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_twinkle_2"
    )

    // Gentle continuous horizontal drift of stars across the night sky
    val starDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(36000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star_horizontal_drift"
    )

    // Subtle aurora glow undulation
    val auroraDrift by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aurora_drift"
    )

    CompositionLocalProvider(LocalThemeProgress provides progress.value) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(if (progress.value > 0.5f) Color(0xFF040A22) else Color(0xFFE8F1FF))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val p = progress.value
                val w = size.width
                val h = size.height
                if (w <= 0f || h <= 0f) return@Canvas

                drawAtmosphericBackground(
                    p = p,
                    w = w,
                    h = h,
                    starTwinkle1 = starTwinkle1,
                    starTwinkle2 = starTwinkle2,
                    starDrift = starDrift,
                    auroraDrift = auroraDrift
                )
            }

            // Contrast overlay for crystal-clear readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (progress.value > 0.5f) {
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x3503081E),
                                    Color(0x40071338),
                                    Color(0x50040A20)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x15FFFFFF),
                                    Color(0x28EEF4FF),
                                    Color(0x35E2ECFF)
                                )
                            )
                        }
                    )
            )

            content()
        }
    }
}

private fun DrawScope.drawAtmosphericBackground(
    p: Float,
    w: Float,
    h: Float,
    starTwinkle1: Float,
    starTwinkle2: Float,
    starDrift: Float,
    auroraDrift: Float
) {
    // 1. Day Mode Sky (when p < 1f)
    if (p < 0.99f) {
        val dayAlpha = (1f - p).coerceIn(0f, 1f)

        // Clean, soft daylight sky gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFBFDCFF).copy(alpha = dayAlpha),
                    Color(0xFFD8EBFF).copy(alpha = dayAlpha),
                    Color(0xFFF2F6FF).copy(alpha = dayAlpha)
                )
            ),
            size = size
        )

        // Soft ambient daylight glow (NO sun disc, pure diffused atmosphere)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.55f * dayAlpha),
                    Color(0xFFE3EFFF).copy(alpha = 0.25f * dayAlpha),
                    Color.Transparent
                ),
                center = Offset(w * 0.70f, h * 0.12f),
                radius = w * 0.65f
            ),
            center = Offset(w * 0.70f, h * 0.12f),
            radius = w * 0.65f
        )

        // Soft secondary ambient glow at bottom left
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFDAEAFF).copy(alpha = 0.40f * dayAlpha),
                    Color.Transparent
                ),
                center = Offset(w * 0.20f, h * 0.85f),
                radius = w * 0.60f
            ),
            center = Offset(w * 0.20f, h * 0.85f),
            radius = w * 0.60f
        )
    }

    // 2. Night Mode Sky (when p > 0.01f)
    if (p > 0.01f) {
        val nightAlpha = p.coerceIn(0f, 1f)

        // Deep midnight blue/navy gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF040A22).copy(alpha = nightAlpha),
                    Color(0xFF0A1850).copy(alpha = nightAlpha),
                    Color(0xFF1E1760).copy(alpha = nightAlpha)
                )
            ),
            size = size
        )

        // Ambient celestial aurora glows (.a1 & .a2, NO moon)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF3880FF).copy(alpha = 0.18f * nightAlpha),
                    Color(0xFF22D6B8).copy(alpha = 0.08f * nightAlpha),
                    Color.Transparent
                ),
                center = Offset(w * 0.35f + auroraDrift, h * 0.22f),
                radius = w * 0.75f
            ),
            center = Offset(w * 0.35f + auroraDrift, h * 0.22f),
            radius = w * 0.75f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF8C4CFF).copy(alpha = 0.18f * nightAlpha),
                    Color(0xFF3860FF).copy(alpha = 0.07f * nightAlpha),
                    Color.Transparent
                ),
                center = Offset(w * 0.75f - auroraDrift, h * 0.45f),
                radius = w * 0.70f
            ),
            center = Offset(w * 0.75f - auroraDrift, h * 0.45f),
            radius = w * 0.70f
        )

        // Gently drifting stars (Group 1)
        val starsGroup1 = listOf(
            Triple(0.08f, 0.12f, 1.4f),
            Triple(0.24f, 0.28f, 1.1f),
            Triple(0.42f, 0.14f, 1.5f),
            Triple(0.60f, 0.26f, 1.1f),
            Triple(0.78f, 0.10f, 1.6f),
            Triple(0.92f, 0.22f, 1.1f),
            Triple(0.16f, 0.48f, 1.2f),
            Triple(0.68f, 0.65f, 1.3f)
        )
        starsGroup1.forEach { (rx, ry, radius) ->
            val driftedX = ((rx * w + starDrift * 0.6f) % w)
            drawCircle(
                color = Color(0xFFD6E4FF).copy(alpha = (starTwinkle1 * 0.85f * nightAlpha).coerceIn(0f, 1f)),
                center = Offset(driftedX, h * ry),
                radius = radius
            )
        }

        // Gently drifting stars (Group 2)
        val starsGroup2 = listOf(
            Triple(0.14f, 0.06f, 1.1f),
            Triple(0.35f, 0.20f, 1.4f),
            Triple(0.52f, 0.08f, 1.1f),
            Triple(0.70f, 0.18f, 1.5f),
            Triple(0.85f, 0.32f, 1.1f),
            Triple(0.95f, 0.50f, 1.2f),
            Triple(0.08f, 0.72f, 1.3f),
            Triple(0.45f, 0.82f, 1.1f)
        )
        starsGroup2.forEach { (rx, ry, radius) ->
            val driftedX = ((rx * w + starDrift * 1.0f) % w)
            drawCircle(
                color = Color.White.copy(alpha = (starTwinkle2 * 0.90f * nightAlpha).coerceIn(0f, 1f)),
                center = Offset(driftedX, h * ry),
                radius = radius
            )
        }
    }
}
