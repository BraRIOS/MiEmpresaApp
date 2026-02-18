package com.brios.miempresa.catalog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.products.data.ProductEntity
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CatalogProductItem(
    product: ProductEntity,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR")) }

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(),
        color = Color.Transparent,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                SubcomposeAsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxSize()
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
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(AppDimensions.extraSmallPadding)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppDimensions.mediumPadding)
                    .padding(horizontal = AppDimensions.extraSmallPadding)
                    .height(AppDimensions.ClientCatalog.productItemTextHeight),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.extraSmallPadding),
            ) {
                Text(
                    text =
                        if (product.hidePrice) {
                            stringResource(R.string.price_consult)
                        } else {
                            currencyFormatter.format(product.price)
                        },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground
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
                .fillMaxSize()
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

@Preview(showBackground = true)
@Composable
private fun CatalogProductItemPreview() {
    val sampleProduct = ProductEntity(
        id = "1",
        name = "Product Name with a very long title to test max lines behavior and text overflow in the card component",
        price = 1200.0,
        companyId = "1",
        imageUrl = null,
        categoryName = "Category"
    )
    MiEmpresaTheme {
        Box(modifier = Modifier.padding(16.dp).fillMaxHeight()) {
            CatalogProductItem(
                product = sampleProduct,
                onClick = {},
                onAddToCart = {},
                modifier = Modifier.padding(AppDimensions.mediumPadding)
            )
        }
    }
}
