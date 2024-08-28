package com.brios.miempresa.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.brios.miempresa.R

@Composable
fun BottomBar(
    onNavigate: (String) -> Unit,
) {

    val homeTab = TabBarItem(
        title = stringResource(id = R.string.home_title),
        screen = MiEmpresaScreen.Home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home)
    val categoriesTab = TabBarItem(
        title = stringResource(id = R.string.categories_title),
        screen = MiEmpresaScreen.Categories,
        selectedIcon = Icons.Filled.GridView,
        unselectedIcon = Icons.Outlined.GridView)

    val tabBarItems = listOf(homeTab, categoriesTab)

    TabView(tabBarItems, onNavigate)
}

data class TabBarItem(
    val title: String,
    val screen: MiEmpresaScreen,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeAmount: Int? = null
)

@Composable
fun TabView(tabBarItems: List<TabBarItem>, onNavigate: (String) -> Unit) {
    var selectedTabIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    NavigationBar {
        tabBarItems.forEachIndexed { index, tabBarItem ->
            NavigationBarItem(
                selected = selectedTabIndex == index,
                onClick = {
                    selectedTabIndex = index
                    onNavigate(tabBarItem.screen.name)
                },
                icon = {
                    Icon(
                        imageVector = if (index == selectedTabIndex) tabBarItem.selectedIcon else tabBarItem.unselectedIcon,
                        contentDescription=tabBarItem.title
                        )
                },
                label = { Text(tabBarItem.title, style = MaterialTheme.typography.labelLarge ) })
        }
    }
}