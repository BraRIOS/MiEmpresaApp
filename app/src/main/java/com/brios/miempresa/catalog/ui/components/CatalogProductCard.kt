package com.brios.miempresa.catalog.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.SubcomposeAsyncImage
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.SlateGray200
import com.brios.miempresa.products.data.ProductEntity
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CatalogProductCard(
    product: ProductEntity,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR")) }

    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
        border = BorderStroke(AppDimensions.smallBorderWidth, SlateGray200),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(AppDimensions.smallPadding),
            ) {
                SubcomposeAsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(AppDimensions.mediumCornerRadius)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        PlaceholderImage()
                    },
                    error = {
                        PlaceholderImage()
                    },
                )

                IconButton(
                    onClick = onAddToCart,
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .size(AppDimensions.smallFabSize)
                            .shadow(AppDimensions.smallBorderWidth, CircleShape)
                            .background(MaterialTheme.colorScheme.surface, CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Column(
                modifier =
                    Modifier
                        .padding(
                            start = AppDimensions.mediumSmallPadding,
                            end = AppDimensions.mediumSmallPadding,
                            bottom = AppDimensions.mediumSmallPadding,
                        ),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.extraSmallPadding),
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                )
                Text(
                    text = currencyFormatter.format(product.price),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun PlaceholderImage() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(AppDimensions.mediumCornerRadius)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
