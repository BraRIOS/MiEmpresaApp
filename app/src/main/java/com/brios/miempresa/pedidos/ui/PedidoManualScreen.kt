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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.components.FormFieldGroup
import com.brios.miempresa.core.ui.components.FormOutlinedTextField
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray400
import com.brios.miempresa.core.ui.theme.SlateGray500
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PedidoManualScreen(
    modifier: Modifier = Modifier,
    viewModel: PedidoManualViewModel = hiltViewModel(),
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
                is PedidoManualEvent.OrderCreated -> onOrderCreated()
                is PedidoManualEvent.ShowError -> {}
            }
        }
    }

    PedidoManualContent(
        modifier = modifier,
        form = form,
        isSaving = isSaving,
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

@Composable
private fun PedidoManualContent(
    modifier: Modifier = Modifier,
    form: OrderFormState,
    isSaving: Boolean = false,
    onUpdateCustomerName: (String) -> Unit = {},
    onUpdateCustomerPhone: (String) -> Unit = {},
    onUpdateNotes: (String) -> Unit = {},
    onAddProductClick: () -> Unit = {},
    onRemoveItem: (Int) -> Unit = {},
    onCreateOrder: () -> Unit = {},
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppDimensions.largePadding)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
        ) {
            item { Spacer(modifier = Modifier.height(AppDimensions.smallPadding)) }

            // Customer info
            item {
                FormFieldGroup(label = stringResource(R.string.pedido_label_customer)) {
                    FormOutlinedTextField(
                        value = form.customerName,
                        onValueChange = onUpdateCustomerName,
                        placeholder = stringResource(R.string.pedido_placeholder_customer),
                        leadingIcon = Icons.Outlined.Person,
                    )
                }
            }

            item {
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
            }

            item {
                FormFieldGroup(label = stringResource(R.string.pedido_label_notes)) {
                    FormOutlinedTextField(
                        value = form.notes,
                        onValueChange = onUpdateNotes,
                        placeholder = stringResource(R.string.pedido_placeholder_notes),
                        leadingIcon = Icons.Outlined.Notes,
                    )
                }
            }

            // Products section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.pedido_section_products),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SlateGray500,
                    )
                    OutlinedButton(
                        onClick = onAddProductClick,
                        shape = RoundedCornerShape(50),
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(AppDimensions.extraSmallPadding))
                        Text(stringResource(R.string.pedido_add_product))
                    }
                }
            }

            if (form.items.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.pedido_no_products),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateGray400,
                    )
                }
            }

            itemsIndexed(form.items) { index, item ->
                OrderItemRow(
                    item = item,
                    currencyFormat = currencyFormat,
                    onRemove = { onRemoveItem(index) },
                )
            }

            // Total
            if (form.items.isNotEmpty()) {
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
                            text = currencyFormat.format(form.total),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(AppDimensions.largePadding)) }
        }

        // Sticky CTA
        Button(
            onClick = onCreateOrder,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(AppDimensions.largePadding),
            enabled = form.isValid && !isSaving,
            shape = RoundedCornerShape(50),
        ) {
            Text(stringResource(R.string.pedido_create_cta))
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
private fun PedidoManualPreview() {
    MiEmpresaTheme {
        PedidoManualContent(
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
