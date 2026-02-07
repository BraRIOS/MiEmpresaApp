package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.RequiredRed
import com.brios.miempresa.core.ui.theme.SlateGray200
import com.brios.miempresa.core.ui.theme.SlateGray400
import com.brios.miempresa.core.ui.theme.SlateGray500

val FormInputShape = RoundedCornerShape(AppDimensions.mediumCornerRadius)

@Composable
fun FormLabel(
    text: String,
    required: Boolean = false,
) {
    Text(
        text =
            buildAnnotatedString {
                append(text)
                if (required) {
                    append(" ")
                    withStyle(SpanStyle(color = RequiredRed, fontSize = 14.sp)) {
                        append("*")
                    }
                }
            },
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = SlateGray500,
        modifier = Modifier.padding(start = AppDimensions.extraSmallPadding, bottom = AppDimensions.smallPadding),
    )
}

@Composable
fun FormFieldGroup(
    label: String,
    required: Boolean = false,
    content: @Composable () -> Unit,
) {
    Column {
        FormLabel(text = label, required = required)
        content()
    }
}

@Composable
fun FormOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val iconTint = if (isFocused) MaterialTheme.colorScheme.primary else SlateGray400

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(placeholder, color = SlateGray400, style = MaterialTheme.typography.bodyLarge)
        },
        leadingIcon = {
            Icon(leadingIcon, contentDescription = null, tint = iconTint)
        },
        isError = isError,
        supportingText =
            supportingText?.let {
                { Text(it) }
            },
        singleLine = true,
        shape = FormInputShape,
        interactionSource = interactionSource,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors =
            OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = SlateGray200,
                unfocusedLeadingIconColor = SlateGray400,
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedContainerColor = MaterialTheme.colorScheme.background,
            ),
        modifier = modifier.fillMaxWidth(),
    )
}
