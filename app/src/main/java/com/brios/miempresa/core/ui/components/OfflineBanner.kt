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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brios.miempresa.R
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
                .background(OfflineBannerYellow)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.WifiOff,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = OnOfflineBannerYellow,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.offline_banner_message),
            style = MaterialTheme.typography.bodySmall,
            color = OnOfflineBannerYellow,
        )
        if (onDismiss != null) {
            Spacer(modifier = Modifier.weight(1f))
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

@Preview
@Composable
private fun OfflineBannerPreview() {
    MiEmpresaTheme {
        OfflineBanner(onDismiss = {})
    }
}
