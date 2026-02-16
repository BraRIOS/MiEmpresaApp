package com.brios.miempresa.orders.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.components.EmptyStateView
import com.brios.miempresa.core.ui.components.MiEmpresaFAB
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray100
import com.brios.miempresa.core.ui.theme.SlateGray200
import com.brios.miempresa.core.ui.theme.SlateGray400
import com.brios.miempresa.orders.data.OrderEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrdersListScreen(
    modifier: Modifier = Modifier,
    viewModel: OrdersListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToCreateOrder: () -> Unit = {},
    onNavigateToOrderDetail: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OrderListContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToCreateOrder = onNavigateToCreateOrder,
        onNavigateToOrderDetail = onNavigateToOrderDetail,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderListContent(
    modifier: Modifier = Modifier,
    uiState: OrdersListUiState,
    onNavigateBack: () -> Unit = {},
    onNavigateToCreateOrder: () -> Unit = {},
    onNavigateToOrderDetail: (String) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.orders_title),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.action_sort),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
            )
        },
        floatingActionButton = {
            if (uiState is OrdersListUiState.Success)
                MiEmpresaFAB(
                    modifier = Modifier.padding(bottom = 80.dp),
                    onClick = onNavigateToCreateOrder,
                    contentDescription = stringResource(R.string.orders_add),
                )
        },
    ) { padding ->
        when (uiState) {
            is OrdersListUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is OrdersListUiState.Empty -> {
                EmptyStateView(
                    icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                    title = stringResource(R.string.orders_empty_title),
                    subtitle = stringResource(R.string.orders_empty_subtitle),
                    actionLabel = stringResource(R.string.empty_order_add_CTA),
                    actionIcon = Icons.Default.AddCircle,
                    onAction = onNavigateToCreateOrder,
                )
            }
            is OrdersListUiState.Success -> {
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
            is OrdersListUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
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
        .format(Date(order.orderDate))
    val totalStr = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
        .format(order.totalAmount)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, SlateGray200),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimensions.mediumLargePadding),
        ) {
            // Row 1: #ID badge + date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = order.displayOrderNumber,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SlateGray100.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateGray400,
                )
            }

            Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))

            // Row 2: CLIENTE label + name
            Text(
                text = "CLIENTE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = SlateGray400,
                letterSpacing = 1.sp,
            )
            Spacer(modifier = Modifier.height(AppDimensions.extraSmallPadding))
            Text(
                text = order.customerName.ifBlank {
                    stringResource(R.string.order_default_customer_name)
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,

            )

            Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))

            // Divider
            HorizontalDivider(color = SlateGray200.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(AppDimensions.mediumSmallPadding))

            // Row 3: MONTO TOTAL + amount + chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        text = "MONTO TOTAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SlateGray400,
                        letterSpacing = 1.sp,
                    )
                    Spacer(modifier = Modifier.height(AppDimensions.extraSmallPadding))
                    Text(
                        text = totalStr,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = SlateGray400,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderListPreview() {
    MiEmpresaTheme {
        OrderListContent(
            uiState = OrdersListUiState.Success(
                orders = listOf(
                    OrderEntity(
                        id = "ORD-A1B2C3",
                        companyId = "c1",
                        customerName = "Juan Pérez",
                        customerPhone = "+54 11 1234-5678",
                        totalAmount = 15500.0,
                        orderDate = System.currentTimeMillis(),
                    ),
                    OrderEntity(
                        id = "ORD-C3D4E5",
                        companyId = "c1",
                        customerName = "María López",
                        customerPhone = "+54 11 9876-5432",
                        notes = "Entregar después de las 18h",
                        totalAmount = 8200.0,
                        orderDate = System.currentTimeMillis() - 86400000,
                    ),
                ),
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderListEmptyPreview() {
    MiEmpresaTheme {
        OrderListContent(uiState = OrdersListUiState.Empty)
    }
}
