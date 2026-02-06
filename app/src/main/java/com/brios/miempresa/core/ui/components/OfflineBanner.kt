package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.OfflineBannerYellow
import com.brios.miempresa.core.ui.theme.OnOfflineBannerYellow

@Composable
fun OfflineBanner(
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .semantics { liveRegion = LiveRegionMode.Polite }
                .background(OfflineBannerYellow)
                .padding(
                    horizontal = AppDimensions.mediumSmallPadding,
                    vertical = AppDimensions.smallPadding,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.WifiOff,
            contentDescription = stringResource(R.string.offline_banner_message),
            modifier = Modifier.size(AppDimensions.smallIconSize),
            tint = OnOfflineBannerYellow,
        )
        Spacer(modifier = Modifier.width(AppDimensions.smallPadding))
        Text(
            text = stringResource(R.string.offline_banner_message),
            style = MaterialTheme.typography.bodySmall,
            color = OnOfflineBannerYellow,
            modifier = Modifier.weight(1f),
        )
        if (onDismiss != null) {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.dismiss),
                    tint = OnOfflineBannerYellow,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OfflineBannerPreview() {
    MiEmpresaTheme {
        OfflineBanner(onDismiss = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun OfflineBannerNoDismissPreview() {
    MiEmpresaTheme {
        OfflineBanner()
    }
}
