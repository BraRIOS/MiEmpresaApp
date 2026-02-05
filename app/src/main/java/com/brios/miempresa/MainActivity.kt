package com.brios.miempresa

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.navigation.NavHostComposable
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Spike 6: Deeplink handling validation
        val deeplink = intent?.data
        if (deeplink?.scheme == "miempresa") {
            val sheetId = deeplink.getQueryParameter("sheetId")

            // TODO US-026: Implementar árbol de prioridades aquí:
            // 1. Query companyDao.getByPublicSheetId(sheetId)
            // 2. Si null + online → syncPublicSheet()
            // 3. Si isOwned → Route.HomeAdmin
            // 4. Si !isOwned → Route.CatalogoCliente
        }

        enableEdgeToEdge()
        setContent {
            MiEmpresaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    NavHostComposable(applicationContext, navController)
                }
            }
        }
    }
}
