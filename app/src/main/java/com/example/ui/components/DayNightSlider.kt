package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Spring-driven Day/Night Glass Toggle Slider matching college-attendance-redesign.html (.tg)
 * - 76dp width, 38dp height, 4dp pad, 30dp thumb
 * - Continuous dual-layer crossfading glass track (day cloud + night stars inside)
 * - Spring physics solver (stiffness ≈ 380, damping ≈ 0.75)
 * - Dynamic velocity squish & press swell
 * - Rotating / crossfading sun & moon icons inside thumb and destination slots
 */
@Composable
fun DayNightSlider(
    isDark: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onProgress: ((Float) -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val haptic = rememberHaptic()

    val trackWidthDp = 76.dp
    val trackHeightDp = 38.dp
    val thumbSizeDp = 30.dp
    val padDp = 4.dp
    val maxTravelDp = trackWidthDp - thumbSizeDp - (padDp * 2) // 38.dp

    val maxTravelPx = with(density) { maxTravelDp.toPx() }

    // Continuous progress 0f (Day) -> 1f (Night)
    val progress = remember { Animatable(if (isDark) 1f else 0f) }
    var isDragging by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    var dragVelocity by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isDark) {
        if (!isDragging) {
            val target = if (isDark) 1f else 0f
            if (progress.value != target) {
                progress.animateTo(
                    targetValue = target,
                    animationSpec = spring(
                        dampingRatio = 0.75f,
                        stiffness = 380f
                    )
                )
                onProgress?.invoke(target)
            }
        }
    }

    val currentP = progress.value
    onProgress?.invoke(currentP)

    // Thumb press swell animation (matching .thumb.press .body { scale: 1.14 })
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed || isDragging) 1.14f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        label = "thumb_press_scale"
    )

    // Dynamic horizontal squish stretch during motion
    val velocityStretch = 1f + (abs(dragVelocity) / 2500f).coerceIn(0f, 0.22f)

    val currentIsDark by rememberUpdatedState(isDark)
    val currentOnToggle by rememberUpdatedState(onToggle)
    val currentOnProgress by rememberUpdatedState(onProgress)

    Box(
        modifier = modifier
            .width(trackWidthDp)
            .height(trackHeightDp)
            .clip(RoundedCornerShape(999.dp))
            .pointerInput(currentIsDark) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        haptic.select()
                        currentOnToggle(!currentIsDark)
                    }
                )
            }
            .pointerInput(maxTravelPx, currentIsDark) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        isDragging = true
                        isPressed = true
                        dragVelocity = 0f
                    },
                    onDragEnd = {
                        isDragging = false
                        isPressed = false
                        val snapDark = progress.value > 0.5f
                        coroutineScope.launch {
                            progress.animateTo(
                                targetValue = if (snapDark) 1f else 0f,
                                animationSpec = spring(
                                    dampingRatio = 0.75f,
                                    stiffness = 380f
                                )
                            )
                            dragVelocity = 0f
                        }
                        if (snapDark != currentIsDark) {
                            haptic.select()
                            currentOnToggle(snapDark)
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        isPressed = false
                        dragVelocity = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragVelocity = dragAmount * 60f
                        val deltaP = if (maxTravelPx > 0) dragAmount / maxTravelPx else 0f
                        val newP = (progress.value + deltaP).coerceIn(0f, 1f)
                        coroutineScope.launch {
                            progress.snapTo(newP)
                        }
                    }
                )
            }
            .testTag("header_theme_toggle"),
        contentAlignment = Alignment.CenterStart
    ) {
        // =====================================================================
        // Track Layers (Smoothly Crossfading without any abrupt jump)
        // =====================================================================
        
        // Day Track Background (.tbg.d from HTML)
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = (1f - currentP).coerceIn(0f, 1f) }
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xCCFFFFFF), Color(0x94E0EBFF))
                    )
                )
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(Color.White, Color(0xF28CC3FF), Color.White)
                        )
                    ),
                    RoundedCornerShape(999.dp)
                )
        )

        // Night Track Background (.tbg.n from HTML)
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = currentP.coerceIn(0f, 1f) }
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xA61A3076), Color(0xCC081238))
                    )
                )
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(Color(0xF22D7DFF), Color(0x805ACDFF), Color(0xD97D55FF))
                        )
                    ),
                    RoundedCornerShape(999.dp)
                )
        )

        // Top Specular Highlight Arc on Track (.tbg::after from HTML)
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (currentP > 0.5f) 0.20f else 0.35f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.5f, 0f),
                            radius = size.width * 0.55f
                        )
                    )
                }
        )

        // Day Track Internal Element: Soft Cloud (.cloud from HTML)
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    alpha = (1f - currentP).coerceIn(0f, 1f)
                    translationX = currentP * 12f
                }
        ) {
            Box(
                modifier = Modifier
                    .offset(x = 34.dp, y = 20.dp)
                    .size(width = 20.dp, height = 9.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.85f))
            )
        }

        // Night Track Internal Elements: Glowing Stars (.star from HTML)
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = currentP.coerceIn(0f, 1f) }
        ) {
            // Star 1
            Box(
                modifier = Modifier
                    .offset(x = 28.dp, y = 10.dp)
                    .size(2.5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCFE0FF))
            )
            // Star 2
            Box(
                modifier = Modifier
                    .offset(x = 36.dp, y = 24.dp)
                    .size(2.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCFE0FF))
            )
            // Star 3
            Box(
                modifier = Modifier
                    .offset(x = 20.dp, y = 26.dp)
                    .size(2.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCFE0FF))
            )
            // Star 4
            Box(
                modifier = Modifier
                    .offset(x = 42.dp, y = 12.dp)
                    .size(2.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCFE0FF))
            )
        }

        // =====================================================================
        // Track Slot Icons
        // =====================================================================
        
        // Left Slot: Sun (visible during night mode as destination action)
        Box(
            modifier = Modifier
                .size(trackHeightDp)
                .align(Alignment.CenterStart),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = null,
                tint = Color(0xFF6C84B8),
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer {
                        alpha = currentP
                        scaleX = 0.5f + (0.5f * currentP)
                        scaleY = 0.5f + (0.5f * currentP)
                        rotationZ = (1f - currentP) * -70f
                    }
            )
        }

        // Right Slot: Moon (visible during day mode as destination action)
        Box(
            modifier = Modifier
                .size(trackHeightDp)
                .align(Alignment.CenterEnd),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DarkMode,
                contentDescription = null,
                tint = Color(0xFF8D9FC8),
                modifier = Modifier
                    .size(15.dp)
                    .graphicsLayer {
                        alpha = 1f - currentP
                        scaleX = 1f - (0.5f * currentP)
                        scaleY = 1f - (0.5f * currentP)
                        rotationZ = currentP * 70f
                    }
            )
        }

        // =====================================================================
        // Animated Thumb (Fluid Sliding & Scaling with Dual-Tint Lens)
        // =====================================================================
        val thumbOffsetDp = padDp + (maxTravelDp * currentP)

        Box(
            modifier = Modifier
                .offset(x = thumbOffsetDp)
                .size(thumbSizeDp)
                .graphicsLayer {
                    scaleX = pressScale * velocityStretch
                    scaleY = pressScale * (1f - (velocityStretch - 1f) * 0.55f)
                }
                .shadow(
                    elevation = if (currentP > 0.5f) 7.dp else 4.dp,
                    shape = CircleShape,
                    ambientColor = if (currentP > 0.5f) Color(0x993278FF) else Color(0x663C64DC),
                    spotColor = if (currentP > 0.5f) Color(0xB3143CDC) else Color(0x663C64DC)
                )
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Day Thumb Tint (.tint.d from HTML)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = (1f - currentP).coerceIn(0f, 1f) }
                    .background(
                        Brush.linearGradient(
                            listOf(Color.White, Color(0xFFE8F0FF))
                        )
                    )
                    .border(
                        BorderStroke(1.dp, Color.White.copy(alpha = 0.9f)),
                        CircleShape
                    )
            )

            // Night Thumb Tint (.tint.n from HTML)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = currentP.coerceIn(0f, 1f) }
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF5A9BFF), Color(0xFF2563EB))
                        )
                    )
                    .border(
                        BorderStroke(1.5.dp, Color(0xA687CDFF)),
                        CircleShape
                    )
            )

            // Top specular gloss overlay (.lens::before from HTML)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawBehind {
                        drawCircle(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.45f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = size.height * 0.55f
                            )
                        )
                    }
            )

            // Sun Icon inside Thumb (.ic-sun from HTML)
            if (currentP < 0.90f) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = "Light Theme",
                    tint = Color(0xFF1F6BFF),
                    modifier = Modifier
                        .size(15.dp)
                        .graphicsLayer {
                            alpha = (1f - currentP * 2f).coerceIn(0f, 1f)
                            rotationZ = currentP * 160f
                            scaleX = (1f - currentP * 0.7f).coerceIn(0f, 1f)
                            scaleY = (1f - currentP * 0.7f).coerceIn(0f, 1f)
                        }
                )
            }

            // Moon Icon inside Thumb (.ic-moon from HTML)
            if (currentP > 0.10f) {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = "Dark Theme",
                    tint = Color.White,
                    modifier = Modifier
                        .size(14.dp)
                        .graphicsLayer {
                            alpha = (currentP * 2f - 1f).coerceIn(0f, 1f)
                            rotationZ = (1f - currentP) * -160f
                            scaleX = (0.3f + currentP * 0.7f).coerceIn(0f, 1f)
                            scaleY = (0.3f + currentP * 0.7f).coerceIn(0f, 1f)
                        }
                )
            }
        }
    }
}
