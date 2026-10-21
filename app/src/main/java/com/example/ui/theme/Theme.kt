package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AlHadidColorScheme = lightColorScheme(
    primary = AcademyGreen,
    onPrimary = Color.White,
    primaryContainer = AcademyIconCircleGreenBg,
    onPrimaryContainer = AcademyDarkGreen,
    secondary = AcademyOchre,
    onSecondary = Color.White,
    secondaryContainer = AcademyGold,
    onSecondaryContainer = AcademyTextDark,
    tertiary = AcademyBrightGold,
    background = AcademyCreamBg,
    onBackground = AcademyTextDark,
    surface = AcademyCardSurface,
    onSurface = AcademyTextDark,
    surfaceVariant = AcademyCreamBg,
    onSurfaceVariant = AcademyTextMuted,
    outline = AcademyCardBorder,
    error = AcademyRedAlert,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AlHadidColorScheme,
        typography = Typography,
        content = content
    )
}
