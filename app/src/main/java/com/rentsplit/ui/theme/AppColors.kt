package com.rentsplit.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Light-mode surface tokens ────────────────────────────────────────────────
val LightSurface0 = Color(0xFFF8FAFC)   // page background
val LightSurface1 = Color(0xFFFFFFFF)   // card base
val LightSurface2 = Color(0xFFF1F5F9)   // elevated card / input bg
val LightSurface3 = Color(0xFFE2E8F0)   // modal / top-level card
val LightSurface4 = Color(0xFFCBD5E1)   // border / divider rim

// ── Light-mode text tokens ───────────────────────────────────────────────────
val LightTextPrimary   = Color(0xFF0F172A)   // main content
val LightTextSecondary = Color(0xFF475569)   // supporting text
val LightTextMuted     = Color(0xFF94A3B8)   // placeholders / dividers

// ── Light-mode accent tokens ─────────────────────────────────────────────────
val LightCyanPrimary = Color(0xFF0099CC)    // deeper cyan for light mode contrast
val LightCyanVariant = Color(0xFF007AA3)    // pressed state

// ── Light-mode gradient pairs ────────────────────────────────────────────────
val LightGradientHero = listOf(Color(0xFFE0F7FF), Color(0xFFEDE9FE))
val LightGradientCard = listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9))

// ── Light semantic background tints ─────────────────────────────────────────
val LightPositiveGreenBg = Color(0xFFDCFCE7)
val LightWarningAmberBg  = Color(0xFFFEF9C3)
val LightNegativeRedBg   = Color(0xFFFFE4E4)

/**
 * Holds the full set of adaptive color tokens for RentSplit.
 * Use [LocalAppColors] to access the current instance inside a Composable.
 */
data class AppColors(
    val surface0: Color,
    val surface1: Color,
    val surface2: Color,
    val surface3: Color,
    val surface4: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val cyanPrimary: Color,
    val cyanVariant: Color,
    val gradientHero: List<Color>,
    val gradientCard: List<Color>,
    val positiveGreenBg: Color,
    val warningAmberBg: Color,
    val negativeRedBg: Color,
    val isDark: Boolean
)

val DarkAppColors = AppColors(
    surface0 = Surface0,
    surface1 = Surface1,
    surface2 = Surface2,
    surface3 = Surface3,
    surface4 = Surface4,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textMuted = TextMuted,
    cyanPrimary = CyanPrimary,
    cyanVariant = CyanVariant,
    gradientHero = GradientHero,
    gradientCard = GradientCard,
    positiveGreenBg = PositiveGreenBg,
    warningAmberBg = WarningAmberBg,
    negativeRedBg = NegativeRedBg,
    isDark = true
)

val LightAppColors = AppColors(
    surface0 = LightSurface0,
    surface1 = LightSurface1,
    surface2 = LightSurface2,
    surface3 = LightSurface3,
    surface4 = LightSurface4,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    cyanPrimary = LightCyanPrimary,
    cyanVariant = LightCyanVariant,
    gradientHero = LightGradientHero,
    gradientCard = LightGradientCard,
    positiveGreenBg = LightPositiveGreenBg,
    warningAmberBg = LightWarningAmberBg,
    negativeRedBg = LightNegativeRedBg,
    isDark = false
)

/**
 * Composition local providing the current [AppColors].
 * Provided by [RentSplitTheme]; defaults to dark colors.
 */
val LocalAppColors = staticCompositionLocalOf { DarkAppColors }
