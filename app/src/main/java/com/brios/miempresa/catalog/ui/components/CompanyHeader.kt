package com.brios.miempresa.catalog.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.brios.miempresa.core.data.local.entities.Company
import com.brios.miempresa.core.ui.components.CompanyAvatar
import com.brios.miempresa.core.ui.theme.AppDimensions

@Composable
fun CompanyHeader(
    company: Company,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding, Alignment.CenterVertically),
    ) {
        CompanyAvatar(
            companyName = company.name,
            logoUrl = company.logoUrl,
            size = AppDimensions.catalogCompanyLogoSize,
        )
        Text(
            text = company.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        company.specialization
            ?.takeIf { it.isNotBlank() }
            ?.let { specialization ->
                Text(
                    text = specialization,
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        company.address
            ?.takeIf { it.isNotBlank() }
            ?.let { address ->
                CompanyMetadataRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(AppDimensions.smallIconSize),
                        )
                    },
                    text = address,
                )
            }
        company.businessHours
            ?.takeIf { it.isNotBlank() }
            ?.let { businessHours ->
                CompanyMetadataRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(AppDimensions.smallIconSize),
                        )
                    },
                    text = businessHours,
                )
            }
    }
}

@Composable
private fun CompanyMetadataRow(
    icon: @Composable () -> Unit,
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppDimensions.extraSmallPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
