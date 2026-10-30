package com.example.proyecto_cloro.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = RojoClaro,
    onPrimary = Negro,
    secondary = Blanco,
    onSecondary = Negro,
    tertiary = RojoClaro,
    background = FondoOscuro,
    onBackground = Blanco,
    surface = SuperficieOscura,
    onSurface = Blanco,
    surfaceVariant = Color(0xFF29292D),
    onSurfaceVariant = TextoSecundarioOscuro,
    outline = Color(0xFF414146)
)

private val LightColorScheme = lightColorScheme(
    primary = Rojo,
    onPrimary = Blanco,
    secondary = Negro,
    onSecondary = Blanco,
    tertiary = RojoOscuro,
    background = FondoClaro,
    onBackground = Negro,
    surface = Blanco,
    onSurface = Negro,
    surfaceVariant = Color(0xFFF0EFF0),
    onSurfaceVariant = TextoSecundarioClaro,
    outline = Color(0xFFE3E1E3)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun Proyecto_cloroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
        shapes = AppShapes,
        content = content
    )
}