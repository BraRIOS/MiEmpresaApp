package com.brios.miempresa.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    isPublic: Boolean? = null,
    onToggleVisibility: ((Boolean) -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    val cardShape = remember { RoundedCornerShape(AppDimensions.smallCornerRadius) }
    val imageShape = remember { RoundedCornerShape(AppDimensions.smallCornerRadius) }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = cardShape,
    ) {
        Row(
            modifier =
                Modifier
                    .padding(AppDimensions.smallPadding)
                    .semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(AppDimensions.itemCardImageSize)
                            .clip(imageShape),
                    contentScale = ContentScale.Crop,
                )
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

            Spacer(modifier = Modifier.width(AppDimensions.mediumPadding))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isPublic != null && onToggleVisibility != null) {
                    IconButton(
                        onClick = { onToggleVisibility(!isPublic) },
                    ) {
                        Icon(
                            imageVector =
                                if (isPublic) {
                                    Icons.Outlined.Visibility
                                } else {
                                    Icons.Outlined.VisibilityOff
                                },
                            contentDescription = stringResource(R.string.toggle_visibility),
                            tint =
                                if (isPublic) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.delete_item),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
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
            title = "Producto de ejemplo",
            subtitle = "$1.500,00",
            isPublic = true,
            onToggleVisibility = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemCardNoImagePreview() {
    MiEmpresaTheme {
        ItemCard(
            title = "Categoría ejemplo",
            subtitle = "12 productos",
            onDelete = {},
        )
    }
}
