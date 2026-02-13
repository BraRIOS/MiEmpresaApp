package com.brios.miempresa.orders.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.components.FormFieldGroup
import com.brios.miempresa.core.ui.components.FormOutlinedTextField
import com.brios.miempresa.core.ui.components.MiEmpresaFAB
import com.brios.miempresa.core.ui.components.StateViewSpotIllustration
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray200
import com.brios.miempresa.core.ui.theme.SlateGray400
import com.brios.miempresa.core.ui.theme.SlateGray500
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OrderManualScreen(
    modifier: Modifier = Modifier,
    viewModel: OrderManualViewModel = hiltViewModel(),
    onOrderCreated: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    var showProductSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is OrderManualEvent.OrderCreated -> onOrderCreated()
                is OrderManualEvent.ShowError -> {}
            }
        }
    }

    OrderManualContent(
        modifier = modifier,
        form = form,
        isSaving = isSaving,
        onNavigateBack = onNavigateBack,
        onUpdateCustomerName = viewModel::updateCustomerName,
        onUpdateCustomerPhone = viewModel::updateCustomerPhone,
        onUpdateNotes = viewModel::updateNotes,
        onAddProductClick = { showProductSheet = true },
        onRemoveItem = viewModel::removeItem,
        onCreateOrder = viewModel::createOrder,
    )

    if (showProductSheet) {
        AddProductToOrderSheet(
            products = products,
            onProductSelected = { product, qty ->
                viewModel.addProduct(product, qty)
                showProductSheet = false
            },
            onDismiss = { showProductSheet = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderManualContent(
    modifier: Modifier = Modifier,
    form: OrderFormState,
    isSaving: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onUpdateCustomerName: (String) -> Unit = {},
    onUpdateCustomerPhone: (String) -> Unit = {},
    onUpdateNotes: (String) -> Unit = {},
    onAddProductClick: () -> Unit = {},
    onRemoveItem: (Int) -> Unit = {},
    onCreateOrder: () -> Unit = {},
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.pedido_manual_title),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.action_close),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier.padding(AppDimensions.mediumPadding),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumSmallPadding),
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
                            text = currencyFormat.format(form.total),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Button(
                        onClick = onCreateOrder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = form.isValid && !isSaving,
                        shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                    ) {
                        Text(
                            text = stringResource(R.string.pedido_create_cta),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = AppDimensions.mediumPadding),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
        ) {
            item { Spacer(modifier = Modifier.height(AppDimensions.smallPadding)) }

            // Form card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                    shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                    border = BorderStroke(1.dp, SlateGray200),
                ) {
                    Column(
                        modifier = Modifier.padding(AppDimensions.mediumLargePadding),
                        verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
                    ) {
                        FormFieldGroup(label = stringResource(R.string.pedido_label_customer)) {
                            FormOutlinedTextField(
                                value = form.customerName,
                                onValueChange = onUpdateCustomerName,
                                placeholder = stringResource(R.string.pedido_placeholder_customer),
                                leadingIcon = Icons.Outlined.Person,
                            )
                        }

                        FormFieldGroup(
                            label = stringResource(R.string.pedido_label_phone),
                            required = true,
                        ) {
                            FormOutlinedTextField(
                                value = form.customerPhone,
                                onValueChange = { input -> onUpdateCustomerPhone(input.filter { it.isDigit() }) },
                                placeholder = stringResource(R.string.pedido_placeholder_phone),
                                leadingIcon = Icons.Outlined.Phone,
                                keyboardType = KeyboardType.Phone,
                            )
                        }

                        FormFieldGroup(label = stringResource(R.string.pedido_label_notes)) {
                            FormOutlinedTextField(
                                value = form.notes,
                                onValueChange = onUpdateNotes,
                                placeholder = stringResource(R.string.pedido_placeholder_notes),
                                leadingIcon = Icons.Outlined.Notes,
                            )
                        }
                    }
                }
            }

            // Products card (D-18: "+" in header, no standalone FAB)
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
                        // Header with "+" button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.pedido_section_products_manual),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SlateGray400,
                                letterSpacing = 1.sp,
                            )
                            MiEmpresaFAB(
                                onClick = onAddProductClick,
                                contentDescription = stringResource(R.string.pedido_add_product),
                                modifier = Modifier.padding(bottom = 0.dp),
                                size = AppDimensions.smallFabSize,
                                iconSize = AppDimensions.smallIconSize,
                            )
                        }

                        if (form.items.isEmpty()) {
                            // Empty state within card
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = AppDimensions.largePadding),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                StateViewSpotIllustration(Icons.Outlined.Inventory2)
                                Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))
                                Text(
                                    text = stringResource(R.string.pedido_no_products_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(modifier = Modifier.height(AppDimensions.extraSmallPadding))
                                Text(
                                    text = stringResource(R.string.pedido_no_products_subtitle),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SlateGray500,
                                )
                            }
                        }
                    }
                }
            }

            // Product items (outside card for scroll)
            if (form.items.isNotEmpty()) {
                itemsIndexed(form.items) { index, item ->
                    OrderItemRow(
                        item = item,
                        currencyFormat = currencyFormat,
                        onRemove = { onRemoveItem(index) },
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(AppDimensions.smallPadding)) }
        }
    }
}

@Composable
private fun OrderItemRow(
    item: OrderFormItem,
    currencyFormat: NumberFormat,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        shape = RoundedCornerShape(AppDimensions.smallCornerRadius),
        border = BorderStroke(1.dp, SlateGray200),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimensions.mediumSmallPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${item.quantity} × ${currencyFormat.format(item.price)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateGray400,
                )
            }
            Text(
                text = currencyFormat.format(item.subtotal),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.pedido_remove_item),
                    modifier = Modifier.size(16.dp),
                    tint = SlateGray400,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderManualEmptyPreview() {
    MiEmpresaTheme {
        OrderManualContent(form = OrderFormState())
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderManualWithItemsPreview() {
    MiEmpresaTheme {
        OrderManualContent(
            form = OrderFormState(
                customerName = "Juan Pérez",
                customerPhone = "1112345678",
                items = listOf(
                    OrderFormItem("p1", "Vino Malbec Reserva", 4500.0, 2),
                    OrderFormItem("p2", "Aceite de Oliva Extra Virgen", 3200.0, 1),
                ),
            ),
        )
    }
}
