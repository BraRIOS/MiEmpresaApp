package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme

@Composable
fun QuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minValue: Int = 1,
    maxValue: Int = 999,
) {
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimensions.extraSmallPadding),
    ) {
        OutlinedIconButton(
            onClick = { onQuantityChange((quantity - 1).coerceAtLeast(minValue)) },
            enabled = quantity > minValue,
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = stringResource(R.string.quantity_decrease),
            )
        }
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = AppDimensions.quantitySelectorMinWidth),
        )
        OutlinedIconButton(
            onClick = { onQuantityChange((quantity + 1).coerceAtMost(maxValue)) },
            enabled = quantity < maxValue,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.quantity_increase),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuantitySelectorPreview() {
    MiEmpresaTheme {
        QuantitySelector(
            quantity = 3,
            onQuantityChange = {},
        )
    }
}
