package com.brios.miempresa.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brios.miempresa.R

@Composable
fun DeleteDialog(itemName: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        icon = {
            Icon(imageVector = Icons.Outlined.Info,
                contentDescription = stringResource(R.string.info),
                modifier = Modifier.size(50.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.delete_dialog_title) + "\n\"$itemName\"?",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(onClick = {
                onConfirm()
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )

}

@Preview
@Composable
fun DeleteDialogPreview(){
    DeleteDialog(
        itemName = "Producto re loco",
        onDismiss = {},
        onConfirm = {}
    )
}