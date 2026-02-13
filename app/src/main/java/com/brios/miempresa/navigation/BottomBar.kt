package com.brios.miempresa.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray400

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
    val unselectedColor = SlateGray400

    val tabBarItems =
        listOf(
            TabBarItem(
                title = stringResource(id = R.string.home_title),
                icon = Icons.Filled.Inventory2,
                selectedColor = primaryColor,
                unselectedColor = unselectedColor,
            ),
            TabBarItem(
                title = stringResource(id = R.string.categories_title),
                icon = Icons.Filled.Category,
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

    Column {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline,
            thickness = 1.dp
        )
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ) {
            tabBarItems.forEachIndexed { index, tabBarItem ->
                NavigationBarItem(
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) },
                    icon = {
                        Icon(
                            modifier = Modifier.size(AppDimensions.defaultIconSize),
                            imageVector = tabBarItem.icon,
                            contentDescription = tabBarItem.title,
                        )
                    },
                    label = {
                        Text(
                            tabBarItem.title.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = tabBarItem.selectedColor,
                        selectedTextColor = tabBarItem.selectedColor,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = tabBarItem.unselectedColor,
                        unselectedTextColor = tabBarItem.unselectedColor,
                    ),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBottomBar(){
    MiEmpresaTheme {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
            BottomBar(selectedTabIndex = 0, onTabSelected = {})
        }
    }
}
