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
import com.brios.miempresa.navigation.NavHostComposable
import com.brios.miempresa.ui.theme.MiEmpresaTheme
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
            android.util.Log.d("Spike6", "✅ Deeplink procesado correctamente")
            android.util.Log.d("Spike6", "  → Scheme: ${deeplink.scheme}")
            android.util.Log.d("Spike6", "  → Host: ${deeplink.host}")
            android.util.Log.d("Spike6", "  → SheetId: $sheetId")
            android.util.Log.d("Spike6", "  → URI completo: $deeplink")

            if (sheetId.isNullOrBlank()) {
                android.util.Log.w("Spike6", "⚠️ sheetId es null o vacío")
            }

            // TODO US-026: Implementar árbol de prioridades aquí:
            // 1. Query companyDao.getByPublicSheetId(sheetId)
            // 2. Si null + online → syncPublicSheet()
            // 3. Si isOwned → Route.HomeAdmin
            // 4. Si !isOwned → Route.CatalogoCliente
        } else if (deeplink != null) {
            android.util.Log.w("Spike6", "⚠️ Deeplink con scheme desconocido: ${deeplink.scheme}")
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
