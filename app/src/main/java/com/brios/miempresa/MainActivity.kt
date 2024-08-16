package com.brios.miempresa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.brios.miempresa.home.PreviewHomePage
import com.brios.miempresa.theme.MiEmpresaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiEmpresaTheme {
                PreviewHomePage()
            }
        }
    }
}