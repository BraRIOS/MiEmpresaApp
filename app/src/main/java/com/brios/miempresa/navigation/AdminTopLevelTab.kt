package com.brios.miempresa.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.ui.graphics.vector.ImageVector
import com.brios.miempresa.R

enum class AdminTopLevelTab(
    @StringRes val titleRes: Int,
    val icon: ImageVector,
) {
    Products(
        titleRes = R.string.home_title,
        icon = Icons.Filled.Inventory2,
    ),
    Categories(
        titleRes = R.string.categories_title,
        icon = Icons.Filled.Category,
    ),
    Company(
        titleRes = R.string.company_tab_title,
        icon = Icons.Filled.Domain,
    ),
}
