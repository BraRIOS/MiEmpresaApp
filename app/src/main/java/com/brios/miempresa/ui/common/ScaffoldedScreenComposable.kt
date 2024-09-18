package com.brios.miempresa.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.brios.miempresa.ui.navigation.BottomBar
import com.brios.miempresa.ui.navigation.DrawerComposable
import com.brios.miempresa.ui.navigation.TopBar
import com.brios.miempresa.ui.navigation.TopBarViewModel

@Composable
fun ScaffoldedScreenComposable(
    navController: NavHostController,
    topBarViewModel: TopBarViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    DrawerComposable(
        navController = navController
    ) {
        Scaffold(
            topBar = {
                TopBar(navController, topBarViewModel.topBarTitle)
            },
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
