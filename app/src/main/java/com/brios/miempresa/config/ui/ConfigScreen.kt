package com.brios.miempresa.config.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.BuildConfig
import com.brios.miempresa.R
import com.brios.miempresa.core.domain.model.defaultCountryCodes
import com.brios.miempresa.core.ui.components.CompanyAvatar
import com.brios.miempresa.core.ui.components.InfoCard
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.Blue50
import com.brios.miempresa.core.ui.theme.Blue500
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray100
import com.brios.miempresa.core.ui.theme.SlateGray200
import com.brios.miempresa.core.ui.theme.SlateGray300
import com.brios.miempresa.core.ui.theme.SlateGray400
import com.brios.miempresa.core.ui.theme.SlateGray500
import com.brios.miempresa.core.ui.theme.SuccessGreen
import com.brios.miempresa.core.util.QrCodeGenerator
import com.brios.miempresa.core.util.QrCodeResult

private const val DEEPLINK_PREFIX = "miempresa://catalogo?sheetId="

@Composable
fun ConfigScreen(
    modifier: Modifier = Modifier,
    viewModel: ConfigViewModel = hiltViewModel(),
    onNavigateToEditCompany: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onNavigateToWelcome: () -> Unit = {},
    onNavigateToClientCatalog: (String) -> Unit = {},
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val publicSheetId by viewModel.publicSheetId.collectAsStateWithLifecycle()
    val companyId by viewModel.companyId.collectAsStateWithLifecycle()
    val activity = LocalActivity.current as Activity

    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var showQrSheet by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ConfigEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                ConfigEvent.NavigateToWelcome -> onNavigateToWelcome()
                ConfigEvent.NavigateToOrders -> onNavigateToOrders()
                ConfigEvent.ShowShareSheet -> showQrSheet = true
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        ConfigScreenContent(
            modifier = Modifier.fillMaxSize(),
            form = form,
            publicSheetId = publicSheetId,
            isSyncing = isSyncing,
            onNavigateToEditCompany = onNavigateToEditCompany,
            onNavigateToOrders = viewModel::navigateToOrders,
            onShareCode = { sheetId ->
                val shareText = activity.getString(R.string.config_share_code_message, sheetId)
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                activity.startActivity(Intent.createChooser(sendIntent, null))
            },
            onGenerateQr = viewModel::showShareSheet,
            onViewCatalog = { companyId?.let { onNavigateToClientCatalog(it) } },
            onSyncNow = viewModel::syncNow,
            onLogoutClick = { showLogoutDialog = true },
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(AppDimensions.mediumPadding),
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.config_logout_title)) },
            text = { Text(stringResource(R.string.config_logout_message)) },
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            containerColor = MaterialTheme.colorScheme.background,
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.signOut(activity)
                }) {
                    Text(stringResource(R.string.config_logout_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.config_logout_dismiss))
                }
            },
        )
    }

    if (showQrSheet && publicSheetId != null) {
        QrCodeBottomSheet(
            publicSheetId = publicSheetId!!,
            onDismiss = { showQrSheet = false },
        )
    }
}

@Composable
fun ConfigScreenContent(
    modifier: Modifier = Modifier,
    form: ConfigFormState,
    publicSheetId: String? = null,
    isSyncing: Boolean = false,
    onNavigateToEditCompany: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onShareCode: (String) -> Unit = {},
    onGenerateQr: () -> Unit = {},
    onViewCatalog: () -> Unit = {},
    onSyncNow: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppDimensions.largePadding, vertical = AppDimensions.mediumPadding),
    ) {
        // Readonly avatar
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            CompanyAvatar(
                companyName = form.companyName,
                logoUrl = form.logoUrl,
                size = AppDimensions.Config.companyLogoSize,
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.largePadding))

        // Readonly company info card
        ReadonlyCompanyCard(form = form)

        Spacer(modifier = Modifier.height(AppDimensions.largePadding))

        // Edit button
        Button(
            onClick = onNavigateToEditCompany,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppDimensions.largeCornerRadius),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(AppDimensions.smallPadding))
            Text(
                text = stringResource(R.string.config_edit_info_button),
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.extraLargePadding))

        // Actions section
        SectionHeader(title = stringResource(R.string.config_section_actions))

        Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))

        // Mis Pedidos
        ActionCard(
            title = stringResource(R.string.config_action_orders),
            subtitle = stringResource(R.string.config_orders_subtitle),
            icon = Icons.AutoMirrored.Outlined.ReceiptLong,
            iconBackgroundColor = Blue50,
            iconTint = Blue500,
            onClick = onNavigateToOrders,
        )

        Spacer(modifier = Modifier.height(AppDimensions.mediumSmallPadding))

        // Compartir Catálogo with 2 inline buttons
        ShareCatalogCard(
            publicSheetId = publicSheetId,
            onShareCode = onShareCode,
            onGenerateQr = onGenerateQr,
        )

        Spacer(modifier = Modifier.height(AppDimensions.mediumSmallPadding))

        // Ver mi catálogo
        ActionCard(
            title = stringResource(R.string.config_view_catalog),
            subtitle = stringResource(R.string.config_view_catalog_subtitle),
            icon = Icons.Outlined.Visibility,
            iconBackgroundColor = SuccessGreen.copy(alpha = 0.1f),
            iconTint = SuccessGreen,
            onClick = onViewCatalog,
        )

        Spacer(modifier = Modifier.height(AppDimensions.extraLargePadding))

        // Sync button
        SyncButton(
            isSyncing = isSyncing,
            onClick = onSyncNow,
        )

        Spacer(modifier = Modifier.height(AppDimensions.mediumSmallPadding))

        // Logout button
        LogoutButton(onClick = onLogoutClick)

        Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))

        // Version
        Text(
            text = stringResource(R.string.config_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.labelSmall,
            color = SlateGray400,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun ReadonlyCompanyCard(form: ConfigFormState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
        border = BorderStroke(1.dp, SlateGray100),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(vertical = AppDimensions.smallPadding)) {
            ReadonlyRow(
                label = stringResource(R.string.name_label).uppercase(),
                value = form.companyName.ifBlank { "—" },
            )
            HorizontalDivider(color = SlateGray100)
            ReadonlyRow(
                label = stringResource(R.string.whatsapp_label).uppercase(),
                value = if (form.whatsappNumber.isNotBlank()) {
                    "${form.whatsappCountryCode} ${form.whatsappNumber}"
                } else {
                    "—"
                },
                emojiFlag =  if (form.whatsappNumber.isNotBlank())
                    defaultCountryCodes.find { it.dialCode == form.whatsappCountryCode }?.emoji else null,
            )
            HorizontalDivider(color = SlateGray100)
            ReadonlyRow(
                label = stringResource(R.string.config_label_specialization).uppercase(),
                value = form.specialization.ifBlank { "—" },
            )
            HorizontalDivider(color = SlateGray100)
            ReadonlyRow(
                label = stringResource(R.string.config_label_address).uppercase(),
                value = form.address.ifBlank { "—" },
            )
            HorizontalDivider(color = SlateGray100)
            ReadonlyRow(
                label = stringResource(R.string.config_label_hours).uppercase(),
                value = form.businessHours.ifBlank { "—" },
            )
        }
    }
}

@Composable
private fun ReadonlyRow(
    label: String,
    value: String,
    emojiFlag: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = AppDimensions.smallPadding,
                vertical = AppDimensions.mediumSmallPadding,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = SlateGray500,
            letterSpacing = 0.5.sp,
        )
        Spacer(modifier = Modifier.width(AppDimensions.mediumPadding))
        Text(
            text = if (emojiFlag != null) "$emojiFlag  $value" else value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}

@Composable
private fun ShareCatalogCard(
    publicSheetId: String?,
    onShareCode: (String) -> Unit,
    onGenerateQr: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
        border = BorderStroke(1.dp, SlateGray100),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(AppDimensions.mediumPadding),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(AppDimensions.defaultIconSize),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.config_action_share),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.config_share_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateGray400,
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.mediumSmallPadding))

            // Two inline buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
            ) {
                OutlinedButton(
                    onClick = { publicSheetId?.let { onShareCode(it) } },
                    enabled = publicSheetId != null,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                    border = BorderStroke(1.dp, SlateGray200),
                    contentPadding = PaddingValues(horizontal = AppDimensions.extraSmallPadding),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(AppDimensions.extraSmallPadding))
                    Text(
                        text = stringResource(R.string.config_share_code),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                OutlinedButton(
                    onClick = onGenerateQr,
                    enabled = publicSheetId != null,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                    border = BorderStroke(1.dp, SlateGray200),
                    contentPadding = PaddingValues(horizontal = AppDimensions.smallPadding),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.QrCode,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(AppDimensions.extraSmallPadding))
                    Text(
                        text = stringResource(R.string.config_share_qr),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = SlateGray500,
        letterSpacing = 1.sp,
    )
}

@Composable
private fun ActionCard(
    title: String,
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    subtitle: String? = null,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
        border = BorderStroke(1.dp, SlateGray100),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimensions.mediumPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(AppDimensions.defaultIconSize),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateGray400,
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SlateGray300,
            )
        }
    }
}

@Composable
private fun SyncButton(
    isSyncing: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = { if (!isSyncing) onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        ),
        shape = RoundedCornerShape(50),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Sync,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(modifier = Modifier.width(AppDimensions.smallPadding))
            Text(
                text = stringResource(R.string.config_sync_now),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
        border = BorderStroke(1.dp, SlateGray200),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Logout,
                contentDescription = null,
                tint = SlateGray500,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(AppDimensions.smallPadding))
            Text(
                text = stringResource(R.string.log_out),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = SlateGray500,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QrCodeBottomSheet(
    publicSheetId: String,
    onDismiss: () -> Unit,
) {
    val deeplink = remember(publicSheetId) { "$DEEPLINK_PREFIX$publicSheetId" }
    val qrBitmap = remember(deeplink) {
        val result = QrCodeGenerator().generate(deeplink)
        (result as? QrCodeResult.Success)?.bitmap
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimensions.extraLargePadding)
                .padding(bottom = AppDimensions.extraLargePadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.config_qr_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(AppDimensions.largePadding))

            // QR code
            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.share_qr_content_description),
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(AppDimensions.mediumCornerRadius)),
                )
            }

            Spacer(modifier = Modifier.height(AppDimensions.largePadding))

            // Google Lens helper text
            InfoCard(
                text = stringResource(R.string.config_qr_helper)
            )
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_7_PRO)
@Composable
private fun ConfigScreenPreview() {
    MiEmpresaTheme {
        ConfigScreenContent(
            form = ConfigFormState(
                companyName = "MiEmpresa",
                whatsappCountryCode = "+54",
                whatsappNumber = "1112345678",
                specialization = "Vinos y Licores",
                address = "Av. Reforma 123, CDMX",
                businessHours = "Lun-Vie 9am - 6pm",
            ),
            publicSheetId = "abc123",
        )
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_7_PRO)
@Composable
private fun ConfigScreenEmptyPreview() {
    MiEmpresaTheme {
        ConfigScreenContent(form = ConfigFormState())
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_7_PRO)
@Composable
private fun QrCodeBottomSheetPreview() {
    MiEmpresaTheme {
        QrCodeBottomSheet(
            publicSheetId = "sample-sheet-id",
            onDismiss = {},
        )
    }
}
