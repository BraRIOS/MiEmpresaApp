package com.brios.miempresa.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.brios.miempresa.R
import com.brios.miempresa.ui.dimens.AppDimensions

@Composable
fun CategoryDialog(
    rowIndex: Int? = null,
    category: Category? = null,
    onDismiss: () -> Unit,
    onSave: (Category, (Boolean) -> Unit) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var imageUrl by remember { mutableStateOf(category?.imageUrl ?: "") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(
                    if (category == null) stringResource(R.string.add_category) else stringResource(R.string.edit_category),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(AppDimensions.mediumPadding))
                    .background(Color.White)
                    .padding(top = AppDimensions.smallPadding, bottom = AppDimensions.mediumPadding, start = AppDimensions.mediumPadding, end = AppDimensions.mediumPadding),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(id = R.string.name_label)) },
                    isError = name.isBlank() && showError,
                    singleLine = true
                )
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text(stringResource(id = R.string.image_url_label)) },
                    singleLine = true
                )
                if (showError) {
                    Text(
                        text = stringResource(R.string.category_dialog_error),
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    val updatedCategory = Category(
                        rowIndex ?: category!!.rowIndex,
                        name,
                        -1,
                        imageUrl
                    )
                    onSave(updatedCategory){ success ->
                        if (success) onDismiss()
                    }
                } else {
                    showError = true
                }
            }) {
                Text(stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss
            ) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}

@Preview
@Composable
fun NewCategoryDialogPreview() {
    CategoryDialog(
        onDismiss = {},
        onSave = {_,_->}
    )
}

@Preview
@Composable
fun UpdateCategoryDialog(){
    CategoryDialog(
        category = Category(
            rowIndex = 1,
            name = "Category",
            productQty = 120,
            imageUrl = "https://picsum.photos/200/300"
        ),
        onDismiss = {},
        onSave = {_,_->}
    )
}