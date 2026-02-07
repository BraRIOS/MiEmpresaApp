package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme

@Composable
fun CompanyAvatar(
    companyName: String,
    modifier: Modifier = Modifier,
    logoUrl: String? = null,
    size: Dp = AppDimensions.largeIconSize,
) {
    if (logoUrl != null) {
        AsyncImage(
            model = logoUrl,
            contentDescription = stringResource(R.string.company_logo, companyName),
            modifier =
                modifier
                    .size(size)
                    .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier =
                modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text =
                    companyName
                        .split(" ")
                        .filter { it.isNotBlank() }
                        .take(2)
                        .joinToString("") { it.first().uppercase() }
                        .ifEmpty { "?" },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompanyAvatarWithLogoPreview() {
    MiEmpresaTheme {
        CompanyAvatar(
            companyName = "Mi Empresa",
            logoUrl = "https://example.com/logo.png",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CompanyAvatarInitialsPreview() {
    MiEmpresaTheme {
        CompanyAvatar(
            companyName = "Mi Empresa",
        )
    }
}
