package com.brios.miempresa.spike

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.brios.miempresa.domain.QrCodeGenerator
import com.brios.miempresa.domain.QrCodeResult
import com.brios.miempresa.ui.theme.MiEmpresaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * ⚠️ DUMMY SPIKE ACTIVITY - DELETE AFTER VALIDATION
 *
 * Tests:
 * - QR generation from deeplinks
 * - Compose Bitmap display
 * - Google Lens / Camera scanning
 * - Error handling
 *
 * Validation criteria:
 * 1. Generate 5 different QRs without errors
 * 2. Scan with Google Lens → Opens MiEmpresa app
 * 3. Compatible API 21+ (test on emulator/device)
 */
@AndroidEntryPoint
class SpikeQrCodeActivity : ComponentActivity() {
    @Inject
    lateinit var qrCodeGenerator: QrCodeGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiEmpresaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    QrCodeTestScreen(qrCodeGenerator)
                }
            }
        }
    }
}

@Composable
fun QrCodeTestScreen(qrCodeGenerator: QrCodeGenerator) {
    var selectedTest by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<QrCodeResult?>(null) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Spike S8: QR Code Generator",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "⚠️ DUMMY TEST ACTIVITY",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Test Cases
        TestCaseButton(
            title = "Test 1: Valid Deeplink",
            deeplink = "miempresa://catalogo?sheetId=test-sheet-123",
            onClick = {
                selectedTest = "Test 1"
                (result as? QrCodeResult.Success)?.bitmap?.recycle()
                result = qrCodeGenerator.generate("miempresa://catalogo?sheetId=test-sheet-123")
            },
        )

        TestCaseButton(
            title = "Test 2: Long URL",
            deeplink = "miempresa://catalogo?sheetId=1A2B3C4D5E6F7G8H9I0J_very_long_id_to_test_encoding",
            onClick = {
                selectedTest = "Test 2"
                (result as? QrCodeResult.Success)?.bitmap?.recycle()
                result = qrCodeGenerator.generate("miempresa://catalogo?sheetId=1A2B3C4D5E6F7G8H9I0J_very_long_id_to_test_encoding")
            },
        )

        TestCaseButton(
            title = "Test 3: Special Characters",
            deeplink = "miempresa://catalogo?sheetId=abc-123_XYZ&param=value",
            onClick = {
                selectedTest = "Test 3"
                (result as? QrCodeResult.Success)?.bitmap?.recycle()
                result = qrCodeGenerator.generate("miempresa://catalogo?sheetId=abc-123_XYZ&param=value")
            },
        )

        TestCaseButton(
            title = "Test 4: Small Size (256px)",
            deeplink = "miempresa://catalogo?sheetId=small-qr",
            onClick = {
                selectedTest = "Test 4"
                (result as? QrCodeResult.Success)?.bitmap?.recycle()
                result = qrCodeGenerator.generate("miempresa://catalogo?sheetId=small-qr", sizePx = 256)
            },
        )

        TestCaseButton(
            title = "Test 5: Large Size (1024px)",
            deeplink = "miempresa://catalogo?sheetId=large-qr",
            onClick = {
                selectedTest = "Test 5"
                (result as? QrCodeResult.Success)?.bitmap?.recycle()
                result = qrCodeGenerator.generate("miempresa://catalogo?sheetId=large-qr", sizePx = 1024)
            },
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Result Display
        selectedTest?.let {
            Text(
                text = "Running: $it",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        result?.let { qrResult ->
            when (qrResult) {
                is QrCodeResult.Success -> {
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "✅ QR Generated Successfully",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Image(
                                bitmap = qrResult.bitmap.asImageBitmap(),
                                contentDescription = "Generated QR Code",
                                modifier = Modifier.size(300.dp),
                            )
                            DisposableEffect(qrResult.bitmap) {
                                onDispose {
                                    qrResult.bitmap.recycle()
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "📱 Scan with Google Lens or Camera app",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                            )
                            Text(
                                text = "Expected: Opens MiEmpresa app",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                            )
                        }
                    }
                }
                is QrCodeResult.Error -> {
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "❌ Generation Failed",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFC62828),
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = qrResult.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFC62828),
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Instructions
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📋 Validation Checklist",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("1. Run all 5 tests → All should generate successfully")
                Text("2. Scan QR with Google Lens → Should open app")
                Text("3. Verify QR is clear and scannable")
                Text("4. Test on API 21 emulator (if available)")
            }
        }
    }
}

@Composable
fun TestCaseButton(
    title: String,
    deeplink: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = deeplink,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
            )
        }
    }
}
