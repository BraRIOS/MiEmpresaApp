package com.brios.miempresa.initializer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import com.brios.miempresa.components.MessageWithIcon

@Composable
fun ErrorView(message:String) {
    MessageWithIcon(message = message, icon = Icons.Filled.Warning)
}