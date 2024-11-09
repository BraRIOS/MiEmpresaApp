package com.brios.miempresa.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.brios.miempresa.R
import com.brios.miempresa.ui.dimens.AppDimensions

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchableDropdownWithChips(
    label: String,
    items: List<String>,
    isError: Boolean = false,
    selectedItems: MutableList<String>,
    onItemSelected: (String) -> Unit,
    onItemRemoved: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    // Filtramos los elementos según el input del usuario
    val filteredItems = items.filter { it.contains(searchQuery, ignoreCase = true) }

    val indicatorColor = if (isError)
        OutlinedTextFieldDefaults.colors().errorIndicatorColor
    else if (expanded)
        MaterialTheme.colorScheme.primary
    else
        OutlinedTextFieldDefaults.colors().unfocusedIndicatorColor


    // La columna que contiene la fila de chips y el buscador
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(top = AppDimensions.smallPadding)) {
        // Campo de entrada con chips en FlowRow
        FlowRow(
            modifier = Modifier
                .border(
                    AppDimensions.smallBorderWidth, indicatorColor,
                    if (expanded && filteredItems.isNotEmpty())
                        RoundedCornerShape(topStart = AppDimensions.extraSmallPadding, topEnd = AppDimensions.extraSmallPadding)
                    else
                        RoundedCornerShape(AppDimensions.extraSmallPadding)
                )
                .wrapContentHeight()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.extraSmallPadding),
            verticalArrangement = Arrangement.Center
        ) {
            // Chips de elementos seleccionados
            selectedItems.forEach { item ->
                InputChip(
                    selected = true,
                    onClick = { },
                    label = {
                        Text(
                            text = item,
                            maxLines = 1,
                        )
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.discard),
                            modifier = Modifier
                                .size(AppDimensions.mediumPadding)
                                .clickable { onItemRemoved(item) }
                        )
                    },
                    modifier = Modifier
                        .padding(horizontal = AppDimensions.extraSmallPadding)
                )
            }

            // Campo de búsqueda
            BasicTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    expanded = true
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(AppDimensions.mediumPadding),
                decorationBox = { innerTextField ->
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = label,
                            color = if (isError) OutlinedTextFieldDefaults.colors().errorLabelColor
                            else OutlinedTextFieldDefaults.colors().unfocusedLabelColor,
                        )
                    }
                    innerTextField()
                },
                textStyle = LocalTextStyle.current
            )
        }

        // Mostramos los resultados de búsqueda debajo del campo de búsqueda
        if (expanded && filteredItems.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(AppDimensions.smallBorderWidth, MaterialTheme.colorScheme.primary)
                    .padding(AppDimensions.smallPadding)
            ) {
                filteredItems.forEach { item ->
                    Text(
                        text = item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppDimensions.extraSmallPadding)
                            .clickable {
                                if (!selectedItems.contains(item)) {
                                    onItemSelected(item)
                                }
                                searchQuery = ""
                                expanded = false
                            }
                    )
                }
            }
        }
    }
}




@Preview
@Composable
fun SearchableDropdownWithChipsPreview() {
    val items = listOf("Category 1", "Category 2", "Category 3")

    val selectedItems = remember { mutableListOf<String>() }
    val actualItems = remember { mutableListOf<String>(items[0]) }
    Surface(){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppDimensions.smallPadding),
            verticalArrangement = Arrangement.Center
        ) {
            SearchableDropdownWithChips(
                label = "Categories",
                items = items,
                selectedItems = selectedItems,
                onItemSelected = { selectedItems.add(it) },
                onItemRemoved = { selectedItems.remove(it) }
            )
            SearchableDropdownWithChips(
                label = "Categories error",
                items = items,
                isError = true,
                selectedItems = selectedItems,
                onItemSelected = { selectedItems.add(it) },
                onItemRemoved = { selectedItems.remove(it) }
            )
            SearchableDropdownWithChips(
                label = "Categories",
                items = items,
                selectedItems = actualItems,
                onItemSelected = { selectedItems.add(it) },
                onItemRemoved = { selectedItems.remove(it) }
            )
        }
    }

}