package com.brios.miempresa.ui.theme

import androidx.compose.ui.graphics.Color

// Logo colors
val Red = Color(0xFFEA192F)
val DarkBlue = Color(0xFF001F4F)
val LightBlue = Color(0xFF04C4D9)
val Orange = Color(0xFFF27D16)
val DarkOrange = Color(0xFFF2561D)
val PlaceholderBG = Color(0xFF919191)
val OnPlaceholderBG = Color(0xFF575757)

internal object MiEmpresaPaletteTokens {
    // Primario basado en el naranja del logo
    val Primary0 = Color(0xFF000000) // Negro
    val Primary10 = Color(0xFF3E1B00) // Muy oscuro
    val Primary20 = Color(0xFF633000) // Oscuro
    val Primary30 = Color(0xFF8A4700) // Más oscuro
    val Primary40 = Color(0xFFB35F00) // Oscuro medio
    val Primary50 = Color(0xFFDA7700) // Medio
    val Primary60 = Color(0xFFF27D16) // Color base
    val Primary70 = Color(0xFFFFA05C) // Claro medio
    val Primary80 = Color(0xFFFFC18F) // Más claro
    val Primary90 = Color(0xFFFFE4C0) // Claro
    val Primary95 = Color(0xFFFFF2E0) // Muy claro
    val Primary99 = Color(0xFFFFFBF9) // Casi blanco
    val Primary100 = Color(0xFFFFFFFF) // Blanco

    val Secondary0 = Color(0xFF000000)      // Negro
    val Secondary10 = Color(0xFF000614)     // Muy oscuro
    val Secondary20 = Color(0xFF000D2A)     // Muy oscuro
    val Secondary30 = Color(0xFF00123A)     // Oscuro
    val Secondary40 = Color(0xFF001F4F)     // **Color principal Azul Oscuro**
    val Secondary50 = Color(0xFF2B4970)     // Oscuro medio
    val Secondary60 = Color(0xFF4D6B8E)     // Medio oscuro
    val Secondary70 = Color(0xFF6E8DAC)     // Medio
    val Secondary80 = Color(0xFF9DB9D6)     // Claro
    val Secondary90 = Color(0xFFC8DCF0)     // Muy claro
    val Secondary95 = Color(0xFFE4EEF9)     // Casi blanco
    val Secondary99 = Color(0xFFF5F7FF)     // Casi blanco
    val Secondary100 = Color(0xFFFFFFFF)    // Blanco

    // Secundario basado en el azul oscuro del logo
    val Complementary0 = Color(0xFF000000)    // Negro
    val Complementary10 = Color(0xFF001014)    // Muy oscuro
    val Complementary20 = Color(0xFF00242B)    // Oscuro
    val Complementary30 = Color(0xFF003641)    // Oscuro medio
    val Complementary40 = Color(0xFF004B59)    // Base oscura
    val Complementary50 = Color(0xFF006173)    // Medio oscuro
    val Complementary60 = Color(0xFF00798C)    // Medio
    val Complementary70 = Color(0xFF04A1B3)    // Base claro
    val Complementary80 = Color(0xFF04C4D9)    // Color original (Azul claro)
    val Complementary90 = Color(0xFF79E0EC)    // Claro
    val Complementary95 = Color(0xFFCDF5FA)    // Muy claro
    val Complementary99 = Color(0xFFF3FDFF)    // Casi blanco
    val Complementary100 = Color(0xFFFFFFFF)   // Blanco

    // Terciario basado en el rojo del logo
    val Tertiary0 = Color(red = 0, green = 0, blue = 0)
    val Tertiary10 = Color(red = 45, green = 5, blue = 10)
    val Tertiary20 = Color(red = 85, green = 15, blue = 25)
    val Tertiary30 = Color(red = 130, green = 25, blue = 40)
    val Tertiary40 = Color(red = 180, green = 35, blue = 55)
    val Tertiary50 = Color(red = 200, green = 50, blue = 70)
    val Tertiary60 = Color(red = 220, green = 80, blue = 90)
    val Tertiary70 = Color(red = 240, green = 130, blue = 150)
    val Tertiary80 = Color(red = 250, green = 170, blue = 190)
    val Tertiary90 = Color(red = 255, green = 210, blue = 220)
    val Tertiary100 = Color(red = 255, green = 255, blue = 255)
}

internal object MiEmpresaLightColorTokens {
    val Background = MiEmpresaPaletteTokens.Primary99
    val Error = Red
    val ErrorContainer = MiEmpresaPaletteTokens.Tertiary90
    val InverseOnSurface = MiEmpresaPaletteTokens.Secondary20
    val InversePrimary = MiEmpresaPaletteTokens.Primary80
    val InverseSurface = MiEmpresaPaletteTokens.Secondary30
    val OnBackground = MiEmpresaPaletteTokens.Secondary10
    val OnError = MiEmpresaPaletteTokens.Tertiary100
    val OnErrorContainer = MiEmpresaPaletteTokens.Tertiary10
    val OnPrimary = MiEmpresaPaletteTokens.Primary100
    val OnPrimaryContainer = MiEmpresaPaletteTokens.Primary30
    val OnPrimaryFixed = MiEmpresaPaletteTokens.Primary10
    val OnPrimaryFixedVariant = MiEmpresaPaletteTokens.Primary30
    val OnSecondary = MiEmpresaPaletteTokens.Secondary99
    val OnSecondaryContainer = MiEmpresaPaletteTokens.Secondary40
    val OnSecondaryFixed = MiEmpresaPaletteTokens.Secondary10
    val OnSecondaryFixedVariant = MiEmpresaPaletteTokens.Secondary30
    val OnSurface = MiEmpresaPaletteTokens.Primary20
    val OnSurfaceVariant = MiEmpresaPaletteTokens.Secondary40
    val OnTertiary = MiEmpresaPaletteTokens.Tertiary100
    val OnTertiaryContainer = MiEmpresaPaletteTokens.Tertiary10
    val OnTertiaryFixed = MiEmpresaPaletteTokens.Tertiary10
    val OnTertiaryFixedVariant = MiEmpresaPaletteTokens.Tertiary30
    // Outline no está definido en MiEmpresaPaletteTokens
    // OutlineVariant no está definido en MiEmpresaPaletteTokens
    val Primary = MiEmpresaPaletteTokens.Primary60
    val PrimaryContainer = MiEmpresaPaletteTokens.Primary80
    val PrimaryFixed = MiEmpresaPaletteTokens.Primary90
    val PrimaryFixedDim = MiEmpresaPaletteTokens.Primary80
    // Scrim no está definido en MiEmpresaPaletteTokens
    val Secondary = MiEmpresaPaletteTokens.Secondary40
    val SecondaryContainer = MiEmpresaPaletteTokens.Secondary90
    val SecondaryFixed = MiEmpresaPaletteTokens.Secondary90
    val SecondaryFixedDim = MiEmpresaPaletteTokens.Secondary80
    val Surface = MiEmpresaPaletteTokens.Primary99
    // SurfaceBright no está definido en MiEmpresaPaletteTokens
    val SurfaceContainer = MiEmpresaPaletteTokens.Primary95
    // SurfaceContainerHigh no está definido en MiEmpresaPaletteTokens
    // SurfaceContainerHighest no está definido en MiEmpresaPaletteTokens
    val SurfaceContainerLow = MiEmpresaPaletteTokens.Primary95
    // SurfaceContainerLowest no está definido en MiEmpresaPaletteTokens
    // SurfaceDim no está definido en MiEmpresaPaletteTokens
    val SurfaceTint = Primary
    val SurfaceVariant = MiEmpresaPaletteTokens.Primary95
    val Tertiary = MiEmpresaPaletteTokens.Tertiary40
    val TertiaryContainer = MiEmpresaPaletteTokens.Tertiary90
    val TertiaryFixed = MiEmpresaPaletteTokens.Tertiary90
    val TertiaryFixedDim = MiEmpresaPaletteTokens.Tertiary80
}

internal object MiEmpresaDarkColorTokens {
    val Background = MiEmpresaPaletteTokens.Primary10
    val Error = Red
    val ErrorContainer = MiEmpresaPaletteTokens.Tertiary30
    val InverseOnSurface = MiEmpresaPaletteTokens.Secondary95
    val InversePrimary = MiEmpresaPaletteTokens.Primary40
    val InverseSurface = MiEmpresaPaletteTokens.Secondary90
    val OnBackground = MiEmpresaPaletteTokens.Secondary90
    val OnError = MiEmpresaPaletteTokens.Tertiary100
    val OnErrorContainer = MiEmpresaPaletteTokens.Tertiary80
    val OnPrimary = MiEmpresaPaletteTokens.Primary10
    val OnPrimaryContainer = MiEmpresaPaletteTokens.Primary80
    val OnPrimaryFixed = MiEmpresaPaletteTokens.Primary90
    val OnPrimaryFixedVariant = MiEmpresaPaletteTokens.Primary80
    val OnSecondary = MiEmpresaPaletteTokens.Secondary10
    val OnSecondaryContainer = MiEmpresaPaletteTokens.Secondary80
    val OnSecondaryFixed = MiEmpresaPaletteTokens.Secondary90
    val OnSecondaryFixedVariant = MiEmpresaPaletteTokens.Secondary80
    val OnSurface = MiEmpresaPaletteTokens.Primary90
    val OnSurfaceVariant = MiEmpresaPaletteTokens.Secondary80
    val OnTertiary = MiEmpresaPaletteTokens.Tertiary10
    val OnTertiaryContainer = MiEmpresaPaletteTokens.Tertiary80
    val OnTertiaryFixed = MiEmpresaPaletteTokens.Tertiary90
    val OnTertiaryFixedVariant = MiEmpresaPaletteTokens.Tertiary80
    val Primary = MiEmpresaPaletteTokens.Primary50
    val PrimaryContainer = MiEmpresaPaletteTokens.Primary30
    val PrimaryFixed = MiEmpresaPaletteTokens.Primary90
    val PrimaryFixedDim = MiEmpresaPaletteTokens.Primary80
    val Secondary = MiEmpresaPaletteTokens.Secondary80
    val SecondaryContainer = MiEmpresaPaletteTokens.Secondary30
    val SecondaryFixed = MiEmpresaPaletteTokens.Secondary90
    val SecondaryFixedDim = MiEmpresaPaletteTokens.Secondary80
    val Surface = MiEmpresaPaletteTokens.Primary10
    val SurfaceContainer = MiEmpresaPaletteTokens.Primary20
    val SurfaceContainerLow = MiEmpresaPaletteTokens.Primary20
    val SurfaceTint = Primary
    val SurfaceVariant = MiEmpresaPaletteTokens.Primary30
    val Tertiary = MiEmpresaPaletteTokens.Tertiary80
    val TertiaryContainer = MiEmpresaPaletteTokens.Tertiary30
    val TertiaryFixed = MiEmpresaPaletteTokens.Tertiary90
    val TertiaryFixedDim = MiEmpresaPaletteTokens.Tertiary80
}

