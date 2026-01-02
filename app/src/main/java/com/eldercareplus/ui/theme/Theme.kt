package com.eldercareplus.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AppColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    onPrimary = SurfaceWhite,
    primaryContainer = PrimaryLight.copy(alpha = 0.2f),
    onPrimaryContainer = PrimaryDark,
    
    secondary = SecondaryBlue,
    onSecondary = SurfaceWhite,
    secondaryContainer = SecondaryLight.copy(alpha = 0.2f),
    
    tertiary = AccentOrange,
    
    background = NeutralBg,
    surface = SurfaceWhite,
    surfaceVariant = SurfaceWhite, // Cards look clean white
    
    error = ErrorRed,
    onError = SurfaceWhite
)

@Composable
fun ElderCarePlusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // We are forcing light theme for now for consistency
    content: @Composable () -> Unit
) {
    val colorScheme = AppColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
