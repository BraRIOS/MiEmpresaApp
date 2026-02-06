package com.brios.miempresa.core.ui.theme

import androidx.compose.ui.unit.dp

object AppDimensions {
    // Grid system (4dp base)
    val extraSmallPadding = 4.dp
    val smallPadding = 8.dp
    val mediumSmallPadding = 12.dp
    val mediumPadding = 16.dp
    val mediumLargePadding = 20.dp
    val largePadding = 24.dp
    val extraLargePadding = 32.dp

    // Icon sizes
    val defaultIconSize = 24.dp
    val mediumIconSize = 32.dp
    val largeIconSize = 48.dp
    val extraLargeIconSize = 64.dp
    val emptyStateIconSize = 120.dp
    val errorStateIconSize = 160.dp

    // Corners
    val smallCornerRadius = 8.dp
    val mediumCornerRadius = 16.dp
    val largeCornerRadius = 24.dp

    // Borders
    val smallBorderWidth = 1.dp
    val mediumBorderWidth = 2.dp

    // Bottom sheet
    val bottomSheetPeekHeight = 400.dp

    // Screen-specific
    object SignInScreen {
        val topPadding = 80.dp
        val logoSize = 200.dp
        val spaceBetweenLogoAndSignInButton = 80.dp
    }

    object Drawer {
        val appLogoSize = 64.dp
    }

    object ProductDetails {
        val productImageSize = 300.dp
        val progressIndicatorSize = 64.dp
    }

    object Products {
        val productCardWidth = 150.dp
        val progressIndicatorSize = 54.dp
        val imageHeight = 80.dp
    }

    object Categories {
        val imageSize = 40.dp
    }

    object CompanyListView {
        val listHeight = 140.dp
    }

    object QrCode {
        val size = 200.dp
    }
}
