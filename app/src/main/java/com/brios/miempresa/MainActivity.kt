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
import com.brios.miempresa.core.util.normalizeSheetId
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
        intent ?: return null
        val deeplinkPayload =
            intent.data
                ?.takeIf { it.scheme == DEEPLINK_SCHEME && it.host == DEEPLINK_HOST }
                ?.let { deeplink ->
                    deeplink.getQueryParameter(DEEPLINK_SHEET_ID_PARAM)?.trim() ?: deeplink.toString()
                }
        return extractSheetIdFromIncomingPayload(
            action = intent.action,
            deeplinkPayload = deeplinkPayload,
            sharedTextPayload = intent.getStringExtra(Intent.EXTRA_TEXT),
        )
    }

    companion object {
        private const val DEEPLINK_SCHEME = "miempresa"
        private const val DEEPLINK_HOST = "catalogo"
        private const val DEEPLINK_SHEET_ID_PARAM = "sheetId"
    }
}

internal fun extractSheetIdFromIncomingPayload(
    action: String?,
    deeplinkPayload: String?,
    sharedTextPayload: String?,
): String? {
    val primaryPayload = if (action == Intent.ACTION_SEND) sharedTextPayload else deeplinkPayload
    val fallbackPayload = if (action == Intent.ACTION_SEND) deeplinkPayload else sharedTextPayload
    return normalizeSheetId(primaryPayload ?: fallbackPayload)
}
