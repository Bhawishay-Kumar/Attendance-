package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Design system tokens matching college-attendance-redesign.html
 */
object GlassTokens {
    // Primary Signature Gradient: #3B6BFF -> #7A4DFF
    val PrimaryGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF3B6BFF), Color(0xFF7A4DFF)),
        start = Offset(0f, 0f),
        end = Offset(400f, 200f)
    )

    // Present Gradient: #13DBA2 -> #08B49C
    val PresentGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF13DBA2), Color(0xFF08B49C)),
        start = Offset(0f, 0f),
        end = Offset(300f, 150f)
    )

    // Absent Gradient: #FF2F75 -> #E51C4A
    val AbsentGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFF2F75), Color(0xFFE51C4A)),
        start = Offset(0f, 0f),
        end = Offset(300f, 150f)
    )

    // No-Class Gradient: #4A4FA8 -> #363A86
    val NoClassGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF4A4FA8), Color(0xFF363A86)),
        start = Offset(0f, 0f),
        end = Offset(300f, 150f)
    )

    // Multi-color Ring Gradient (#3B6BFF -> #8B5CF6 -> #14D4A0)
    val RingGradient = Brush.sweepGradient(
        listOf(
            Color(0xFF3B6BFF),
            Color(0xFF8B5CF6),
            Color(0xFF14D4A0),
            Color(0xFF3B6BFF)
        )
    )

    // Dock Edge Gradient
    val DockEdgeDark = Brush.linearGradient(
        listOf(
            Color(0xF22882FF),
            Color(0x8C46C8FF),
            Color(0xBF6E50FF),
            Color(0xF2BE5AFF)
        )
    )

    val DockEdgeLight = Brush.linearGradient(
        listOf(
            Color.White,
            Color(0xD982D2FF),
            Color.White,
            Color(0xE6D78CFF)
        )
    )

    // Liquid Glass Active Capsule Tint Gradient
    val ActiveCapsuleGradient = Brush.linearGradient(
        listOf(
            Color(0xFF0865FF),
            Color(0xFF4A44F5),
            Color(0xFF7628F5),
            Color(0xFFB13CF0)
        )
    )

    // Dark Tokens
    val DarkSurfaceBrush = Brush.linearGradient(
        listOf(Color(0xC7142A74), Color(0xDB081442))
    )
    val DarkBorder = Color(0x8C3E70EB)
    val GlassBorderDark = DarkBorder
    val DarkText = Color(0xFFF2F5FF)
    val DarkMuted = Color(0xFF9DB0DF)
    val DarkCode = Color(0xFF27D3F0)
    val DarkChip = Color(0x80244096)
    val DarkChipBorder = Color(0x805A87EB)
    val DarkChipFg = Color(0xFFB9C9F2)
    val DarkPanel = Color(0xF0060E2C)
    val DarkField = Color(0xB30A184C)

    // Light Tokens
    val LightSurfaceBrush = Brush.linearGradient(
        listOf(Color(0xEBFFFFFF), Color(0xD6EEF5FF))
    )
    val LightBorder = Color(0x99A0BEF5)
    val GlassBorderLight = LightBorder
    val LightText = Color(0xFF0E1A52)
    val LightMuted = Color(0xFF6578A8)
    val LightCode = Color(0xFF1D63E8)
    val LightChip = Color(0xE6E8F0FF)
    val LightChipBorder = Color(0x99AAC3F0)
    val LightChipFg = Color(0xFF4D62A0)
    val LightPanel = Color(0xF2F0F6FF)
    val LightField = Color(0xE6FFFFFF)

    // Status Pill Colors
    val DarkOkPillBg = Color(0x800A5A5A)
    val DarkOkPillBorder = Color(0xB32DE6B4)
    val DarkOkPillFg = Color(0xFF3DF0B8)

    val LightOkPillBg = Color(0xE6D2F7E6)
    val LightOkPillBorder = Color(0x8C28BE8C)
    val LightOkPillFg = Color(0xFF0A7A55)

    val DarkNoPillBg = Color(0x806E143C)
    val DarkNoPillBorder = Color(0xB3FF5A8C)
    val DarkNoPillFg = Color(0xFFFF86A8)

    val LightNoPillBg = Color(0xE6FFE1E9)
    val LightNoPillBorder = Color(0x80F05A82)
    val LightNoPillFg = Color(0xFFC2173E)

    val DarkNcPillBg = Color(0x803C3296)
    val DarkNcPillBorder = Color(0xB39682FF)
    val DarkNcPillFg = Color(0xFFC3B8FF)

    val LightNcPillBg = Color(0xF2E8E4FF)
    val LightNcPillBorder = Color(0x808C78F0)
    val LightNcPillFg = Color(0xFF4C3CC4)

    val DarkNmPillBg = Color(0x80192D78)
    val DarkNmPillBorder = Color(0x80648CF0)
    val DarkNmPillFg = Color(0xFFB9C9F2)

    val LightNmPillBg = Color(0xF2EEF4FF)
    val LightNmPillBorder = Color(0x99AAC3F0)
    val LightNmPillFg = Color(0xFF5D70A8)

    // Status Confirmation Strips
    val DarkOkStripBg = Brush.linearGradient(listOf(Color(0xFF12C99B), Color(0xFF0A9F8D)))
    val LightOkStripBg = Brush.linearGradient(listOf(Color(0xFFC9F3DF), Color(0xFFB5ECD3)))
    val LightOkStripFg = Color(0xFF0A7A55)

    val DarkNoStripBg = Brush.linearGradient(listOf(Color(0xFFD62957), Color(0xFFA8173F)))
    val LightNoStripBg = Brush.linearGradient(listOf(Color(0xFFFFD9E2), Color(0xFFFFC9D6)))
    val LightNoStripFg = Color(0xFFC2173E)

    val DarkNcStripBg = Brush.linearGradient(listOf(Color(0xFF4A4FA8), Color(0xFF363A86)))
    val LightNcStripBg = Brush.linearGradient(listOf(Color(0xFFE4E0FF), Color(0xFFD6D0FB)))
    val LightNcStripFg = Color(0xFF4C3CC4)

    // Helper functions
    @Composable
    fun textColor(isDark: Boolean = isAppDarkTheme()): Color = if (isDark) DarkText else LightText

    @Composable
    fun mutedColor(isDark: Boolean = isAppDarkTheme()): Color = if (isDark) DarkMuted else LightMuted

    @Composable
    fun codeColor(isDark: Boolean = isAppDarkTheme()): Color = if (isDark) DarkCode else LightCode

    @Composable
    fun surfaceBrush(isDark: Boolean = isAppDarkTheme()): Brush = if (isDark) DarkSurfaceBrush else LightSurfaceBrush

    @Composable
    fun borderColor(isDark: Boolean = isAppDarkTheme()): Color = if (isDark) DarkBorder else LightBorder

    @Composable
    fun chipColor(isDark: Boolean = isAppDarkTheme()): Color = if (isDark) DarkChip else LightChip

    @Composable
    fun chipBorderColor(isDark: Boolean = isAppDarkTheme()): Color = if (isDark) DarkChipBorder else LightChipBorder

    @Composable
    fun chipFgColor(isDark: Boolean = isAppDarkTheme()): Color = if (isDark) DarkChipFg else LightChipFg

    @Composable
    fun panelColor(isDark: Boolean = isAppDarkTheme()): Color = if (isDark) DarkPanel else LightPanel

    @Composable
    fun fieldColor(isDark: Boolean = isAppDarkTheme()): Color = if (isDark) DarkField else LightField
}

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
            .then(clickMod)
            .padding(BorderStroke(1.5.dp, bColor).width)
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
