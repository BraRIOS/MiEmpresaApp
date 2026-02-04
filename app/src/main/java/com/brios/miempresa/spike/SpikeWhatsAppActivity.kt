package com.brios.miempresa.spike

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brios.miempresa.ui.theme.MiEmpresaTheme
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLEncoder

@AndroidEntryPoint
class SpikeWhatsAppActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MiEmpresaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    WhatsAppTestScreen(
                        onTestArgentina = { testWhatsAppIntent("+5491123456789") },
                        onTestUSA = { testWhatsAppIntent("+12025551234") },
                        onTestEspana = { testWhatsAppIntent("+34612345678") },
                    )
                }
            }
        }
    }

    private fun testWhatsAppIntent(number: String) {
        val mensaje =
            """
            ¡Hola! Quiero hacer este pedido:

            - Malbec Reserva x2 - ${'$'}12500
            - Aceite oliva x1 - €35.50

            Total: ${'$'}28500

            Enviado desde MiEmpresa 🛒
            """.trimIndent()

        val encodedText = URLEncoder.encode(mensaje, "UTF-8")
        val intent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://wa.me/$number?text=$encodedText"),
            )

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "WhatsApp no instalado", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun WhatsAppTestScreen(
    onTestArgentina: () -> Unit,
    onTestUSA: () -> Unit,
    onTestEspana: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Spike 7: WhatsApp Intent Test",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "Validar encoding: ñ, $, €, acentos, emoji 🛒",
            style = MaterialTheme.typography.bodyMedium,
        )

        Button(onClick = onTestArgentina) {
            Text("Test Argentina +54")
        }

        Button(onClick = onTestUSA) {
            Text("Test USA +1")
        }

        Button(onClick = onTestEspana) {
            Text("Test España +34")
        }
    }
}
