package com.brios.miempresa.catalog.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppDimensions.mediumPadding,
                    vertical = AppDimensions.mediumPadding,
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
    ) {
        CompanyAvatar(
            companyName = company.name,
            logoUrl = company.logoUrl,
            size = AppDimensions.extraLargeIconSize,
        )
        Text(
            text = company.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        company.specialization
            ?.takeIf { it.isNotBlank() }
            ?.let { specialization ->
                Text(
                    text = specialization,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        )
    }
}
