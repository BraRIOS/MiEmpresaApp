package com.brios.miempresa.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.brios.miempresa.ui.dimens.AppDimensions
import com.brios.miempresa.ui.theme.PlaceholderColor

@Composable
fun SearchBar(
    query: String, onQueryChange: (String) -> Unit,
    modifier: Modifier, placeholderText: String
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimensions.mediumPadding)
            .clip(MaterialTheme.shapes.medium),
        placeholder = {
            Text(placeholderText, style = MaterialTheme.typography.bodyLarge)
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = Color.Transparent,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedPlaceholderColor = PlaceholderColor,
            focusedPlaceholderColor = PlaceholderColor,
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
    )
}

@Preview
@Composable
fun SearchBarPreview() {
    SearchBar(query = "", onQueryChange = {}, modifier = Modifier, placeholderText = "Search")
}