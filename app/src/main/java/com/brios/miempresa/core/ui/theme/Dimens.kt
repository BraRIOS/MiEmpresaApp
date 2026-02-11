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
    val smallIconSize = 20.dp
    val defaultIconSize = 24.dp
    val mediumIconSize = 32.dp
    val largeIconSize = 48.dp
    val extraLargeIconSize = 64.dp
    val emptyStateIconSize = 160.dp
    val emptyStateInnerIconSize = 88.dp
    val errorStateIconSize = 160.dp

    // Corners
    val smallCornerRadius = 8.dp
    val inputCornerRadius = 12.dp
    val mediumCornerRadius = 16.dp
    val largeCornerRadius = 24.dp

    // Borders
    val smallBorderWidth = 1.dp
    val mediumBorderWidth = 2.dp

    // Bottom sheet
    val bottomSheetPeekHeight = 400.dp

    // Component-specific
    val itemCardImageSize = 64.dp
    val categoryEmojiContainerSize = 56.dp
    val quantitySelectorMinWidth = 40.dp
    val emptyStateDotSize = 6.dp

    // Screen-specific
    object SignInScreen {
        val logoSize = 120.dp
        val googleButtonHeight = 56.dp
    }

    // Buttons
    val mainFABSize = 64.dp
    val mainFABIconSize = 32.dp

    val smallFabSize = 40.0.dp
    val smallFabIconSize = 20.0.dp

    // Chips
    val categoryFilterChipWidth = 120.dp


    object Drawer {
        val appLogoSize = 64.dp
        val drawerCornerRadius = 28.dp
        val userAvatarSize = 40.dp
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
        val emojiPreviewWidth = 72.dp
        val emojiPreviewHeight = 80.dp
        val emojiGridItemSize = 48.dp
    }

    object CompanyListView {
        val listHeight = 140.dp
    }

    object QrCode {
        val size = 200.dp
    }

    object WelcomeScreen {
        val logoSize = 160.dp
        val actionCardIconContainerSize = 40.dp
        val actionCardIconCornerRadius = 8.dp
        val actionCardPadding = 24.dp
        val actionCardCornerRadius = 16.dp
    }

    object OnboardingProgress {
        val progressCardCornerRadius = 16.dp
        val progressCardPadding = 16.dp
        val progressBarHeight = 12.dp
        val stepCircleSize = 48.dp
        val stepCheckIconSize = 20.dp
        val stepSpinnerIconSize = 24.dp
        val stepPendingDotSize = 10.dp
        val stepVerticalSpacing = 32.dp
        val stepCircleToTextGap = 20.dp
        val connectorLineWidth = 2.dp
        val connectorLineStartPx = 24 // center of 48dp circle
    }

    object OnboardingSuccess {
        val checkCircleSize = 128.dp
        val checkIconSize = 60.dp
        val avatarSize = 64.dp
        val ctaButtonHeight = 56.dp
        val ctaCornerRadius = 16.dp
        val summaryCardPadding = 20.dp
        val summaryCardCornerRadius = 16.dp
    }
}
