package com.brios.miempresa.config.ui

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.components.CompanyAvatar
import com.brios.miempresa.core.ui.components.FormFieldGroup
import com.brios.miempresa.core.ui.components.FormOutlinedTextField
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray100
import com.brios.miempresa.core.ui.theme.SlateGray200
import com.brios.miempresa.core.ui.theme.SlateGray300
import com.brios.miempresa.core.ui.theme.SlateGray400
import com.brios.miempresa.core.ui.theme.SlateGray500
import com.brios.miempresa.onboarding.ui.components.CountryCodeDropdown

private val Blue50 = Color(0xFFEFF6FF)
private val Blue500 = Color(0xFF3B82F6)
private val avatarSize = 112.dp
private val cameraOverlaySize = 36.dp

@Composable
fun ConfigScreen(
    modifier: Modifier = Modifier,
    viewModel: ConfigViewModel = hiltViewModel(),
    onNavigateToOrders: () -> Unit = {},
    onShowShareSheet: () -> Unit = {},
    onNavigateToWelcome: () -> Unit = {},
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val activity = LocalActivity.current as Activity

    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.toString()?.let { viewModel.updateLocalLogoUri(it) }
    }

    ConfigScreenContent(
        modifier = modifier,
        form = form,
        isSaving = uiState is ConfigUiState.Saving,
        isSyncing = isSyncing,
        onUpdateName = viewModel::updateCompanyName,
        onUpdateCountryCode = viewModel::updateCountryCode,
        onUpdateWhatsapp = viewModel::updateWhatsappNumber,
        onUpdateSpecialization = viewModel::updateSpecialization,
        onUpdateAddress = viewModel::updateAddress,
        onUpdateBusinessHours = viewModel::updateBusinessHours,
        onPickLogo = { imagePickerLauncher.launch("image/*") },
        onNavigateToOrders = onNavigateToOrders,
        onShowShareSheet = onShowShareSheet,
        onSyncNow = viewModel::syncNow,
        onLogoutClick = { showLogoutDialog = true },
    )

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
                    onNavigateToWelcome()
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
}

@Composable
fun ConfigScreenContent(
    modifier: Modifier = Modifier,
    form: ConfigFormState,
    isSaving: Boolean = false,
    isSyncing: Boolean = false,
    onUpdateName: (String) -> Unit = {},
    onUpdateCountryCode: (String) -> Unit = {},
    onUpdateWhatsapp: (String) -> Unit = {},
    onUpdateSpecialization: (String) -> Unit = {},
    onUpdateAddress: (String) -> Unit = {},
    onUpdateBusinessHours: (String) -> Unit = {},
    onPickLogo: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onShowShareSheet: () -> Unit = {},
    onSyncNow: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppDimensions.largePadding),
    ) {
        Spacer(modifier = Modifier.height(AppDimensions.smallPadding))

        // Avatar with camera overlay
        AvatarSection(
            companyName = form.companyName,
            logoUrl = form.localLogoUri ?: form.logoUrl,
            onPickLogo = onPickLogo,
        )

        Spacer(modifier = Modifier.height(AppDimensions.extraLargePadding))

        // Company info section
        SectionHeader(title = stringResource(R.string.config_section_info))

        Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))

        // Name (required)
        FormFieldGroup(
            label = stringResource(R.string.config_label_name),
            required = true,
        ) {
            FormOutlinedTextField(
                value = form.companyName,
                onValueChange = onUpdateName,
                placeholder = stringResource(R.string.placeholder_company_name),
                leadingIcon = Icons.Outlined.Category,
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))

        // WhatsApp (required)
        FormFieldGroup(
            label = stringResource(R.string.config_label_whatsapp),
            required = true,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
                verticalAlignment = Alignment.Top,
            ) {
                CountryCodeDropdown(
                    selectedCode = form.whatsappCountryCode,
                    onCodeSelected = onUpdateCountryCode,
                )
                FormOutlinedTextField(
                    value = form.whatsappNumber,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }
                        onUpdateWhatsapp(filtered)
                    },
                    placeholder = stringResource(R.string.placeholder_whatsapp),
                    leadingIcon = Icons.Outlined.Category,
                    keyboardType = KeyboardType.Phone,
                )
            }
        }

        Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))

        // Specialization
        FormFieldGroup(label = stringResource(R.string.config_label_specialization)) {
            FormOutlinedTextField(
                value = form.specialization,
                onValueChange = onUpdateSpecialization,
                placeholder = stringResource(R.string.placeholder_specialization),
                leadingIcon = Icons.Outlined.Category,
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))

        // Address
        FormFieldGroup(label = stringResource(R.string.config_label_address)) {
            FormOutlinedTextField(
                value = form.address,
                onValueChange = onUpdateAddress,
                placeholder = stringResource(R.string.placeholder_address),
                leadingIcon = Icons.Outlined.LocationOn,
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))

        // Business hours
        FormFieldGroup(label = stringResource(R.string.config_label_hours)) {
            FormOutlinedTextField(
                value = form.businessHours,
                onValueChange = onUpdateBusinessHours,
                placeholder = stringResource(R.string.placeholder_hours),
                leadingIcon = Icons.Outlined.Schedule,
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.extraLargePadding))

        // Actions section
        SectionHeader(title = stringResource(R.string.config_section_actions))

        Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))

        ActionCard(
            title = stringResource(R.string.config_action_orders),
            icon = Icons.Outlined.ReceiptLong,
            iconBackgroundColor = Blue50,
            iconTint = Blue500,
            onClick = onNavigateToOrders,
        )

        Spacer(modifier = Modifier.height(AppDimensions.mediumSmallPadding))

        ActionCard(
            title = stringResource(R.string.config_action_share),
            icon = Icons.Outlined.Share,
            iconBackgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            iconTint = MaterialTheme.colorScheme.primary,
            onClick = onShowShareSheet,
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
            text = stringResource(R.string.config_version),
            style = MaterialTheme.typography.labelSmall,
            color = SlateGray400,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 10.sp,
        )

        Spacer(modifier = Modifier.height(AppDimensions.extraLargePadding))
    }
}

@Composable
private fun AvatarSection(
    companyName: String,
    logoUrl: String?,
    onPickLogo: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box {
            CompanyAvatar(
                companyName = companyName,
                logoUrl = logoUrl,
                size = avatarSize,
            )
            // Camera overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .size(cameraOverlaySize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onPickLogo() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = stringResource(R.string.config_change_logo),
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
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
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateGray100),
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
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
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
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateGray200),
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
            ),
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
