package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ScreenTab
import com.example.ui.theme.isAppDarkTheme

/**
 * Liquid-Glass Floating Navigation Dock inspired by glass-dock (1).html:
 * - Floating pill dock with luminous gradient edge and 24px glass blur
 * - Active tab expansion: Active item expands horizontally to display icon + label
 * - Sliding liquid-glass lens behind the active item with electric blue-to-violet gradient
 * - Micro-animations on each icon upon selection (hop, pop, spin, grow, turn)
 * - Tactile spring-back press interaction
 * - Respects Android navigation bar safe-area insets
 */
@Composable
fun FloatingBottomNavBar(
    currentTab: ScreenTab,
    onTabSelected: (ScreenTab) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = isAppDarkTheme()
) {
    val tabs = remember { ScreenTab.values() }
    val selectedIndex = tabs.indexOf(currentTab).coerceAtLeast(0)

    // Liquid Glass Dock Rim Gradient Border matching glass-dock.html
    val dockBorderBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xF22882FF),
                Color(0x8C46C8FF),
                Color(0xBF6E50FF),
                Color(0xF2BE5AFF)
            ),
            start = Offset(0f, 0f),
            end = Offset(700f, 100f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White,
                Color(0xD982D2FF),
                Color.White,
                Color(0xE6D78CFF)
            ),
            start = Offset(0f, 0f),
            end = Offset(700f, 100f)
        )
    }

    val dockBackgroundBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xF01D357A),
                Color(0xFA09143D)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xF8FFFFFF),
                Color(0xFAEBF3FF)
            )
        )
    }

    // Active Lens Gradient (Electric Blue -> Royal Purple -> Violet -> Magenta)
    val activeLensGradient = if (isDark) {
        Brush.horizontalGradient(
            listOf(
                Color(0xFF0865FF),
                Color(0xFF4A44F5),
                Color(0xFF7628F5),
                Color(0xFFB13CF0)
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                Color(0xFF2F7BFF),
                Color(0xFF4A44F5),
                Color(0xFF7628F5)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = if (isDark) Color(0xF8060F30) else Color(0xF8F4F8FF),
            tonalElevation = 0.dp,
            border = BorderStroke(1.5.dp, dockBorderBrush),
            shadowElevation = if (isDark) 16.dp else 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(999.dp))
                .testTag("floating_bottom_nav_bar")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(dockBackgroundBrush)
                    .drawBehind {
                        // Top specular arc highlight matching .dock::after
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = if (isDark) 0.22f else 0.40f),
                                    Color.Transparent
                                ),
                                center = Offset(size.width * 0.5f, 0f),
                                radius = size.width * 0.5f
                            ),
                            radius = size.width * 0.5f,
                            center = Offset(size.width * 0.5f, 0f)
                        )
                    }
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val isSelected = index == selectedIndex
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()

                        // Active Tab Weight Expands (A = 2.6 in glass-dock.html)
                        val targetWeight = if (isSelected) 2.6f else 1.0f
                        val animatedWeight by animateFloatAsState(
                            targetValue = targetWeight,
                            animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
                            label = "nav_tab_weight_$index"
                        )

                        // Tactile press bounce
                        val pressScale by animateFloatAsState(
                            targetValue = if (isPressed) 0.94f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "nav_press_scale_$index"
                        )

                        // Micro-animation trigger when tab becomes active
                        val animProgress = remember { Animatable(0f) }
                        LaunchedEffect(isSelected) {
                            if (isSelected) {
                                animProgress.snapTo(0f)
                                animProgress.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                                )
                            }
                        }

                        // Tab container with active liquid lens
                        Box(
                            modifier = Modifier
                                .weight(animatedWeight)
                                .height(56.dp)
                                .graphicsLayer {
                                    scaleX = pressScale
                                    scaleY = pressScale
                                }
                                .clip(RoundedCornerShape(999.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier
                                            .background(activeLensGradient)
                                            .shadow(
                                                elevation = 8.dp,
                                                shape = RoundedCornerShape(999.dp),
                                                ambientColor = Color(0xFF4A44F5),
                                                spotColor = Color(0xFF7628F5)
                                            )
                                            .drawBehind {
                                                // Inside top highlight on lens
                                                drawRect(
                                                    brush = Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color.White.copy(alpha = 0.42f),
                                                            Color.Transparent
                                                        ),
                                                        startY = 0f,
                                                        endY = size.height * 0.48f
                                                    )
                                                )
                                            }
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    if (!isSelected) {
                                        onTabSelected(tab)
                                    }
                                }
                                .testTag("nav_tab_${tab.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = if (isSelected) 10.dp else 0.dp)
                            ) {
                                // Micro-animated Icon based on destination
                                val iconModifier = Modifier
                                    .size(22.dp)
                                    .graphicsLayer {
                                        if (isSelected) {
                                            when (tab) {
                                                ScreenTab.HOME -> {
                                                    // Hop bounce
                                                    val p = animProgress.value
                                                    translationY = -6.dp.toPx() * kotlin.math.sin(p * Math.PI.toFloat())
                                                }
                                                ScreenTab.DAILY -> {
                                                    // Pop checkmark
                                                    val p = animProgress.value
                                                    val scale = 1f + 0.20f * kotlin.math.sin(p * Math.PI.toFloat())
                                                    scaleX = scale
                                                    scaleY = scale
                                                }
                                                ScreenTab.TIMETABLE -> {
                                                    // Spin clock
                                                    rotationZ = animProgress.value * 360f
                                                }
                                                ScreenTab.REPORTS -> {
                                                    // Grow bars
                                                    val p = animProgress.value
                                                    scaleY = 0.4f + 0.6f * p
                                                }
                                                ScreenTab.SETTINGS -> {
                                                    // Turn gear
                                                    rotationZ = animProgress.value * 180f
                                                }
                                            }
                                        }
                                    }

                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) {
                                        Color.White
                                    } else {
                                        if (isDark) Color(0xFF91A9D0) else Color(0xFF5B70A6)
                                    },
                                    modifier = iconModifier
                                )

                                // Active label expansion
                                AnimatedVisibility(
                                    visible = isSelected,
                                    enter = fadeIn(tween(250)) + expandHorizontally(
                                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                                    ),
                                    exit = fadeOut(tween(150)) + shrinkHorizontally(
                                        animationSpec = tween(250, easing = FastOutSlowInEasing)
                                    )
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Spacer(modifier = Modifier.width(7.dp))
                                        Text(
                                            text = tab.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            letterSpacing = (-0.01).sp
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
}
