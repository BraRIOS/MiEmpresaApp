package com.brios.miempresa

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.navigation.NavHostComposable
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private var pendingDeeplinkSheetId: String? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingDeeplinkSheetId = extractSheetId(intent)

        enableEdgeToEdge()
        setContent {
            MiEmpresaTheme {
                Surface(
                    modifier =
                        Modifier
                            .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    NavHostComposable(
                        applicationContext = applicationContext,
                        navController = navController,
                        pendingDeeplinkSheetId = pendingDeeplinkSheetId,
                        onDeeplinkConsumed = { consumedSheetId ->
                            if (pendingDeeplinkSheetId == consumedSheetId) {
                                pendingDeeplinkSheetId = null
                            }
                        },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDeeplinkSheetId = extractSheetId(intent)
    }

    private fun extractSheetId(intent: Intent?): String? {
        val deeplink = intent?.data ?: return null
        if (deeplink.scheme != DEEPLINK_SCHEME || deeplink.host != DEEPLINK_HOST) return null
        val rawSheetId = deeplink.getQueryParameter(DEEPLINK_SHEET_ID_PARAM)?.trim()
        return normalizeSheetId(rawSheetId)
    }

    private fun normalizeSheetId(rawValue: String?): String? {
        val normalized = rawValue?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val match = SHEETS_URL_REGEX.find(normalized)
        return match?.groupValues?.getOrNull(1) ?: normalized
    }

    companion object {
        private const val DEEPLINK_SCHEME = "miempresa"
        private const val DEEPLINK_HOST = "catalogo"
        private const val DEEPLINK_SHEET_ID_PARAM = "sheetId"
        private val SHEETS_URL_REGEX = Regex("""/spreadsheets/d/([a-zA-Z0-9-_]+)""")
    }
}
