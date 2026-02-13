package com.brios.miempresa.pedidos.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray100
import com.brios.miempresa.core.ui.theme.SlateGray200
import com.brios.miempresa.core.ui.theme.SlateGray400
import com.brios.miempresa.core.ui.theme.SlateGray500
import com.brios.miempresa.core.ui.theme.WhatsAppGreen
import com.brios.miempresa.pedidos.data.OrderEntity
import com.brios.miempresa.pedidos.data.OrderItemEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri

@Composable
fun PedidoDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: PedidoDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()

    PedidoDetailContent(
        modifier = modifier,
        state = state.copy(items = items),
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PedidoDetailContent(
    modifier: Modifier = Modifier,
    state: PedidoDetailState,
    onNavigateBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.pedido_detail_title),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            val order = state.order ?: return@Scaffold
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier.padding(AppDimensions.mediumPadding),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.pedido_total),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = currencyFormat.format(order.totalAmount),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    // D-07: WhatsApp CTA = white bg + green WA icon
                    OutlinedButton(
                        onClick = {
                            val message = buildWhatsAppMessage(order, state.items, currencyFormat)
                            val phone = order.customerPhone?.replace(Regex("[^\\d+]"), "") ?: ""
                            val url = "https://wa.me/$phone?text=${Uri.encode(message)}"
                            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, SlateGray200),
                    ) {
                        Icon(
                            Icons.Outlined.Call,
                            contentDescription = null,
                            modifier = Modifier.size(AppDimensions.smallIconSize),
                            tint = WhatsAppGreen,
                        )
                        Spacer(modifier = Modifier.width(AppDimensions.smallPadding))
                        Text(
                            text = stringResource(R.string.pedido_whatsapp_button),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        },
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val order = state.order ?: return@Scaffold

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = AppDimensions.mediumLargePadding),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
        ) {
            item { Spacer(modifier = Modifier.height(AppDimensions.smallPadding)) }

            // Info card with icon rows + dashed dividers
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                    shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                    border = BorderStroke(1.dp, SlateGray200),
                ) {
                    Column {
                        // Header
                        Text(
                            text = stringResource(R.string.pedido_info_header),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SlateGray400,
                            letterSpacing = 1.sp,
                            modifier = Modifier
                                .padding(horizontal = AppDimensions.mediumLargePadding)
                                .padding(top = AppDimensions.mediumPadding, bottom = AppDimensions.mediumSmallPadding),
                        )
                        HorizontalDivider(color = SlateGray200.copy(alpha = 0.5f))

                        Column(modifier = Modifier.padding(horizontal = AppDimensions.mediumLargePadding)) {
                            val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                                .format(Date(order.createdAt))

                            InfoRow(
                                icon = Icons.Outlined.CalendarToday,
                                label = "Fecha",
                                value = dateStr,
                                showDivider = true,
                            )
                            InfoRow(
                                icon = Icons.Outlined.Person,
                                label = "Cliente",
                                value = order.customerName,
                                showDivider = true,
                            )
                            InfoRow(
                                icon = Icons.Outlined.Call,
                                label = "WhatsApp",
                                value = order.customerPhone ?: "—",
                                showDivider = true,
                            )
                            InfoRow(
                                icon = Icons.Outlined.Notes,
                                label = "Notas",
                                value = order.notes ?: "Sin observaciones",
                                isItalic = order.notes == null,
                                showDivider = false,
                            )
                        }
                    }
                }
            }

            // Products card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                    shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                    border = BorderStroke(1.dp, SlateGray200),
                ) {
                    Column(modifier = Modifier.padding(AppDimensions.mediumLargePadding)) {
                        // Header with count badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.pedido_section_products),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SlateGray400,
                                letterSpacing = 1.sp,
                            )
                            Text(
                                text = "${state.items.size} items",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(AppDimensions.smallCornerRadius))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .padding(horizontal = 10.dp, vertical = 2.dp),
                            )
                        }

                        Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))

                        state.items.forEachIndexed { index, item ->
                            ProductItemRow(item = item, currencyFormat = currencyFormat)
                            if (index < state.items.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = AppDimensions.mediumPadding),
                                    color = SlateGray200.copy(alpha = 0.5f),
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(AppDimensions.smallPadding)) }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    showDivider: Boolean,
    isItalic: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppDimensions.mediumSmallPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(AppDimensions.mediumIconSize)
                .background(SlateGray100.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = SlateGray400,
            )
        }
        Spacer(modifier = Modifier.width(AppDimensions.mediumSmallPadding))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SlateGray500,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
            )
        }
    }
    if (showDivider) {
        HorizontalDivider(
            color = SlateGray200.copy(alpha = 0.4f),
            modifier = Modifier.padding(start = 44.dp),
        )
    }
}

@Composable
private fun ProductItemRow(
    item: OrderItemEntity,
    currencyFormat: NumberFormat,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.productName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${currencyFormat.format(item.priceAtOrder)} c/u",
                style = MaterialTheme.typography.labelSmall,
                color = SlateGray500,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = currencyFormat.format(item.priceAtOrder * item.quantity),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Cant: ${item.quantity}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = SlateGray500,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SlateGray100)
                    .padding(horizontal = AppDimensions.smallPadding, vertical = 2.dp),
            )
        }
    }
}

private fun buildWhatsAppMessage(
    order: OrderEntity,
    items: List<OrderItemEntity>,
    currencyFormat: NumberFormat,
): String {
    val sb = StringBuilder()
    sb.appendLine("*Pedido de ${order.customerName}*")
    sb.appendLine()
    items.forEach { item ->
        sb.appendLine("• ${item.productName} x${item.quantity} — ${currencyFormat.format(item.priceAtOrder * item.quantity)}")
    }
    sb.appendLine()
    sb.appendLine("*Total: ${currencyFormat.format(order.totalAmount)}*")
    order.notes?.let {
        sb.appendLine()
        sb.appendLine("Notas: $it")
    }
    return sb.toString()
}

@Preview(showBackground = true)
@Composable
private fun PedidoDetailPreview() {
    MiEmpresaTheme {
        PedidoDetailContent(
            state = PedidoDetailState(
                order = OrderEntity(
                    id = "1",
                    companyId = "c1",
                    customerName = "Juan Pérez",
                    customerPhone = "+5411 1234-5678",
                    notes = "Entregar después de las 18h",
                    totalAmount = 12200.0,
                ),
                items = listOf(
                    OrderItemEntity("i1", "1", "p1", "Vino Malbec Reserva", 4500.0, 2),
                    OrderItemEntity("i2", "1", "p2", "Aceite Extra Virgen", 3200.0, 1),
                ),
                isLoading = false,
            ),
        )
    }
}
