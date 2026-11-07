package com.ufpso.finanapp.ui.theme


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenContainer,
    onPrimaryContainer = GreenDark,
    background = GreenBackground,
    surface = Color.White,
    onBackground = TextDark,
    onSurface = TextDark,
    onSurfaceVariant = TextGray,
    error = ExpenseRed
)

@Composable
fun FINANAPPTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}