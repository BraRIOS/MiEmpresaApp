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

    val mediumLargeIconSize = 40.dp
    val largeIconSize = 48.dp
    val extraLargeIconSize = 64.dp
    val catalogCompanyLogoSize = 80.dp

    // Spot Illustration (Empty States, Error States, Not Found States)
    val mediumSpotIllustrationSize = 120.dp
    val mediumSpotIllustrationIconSize = 64.dp
    val largeSpotIllustrationSize = 160.dp
    val largeSpotIllustrationIconSize = 88.dp


    // Corners
    val smallCornerRadius = 8.dp
    val inputCornerRadius = 12.dp
    val mediumCornerRadius = 16.dp
    val largeCornerRadius = 24.dp

    // Borders
    val smallBorderWidth = 1.dp
    val mediumBorderWidth = 2.dp

    // Bottom sheet
    val bottomSheetPeekHeight = 220.dp

    // Component-specific
    val productItemImageSize = 80.dp
    val categoryEmojiContainerSize = 56.dp

    // Buttons
    val mainFABSize = 64.dp
    val mainFABIconSize = 32.dp

    val smallFabSize = 48.dp
    val smallFabIconSize = 20.dp

    // Chips
    val categoryFilterChipWidth = 128.dp

    //SearchBar
    val searchBarHeight = 48.dp

    // Screen-specific
    object SignInScreen {
        val logoSize = 120.dp
        val googleButtonHeight = 56.dp
    }


    object Drawer {
        val companyLogoSize = 64.dp
        val drawerCornerRadius = 28.dp
        val userAvatarSize = 40.dp
    }

    object CompanyListView {
        val companyAvatarSize = 56.dp
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
        val stepCircleToTextGap = 20.dp
        val connectorLineWidth = 2.dp
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

    object Config {
        val companyLogoSize = 128.dp
    }

    object ClientCatalog {
        val productItemTextHeight = 96.dp
    }
}
