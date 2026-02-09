package com.brios.miempresa.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions

data class TabBarItem(
    val title: String,
    val icon: ImageVector,
    val selectedColor: Color,
    val unselectedColor: Color,
)

@Composable
fun BottomBar(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val unselectedColor = LocalContentColor.current.copy(alpha = 0.6f)

    val tabBarItems =
        listOf(
            TabBarItem(
                title = stringResource(id = R.string.home_title),
                icon = Icons.Filled.Home,
                selectedColor = primaryColor,
                unselectedColor = unselectedColor,
            ),
            TabBarItem(
                title = stringResource(id = R.string.categories_title),
                icon = Icons.Filled.GridView,
                selectedColor = primaryColor,
                unselectedColor = unselectedColor,
            ),
            TabBarItem(
                title = stringResource(id = R.string.config_title),
                icon = Icons.Filled.Settings,
                selectedColor = primaryColor,
                unselectedColor = unselectedColor,
            ),
        )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        tabBarItems.forEachIndexed { index, tabBarItem ->
            NavigationBarItem(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        modifier = Modifier.size(AppDimensions.mediumIconSize),
                        imageVector = tabBarItem.icon,
                        contentDescription = tabBarItem.title,
                    )
                },
                label = {
                    Text(
                        tabBarItem.title,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = tabBarItem.selectedColor,
                        selectedTextColor = tabBarItem.selectedColor,
                        indicatorColor = MaterialTheme.colorScheme.surfaceContainer,
                        unselectedIconColor = tabBarItem.unselectedColor,
                        unselectedTextColor = tabBarItem.unselectedColor,
                    ),
            )
        }
    }
}
