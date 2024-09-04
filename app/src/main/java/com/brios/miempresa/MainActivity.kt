package com.brios.miempresa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.brios.miempresa.navigation.BottomBar
import com.brios.miempresa.navigation.NavHostComposable
import com.brios.miempresa.navigation.TopBar
import com.brios.miempresa.navigation.TopBarViewModel
import com.brios.miempresa.ui.theme.MiEmpresaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: TopBarViewModel = viewModel()
            val navController = rememberNavController()
            MiEmpresaTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = {
                            TopBar(navController, viewModel.topBarTitle)
                        },
                        content = {paddingValues ->
                            NavHostComposable(paddingValues, navController, viewModel)
                        },
                        bottomBar = {
                            BottomBar{
                                navController.navigate(it)
                            }
                        }
                    )
                }
            }
        }
    }
}