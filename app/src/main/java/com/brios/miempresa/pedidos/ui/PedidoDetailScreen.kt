package com.brios.miempresa.pedidos.ui

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray400
import com.brios.miempresa.core.ui.theme.SlateGray500
import com.brios.miempresa.pedidos.data.OrderEntity
import com.brios.miempresa.pedidos.data.OrderItemEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PedidoDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: PedidoDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()

    PedidoDetailContent(
        modifier = modifier,
        state = state.copy(items = items),
    )
}

@Composable
private fun PedidoDetailContent(
    modifier: Modifier = Modifier,
    state: PedidoDetailState,
) {
    val context = LocalContext.current
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val order = state.order ?: return

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppDimensions.largePadding)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
        ) {
            item { Spacer(modifier = Modifier.height(AppDimensions.smallPadding)) }

            // Order metadata
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                    shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimensions.mediumPadding),
                    ) {
                        Text(
                            text = order.customerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        order.customerPhone?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SlateGray500,
                            )
                        }
                        Spacer(modifier = Modifier.height(AppDimensions.smallPadding))
                        Text(
                            text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                                .format(Date(order.createdAt)),
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateGray400,
                        )
                        order.notes?.let {
                            Spacer(modifier = Modifier.height(AppDimensions.smallPadding))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateGray400,
                            )
                        }
                    }
                }
            }

            // Items header
            item {
                Text(
                    text = stringResource(R.string.pedido_section_products),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = SlateGray500,
                )
            }

            // Items list
            items(state.items, key = { it.id }) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.productName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${item.quantity} × ${currencyFormat.format(item.priceAtOrder)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateGray400,
                        )
                    }
                    Text(
                        text = currencyFormat.format(item.priceAtOrder * item.quantity),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Total
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.pedido_total),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = currencyFormat.format(order.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        // WhatsApp button
        Button(
            onClick = {
                val message = buildWhatsAppMessage(order, state.items, currencyFormat)
                val phone = order.customerPhone?.replace(Regex("[^\\d+]"), "") ?: ""
                val url = "https://wa.me/$phone?text=${Uri.encode(message)}"
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(AppDimensions.largePadding),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Icon(
                Icons.Outlined.Chat,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.size(AppDimensions.smallPadding))
            Text(stringResource(R.string.pedido_whatsapp_button))
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
