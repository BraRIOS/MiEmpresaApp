package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray200
import com.brios.miempresa.core.ui.theme.SlateGray400
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OrderProductListItem(
    name: String,
    price: Double,
    quantity: Int,
    imageUrl: String?,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    isCard: Boolean = true
) {
    if (isCard) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
            shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
            border = BorderStroke(1.dp, SlateGray200),
        ) {
            OrderProductContent(
                name = name,
                price = price,
                quantity = quantity,
                imageUrl = imageUrl,
                onQuantityChange = onQuantityChange,
                onRemove = onRemove
            )
        }
    } else {
        OrderProductContent(
            name = name,
            price = price,
            quantity = quantity,
            imageUrl = imageUrl,
            onQuantityChange = onQuantityChange,
            onRemove = onRemove,
            modifier = modifier
        )
    }
}

@Composable
private fun OrderProductContent(
    name: String,
    price: Double,
    quantity: Int,
    imageUrl: String?,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))

    Row(
        modifier = modifier
            .padding(AppDimensions.mediumSmallPadding)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        // Image
        BoxImage(imageUrl = imageUrl)

        Spacer(modifier = Modifier.width(AppDimensions.mediumPadding))

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .height(80.dp), // Match image height for alignment
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row: Name and Delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Bottom row: Price and Quantity selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currencyFormat.format(price * quantity),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${currencyFormat.format(price)} c/u",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateGray400,
                    )
                }

                QuantitySelector(
                    quantity = quantity,
                    onQuantityChange = onQuantityChange,
                    modifier = Modifier.height(32.dp)
                )
            }
        }
    }
}

@Composable
private fun BoxImage(imageUrl: String?) {
    val shape = RoundedCornerShape(AppDimensions.smallCornerRadius)
    val modifier = Modifier
        .size(80.dp)
        .clip(shape)
        .background(SlateGray200.copy(alpha = 0.3f))

    if (imageUrl != null) {
        Image(
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build()
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        // Placeholder
        androidx.compose.foundation.layout.Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            // You might want a placeholder icon here
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderProductListItemPreview() {
    MiEmpresaTheme {
        OrderProductListItem(
            name = "Café con Leche",
            price = 550.0,
            quantity = 2,
            imageUrl = null,
            onQuantityChange = {},
            onRemove = {},
            modifier = Modifier.padding(16.dp),
            isCard = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderProductListTilePreview() {
    MiEmpresaTheme {
        OrderProductListItem(
            name = "Café con Leche",
            price = 550.0,
            quantity = 2,
            imageUrl = null,
            onQuantityChange = {},
            onRemove = {},
            modifier = Modifier.padding(16.dp),
            isCard = false
        )
    }
}
