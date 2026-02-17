package com.example.bookshelf.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AlienGreen,
    secondary = AlienDarkGreen,
    tertiary = AlienLightGreen,
    background = AlienBlack,
    surface = AlienBlack,
    onPrimary = AlienBlack,
    onSecondary = AlienBlack,
    onTertiary = AlienBlack,
    onBackground = AlienGreen,
    onSurface = AlienGreen
)

@Composable
fun BookShelfTheme(
    darkTheme: Boolean = true, // Force dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = com.example.bookshelf.ui.theme.DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}