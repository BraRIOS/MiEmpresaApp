package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSizeDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme

@Composable
fun DeleteDialog(
    itemName: String,
    title: String?=null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = stringResource(R.string.info),
                modifier = Modifier.size(AppDimensions.largeIconSize),
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        onDismissRequest = onDismiss,
        title = {
            Text(
                title ?: stringResource(R.string.delete),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
            )
        },
        text = {
            Text(
                stringResource(R.string.delete_dialog_title) + "\"$itemName\"?",
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                )
        },
        confirmButton = {
            Button(onClick = {
                onConfirm()
            }) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors().copy(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    )
}

@Preview
@Composable
fun DeleteDialogPreview() {
    MiEmpresaTheme {
        DeleteDialog(
            itemName = "Producto re loco",
            title = stringResource(R.string.delete_product),
            onDismiss = {},
            onConfirm = {},
        )
    }
}
