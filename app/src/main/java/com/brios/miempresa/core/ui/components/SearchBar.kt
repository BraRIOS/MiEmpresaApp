package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray100
import com.brios.miempresa.core.ui.theme.SlateGray200

enum class SearchBarVariant {
    Outlined,
    Filled,
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "",
    variant: SearchBarVariant = SearchBarVariant.Outlined,
) {
    val shape =
        remember {
            when (variant) {
                SearchBarVariant.Outlined -> RoundedCornerShape(AppDimensions.mediumCornerRadius)
                SearchBarVariant.Filled -> RoundedCornerShape(AppDimensions.largeCornerRadius)
            }
        }

    val containerColor =
        when (variant) {
            SearchBarVariant.Outlined -> MaterialTheme.colorScheme.surfaceContainerLowest
            SearchBarVariant.Filled -> SlateGray100.copy(alpha = 0.8f)
        }

    val borderModifier =
        when (variant) {
            SearchBarVariant.Outlined ->
                Modifier
                    .shadow(1.dp, shape)
                    .border(1.dp, SlateGray200, shape)
            SearchBarVariant.Filled -> Modifier
        }

    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimensions.mediumPadding)
                .height(48.dp)
                .then(borderModifier)
                .clip(shape),
        placeholder = {
            Text(placeholderText, style = MaterialTheme.typography.bodyMedium)
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = containerColor,
                unfocusedContainerColor = containerColor,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
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
            variant = SearchBarVariant.Outlined,
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
            placeholderText = "Buscar categorías...",
            variant = SearchBarVariant.Filled,
        )
    }
}
