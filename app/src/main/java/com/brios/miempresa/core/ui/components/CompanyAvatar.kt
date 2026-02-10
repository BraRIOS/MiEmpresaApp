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
import coil.compose.SubcomposeAsyncImage
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
    val initials = companyName
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

    if (!logoUrl.isNullOrEmpty()) {
        SubcomposeAsyncImage(
            model = logoUrl,
            contentDescription = stringResource(R.string.company_logo, companyName),
            modifier =
                modifier
                    .size(size)
                    .clip(CircleShape),
            contentScale = ContentScale.Crop,
            loading = { InitialsAvatar(initials, size) },
            error = { InitialsAvatar(initials, size) },
        )
    } else {
        InitialsAvatar(initials, size, modifier)
    }
}

@Composable
private fun InitialsAvatar(
    initials: String,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CompanyAvatarWithLogoPreview() {
    MiEmpresaTheme {
        CompanyAvatar(
            companyName = "Mi Empresa",
            logoUrl = "https://avatars.githubusercontent.com/u/81847?v=4",
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
