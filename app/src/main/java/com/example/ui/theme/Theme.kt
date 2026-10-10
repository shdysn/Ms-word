package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = WordBlueLight,
    onPrimary = Color.White,
    primaryContainer = WordBlueDark,
    onPrimaryContainer = Color.White,
    secondary = WordBlueLight,
    onSecondary = Color.White,
    background = Color(0xFF141414),
    surface = Color(0xFF1F1F1F),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurface = Color(0xFFE0E0E0),
    onBackground = Color(0xFFE0E0E0)
)

private val LightColorScheme = lightColorScheme(
    primary = WordBluePrimary,
    onPrimary = Color.White,
    primaryContainer = WordBlueContainer,
    onPrimaryContainer = WordOnBlueContainer,
    secondary = WordBlueDark,
    onSecondary = Color.White,
    background = EditorCanvasBackground,
    surface = Color.White,
    surfaceVariant = RibbonBackground,
    onSurface = DocTextPrimary,
    onBackground = DocTextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep Microsoft Word signature blue branding consistent
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
