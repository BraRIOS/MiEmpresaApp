package com.brios.miempresa.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dining
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.brios.miempresa.R

@Composable
fun BottomBar(
    onNavigate: (String) -> Unit,
) {

    val productsTab = TabBarItem(
        title = stringResource(id = R.string.home_title),
        screen = MiEmpresaScreen.Products,
        icon = Icons.Filled.Dining,
        selectedColor = MaterialTheme.colorScheme.primary,
        unselectedColor = LocalContentColor.current.copy(alpha = 0.6f)
        )
    val categoriesTab = TabBarItem(
        title = stringResource(id = R.string.categories_title),
        screen = MiEmpresaScreen.Categories,
        icon = Icons.Filled.GridView,
        selectedColor = MaterialTheme.colorScheme.primary,
        unselectedColor = LocalContentColor.current.copy(alpha = 0.6f)
       )

    val tabBarItems = listOf(productsTab, categoriesTab)

    TabView(tabBarItems, onNavigate)
}

data class TabBarItem(
    val title: String,
    val screen: MiEmpresaScreen,
    val icon: ImageVector,
    val selectedColor: Color,
    val unselectedColor: Color,
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
                        imageVector =  tabBarItem.icon,
                        contentDescription=tabBarItem.title,
                        tint = if (selectedTabIndex == index) tabBarItem.selectedColor else tabBarItem.unselectedColor
                        )
                },
                label = {
                    Text(
                        tabBarItem.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selectedTabIndex == index) tabBarItem.selectedColor else tabBarItem.unselectedColor
                    )
                })
        }
    }
}