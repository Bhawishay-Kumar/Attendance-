package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AppCaveatFontFamily
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.LoraFontFamily
import com.example.ui.theme.Outfit
import com.example.ui.theme.WorkSans
import com.example.ui.theme.isAppDarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Heart particle data class for the interactive heart burst
private data class HeartParticle(
    val id: Long,
    val initialOffset: Offset,
    val targetOffset: Offset,
    val color: Color,
    val size: Float
)

/**
 * Developer Info & Message Section matching college-attendance-redesign-2.html (.dv2, .dev)
 * Features:
 * - Centered Developer Card (.dv2):
 *   - "— DEVELOPER —" label with subtle blue lines
 *   - 120 dp circular BK logo inside conic gradient ring with soft glow halo.
 *     Tap triggers 360° spin with haptic; touch tilts logo gently in 3D.
 *   - Bhawishya Kumar in Lora 29 sp bold italic
 *   - "App Developer & Creator" pill with spark icon
 *   - Sparkle divider
 *   - "Built with passion ♥" in Caveat with tappable heart (beat + particle burst)
 *   - Tagline "BETTER APPS FOR A BRIGHTER TOMORROW" with gradient bar
 * - Personal Message Card (.dev):
 *   - Heart badge + "A MESSAGE FOR YOU"
 *   - "Thank you for using this app! ♥"
 *   - Verbatim message, gradient bar, "MY AIM", aim text
 *   - "Thank you for being here!" + interactive smile button (bounce/laugh)
 * - Footer:
 *   - "Made with 💙 by Bhawishya Kumar"
 */
@Composable
fun DeveloperInfoSection(
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()
    val haptic = rememberHaptic()
    val coroutineScope = rememberCoroutineScope()

    val cardBg = if (isDark) {
        Color(0xB3101E4A)
    } else {
        Color(0xCCFFFFFF)
    }

    val cardBorder = if (isDark) {
        BorderStroke(1.5.dp, Color(0x663E70EB))
    } else {
        BorderStroke(1.5.dp, Color(0x66B0C4FF))
    }

    val textNavy = if (isDark) Color(0xFFF2F5FF) else Color(0xFF0C1B4A)
    val textMuted = if (isDark) Color(0xFF9DB0DF) else Color(0xFF5B70A6)
    val accentCyan = Color(0xFF38BDF8)

    // BK Logo Spin & Tilt state
    val ringRotation = remember { Animatable(0f) }
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }

    // Heart interaction & particles
    var isHeartActive by remember { mutableStateOf(false) }
    val heartScale = remember { Animatable(1f) }
    val particles = remember { mutableStateListOf<HeartParticle>() }

    // Smile interaction state
    var smileBouncing by remember { mutableStateOf(false) }
    val smileScale by animateFloatAsState(
        targetValue = if (smileBouncing) 1.35f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 450f),
        label = "smile_bounce"
    )
    val smileRotation by animateFloatAsState(
        targetValue = if (smileBouncing) 18f else 0f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 450f),
        label = "smile_rot"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("developer_info_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // =====================================================================
        // 1. Centered Developer Card (.dv2 in HTML)
        // =====================================================================
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = cardBorder,
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("developer_card_dv2")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 20.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                // Background radial halo behind logo
                Canvas(
                    modifier = Modifier
                        .size(260.dp)
                        .align(Alignment.TopCenter)
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                Color(0x404696FF),
                                Color(0x208B5CF6),
                                Color.Transparent
                            )
                        ),
                        radius = size.width / 2f
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Tag: "— DEVELOPER —" flanked by subtle gradient lines
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(44.dp)
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, accentCyan)
                                    )
                                )
                        )
                        Text(
                            text = "— DEVELOPER —",
                            fontFamily = WorkSans,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.5.sp,
                            color = accentCyan
                        )
                        Box(
                            modifier = Modifier
                                .width(44.dp)
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(accentCyan, Color.Transparent)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Circular 120dp BK Logo inside Conic Gradient Ring with Spin & 3D Tilt
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(120.dp)
                            .graphicsLayer {
                                rotationX = tiltX
                                rotationY = tiltY
                                cameraDistance = 12 * density
                            }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragEnd = {
                                        tiltX = 0f
                                        tiltY = 0f
                                    },
                                    onDragCancel = {
                                        tiltX = 0f
                                        tiltY = 0f
                                    }
                                ) { change, dragAmount ->
                                    change.consume()
                                    tiltY = (tiltY + dragAmount.x * 0.15f).coerceIn(-18f, 18f)
                                    tiltX = (tiltX - dragAmount.y * 0.15f).coerceIn(-18f, 18f)
                                }
                            }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                haptic.select()
                                coroutineScope.launch {
                                    ringRotation.snapTo(0f)
                                    ringRotation.animateTo(
                                        targetValue = 360f,
                                        animationSpec = tween(
                                            durationMillis = 1100,
                                            easing = CubicBezierEasing(0.3f, 0.9f, 0.3f, 1f)
                                        )
                                    )
                                }
                            }
                            .testTag("btn_developer_logo")
                    ) {
                        // Conic gradient spinning ring border
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(ringRotation.value)
                        ) {
                            val strokeWidth = 5.dp.toPx()
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    listOf(
                                        Color(0xFF22D3EE),
                                        Color(0xFF3B82F6),
                                        Color(0xFF8B5CF6),
                                        Color(0xFF22D3EE)
                                    )
                                ),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                            )
                        }

                        // Inner circular image container with dev_logo.png
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .size(106.dp)
                                .clip(CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.dev_logo),
                                contentDescription = "Bhawishya Kumar Logo",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Developer Name in Lora font (serif), bold, italic, 29sp
                    Text(
                        text = "Bhawishya Kumar",
                        fontFamily = LoraFontFamily,
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        letterSpacing = (-0.2).sp,
                        color = textNavy,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // "App Developer & Creator" Pill
                    Surface(
                        shape = CircleShape,
                        color = if (isDark) Color(0x2E38BDF8) else Color(0x1A2563EB),
                        border = BorderStroke(1.5.dp, if (isDark) Color(0x6638BDF8) else Color(0x402563EB)),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = accentCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "App Developer & Creator",
                                fontFamily = Outfit,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Sparkle Divider: line - ✦ - line
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, Color(0xFF8B5CF6).copy(alpha = 0.5f))
                                    )
                                )
                        )
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(15.dp)
                        )
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF8B5CF6).copy(alpha = 0.5f), Color.Transparent)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // "Built with passion ♥" in Caveat font (34sp) with interactive Heart button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "Built with passion",
                            fontFamily = AppCaveatFontFamily,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Normal,
                            color = accentCyan
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // Interactive Heart Icon
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .scale(heartScale.value)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    isHeartActive = !isHeartActive
                                    haptic.tap()
                                    coroutineScope.launch {
                                        heartScale.snapTo(1f)
                                        heartScale.animateTo(
                                            targetValue = 1.38f,
                                            animationSpec = spring(dampingRatio = 0.45f, stiffness = 500f)
                                        )
                                        heartScale.animateTo(
                                            targetValue = 1f,
                                            animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
                                        )
                                    }

                                    // Emit 12 particles
                                    if (isHeartActive) {
                                        particles.clear()
                                        val colors = listOf(Color(0xFFFF3B5C), Color(0xFFFF6B81), Color(0xFFFF8FA3), Color(0xFFE51C46))
                                        for (i in 0 until 12) {
                                            val angle = Math.toRadians((-200.0 + (220.0 * (i + Random.nextDouble() * 0.6)) / 12.0))
                                            val dist = 40.0 + Random.nextDouble() * 60.0
                                            particles.add(
                                                HeartParticle(
                                                    id = System.nanoTime() + i,
                                                    initialOffset = Offset.Zero,
                                                    targetOffset = Offset(
                                                        (cos(angle) * dist).toFloat(),
                                                        (sin(angle) * dist - 25.0).toFloat()
                                                    ),
                                                    color = colors[i % colors.size],
                                                    size = (9 + Random.nextInt(10)).toFloat()
                                                )
                                            )
                                        }
                                        coroutineScope.launch {
                                            delay(1000)
                                            particles.clear()
                                        }
                                    }
                                }
                                .testTag("btn_developer_heart")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Heart",
                                tint = if (isHeartActive) Color(0xFFFF3B5C) else Color(0xFFFF6B81),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Slogan with Blue-to-Purple underline
                    Text(
                        text = "BETTER APPS FOR A BRIGHTER TOMORROW",
                        fontFamily = WorkSans,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.2.sp,
                        color = textNavy.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Gradient Underline Bar
                    Box(
                        modifier = Modifier
                            .width(84.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF3B6BFF), Color(0xFF8B5CF6))
                                )
                            )
                    )
                }
            }
        }

        // =====================================================================
        // 2. Personal Message Card (.dev in HTML)
        // =====================================================================
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = cardBorder,
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("personal_message_card")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Top-right soft radial glow
                Canvas(
                    modifier = Modifier
                        .size(180.dp)
                        .align(Alignment.TopEnd)
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0x35A06EFF), Color.Transparent)
                        ),
                        radius = size.width / 2f
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header: Purple heart icon inside rounded square + "A MESSAGE FOR YOU"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF8B5CF6),
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        Text(
                            text = "A MESSAGE FOR YOU",
                            fontFamily = WorkSans,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = textNavy
                        )
                    }

                    // Message paragraphs
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Thank you for using this app!",
                                fontFamily = Outfit,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = textNavy
                            )
                            InteractiveAnimatedHeart(
                                initialIsRed = false,
                                size = 18.dp,
                                testTag = "btn_blue_heart_msg"
                            )
                        }

                        Text(
                            text = "I created this app with a simple goal — to make your daily life a little easier, more organized and more productive. Your support and trust mean a lot to me.",
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp,
                            color = textMuted
                        )
                    }

                    // Blue-to-purple gradient divider bar (74dp x 5dp)
                    Box(
                        modifier = Modifier
                            .width(74.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF3B6BFF), Color(0xFF8B5CF6))
                                )
                            )
                    )

                    // Subsection: MY AIM
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "MY AIM",
                            fontFamily = Outfit,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = textNavy
                        )

                        Text(
                            text = "To keep improving, add more useful features and create apps that truly make a difference.",
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp,
                            color = textMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Handwritten closing line & interactive Smile button (properly resized & aligned)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "Thank you for being here!",
                            fontFamily = AppCaveatFontFamily,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = accentCyan
                        )

                        // Interactive Smile Button (.smile2 in HTML) - resized to 28dp & aligned
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFC93F),
                            border = BorderStroke(1.dp, Color(0x33B45309)),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .size(28.dp)
                                .scale(smileScale)
                                .rotate(smileRotation)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    haptic.select()
                                    coroutineScope.launch {
                                        smileBouncing = true
                                        delay(320)
                                        smileBouncing = false
                                    }
                                }
                                .testTag("btn_developer_smile")
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val r = size.width / 2f

                                // Blush cheeks
                                drawCircle(
                                    color = Color(0x8CFF7A8A),
                                    radius = r * 0.18f,
                                    center = Offset(cx - r * 0.52f, cy + r * 0.18f)
                                )
                                drawCircle(
                                    color = Color(0x8CFF7A8A),
                                    radius = r * 0.18f,
                                    center = Offset(cx + r * 0.52f, cy + r * 0.18f)
                                )

                                // Eyes: arch lines
                                val strokeW = 1.8.dp.toPx()
                                drawArc(
                                    color = Color(0xFF6B3A00),
                                    startAngle = 180f,
                                    sweepAngle = 180f,
                                    useCenter = false,
                                    topLeft = Offset(cx - r * 0.54f, cy - r * 0.22f),
                                    size = androidx.compose.ui.geometry.Size(r * 0.36f, r * 0.26f),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                                )
                                drawArc(
                                    color = Color(0xFF6B3A00),
                                    startAngle = 180f,
                                    sweepAngle = 180f,
                                    useCenter = false,
                                    topLeft = Offset(cx + r * 0.18f, cy - r * 0.22f),
                                    size = androidx.compose.ui.geometry.Size(r * 0.36f, r * 0.26f),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                                )

                                // Big cheerful smile
                                drawArc(
                                    color = Color(0xFF6B3A00),
                                    startAngle = 0f,
                                    sweepAngle = 180f,
                                    useCenter = false,
                                    topLeft = Offset(cx - r * 0.40f, cy - r * 0.04f),
                                    size = androidx.compose.ui.geometry.Size(r * 0.80f, r * 0.52f),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                                )
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 3. Centered Footer Text with Interactive Heart
        // =====================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .testTag("developer_footer"),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Made with",
                fontFamily = WorkSans,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = textMuted
            )
            InteractiveAnimatedHeart(
                initialIsRed = false,
                size = 15.dp,
                testTag = "btn_blue_heart_footer"
            )
            Text(
                text = "by Bhawishya Kumar",
                fontFamily = WorkSans,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = textMuted
            )
        }
    }
}

/**
 * Interactive Animated Heart that beats with spring animation, bursts celebratory particles,
 * and smoothly animates its color between vibrant blue and glowing red on click.
 */
@Composable
fun InteractiveAnimatedHeart(
    initialIsRed: Boolean = false,
    size: androidx.compose.ui.unit.Dp = 18.dp,
    testTag: String = "interactive_heart",
    modifier: Modifier = Modifier
) {
    val haptic = rememberHaptic()
    val coroutineScope = rememberCoroutineScope()
    var isRed by remember { mutableStateOf(initialIsRed) }
    val scale = remember { Animatable(1f) }
    val particles = remember { mutableStateListOf<HeartParticle>() }

    val animatedColor by animateColorAsState(
        targetValue = if (isRed) Color(0xFFFF3B5C) else Color(0xFF3B82F6),
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "heart_color_anim"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size + 14.dp)
            .scale(scale.value)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isRed = !isRed
                haptic.tap()
                coroutineScope.launch {
                    scale.snapTo(1f)
                    scale.animateTo(
                        targetValue = 1.42f,
                        animationSpec = spring(dampingRatio = 0.45f, stiffness = 500f)
                    )
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
                    )
                }

                if (isRed) {
                    particles.clear()
                    val colors = listOf(Color(0xFFFF3B5C), Color(0xFFFF6B81), Color(0xFFFF8FA3), Color(0xFF38BDF8))
                    for (i in 0 until 10) {
                        val angle = Math.toRadians((-200.0 + (220.0 * (i + Random.nextDouble() * 0.6)) / 10.0))
                        val dist = 30.0 + Random.nextDouble() * 45.0
                        particles.add(
                            HeartParticle(
                                id = System.nanoTime() + i,
                                initialOffset = Offset.Zero,
                                targetOffset = Offset(
                                    (cos(angle) * dist).toFloat(),
                                    (sin(angle) * dist - 20.0).toFloat()
                                ),
                                color = colors[i % colors.size],
                                size = (7 + Random.nextInt(7)).toFloat()
                            )
                        )
                    }
                    coroutineScope.launch {
                        delay(900)
                        particles.clear()
                    }
                }
            }
            .testTag(testTag)
    ) {
        // Render burst particles
        particles.forEach { p ->
            val pProgress = remember { Animatable(0f) }
            LaunchedEffect(p.id) {
                pProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
                )
            }
            Box(
                modifier = Modifier
                    .offset(
                        x = (p.targetOffset.x * pProgress.value).dp,
                        y = (p.targetOffset.y * pProgress.value).dp
                    )
                    .size((p.size * (1f - pProgress.value)).dp)
                    .clip(CircleShape)
                    .background(p.color.copy(alpha = 1f - pProgress.value))
            )
        }

        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Interactive Heart",
            tint = animatedColor,
            modifier = Modifier.size(size)
        )
    }
}
