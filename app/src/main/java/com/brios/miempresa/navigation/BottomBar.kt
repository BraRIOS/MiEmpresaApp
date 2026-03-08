package com.brios.miempresa.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray400

@Composable
fun BottomBar(
    selectedTab: AdminTopLevelTab,
    onTabSelected: (AdminTopLevelTab) -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val unselectedColor = SlateGray400

    Column {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline,
            thickness = 1.dp
        )
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ) {
            AdminTopLevelTab.entries.forEach { tab ->
                val title = stringResource(id = tab.titleRes)
                NavigationBarItem(
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        Icon(
                            modifier = Modifier.size(AppDimensions.defaultIconSize),
                            imageVector = tab.icon,
                            contentDescription = title,
                        )
                    },
                    label = {
                        Text(
                            title.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = primaryColor,
                        selectedTextColor = primaryColor,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = unselectedColor,
                        unselectedTextColor = unselectedColor,
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
            BottomBar(selectedTab = AdminTopLevelTab.Products, onTabSelected = {})
        }
    }
}
