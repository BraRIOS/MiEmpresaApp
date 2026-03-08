package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.brios.miempresa.core.ui.theme.AppDimensions

@Composable
fun InfoCard(
    text: String,
    modifier: Modifier = Modifier
) {
    val infoBg = Color(0xFFEFF6FF) // blue-50
    val infoBorder = Color(0xFFDBEAFE) // blue-100
    val infoIcon = Color(0xFF3B82F6) // blue-500
    val infoText = Color(0xFF2563EB) // blue-600

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = infoBg,
                shape = RoundedCornerShape(AppDimensions.smallCornerRadius),
            )
            .border(
                AppDimensions.smallBorderWidth,
                infoBorder,
                RoundedCornerShape(AppDimensions.smallCornerRadius),
            )
            .padding(AppDimensions.mediumSmallPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = infoIcon,
            modifier = Modifier.size(AppDimensions.defaultSmallIconSize),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = infoText,
        )
    }
}
