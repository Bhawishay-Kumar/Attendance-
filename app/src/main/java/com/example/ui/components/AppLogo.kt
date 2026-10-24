package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.isAppDarkTheme

/**
 * Theme-aware AppLogo component.
 *
 * Uses CSS / Compose theme properties to adapt border rings, glow halos, and surface
 * reflections between Light and Dark mode while rendering the official 3D Attendance Logo.
 */
@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    showGlow: Boolean = true,
    animatedGlow: Boolean = false,
    contentDescriptionText: String = "College Attendance Tracker Logo"
) {
    val isDark = isAppDarkTheme()

    // Smooth ambient pulsing glow (GPU graphicsLayer)
    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")
    val pulseAlpha by if (animatedGlow) {
        infiniteTransition.animateFloat(
            initialValue = 0.55f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_alpha"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0.75f) }
    }

    val glowBrush = if (isDark) {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFF22D3EE).copy(alpha = 0.45f * pulseAlpha),
                Color(0xFF818CF8).copy(alpha = 0.25f * pulseAlpha),
                Color.Transparent
            )
        )
    } else {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFF3B82F6).copy(alpha = 0.35f * pulseAlpha),
                Color(0xFF8B5CF6).copy(alpha = 0.15f * pulseAlpha),
                Color.Transparent
            )
        )
    }

    val ringBorder = BorderStroke(
        width = (size.value * 0.02f).coerceIn(1.2f, 2.5f).dp,
        brush = if (isDark) {
            Brush.linearGradient(
                listOf(
                    Color(0xFF22D3EE).copy(alpha = 0.85f),
                    Color(0xFF818CF8).copy(alpha = 0.65f),
                    Color(0xFFC084FC).copy(alpha = 0.85f)
                )
            )
        } else {
            Brush.linearGradient(
                listOf(
                    Color(0xFF2563EB).copy(alpha = 0.75f),
                    Color(0xFF6366F1).copy(alpha = 0.55f),
                    Color(0xFF9333EA).copy(alpha = 0.75f)
                )
            )
        }
    )

    val cornerRadius = (size.value * (32f / 120f)).dp

    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = contentDescriptionText },
        contentAlignment = Alignment.Center
    ) {
        // Outer atmospheric glow halo
        if (showGlow) {
            Box(
                modifier = Modifier
                    .size(size * 1.35f)
                    .graphicsLayer {
                        scaleX = 1.05f
                        scaleY = 1.05f
                    }
                    .background(glowBrush, shape = RoundedCornerShape(cornerRadius * 1.3f))
            )
        }

        // Inner Theme-aware Glass / Shadow Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(cornerRadius)),
            contentAlignment = Alignment.Center
        ) {
            // High-resolution app logo asset
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = contentDescriptionText,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            // Theme-aware glass specular overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isDark) 0.08f else 0.15f),
                                Color.Transparent,
                                Color.Black.copy(alpha = if (isDark) 0.10f else 0.04f)
                            )
                        )
                    )
            )
        }
    }
}
