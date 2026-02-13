package com.brios.miempresa.pedidos.ui

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray400
import com.brios.miempresa.core.ui.theme.SlateGray500
import com.brios.miempresa.pedidos.data.OrderEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PedidosListScreen(
    modifier: Modifier = Modifier,
    viewModel: PedidosListViewModel = hiltViewModel(),
    onNavigateToCreateOrder: () -> Unit = {},
    onNavigateToOrderDetail: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PedidosListContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateToCreateOrder = onNavigateToCreateOrder,
        onNavigateToOrderDetail = onNavigateToOrderDetail,
    )
}

@Composable
private fun PedidosListContent(
    modifier: Modifier = Modifier,
    uiState: PedidosListUiState,
    onNavigateToCreateOrder: () -> Unit = {},
    onNavigateToOrderDetail: (String) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateOrder,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.pedidos_add))
            }
        },
    ) { padding ->
        when (uiState) {
            is PedidosListUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is PedidosListUiState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = SlateGray400,
                        )
                        Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))
                        Text(
                            text = stringResource(R.string.pedidos_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = SlateGray500,
                        )
                        Spacer(modifier = Modifier.height(AppDimensions.smallPadding))
                        Text(
                            text = stringResource(R.string.pedidos_empty_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateGray400,
                        )
                    }
                }
            }
            is PedidosListUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = AppDimensions.largePadding),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumSmallPadding),
                ) {
                    item { Spacer(modifier = Modifier.height(AppDimensions.smallPadding)) }
                    items(uiState.orders, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            onClick = { onNavigateToOrderDetail(order.id) },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
            is PedidosListUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: OrderEntity,
    onClick: () -> Unit,
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        .format(Date(order.createdAt))
    val totalStr = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
        .format(order.totalAmount)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimensions.mediumPadding),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = order.customerName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = totalStr,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(AppDimensions.extraSmallPadding))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = SlateGray400,
                )
                Spacer(modifier = Modifier.size(AppDimensions.extraSmallPadding))
                Text(
                    text = order.customerPhone ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateGray400,
                )
            }

            Spacer(modifier = Modifier.height(AppDimensions.extraSmallPadding))

            Text(
                text = dateStr,
                style = MaterialTheme.typography.labelSmall,
                color = SlateGray400,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PedidosListPreview() {
    MiEmpresaTheme {
        PedidosListContent(
            uiState = PedidosListUiState.Success(
                orders = listOf(
                    OrderEntity(
                        id = "1",
                        companyId = "c1",
                        customerName = "Juan Pérez",
                        customerPhone = "+54 11 1234-5678",
                        totalAmount = 15500.0,
                        createdAt = System.currentTimeMillis(),
                    ),
                    OrderEntity(
                        id = "2",
                        companyId = "c1",
                        customerName = "María López",
                        customerPhone = "+54 11 9876-5432",
                        notes = "Entregar después de las 18h",
                        totalAmount = 8200.0,
                        createdAt = System.currentTimeMillis() - 86400000,
                    ),
                ),
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PedidosListEmptyPreview() {
    MiEmpresaTheme {
        PedidosListContent(uiState = PedidosListUiState.Empty)
    }
}
