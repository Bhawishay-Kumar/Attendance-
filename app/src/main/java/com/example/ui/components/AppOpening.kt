package com.example.ui.components

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.isAppDarkTheme
import kotlinx.coroutines.delay

/**
 * Checks if system-wide reduced motion or transition animation is disabled.
 */
fun isReducedMotionEnabled(context: Context): Boolean {
    return try {
        val transitionScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            1.0f
        )
        val animatorScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f
        )
        transitionScale == 0f || animatorScale == 0f
    } catch (_: Exception) {
        false
    }
}

/**
 * Root wrapper providing a 1900 ms AppOpening splash screen on app launch.
 *
 * Sequence:
 * - Fade in & scale with cubic-bezier(0.22, 1, 0.36, 1)
 * - Float with two expanding pulse rings and soft glow
 * - Animated loading dots
 * - Reduced-motion fallback (skips straight to main content)
 * - Plays once at the root, preserving all routes and states behind it.
 */
@Composable
fun AppOpeningRoot(
    modifier: Modifier = Modifier,
    onSplashFinished: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isDark = isAppDarkTheme()
    val isReducedMotion = remember { isReducedMotionEnabled(context) }

    // Splash plays once at root
    var isSplashVisible by rememberSaveable { mutableStateOf(!isReducedMotion) }
    var animationStage by remember { mutableStateOf(if (isReducedMotion) 2 else 0) }

    // Official cubic-bezier(0.22, 1, 0.36, 1) easing from specification
    val splashEasing = remember { CubicBezierEasing(0.22f, 1f, 0.36f, 1f) }

    LaunchedEffect(isReducedMotion) {
        if (isReducedMotion) {
            isSplashVisible = false
            onSplashFinished()
            return@LaunchedEffect
        }

        // Stage 1: Entrance fade & scale (0ms -> 450ms)
        animationStage = 1

        // Stage 2: Hold & pulse (450ms -> 1600ms)
        delay(450L)
        animationStage = 2

        // Stage 3: Smooth exit at 1900 ms total
        delay(1450L) // 450 + 1450 = 1900 ms
        isSplashVisible = false
        onSplashFinished()
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Main App Content (Always mounted, routing preserved behind splash)
        content()

        // 1900ms Splash Overlay
        AnimatedVisibility(
            visible = isSplashVisible,
            enter = fadeIn(animationSpec = tween(150, easing = LinearEasing)),
            exit = fadeOut(animationSpec = tween(280, easing = splashEasing)),
            modifier = Modifier.fillMaxSize()
        ) {
            val bgGradient = if (isDark) {
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF04091A),
                        Color(0xFF071233),
                        Color(0xFF020612)
                    )
                )
            } else {
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF0F6FF),
                        Color(0xFFE4EDFC),
                        Color(0xFFDAE6FA)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgGradient),
                contentAlignment = Alignment.Center
            ) {
                // Expanding pulse rings
                if (animationStage >= 1 && !isReducedMotion) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse_rings")

                    val ring1Scale by infiniteTransition.animateFloat(
                        initialValue = 0.95f,
                        targetValue = 1.65f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1400, easing = splashEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "ring1_scale"
                    )

                    val ring1Alpha by infiniteTransition.animateFloat(
                        initialValue = 0.55f,
                        targetValue = 0.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1400, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "ring1_alpha"
                    )

                    val ring2Scale by infiniteTransition.animateFloat(
                        initialValue = 0.85f,
                        targetValue = 1.45f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1400, delayMillis = 350, easing = splashEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "ring2_scale"
                    )

                    val ring2Alpha by infiniteTransition.animateFloat(
                        initialValue = 0.45f,
                        targetValue = 0.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1400, delayMillis = 350, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "ring2_alpha"
                    )

                    // Outer Ring 1
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .graphicsLayer {
                                scaleX = ring1Scale
                                scaleY = ring1Scale
                                alpha = ring1Alpha
                            }
                            .border(
                                width = 1.5.dp,
                                color = if (isDark) Color(0xFF22D3EE) else Color(0xFF3B82F6),
                                shape = CircleShape
                            )
                    )

                    // Outer Ring 2
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .graphicsLayer {
                                scaleX = ring2Scale
                                scaleY = ring2Scale
                                alpha = ring2Alpha
                            }
                            .border(
                                width = 1.5.dp,
                                color = if (isDark) Color(0xFF818CF8) else Color(0xFF6366F1),
                                shape = CircleShape
                            )
                    )
                }

                // Center Content: Logo + Brand Title + Loading Dots
                val logoScale by animateFloatAsState(
                    targetValue = if (animationStage >= 1) 1f else 0.82f,
                    animationSpec = tween(durationMillis = 450, easing = splashEasing),
                    label = "logo_scale"
                )

                val contentAlpha by animateFloatAsState(
                    targetValue = if (animationStage >= 1) 1f else 0f,
                    animationSpec = tween(durationMillis = 400, easing = splashEasing),
                    label = "content_alpha"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                        alpha = contentAlpha
                    }
                ) {
                    // Theme-aware Logo
                    AppLogo(
                        size = 112.dp,
                        showGlow = true,
                        animatedGlow = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // App Title
                    Text(
                        text = "College Attendance Tracker",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFF3F6FF) else Color(0xFF0E1250),
                        letterSpacing = 0.3.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Subtitle / Department Capsule
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDark) Color(0x3322D3EE) else Color(0x223B82F6),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDark) Color(0x6622D3EE) else Color(0x553B82F6)
                        )
                    ) {
                        Text(
                            text = "B-TECH IN IIHT • SMART ATTENDANCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF22D3EE) else Color(0xFF1D4ED8),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Three animated loading dots
                    SplashLoadingDots(isDark = isDark, isReducedMotion = isReducedMotion)
                }
            }
        }
    }
}

/**
 * Three smooth pulsing loading dots matching the splash specification.
 */
@Composable
private fun SplashLoadingDots(
    isDark: Boolean,
    isReducedMotion: Boolean
) {
    if (isReducedMotion) return

    val infiniteTransition = rememberInfiniteTransition(label = "dots_transition")

    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    val dotColor = if (isDark) Color(0xFF22D3EE) else Color(0xFF2563EB)

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .graphicsLayer { alpha = dot1 }
                .background(dotColor, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(7.dp)
                .graphicsLayer { alpha = dot2 }
                .background(dotColor, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(7.dp)
                .graphicsLayer { alpha = dot3 }
                .background(dotColor, CircleShape)
        )
    }
}
