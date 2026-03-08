package com.brios.miempresa.catalog.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStoreSheet(
    sheetId: String,
    isSubmitting: Boolean,
    onSheetIdChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDimensions.mediumPadding)
                    .padding(bottom = AppDimensions.largePadding),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
        ) {
            Text(
                text = stringResource(R.string.my_stores_add_store_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            OutlinedTextField(
                value = sheetId,
                onValueChange = onSheetIdChange,
                label = { Text(stringResource(R.string.my_stores_code_label)) },
                supportingText = { Text(stringResource(R.string.my_stores_code_helper)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppDimensions.smallCornerRadius),
                singleLine = true,
                enabled = !isSubmitting,
            )

            Spacer(modifier = Modifier.height(AppDimensions.extraSmallPadding))

            Button(
                onClick = onSubmit,
                enabled = !isSubmitting && sheetId.isNotBlank(),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(AppDimensions.defaultSmallIconSize),
                        strokeWidth = AppDimensions.smallBorderWidth,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.my_stores_add_store_button),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
