package com.brios.miempresa.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavHostController,
    title: String,
    openDrawer: () -> Unit = {},
    actions: @Composable () -> Unit = {},
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    val showBackButton by remember(currentBackStackEntry) {
        derivedStateOf {
            navController.previousBackStackEntry != null && !basePages.contains(navController.currentDestination?.route)
        }
    }

    CenterAlignedTopAppBar(
        navigationIcon = {
            Icon(
                imageVector = if (!showBackButton) Icons.Filled.Menu else Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription =
                    if (!showBackButton) {
                        stringResource(id = R.string.show_menu)
                    } else {
                        stringResource(id = R.string.go_back)
                    },
                modifier =
                    Modifier
                        .padding(start = AppDimensions.smallPadding, end = AppDimensions.mediumPadding)
                        .clickable {
                            if (showBackButton) {
                                navController.popBackStack()
                            } else {
                                openDrawer()
                            }
                        },
            )
        },
        title = {
            Text(text = title)
        },
        actions = { actions() },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewTopBar(){
    MiEmpresaTheme {
        TopBar(navController = NavHostController(LocalContext.current), title = "Home")
    }
}
