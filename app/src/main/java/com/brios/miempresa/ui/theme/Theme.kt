package com.brios.miempresa.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MiEmpresaDarkColorTokens.Primary,
    onPrimary = MiEmpresaDarkColorTokens.OnPrimary,
    surfaceContainer = MiEmpresaDarkColorTokens.SurfaceContainer,
    surfaceContainerLow = MiEmpresaDarkColorTokens.SurfaceContainerLow,
    primaryContainer = MiEmpresaDarkColorTokens.PrimaryContainer,
    onPrimaryContainer = MiEmpresaDarkColorTokens.OnPrimaryContainer,
    inversePrimary = MiEmpresaDarkColorTokens.InversePrimary,
    secondary = MiEmpresaDarkColorTokens.Secondary,
    onSecondary = MiEmpresaDarkColorTokens.OnSecondary,
    secondaryContainer = MiEmpresaDarkColorTokens.SecondaryContainer,
    onSecondaryContainer = MiEmpresaDarkColorTokens.OnSecondaryContainer,
    tertiary = MiEmpresaDarkColorTokens.Tertiary,
    onTertiary = MiEmpresaDarkColorTokens.OnTertiary,
    tertiaryContainer = MiEmpresaDarkColorTokens.TertiaryContainer,
    onTertiaryContainer = MiEmpresaDarkColorTokens.OnTertiaryContainer,
    background = MiEmpresaDarkColorTokens.Background,
    surfaceTint = MiEmpresaDarkColorTokens.SurfaceTint,
    surfaceVariant = MiEmpresaDarkColorTokens.SurfaceVariant,
    surface= MiEmpresaDarkColorTokens.Surface,
    onSurface = MiEmpresaDarkColorTokens.OnSurface,
    error = MiEmpresaDarkColorTokens.Error
)
private val LightColorScheme = lightColorScheme(
    primary = MiEmpresaLightColorTokens.Primary,
    onPrimary = MiEmpresaLightColorTokens.OnPrimary,
    surfaceContainer = MiEmpresaLightColorTokens.SurfaceContainer,
    surfaceContainerLow = MiEmpresaLightColorTokens.SurfaceContainerLow,
    primaryContainer = MiEmpresaLightColorTokens.PrimaryContainer,
    onPrimaryContainer = MiEmpresaLightColorTokens.OnPrimaryContainer,
    inversePrimary = MiEmpresaLightColorTokens.InversePrimary,
    secondary = MiEmpresaLightColorTokens.Secondary,
    onSecondary = MiEmpresaLightColorTokens.OnSecondary,
    secondaryContainer = MiEmpresaLightColorTokens.SecondaryContainer,
    onSecondaryContainer = MiEmpresaLightColorTokens.OnSecondaryContainer,
    tertiary = MiEmpresaLightColorTokens.Tertiary,
    onTertiary = MiEmpresaLightColorTokens.OnTertiary,
    tertiaryContainer = MiEmpresaLightColorTokens.TertiaryContainer,
    onTertiaryContainer = MiEmpresaLightColorTokens.OnTertiaryContainer,
    background = MiEmpresaLightColorTokens.Background,
    surfaceTint = MiEmpresaLightColorTokens.SurfaceTint,
    surfaceVariant = MiEmpresaLightColorTokens.SurfaceVariant,
    surface= MiEmpresaLightColorTokens.Surface,
    onSurface = MiEmpresaLightColorTokens.OnSurface,
    error = MiEmpresaLightColorTokens.Error
)

@Composable
fun MiEmpresaTheme(
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
        content = content
    )
}