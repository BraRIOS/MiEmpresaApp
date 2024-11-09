package com.brios.miempresa.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.brios.miempresa.R
import com.brios.miempresa.ui.dimens.AppDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavHostController,
    title: String,
    openDrawer: () -> Unit = {},
    editProduct: () -> Unit = {},
    deleteProduct: () -> Unit = {}
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
                    contentDescription = if(!showBackButton) stringResource(id = R.string.show_menu)
                        else stringResource(id = R.string.go_back),
                    modifier = Modifier
                        .padding(start = AppDimensions.smallPadding, end = AppDimensions.mediumPadding)
                        .clickable {
                            if (showBackButton) {
                                navController.popBackStack()
                            } else openDrawer()
                        }
                )
            }
            else{
                Box(modifier = Modifier.padding(start = AppDimensions.smallPadding)){
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
                Row(
                    modifier = Modifier.padding(end = AppDimensions.smallPadding),
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FloatingActionButton(
                        onClick = { editProduct() },
                        shape = CircleShape,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit_product),
                        )
                    }
                    FloatingActionButton(
                        onClick = { deleteProduct() },
                        shape = CircleShape,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.delete_product),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        colors =  if (isProductScreen)
            TopAppBarDefaults.largeTopAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            )
            else TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
        modifier = Modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 1f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0f)
                    )
                )
            )
            .padding(top = if (isProductScreen) AppDimensions.mediumPadding else 0.dp)
    )
}

@Preview
@Composable
fun TopBarPreview() {
    val navController = NavHostController(LocalContext.current)
    navController.currentDestination?.route = MiEmpresaScreen.Product.name + "/{rowIndex}"
    TopBar(navController = navController, title = "Preview")
}