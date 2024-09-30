package com.brios.miempresa.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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
        .padding(top = 8.dp)) {
        // Campo de entrada con chips en FlowRow
        FlowRow(
            modifier = Modifier
                .border(
                    1.dp, indicatorColor,
                    if (expanded && filteredItems.isNotEmpty())
                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                    else
                        RoundedCornerShape(4.dp)
                )
                .wrapContentHeight()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
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
                            contentDescription = "Remove",
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onItemRemoved(item) }
                        )
                    },
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
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
                    .padding(16.dp),
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
                    .background(Color.White)
                    .border(1.dp, MaterialTheme.colorScheme.primary)
                    .padding(8.dp)
            ) {
                filteredItems.forEach { item ->
                    Text(
                        text = item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
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
    Surface(){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
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
        }
    }

}