package com.brios.miempresa.catalog.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Store
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.components.EmptyStateView
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme

@Composable
fun MyStoresScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyStateView(
        modifier = modifier,
        icon = Icons.Filled.Store,
        title = stringResource(R.string.visited_stores),
        subtitle = stringResource(R.string.my_stores_placeholder_subtitle),
        actionLabel = stringResource(R.string.go_back),
        onAction = onNavigateBack,
    )
}

@Preview(showBackground = true)
@Composable
private fun MyStoresScreenPreview() {
    MiEmpresaTheme {
        MyStoresScreen(onNavigateBack = {})
    }
}
