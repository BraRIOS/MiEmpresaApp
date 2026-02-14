package com.brios.miempresa.catalog.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brios.miempresa.R
import com.brios.miempresa.catalog.domain.CatalogAccessError
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme

@Composable
fun DeeplinkErrorScreen(
    error: CatalogAccessError,
    showRetry: Boolean,
    onRetry: () -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(AppDimensions.mediumPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = stringResource(R.string.warning_icon_description),
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(AppDimensions.extraLargeIconSize),
        )

        Spacer(modifier = Modifier.height(AppDimensions.largePadding))

        Text(
            text = error.toMessage(),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(AppDimensions.extraLargePadding))

        if (showRetry) {
            Button(
                onClick = onRetry,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
            ) {
                Text(text = stringResource(R.string.deeplink_retry))
            }

            Spacer(modifier = Modifier.height(AppDimensions.smallPadding))
        }

        OutlinedButton(
            onClick = onGoHome,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
        ) {
            Text(text = stringResource(R.string.deeplink_go_home))
        }
    }
}

@Composable
private fun CatalogAccessError.toMessage(): String {
    return when (this) {
        CatalogAccessError.NO_INTERNET_FIRST_VISIT -> stringResource(R.string.deeplink_error_no_internet_first_visit)
        CatalogAccessError.CATALOG_NOT_FOUND -> stringResource(R.string.deeplink_error_catalog_not_found)
        CatalogAccessError.CATALOG_NOT_AVAILABLE,
        CatalogAccessError.UNKNOWN,
        -> stringResource(R.string.deeplink_error_catalog_not_available)
    }
}

@Preview(showBackground = true)
@Composable
private fun DeeplinkErrorNoInternetPreview() {
    MiEmpresaTheme {
        DeeplinkErrorScreen(
            error = CatalogAccessError.NO_INTERNET_FIRST_VISIT,
            showRetry = false,
            onRetry = {},
            onGoHome = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DeeplinkErrorNotFoundPreview() {
    MiEmpresaTheme {
        DeeplinkErrorScreen(
            error = CatalogAccessError.CATALOG_NOT_FOUND,
            showRetry = true,
            onRetry = {},
            onGoHome = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DeeplinkErrorUnavailablePreview() {
    MiEmpresaTheme {
        DeeplinkErrorScreen(
            error = CatalogAccessError.CATALOG_NOT_AVAILABLE,
            showRetry = true,
            onRetry = {},
            onGoHome = {},
        )
    }
}
