package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassTokens
import com.example.ui.theme.isAppDarkTheme

/**
 * Shared Glass Card component matching `.surf` from HTML prototype:
 * 24dp rounded corners, translucent gradient surface, 1.5dp border, soft shadow.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    isDark: Boolean = isAppDarkTheme(),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    accentBorder: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val bgBrush = GlassTokens.surfaceBrush(isDark)
    val bColor = accentBorder ?: GlassTokens.borderColor(isDark)

    val clickMod = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 4.dp else 3.dp,
                shape = shape,
                ambientColor = if (isDark) Color(0x66081EC8) else Color(0x335064FF),
                spotColor = if (isDark) Color(0x663C6EFF) else Color(0x2E788CFF)
            )
            .clip(shape)
            .background(bgBrush)
            .border(1.5.dp, bColor, shape)
            .then(clickMod)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
        ) {
            content()
        }
    }
}

/**
 * Subject Icon Tile matching Lucide iconography and gradient tiles from HTML prototype:
 * Displays spinning/yarn, weave, garment, chemistry, computing, or theory icons.
 */
@Composable
fun CardSubjectIcon(
    subjectCode: String,
    classType: String = "",
    size: Dp = 44.dp,
    isDark: Boolean = isAppDarkTheme(),
    modifier: Modifier = Modifier
) {
    val codeUpper = subjectCode.uppercase()
    val isLab = classType.equals("LAB", ignoreCase = true) || codeUpper.contains("LAB")

    val (iconVector, bgBrush, iconTint) = when {
        // Spinning / Fiber (BHT 503 / Yarn)
        codeUpper.contains("503") || codeUpper.contains("YARN") || codeUpper.contains("FIBER") -> {
            Triple(
                Icons.Default.Timeline,
                if (isDark) Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF2563EB)))
                else Brush.linearGradient(listOf(Color(0xFFDBEAFE), Color(0xFFBFDBFE))),
                if (isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8)
            )
        }
        // Fabric / Weaving (BHT 501 / Knitting)
        codeUpper.contains("501") || codeUpper.contains("FABRIC") || codeUpper.contains("WEAV") || codeUpper.contains("KNIT") -> {
            Triple(
                Icons.Default.DesignServices,
                if (isDark) Brush.linearGradient(listOf(Color(0xFF064E3B), Color(0xFF059669)))
                else Brush.linearGradient(listOf(Color(0xFFD1FAE5), Color(0xFFA7F3D0))),
                if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857)
            )
        }
        // Garment / Fashion (BHT 502 / Shirt)
        codeUpper.contains("502") || codeUpper.contains("GARMENT") || codeUpper.contains("SHIRT") -> {
            Triple(
                Icons.Default.Checkroom,
                if (isDark) Brush.linearGradient(listOf(Color(0xFF4C1D95), Color(0xFF7C3AED)))
                else Brush.linearGradient(listOf(Color(0xFFEDE9FE), Color(0xFFDDD6FE))),
                if (isDark) Color(0xFFC4B5FD) else Color(0xFF6D28D9)
            )
        }
        // Chemical Processing / Wet Processing (BHT 504 / Beaker)
        codeUpper.contains("504") || codeUpper.contains("CHEM") || codeUpper.contains("PROCESSING") -> {
            Triple(
                Icons.Default.Science,
                if (isDark) Brush.linearGradient(listOf(Color(0xFF831843), Color(0xFFDB2777)))
                else Brush.linearGradient(listOf(Color(0xFFFCE7F3), Color(0xFFFBCFE8))),
                if (isDark) Color(0xFFF472B6) else Color(0xFFBE185D)
            )
        }
        // Practical / Lab
        isLab -> {
            Triple(
                Icons.Default.Science,
                if (isDark) Brush.linearGradient(listOf(Color(0xFF0F766E), Color(0xFF0D9488)))
                else Brush.linearGradient(listOf(Color(0xFFCCFBF1), Color(0xFF99F6E4))),
                if (isDark) Color(0xFF5EEAD4) else Color(0xFF0F766E)
            )
        }
        // General Engineering / Elective
        codeUpper.contains("ENG") || codeUpper.contains("TECH") -> {
            Triple(
                Icons.Default.Settings,
                if (isDark) Brush.linearGradient(listOf(Color(0xFF7C2D12), Color(0xFFEA580C)))
                else Brush.linearGradient(listOf(Color(0xFFFFEDD5), Color(0xFFFED7AA))),
                if (isDark) Color(0xFFFDBA74) else Color(0xFFC2410C)
            )
        }
        // Default Theory Lecture
        else -> {
            Triple(
                Icons.Default.MenuBook,
                if (isDark) Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF334155)))
                else Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))),
                if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
            )
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(bgBrush)
            .border(
                1.dp,
                if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.6f),
                RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(size * 0.52f)
        )
    }
}
