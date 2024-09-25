package com.brios.miempresa.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.brios.miempresa.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavHostController,
    title: String,
    openDrawer: () -> Unit = {},
    editProduct: () -> Unit = {},
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    val showBackButton by remember(currentBackStackEntry) {
        derivedStateOf {
            navController.previousBackStackEntry != null && !basePages.contains(navController.currentDestination?.route)
        }
    }

    val isProductScreen = navController.currentDestination?.route == MiEmpresaScreen.Product.name + "/{rowIndex}"

    TopAppBar(
        navigationIcon = {
            if(!isProductScreen) {
                Icon(
                    imageVector = if(!showBackButton) Icons.Filled.Menu else Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = if(!showBackButton) "Show menu" else "Go back",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clickable {
                            if (showBackButton) {
                                navController.popBackStack()
                            } else openDrawer()
                        }
                )
            }
            else{
                Box(modifier = Modifier.padding(start = 8.dp)){
                    FloatingActionButton(
                        onClick = { navController.popBackStack() },
                        shape = CircleShape,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back),
                        )
                    }
                }
            }
        },
        title = {
            Text(text = title)
        },
        actions = {
            if (isProductScreen) {
                Box(modifier = Modifier.padding(end = 8.dp)) {
                    FloatingActionButton(
                        onClick = { editProduct() },
                        shape = CircleShape,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit_product),
                        )
                    }
                }
            }
        },
        colors = if (isProductScreen)
            TopAppBarDefaults.largeTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha =0f),
                scrolledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0f)
            ) else TopAppBarDefaults.largeTopAppBarColors(),
        modifier = Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 1f),
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0f)
                )
            )
        )
    )
}

@Preview
@Composable
fun TopBarPreview() {
    val navController = NavHostController(LocalContext.current)
    navController.currentDestination?.route = MiEmpresaScreen.Product.name + "/{rowIndex}"
    TopBar(navController = navController, title = "Preview")
}