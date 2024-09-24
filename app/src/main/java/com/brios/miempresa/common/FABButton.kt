package com.brios.miempresa.common

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun FABButton(
    action: () -> Unit,
    actionText: String,
    actionIcon: ImageVector,
) {
    FloatingActionButton(
        onClick = action,
    ){
        Icon(imageVector = actionIcon, contentDescription = actionText)
    }
}