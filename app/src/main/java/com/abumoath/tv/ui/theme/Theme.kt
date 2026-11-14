package com.abumoath.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Typography
import androidx.tv.material3.darkColorScheme

/** ABU MOATH brand palette: luxury black, royal gold, royal blue. */
object Brand {
    val Black = Color(0xFF07080C)
    val Surface = Color(0xFF10131B)
    val Surface2 = Color(0xFF171B26)
    val Line = Color(0xFF252B3A)
    val Gold = Color(0xFFD4AF37)
    val GoldLight = Color(0xFFF3D77A)
    val GoldDark = Color(0xFF8E6A14)
    val RoyalBlue = Color(0xFF2F5BD3)
    val Text = Color(0xFFEEF0F6)
    val Muted = Color(0xFF9AA3B8)
    val Live = Color(0xFFE53935)
    val GoldGradient = Brush.linearGradient(listOf(GoldLight, Gold, GoldDark))
    val ScrimBottom = Brush.verticalGradient(listOf(Color.Transparent, Black.copy(alpha = 0.7f), Black))
    val ScrimStart = Brush.horizontalGradient(listOf(Black, Black.copy(alpha = 0.75f), Color.Transparent))
}

private val colors = darkColorScheme(
    primary = Brand.Gold,
    onPrimary = Color(0xFF121212),
    primaryContainer = Brand.GoldDark,
    onPrimaryContainer = Brand.GoldLight,
    secondary = Brand.RoyalBlue,
    onSecondary = Color.White,
    background = Brand.Black,
    onBackground = Brand.Text,
    surface = Brand.Surface,
    onSurface = Brand.Text,
    surfaceVariant = Brand.Surface2,
    onSurfaceVariant = Brand.Muted,
    error = Color(0xFFFF6B60)
)

// Large, high-contrast type for viewing from 3 m away (10-foot UI).
private val typography = Typography(
    displaySmall = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 18.sp, lineHeight = 28.sp),
    bodyMedium = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodySmall = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 12.sp)
)

@Composable
fun AbuMoathTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}
