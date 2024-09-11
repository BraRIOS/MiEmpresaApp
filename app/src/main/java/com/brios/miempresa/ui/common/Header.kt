package com.brios.miempresa.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brios.miempresa.ui.theme.Purple40

@Composable
fun Header(
    title: String,
    hasAction: Boolean = false,
    action: () -> Unit = {},
    actionText: String = "",
    actionIcon: ImageVector? = null,
    hasSearch: Boolean = false,
    searchPlaceholder: String = "",
    searchQuery: String = "",
    onQueryChange: (String) -> Unit = {}
){
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Row( horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                color = Purple40
            )
            if (hasAction) {
                Button(
                    onClick = action,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Purple40
                    ),
                ) {
                    if (actionIcon != null) {
                        Icon(
                            imageVector = actionIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(
                        text = actionText,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        if (hasSearch) {
            SearchBar(
                query = searchQuery,
                onQueryChange = onQueryChange,
                modifier = Modifier
                    .padding(vertical = 4.dp),
                placeholderText = searchPlaceholder
            )
        }
    }
}

@Preview
@Composable
fun HeaderPreview () {
    Header(
        title = "Preview",
        hasAction = true,
        actionText = "Agregar Preview",
        actionIcon = Icons.Filled.Add,
        hasSearch = true,
        searchPlaceholder = "Buscar preview ...",
    )
}