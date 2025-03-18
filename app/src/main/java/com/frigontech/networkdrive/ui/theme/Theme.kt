package com.frigontech.networkdrive.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    secondary = Color(0xFF303030),
    background = Color(0xFF171717),
    surface = Color(0xFF313131),
    onSecondary = Color(0xFF242424),

    tertiary = Color(0xFF4aa4ff),
)

private val LightColorScheme = lightColorScheme(
    primary = Color.Black,
    secondary = Color(0xFFe3e3e3),
    background = Color(0xFFd4d4d4),
    surface = Color(0xFFcccccc),
    onSecondary = Color(0xFFe3e3e3),

    tertiary = Color(0xFF308dff)

    /* Other default colors to override
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

object IsDarkMode{
    var isDarkMode = mutableStateOf(false)
}

@Composable
fun NetworkDriveExplorerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    IsDarkMode.isDarkMode.value = darkTheme
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}