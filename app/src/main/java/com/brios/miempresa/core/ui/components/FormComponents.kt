package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
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

@Composable
fun SimpleFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    inputModifier: Modifier = Modifier,
    prefix: String? = null,
    isError: Boolean = false,
    errorText: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    contentPadding: PaddingValues = PaddingValues(AppDimensions.mediumPadding),
) {
    Column(
        modifier = modifier.padding(contentPadding),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color =
                if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                },
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = inputModifier
                .fillMaxWidth()
                .padding(vertical = AppDimensions.smallPadding),
            singleLine = singleLine,
            minLines = minLines,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            cursorBrush = SolidColor(if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (prefix != null) {
                        Text(
                            text = prefix,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(end = AppDimensions.extraSmallPadding),
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )

        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = AppDimensions.extraSmallPadding),
            )
        }
    }
}
