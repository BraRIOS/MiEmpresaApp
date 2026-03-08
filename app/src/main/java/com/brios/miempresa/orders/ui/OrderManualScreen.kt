package com.brios.miempresa.orders.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.components.CountryCodeDropdown
import com.brios.miempresa.core.ui.components.FormFieldGroup
import com.brios.miempresa.core.ui.components.FormOutlinedTextField
import com.brios.miempresa.core.ui.components.OrderProductListItem
import com.brios.miempresa.core.ui.components.StateViewSpotIllustration
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray200
import com.brios.miempresa.core.ui.theme.SlateGray400
import com.brios.miempresa.core.ui.theme.SlateGray500
import com.brios.miempresa.navigation.rememberScreenActionGuard
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Locale.getDefault

@Composable
fun OrderManualScreen(
    modifier: Modifier = Modifier,
    viewModel: OrderManualViewModel = hiltViewModel(),
    onOrderCreated: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    val screenActionGuard = rememberScreenActionGuard()
    val isScreenInteractive = screenActionGuard.isScreenInteractive

    val form by viewModel.form.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val orderCreated by viewModel.orderCreated.collectAsStateWithLifecycle()

    var showProductSheet by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = !isSaving) {
        screenActionGuard.runAndNavigate(onNavigateBack)
    }

    LaunchedEffect(orderCreated, isScreenInteractive) {
        if (orderCreated) {
            screenActionGuard.runAndNavigate {
                onOrderCreated()
                viewModel.onOrderNavigationHandled()
            }
        }
    }

    OrderManualContent(
        modifier = modifier,
        form = form,
        isSaving = isSaving,
        isScreenInteractive = isScreenInteractive,
        onNavigateBack = { screenActionGuard.runAndNavigate(onNavigateBack) },
        onUpdateCustomerName = viewModel::updateCustomerName,
        onUpdateCustomerPhone = viewModel::updateCustomerPhone,
        onUpdateCustomerPhoneCountryCode = viewModel::updateCustomerPhoneCountryCode,
        onUpdateDate = viewModel::updateDate,
        onUpdateNotes = viewModel::updateNotes,
        onAddProductClick = { screenActionGuard.runIfActive { showProductSheet = true } },
        onRemoveItem = viewModel::removeItem,
        onUpdateItemQuantity = viewModel::updateItemQuantity,
        onCreateOrder = { screenActionGuard.runIfActive { viewModel.createOrder() } },
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
    isScreenInteractive: Boolean = true,
    onNavigateBack: () -> Unit = {},
    onUpdateCustomerName: (String) -> Unit = {},
    onUpdateCustomerPhone: (String) -> Unit = {},
    onUpdateCustomerPhoneCountryCode: (String) -> Unit = {},
    onUpdateDate: (Long) -> Unit = {},
    onUpdateNotes: (String) -> Unit = {},
    onAddProductClick: () -> Unit = {},
    onRemoveItem: (Int) -> Unit = {},
    onUpdateItemQuantity: (Int, Int) -> Unit = { _, _ -> },
    onCreateOrder: () -> Unit = {},
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
    val dateFormat = SimpleDateFormat("dd MMM, yyyy", getDefault())

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = form.date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onUpdateDate(it) }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.order_manual_title),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = isScreenInteractive) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.action_close),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
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
                            text = stringResource(R.string.order_total),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            .fillMaxWidth(),
                        enabled = form.isValid && !isSaving && isScreenInteractive,
                        shape = CircleShape,
                        contentPadding = PaddingValues(AppDimensions.mediumPadding),
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(AppDimensions.smallPadding))
                        Text(
                            text = stringResource(R.string.order_create_cta),
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
            contentPadding = PaddingValues(bottom = AppDimensions.extraLargePadding),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
        ) {
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
                        FormFieldGroup(label = stringResource(R.string.order_label_customer).uppercase(getDefault())) {
                            FormOutlinedTextField(
                                value = form.customerName,
                                onValueChange = onUpdateCustomerName,
                                placeholder = stringResource(R.string.order_placeholder_customer),
                                leadingIcon = Icons.Outlined.Person,
                            )
                        }

                        FormFieldGroup(
                            label = stringResource(R.string.whatsapp_label).uppercase(),
                            required = true,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
                                verticalAlignment = Alignment.Top,
                            ) {
                                CountryCodeDropdown(
                                    selectedCode = form.customerPhoneCountryCode,
                                    onCodeSelected = onUpdateCustomerPhoneCountryCode,
                                )
                                FormOutlinedTextField(
                                    value = form.customerPhone,
                                    onValueChange = { input -> onUpdateCustomerPhone(input.filter { it.isDigit() }) },
                                    placeholder = stringResource(R.string.placeholder_whatsapp),
                                    keyboardType = KeyboardType.Phone,
                                )
                            }
                        }

                        FormFieldGroup(label = "FECHA DEL PEDIDO") {
                            FormOutlinedTextField(
                                value = dateFormat.format(Date(form.date)),
                                onValueChange = {},
                                placeholder = "",
                                leadingIcon = null,
                                trailingIcon = {
                                     Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = SlateGray400)
                                },
                                readOnly = true,
                                onClick = { showDatePicker = true }
                            )
                        }

                        FormFieldGroup(label = stringResource(R.string.order_label_notes).uppercase()) {
                            FormOutlinedTextField(
                                value = form.notes,
                                onValueChange = onUpdateNotes,
                                placeholder = stringResource(R.string.order_placeholder_notes),
                                singleLine = false,
                                minLines = 3
                            )
                        }
                    }
                }
            }

            // Products Section
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppDimensions.mediumPadding)
                                .padding(top = AppDimensions.mediumPadding, bottom = AppDimensions.smallPadding),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.order_section_products),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateGray400,
                                    letterSpacing = 1.sp,
                                )
                                val itemCount = form.items.size
                                if (itemCount > 0) {
                                    Spacer(modifier = Modifier.width(AppDimensions.smallPadding))
                                    Text(
                                        text = stringResource(R.string.items_count, itemCount),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(AppDimensions.smallCornerRadius))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                            .padding(horizontal = 10.dp, vertical = 2.dp),
                                    )
                                }
                            }

                            TextButton(
                                onClick = onAddProductClick,
                                enabled = isScreenInteractive,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.order_add_product),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (form.items.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = AppDimensions.largePadding),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                StateViewSpotIllustration(
                                    Icons.Outlined.Inventory2,
                                    containerSize = AppDimensions.mediumSpotIllustrationSize,
                                    iconSize = AppDimensions.mediumSpotIllustrationIconSize,
                                )
                                Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))
                                Text(
                                    text = stringResource(R.string.order_no_products_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(modifier = Modifier.height(AppDimensions.extraSmallPadding))
                                Text(
                                    text = stringResource(R.string.order_no_products_subtitle),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SlateGray500,
                                )
                            }
                        } else {
                            form.items.forEachIndexed { index, item ->
                                OrderProductListItem(
                                    name = item.productName,
                                    price = item.price,
                                    quantity = item.quantity,
                                    imageUrl = item.thumbnailUrl,
                                    onQuantityChange = { newQty -> onUpdateItemQuantity(index, newQty) },
                                    onRemove = { onRemoveItem(index) },
                                    isCard = false
                                )
                                if (index < form.items.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = AppDimensions.mediumLargePadding),
                                        color = SlateGray200
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(AppDimensions.smallPadding)) }
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
private fun OrderManualContentPreview() {
    MiEmpresaTheme {
        OrderManualContent(
            form = OrderFormState(
                customerName = "John Doe",
                customerPhone = "1234567890",
                items = listOf(
                    OrderFormItem(
                        productId = "1",
                        productName = "Product A",
                        price = 10.0,
                        quantity = 2
                    ),
                    OrderFormItem(
                        productId = "2",
                        productName = "Product B",
                        price = 20.0,
                        quantity = 1
                    )
                )
            )
        )
    }
}
