package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray300

enum class SearchBarVariant { Outlined, Filled }

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "",
    variant: SearchBarVariant = SearchBarVariant.Outlined,
) {
    val shape = remember {
        RoundedCornerShape(
            if (variant == SearchBarVariant.Filled) {
                AppDimensions.largeCornerRadius
            } else {
                AppDimensions.mediumCornerRadius
            },
        )
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val backgroundColor = when (variant) {
        SearchBarVariant.Outlined -> MaterialTheme.colorScheme.surfaceContainerLowest
        SearchBarVariant.Filled -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val iconTint = when (variant) {
        SearchBarVariant.Outlined -> if (isFocused) MaterialTheme.colorScheme.primary else SlateGray300
        SearchBarVariant.Filled -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (variant == SearchBarVariant.Outlined) {
                    Modifier.padding(horizontal = AppDimensions.mediumPadding)
                } else {
                    Modifier
                },
            )
            .height(48.dp)
            .then(
                when (variant) {
                    SearchBarVariant.Outlined -> {
                        val borderColor =
                            if (isFocused) MaterialTheme.colorScheme.primary else SlateGray300
                        Modifier
                            .shadow(1.dp, shape)
                            .border(1.dp, borderColor, shape)
                    }
                    SearchBarVariant.Filled -> Modifier
                },
            )
            .background(backgroundColor, shape),
        textStyle = MaterialTheme.typography.bodyMedium,
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        interactionSource = interactionSource,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppDimensions.mediumPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = iconTint,
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholderText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun SearchBarOutlinedPreview() {
    MiEmpresaTheme {
        SearchBar(
            query = "",
            onQueryChange = {},
            placeholderText = "Buscar productos...",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchBarFilledPreview() {
    MiEmpresaTheme {
        SearchBar(
            query = "",
            onQueryChange = {},
            placeholderText = "Buscar emojis...",
            variant = SearchBarVariant.Filled,
        )
    }
}
