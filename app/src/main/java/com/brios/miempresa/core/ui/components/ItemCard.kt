package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray200
import com.brios.miempresa.core.ui.theme.SlateGray400
import com.brios.miempresa.core.ui.theme.SlateGray700
import com.brios.miempresa.core.ui.theme.VisibilityActiveBlue

enum class ItemType {
    PRODUCT,
    CATEGORY,
}

@Composable
fun ItemCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    emojiIcon: String? = null,
    productCount: Int? = null,
    isPublic: Boolean? = null,
    itemType: ItemType = ItemType.PRODUCT,
    badge: (@Composable () -> Unit)? = null,
    onToggleVisibility: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    if (itemType == ItemType.CATEGORY) {
        CategoryItem(
            title = title,
            subtitle = subtitle,
            emojiIcon = emojiIcon,
            productCount = productCount,
            badge = badge,
            isPublic = isPublic,
            onToggleVisibility = onToggleVisibility,
            onDelete = onDelete,
            onClick = onClick,
            modifier = modifier
        )
    } else {
        ProductItem(
            title = title,
            price = subtitle,
            imageUrl = imageUrl,
            isPublic = isPublic,
            badge = badge,
            onToggleVisibility = onToggleVisibility,
            onDelete = onDelete,
            onClick = onClick,
            modifier = modifier
        )
    }
}

@Composable
fun ProductItem(
    title: String,
    price: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    isPublic: Boolean? = null,
    badge: (@Composable () -> Unit)? = null,
    onToggleVisibility: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    val imageShape = remember { RoundedCornerShape(AppDimensions.smallCornerRadius) }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column {
            Row(
                modifier =
                    Modifier
                        .padding(AppDimensions.mediumPadding)
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {},
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (imageUrl != null)
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        modifier =
                            Modifier
                                .size(AppDimensions.productItemImageSize)
                                .clip(imageShape),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.miempresa_logo_glyph),
                        error = painterResource(R.drawable.miempresa_logo_glyph),
                    )
                else
                    Box(
                        modifier =
                            Modifier
                                .size(AppDimensions.productItemImageSize)
                                .clip(imageShape)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = stringResource(R.string.sin_imagen),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }

                Spacer(modifier = Modifier.width(AppDimensions.mediumPadding))

                Column(modifier = Modifier
                    .weight(1f)
                    .height(AppDimensions.productItemImageSize),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(AppDimensions.extraSmallPadding),
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Text(
                            text = price,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding, Alignment.End),
                    ) {
                        if (badge != null) {
                            Box(modifier = Modifier.weight(1f)) {
                                badge()
                            }
                        }

                        if (isPublic != null) {
                            val icon = if (isPublic) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                            val tint = if (isPublic) VisibilityActiveBlue else SlateGray400
                            val description = if (isPublic) stringResource(R.string.filter_public) else stringResource(R.string.filter_private)

                            if (onToggleVisibility != null) {
                                IconButton(
                                    onClick = onToggleVisibility,
                                    modifier = Modifier.size(AppDimensions.mediumIconSize)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = stringResource(R.string.toggle_visibility),
                                        tint = tint,
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = description,
                                    modifier = Modifier.size(AppDimensions.defaultSmallIconSize),
                                    tint = tint,
                                )
                            }
                        }

                        if (onDelete != null) {
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(AppDimensions.mediumIconSize)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryItem(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    emojiIcon: String? = null,
    productCount: Int? = null,
    badge: (@Composable () -> Unit)? = null,
    isPublic: Boolean? = null,
    onToggleVisibility: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    val cardShape = remember { RoundedCornerShape(AppDimensions.mediumCornerRadius) }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = cardShape,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(AppDimensions.mediumSmallPadding)
                    .semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(AppDimensions.categoryEmojiContainerSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (!emojiIcon.isNullOrEmpty())
                    Text(
                        text = emojiIcon,
                        fontSize = 24.sp,
                    )
                else
                    Icon(Icons.Outlined.Sell, contentDescription = title, tint = SlateGray400)
            }

            Spacer(modifier = Modifier.width(AppDimensions.mediumSmallPadding))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (productCount != null) {
                    ProductCountBadge(count = productCount)
                } else {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
                    modifier = Modifier.padding(top = AppDimensions.extraSmallPadding),
                ) {
                    if (badge != null) {
                        badge()
                    }
                }
            }

            // Trailing actions
            if (onToggleVisibility != null || onDelete != null || isPublic != null) {
                Box(
                    modifier =
                        Modifier
                            .padding(start = AppDimensions.smallPadding)
                            .size(width = 1.dp, height = 40.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant),
                )
                // Interactive visibility toggle
                if (isPublic != null && onToggleVisibility != null) {
                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier.padding(start = AppDimensions.extraSmallPadding),
                    ) {
                        Icon(
                            imageVector =
                                if (isPublic) Icons.Outlined.Visibility
                                else Icons.Outlined.VisibilityOff,
                            contentDescription = stringResource(R.string.toggle_visibility),
                            modifier = Modifier.size(AppDimensions.defaultSmallIconSize),
                            tint = if (isPublic) VisibilityActiveBlue else SlateGray400,
                        )
                    }
                } else if (isPublic != null) {
                    // Read-only visibility indicator
                    Icon(
                        imageVector =
                            if (isPublic) Icons.Outlined.Visibility
                            else Icons.Outlined.VisibilityOff,
                        contentDescription =
                            if (isPublic) stringResource(R.string.filter_public)
                            else stringResource(R.string.filter_private),
                        modifier = Modifier
                            .padding(start = AppDimensions.extraSmallPadding)
                            .size(AppDimensions.defaultSmallIconSize),
                        tint = if (isPublic) VisibilityActiveBlue else SlateGray400,
                    )
                }
                if (onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.delete),
                            modifier = Modifier.size(AppDimensions.defaultSmallIconSize),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCountBadge(count: Int) {
    val isZero = count == 0
    val backgroundColor = if (isZero) SlateGray200 else MaterialTheme.colorScheme.surface
    val contentColor = if (isZero) SlateGray700 else MaterialTheme.colorScheme.onPrimaryContainer
    val text = if (isZero) stringResource(R.string.no_products_label) else "$count productos"
    Surface(
        modifier = Modifier.padding(top = AppDimensions.smallPadding),
        color = backgroundColor,
        shape = RoundedCornerShape(AppDimensions.smallCornerRadius),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = AppDimensions.smallPadding, vertical = AppDimensions.extraSmallPadding),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemCardPreview() {
    MiEmpresaTheme {
        ItemCard(
            title = "Granos de Café Artesanal",
            subtitle = "$22.50",
            isPublic = true,
            badge = {
                CategoryBadge(emoji = "☕", name = "Café")
            },
            onToggleVisibility = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemCardPrivatePreview() {
    MiEmpresaTheme {
        ItemCard(
            title = "PIZZETAS GRANDES SIMPLE CON MUZZARELLA",
            subtitle = "$35.00",
            isPublic = false,
            badge = {
                CategoryBadge(emoji = "🏷️", name = "Accesorios")
            },
            onToggleVisibility = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemCardNoActionsPreview() {
    MiEmpresaTheme {
        ItemCard(
            title = "Producto de ejemplo",
            subtitle = "$1.500,00",
            badge = {
                CategoryBadge(emoji = "🏷️", name = "Accesorios")
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemCardEmojiPreview() {
    MiEmpresaTheme {
        ItemCard(
            title = "Bebidas",
            subtitle = "",
            productCount = 10,
            emojiIcon = "🥤",
            itemType = ItemType.CATEGORY,
            onDelete = {},
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemCardEmojiZeroPreview() {
    MiEmpresaTheme {
        ItemCard(
            title = "Sin icono",
            subtitle = "",
            productCount = 0,
            emojiIcon = "🍿",
            itemType = ItemType.CATEGORY,
            onDelete = {},
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemCardNoEmojiZeroPreview() {
    MiEmpresaTheme {
        ItemCard(
            title = "Snacks",
            subtitle = "",
            productCount = 0,
            itemType = ItemType.CATEGORY,
            onDelete = {},
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductItemTilePreview() {
    MiEmpresaTheme {
        ProductItem(
            title = "Granos de Café Artesanal con Tueste Medio Oscuro",
            price = "$22.50",
            isPublic = true,
            badge = {
                CategoryBadge(emoji = "☕", name = "Café")
            },
            onToggleVisibility = {},
            onDelete = {},
        )
    }
}
