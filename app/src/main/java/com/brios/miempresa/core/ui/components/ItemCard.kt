package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    emojiIcon: String? = null,
    isPublic: Boolean? = null,
    badge: (@Composable () -> Unit)? = null,
    onToggleVisibility: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    val cardShape = remember { RoundedCornerShape(AppDimensions.mediumCornerRadius) }
    val imageShape = remember { RoundedCornerShape(AppDimensions.smallCornerRadius) }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = cardShape,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
        border = BorderStroke(1.dp, SlateGray200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(AppDimensions.mediumSmallPadding)
                    .semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Thumbnail
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier =
                        Modifier
                            .size(AppDimensions.itemCardImageSize)
                            .clip(imageShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.miempresa_logo_glyph),
                    error = painterResource(R.drawable.miempresa_logo_glyph),
                )
            } else if (!emojiIcon.isNullOrEmpty()) {
                Box(
                    modifier =
                        Modifier
                            .size(AppDimensions.categoryEmojiContainerSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = emojiIcon,
                        fontSize = 24.sp,
                    )
                }
            } else {
                Box(
                    modifier =
                        Modifier
                            .size(AppDimensions.itemCardImageSize)
                            .clip(imageShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
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

            // Trailing actions with border separator
            if (onToggleVisibility != null || onDelete != null) {
                Box(
                    modifier =
                        Modifier
                            .padding(start = AppDimensions.smallPadding)
                            .width(1.dp)
                            .size(width = 1.dp, height = 40.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant),
                )
                Column(
                    modifier = Modifier.padding(start = AppDimensions.extraSmallPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (onToggleVisibility != null && isPublic != null) {
                        IconButton(onClick = onToggleVisibility) {
                            Icon(
                                imageVector =
                                    if (isPublic) {
                                        Icons.Outlined.Visibility
                                    } else {
                                        Icons.Outlined.VisibilityOff
                                    },
                                contentDescription = stringResource(R.string.toggle_visibility),
                                modifier = Modifier.size(AppDimensions.smallIconSize),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.delete_item),
                                modifier = Modifier.size(AppDimensions.smallIconSize),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
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
            title = "Juego de Tazas de Cerámica",
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
        )
    }
}
