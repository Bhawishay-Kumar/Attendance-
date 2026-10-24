package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.ui.theme.isAppDarkTheme

/**
 * Adaptive background tokens adhering to reference design:
 * Light theme: soft white & icy-blue base ("#F7FAFF", "#EEF6FF"), fluid curves, subtle lavender/cyan/blue gradient waves.
 * Dark theme: deep midnight navy ("#030D20", "#061735", "#081D42"), luminous cyan-blue wave gradients, ambient glows.
 */
object BackgroundTokens {
    // Light Palette
    val LightBaseTop = Color(0xFFF7FAFF)
    val LightBaseBottom = Color(0xFFEEF6FF)

    val LightWaveLayer1 = Color(0x3593C5FD) // Translucent sky blue
    val LightWaveLayer2 = Color(0x28C4B5FD) // Translucent lavender
    val LightWaveLayer3 = Color(0x1EA5F3FC) // Translucent soft cyan
    val LightGlowOrb = Color(0x1CB0D4FF)

    // Dark Palette
    val DarkBaseTop = Color(0xFF030D20)
    val DarkBaseMid = Color(0xFF061735)
    val DarkBaseBottom = Color(0xFF081D42)

    val DarkWaveLayer1 = Color(0x401E3A8A) // Electric navy
    val DarkWaveLayer2 = Color(0x354338CA) // Royal indigo
    val DarkWaveLayer3 = Color(0x2E0284C7) // Cyan ocean
    val DarkWaveAccent = Color(0x207C3AED) // Subtle purple accent
    val DarkGlowOrb = Color(0x2216D9F2)   // Restrained cyan glow
}

@Composable
fun AppAdaptiveBackground(
    modifier: Modifier = Modifier,
    isDark: Boolean = isAppDarkTheme(),
    content: @Composable BoxScope.() -> Unit
) {
    val baseGradient = if (isDark) {
        Brush.verticalGradient(
            listOf(
                BackgroundTokens.DarkBaseTop,
                BackgroundTokens.DarkBaseMid,
                BackgroundTokens.DarkBaseBottom
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                BackgroundTokens.LightBaseTop,
                BackgroundTokens.LightBaseBottom
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseGradient)
            .drawBehind {
                drawAdaptiveAmbientBackground(isDark)
            }
    ) {
        content()
    }
}

private fun DrawScope.drawAdaptiveAmbientBackground(isDark: Boolean) {
    val width = size.width
    val height = size.height
    if (width <= 0f || height <= 0f) return

    // 1. Top-Right Soft Ambient Light
    val topGlowBrush = if (isDark) {
        Brush.radialGradient(
            colors = listOf(
                BackgroundTokens.DarkGlowOrb,
                Color(0x101769FF),
                Color.Transparent
            ),
            center = Offset(width * 0.92f, height * 0.08f),
            radius = width * 0.65f
        )
    } else {
        Brush.radialGradient(
            colors = listOf(
                BackgroundTokens.LightGlowOrb,
                Color(0x10BAE6FD),
                Color.Transparent
            ),
            center = Offset(width * 0.90f, height * 0.08f),
            radius = width * 0.60f
        )
    }
    drawCircle(
        brush = topGlowBrush,
        center = Offset(width * 0.92f, height * 0.08f),
        radius = width * 0.65f
    )

    // 2. Middle-Left Faint Ambient Glow (behind quick cards / stats)
    val midGlowBrush = if (isDark) {
        Brush.radialGradient(
            colors = listOf(Color(0x141E3A8A), Color.Transparent),
            center = Offset(width * 0.10f, height * 0.35f),
            radius = width * 0.50f
        )
    } else {
        Brush.radialGradient(
            colors = listOf(Color(0x14E0E7FF), Color.Transparent),
            center = Offset(width * 0.10f, height * 0.35f),
            radius = width * 0.50f
        )
    }
    drawCircle(
        brush = midGlowBrush,
        center = Offset(width * 0.10f, height * 0.35f),
        radius = width * 0.50f
    )

    // 3. Flowing Curved Abstract Wave 1 (Deep outer curve at bottom right edge)
    val wavePath1 = Path().apply {
        moveTo(width, height * 0.65f)
        cubicTo(
            width * 0.95f, height * 0.72f,
            width * 0.75f, height * 0.78f,
            width * 0.70f, height * 0.88f
        )
        cubicTo(
            width * 0.66f, height * 0.94f,
            width * 0.58f, height * 0.98f,
            width * 0.40f, height
        )
        lineTo(width, height)
        close()
    }

    val waveBrush1 = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                BackgroundTokens.DarkWaveLayer1,
                BackgroundTokens.DarkWaveLayer2,
                BackgroundTokens.DarkWaveAccent
            ),
            start = Offset(width * 0.5f, height * 0.7f),
            end = Offset(width, height)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                BackgroundTokens.LightWaveLayer1,
                BackgroundTokens.LightWaveLayer2,
                Color(0x18DDD6FE)
            ),
            start = Offset(width * 0.5f, height * 0.7f),
            end = Offset(width, height)
        )
    }
    drawPath(path = wavePath1, brush = waveBrush1)

    // 4. Flowing Curved Abstract Wave 2 (Luminous inner overlap curve)
    val wavePath2 = Path().apply {
        moveTo(width, height * 0.76f)
        cubicTo(
            width * 0.88f, height * 0.80f,
            width * 0.82f, height * 0.87f,
            width * 0.80f, height * 0.92f
        )
        cubicTo(
            width * 0.78f, height * 0.96f,
            width * 0.70f, height * 0.99f,
            width * 0.62f, height
        )
        lineTo(width, height)
        close()
    }

    val waveBrush2 = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                BackgroundTokens.DarkWaveLayer3,
                BackgroundTokens.DarkWaveLayer2.copy(alpha = 0.5f)
            ),
            start = Offset(width * 0.7f, height * 0.8f),
            end = Offset(width, height)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                BackgroundTokens.LightWaveLayer3,
                BackgroundTokens.LightWaveLayer1.copy(alpha = 0.5f)
            ),
            start = Offset(width * 0.7f, height * 0.8f),
            end = Offset(width, height)
        )
    }
    drawPath(path = wavePath2, brush = waveBrush2)

    // 5. Left subtle organic bottom swell
    val leftSwellPath = Path().apply {
        moveTo(0f, height * 0.88f)
        cubicTo(
            width * 0.12f, height * 0.89f,
            width * 0.22f, height * 0.95f,
            width * 0.28f, height
        )
        lineTo(0f, height)
        close()
    }
    val leftSwellBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(Color(0x181E3A8A), Color.Transparent),
            start = Offset(0f, height * 0.9f),
            end = Offset(width * 0.3f, height)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0x16DBEAFE), Color.Transparent),
            start = Offset(0f, height * 0.9f),
            end = Offset(width * 0.3f, height)
        )
    }
    drawPath(path = leftSwellPath, brush = leftSwellBrush)
}
