package com.brios.miempresa.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.brios.miempresa.navigation.BottomBar
import com.brios.miempresa.navigation.DrawerComposable
import com.brios.miempresa.navigation.TopBar
import com.brios.miempresa.navigation.TopBarViewModel
import kotlinx.coroutines.launch

@Composable
fun ScaffoldedScreenComposable(
    navController: NavHostController,
    topBarViewModel: TopBarViewModel = hiltViewModel(),
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    DrawerComposable(
        navController,
        drawerState
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    navController,
                    topBarViewModel.topBarTitle,
                    openDrawer = {scope.launch { drawerState.open() }})
            },
            floatingActionButton = floatingActionButton,
            content = { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(horizontal = 4.dp)
                ) {
                    content()
                }
            },
            bottomBar = {
                BottomBar(navController) {
                    navController.navigate(it)
                }
            }
        )
    }
}
