package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Accent theme presets
enum class AccentTheme(val label: String, val primary: Color, val darkPrimary: Color) {
    IIHT_BLUE("IIHT Blue", Color(0xFF1E3A8A), Color(0xFF60A5FA)),
    WEAVE_TEAL("Weave Teal", Color(0xFF0F766E), Color(0xFF2DD4BF)),
    VARANASI_AMBER("Varanasi Amber", Color(0xFFB45309), Color(0xFFFBBF24)),
    ROYAL_PURPLE("Royal Purple", Color(0xFF6D28D9), Color(0xFFA78BFA))
}

// Brand Colors & Signature Palette
val MidnightDark = Color(0xFF090D16)
val MidnightNavy = Color(0xFF0F172A)
val SurfaceDarkElevated = Color(0xFF131D31)
val SurfaceDarkCard = Color(0xFF162036)
val NavyDark = Color(0xFF0F172A)
val DeepBlue = Color(0xFF1E3A8A)
val BrightBlue = Color(0xFF2563EB)
val ElectricBlue = Color(0xFF38BDF8)
val BrandCyan = Color(0xFF06B6D4)
val SkyBlue = Color(0xFF0284C7)
val SkyGlow = Color(0xFF38BDF8)
val RoyalIndigo = Color(0xFF4F46E5)
val DeepViolet = Color(0xFF7C3AED)
val TealPrimary = Color(0xFF0D9488)
val TealLight = Color(0xFF14B8A6)
val AmberAccent = Color(0xFFF59E0B)

// Reference Mockup Design Tokens
val MockupLightBg = Color(0xFFF8FBFF)
val MockupLightCard = Color(0xFFFFFFFF)
val MockupLightTextPrimary = Color(0xFF102653)
val MockupLightTextSecondary = Color(0xFF64748B)
val MockupLightAccentBlue = Color(0xFF1769FF)
val MockupLightBorder = Color(0xFFE0EDFB)
val MockupLightSurfacePale = Color(0xFFF0F6FF)

val MockupDarkBg = Color(0xFF061329)
val MockupDarkCard = Color(0xFF10264A)
val MockupDarkCardSurface = Color(0xFF0D2040)
val MockupDarkTextPrimary = Color(0xFFF5F8FF)
val MockupDarkTextSecondary = Color(0xFF8EA5C8)
val MockupDarkAccentBlue = Color(0xFF1769FF)
val MockupDarkAccentCyan = Color(0xFF38BDF8)
val MockupDarkBorder = Color(0xFF1B3B6B)

// Glass Surfaces
val GlassSurfaceDark = Color(0xEB10264A)
val GlassSurfaceLight = Color(0xFFFFFFFF)

// Attendance Status Colors - Light Mode (High Contrast & Accessible WCAG AA)
val StatusPresentLight = Color(0xFF15803D)
val StatusPresentBgLight = Color(0xFFDCFCE7)
val StatusPresentBorderLight = Color(0xFF86EFAC)
val StatusPresentTextLight = Color(0xFF14532D)

val StatusAbsentLight = Color(0xFFB91C1C)
val StatusAbsentBgLight = Color(0xFFFEE2E2)
val StatusAbsentBorderLight = Color(0xFFFCA5A5)
val StatusAbsentTextLight = Color(0xFF7F1D1D)

val StatusCancelledLight = Color(0xFF475569)
val StatusCancelledBgLight = Color(0xFFF1F5F9)
val StatusCancelledBorderLight = Color(0xFFCBD5E1)
val StatusCancelledTextLight = Color(0xFF1E293B)

val StatusIncompleteLight = Color(0xFFB45309)
val StatusIncompleteBgLight = Color(0xFFFEF3C7)
val StatusIncompleteBorderLight = Color(0xFFFDE68A)
val StatusIncompleteTextLight = Color(0xFF78350F)

val StatusUnmarkedLight = Color(0xFF854D0E)
val StatusUnmarkedBgLight = Color(0xFFFEF9C3)
val StatusUnmarkedBorderLight = Color(0xFFFEF08A)
val StatusUnmarkedTextLight = Color(0xFF713F12)

// Attendance Status Colors - Dark Mode (Luminescent, readable & Non-glaring)
val StatusPresentDark = Color(0xFF4ADE80)
val StatusPresentBgDark = Color(0x2E10B981)
val StatusPresentBorderDark = Color(0x554ADE80)
val StatusPresentTextDark = Color(0xFF86EFAC)

val StatusAbsentDark = Color(0xFFF87171)
val StatusAbsentBgDark = Color(0x2EEF4444)
val StatusAbsentBorderDark = Color(0x55F87171)
val StatusAbsentTextDark = Color(0xFFFCA5A5)

val StatusCancelledDark = Color(0xFF94A3B8)
val StatusCancelledBgDark = Color(0x2E334155)
val StatusCancelledBorderDark = Color(0x5594A3B8)
val StatusCancelledTextDark = Color(0xFFE2E8F0)

val StatusIncompleteDark = Color(0xFFFCD34D)
val StatusIncompleteBgDark = Color(0x2EF59E0B)
val StatusIncompleteBorderDark = Color(0x55FCD34D)
val StatusIncompleteTextDark = Color(0xFFFDE68A)

val StatusUnmarkedDark = Color(0xFFFDE047)
val StatusUnmarkedBgDark = Color(0x2EEAB308)
val StatusUnmarkedBorderDark = Color(0x55FDE047)
val StatusUnmarkedTextDark = Color(0xFFFEF08A)

// Backward compatibility defaults
val StatusPresent = Color(0xFF16A34A)
val StatusPresentBg = Color(0xFFDCFCE7)
val StatusPresentText = Color(0xFF14532D)

val StatusAbsent = Color(0xFFDC2626)
val StatusAbsentBg = Color(0xFFFEE2E2)
val StatusAbsentText = Color(0xFF7F1D1D)

val StatusCancelled = Color(0xFF64748B)
val StatusCancelledBg = Color(0xFFF1F5F9)
val StatusCancelledText = Color(0xFF1E293B)

val StatusIncomplete = Color(0xFFD97706)
val StatusIncompleteBg = Color(0xFFFEF3C7)
val StatusIncompleteText = Color(0xFF78350F)

val StatusUnmarked = Color(0xFFEAB308)
val StatusUnmarkedBg = Color(0xFFFEF9C3)
val StatusUnmarkedText = Color(0xFF713F12)

// Neutrals Scale (Tailwind / Slate compatible)
val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)
val Slate200 = Color(0xFFE2E8F0)
val Slate300 = Color(0xFFCBD5E1)
val Slate400 = Color(0xFF94A3B8)
val Slate500 = Color(0xFF64748B)
val Slate600 = Color(0xFF475569)
val Slate700 = Color(0xFF334155)
val Slate800 = Color(0xFF1E293B)
val Slate900 = Color(0xFF0F172A)
val Slate950 = Color(0xFF090D16)
