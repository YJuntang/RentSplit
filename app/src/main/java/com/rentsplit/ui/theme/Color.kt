package com.rentsplit.ui.theme

import androidx.compose.ui.graphics.Color

// ── Core Surfaces (dark hierarchy) ──────────────────────────────────
val Surface0 = Color(0xFF000000)   // pure black background
val Surface1 = Color(0xFF171717)   // chatgpt-like dark gray
val Surface2 = Color(0xFF212121)   // elevated elements
val Surface3 = Color(0xFF2D2D2D)   // modal / top-level card
val Surface4 = Color(0xFF424242)   // borders/dividers

// ── Brand Accents ────────────────────────────────────────────────────
val CyanPrimary     = Color(0xFF0EA5E9)   // sleeker, muted sky blue
val CyanVariant     = Color(0xFF0284C7)   // deeper blue
val PurpleAccent    = Color(0xFF7C3AED)   // violet – secondary accent
val IndigoAccent    = Color(0xFF4F46E5)   // indigo – gradient partner

// ── Semantic Colors ──────────────────────────────────────────────────
val PositiveGreen   = Color(0xFF22C55E)   // settled / income
val PositiveGreenBg = Color(0xFF052010)   // settled background tint
val WarningAmber    = Color(0xFFF59E0B)   // approaching limit
val WarningAmberBg  = Color(0xFF1F1200)   // warning background tint
val NegativeRed     = Color(0xFFEF4444)   // debt / overdue
val NegativeRedBg   = Color(0xFF1F0505)   // debt background tint

// ── Text ─────────────────────────────────────────────────────────────
val TextPrimary     = Color(0xFFF9FAFB)   // clean white/off-white
val TextSecondary   = Color(0xFFA1A1AA)   // gray-400
val TextMuted       = Color(0xFF52525B)   // gray-600

// ── Member Avatar Colors (16-color expanded palette) ─────────────────
val MemberPalette = listOf(
    Color(0xFF00D4FF),  // electric cyan
    Color(0xFF7C3AED),  // violet
    Color(0xFF22C55E),  // emerald
    Color(0xFFF59E0B),  // amber
    Color(0xFFEF4444),  // rose red
    Color(0xFFEC4899),  // hot pink
    Color(0xFF06B6D4),  // teal
    Color(0xFF84CC16),  // lime
    Color(0xFFF97316),  // orange
    Color(0xFF8B5CF6),  // purple
    Color(0xFF14B8A6),  // teal green
    Color(0xFFFBBF24),  // yellow
    Color(0xFF60A5FA),  // sky blue
    Color(0xFFF472B6),  // pink
    Color(0xFF34D399),  // mint
    Color(0xFFA78BFA),  // lavender
)

val MemberPaletteHex = listOf(
    "#00D4FF", "#7C3AED", "#22C55E", "#F59E0B",
    "#EF4444", "#EC4899", "#06B6D4", "#84CC16",
    "#F97316", "#8B5CF6", "#14B8A6", "#FBBF24",
    "#60A5FA", "#F472B6", "#34D399", "#A78BFA",
)

// ── Category Colors ───────────────────────────────────────────────────
val CategoryRent       = Color(0xFF00D4FF)
val CategoryElectric   = Color(0xFFFBBF24)
val CategoryWater      = Color(0xFF60A5FA)
val CategoryInternet   = Color(0xFF8B5CF6)
val CategoryGroceries  = Color(0xFF22C55E)
val CategoryMisc       = Color(0xFF94A3B8)

// ── Gradient Pairs ────────────────────────────────────────────────────
val GradientCyan    = listOf(Color(0xFF0EA5E9), Color(0xFF0284C7))
val GradientPurple  = listOf(Color(0xFF7C3AED), Color(0xFF4F46E5))
val GradientHero    = listOf(Color(0xFF000000), Color(0xFF171717))
val GradientCard    = listOf(Color(0xFF171717), Color(0xFF212121))
