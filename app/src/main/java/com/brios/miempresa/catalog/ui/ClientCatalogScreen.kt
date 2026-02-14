package com.brios.miempresa.catalog.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.components.EmptyStateView
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme

@Composable
fun ClientCatalogScreen(
    companyId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyStateView(
        modifier = modifier,
        icon = Icons.Filled.Storefront,
        title = stringResource(R.string.client_catalog_placeholder_title),
        subtitle = stringResource(R.string.client_catalog_placeholder_subtitle, companyId),
        actionLabel = stringResource(R.string.go_back),
        onAction = onNavigateBack,
    )
}

@Preview(showBackground = true)
@Composable
private fun ClientCatalogScreenPreview() {
    MiEmpresaTheme {
        ClientCatalogScreen(
            companyId = "sheet-demo",
            onNavigateBack = {},
        )
    }
}
