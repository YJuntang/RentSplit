package com.rentsplit.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Font Family ──────────────────────────────────────────────────────
val RentSplitFontFamily = FontFamily(
    Font(com.rentsplit.R.font.plusjakartasans_regular, FontWeight.Normal),
    Font(com.rentsplit.R.font.plusjakartasans_medium, FontWeight.Medium),
    Font(com.rentsplit.R.font.plusjakartasans_semibold, FontWeight.SemiBold),
    Font(com.rentsplit.R.font.plusjakartasans_bold, FontWeight.Bold),
    Font(com.rentsplit.R.font.plusjakartasans_extrabold, FontWeight.ExtraBold)
)

val Typography = Typography(
    // Hero amounts (e.g., RM 1,234.50 on dashboard)
    displayLarge = TextStyle(
        fontFamily = RentSplitFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp,
        lineHeight = 64.sp,
        letterSpacing = (-2.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = RentSplitFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 42.sp,
        lineHeight = 50.sp,
        letterSpacing = (-2.0).sp
    ),
    displaySmall = TextStyle(
        fontFamily = RentSplitFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-1.5).sp
    ),
    // Section headings
    headlineLarge = TextStyle(
        fontFamily = RentSplitFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-1.0).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = RentSplitFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-1.0).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = RentSplitFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = (-1.0).sp
    ),
    // Card titles
    titleLarge = TextStyle(
        fontFamily = RentSplitFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.5).sp
    ),
    titleMedium = TextStyle(
        fontFamily = RentSplitFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.5).sp
    ),
    titleSmall = TextStyle(
        fontFamily = RentSplitFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.5).sp
    ),
    // Body text
    bodyLarge = TextStyle(
        fontFamily = RentSplitFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.4).sp
    ),
    bodyMedium = TextStyle(
        fontFamily = RentSplitFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.4).sp
    ),
    bodySmall = TextStyle(
        fontFamily = RentSplitFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.2).sp
    ),
    // Labels / chips / tags
    labelLarge = TextStyle(
        fontFamily = RentSplitFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.2).sp
    ),
    labelMedium = TextStyle(
        fontFamily = RentSplitFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = (-0.1).sp
    ),
    labelSmall = TextStyle(
        fontFamily = RentSplitFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = (-0.1).sp
    )
)
