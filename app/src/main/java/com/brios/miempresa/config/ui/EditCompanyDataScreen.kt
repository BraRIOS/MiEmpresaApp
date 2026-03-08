package com.brios.miempresa.config.ui

import androidx.activity.compose.BackHandler
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.components.CompanyAvatar
import com.brios.miempresa.core.ui.components.FormFieldGroup
import com.brios.miempresa.core.ui.components.FormOutlinedTextField
import com.brios.miempresa.core.ui.components.buildLimitSupportingText
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray500
import com.brios.miempresa.core.ui.components.CountryCodeDropdown
import com.brios.miempresa.navigation.rememberScreenActionGuard


@Composable
fun EditCompanyDataScreen(
    onNavigateBack: () -> Unit,
    viewModel: ConfigViewModel = hiltViewModel(),
) {
    val screenActionGuard = rememberScreenActionGuard()
    val isScreenInteractive = screenActionGuard.isScreenInteractive

    val form by viewModel.form.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSaving = uiState is ConfigUiState.Saving

    BackHandler(enabled = !isSaving) {
        screenActionGuard.runAndNavigate(onNavigateBack)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.toString()?.let { viewModel.updateLocalLogoUri(it) }
    }

    EditCompanyDataContent(
        form = form,
        isSaving = isSaving,
        isScreenInteractive = isScreenInteractive,
        onUpdateName = viewModel::updateCompanyName,
        onUpdateCountryCode = viewModel::updateCountryCode,
        onUpdateWhatsapp = viewModel::updateWhatsappNumber,
        onUpdateSpecialization = viewModel::updateSpecialization,
        onUpdateAddress = viewModel::updateAddress,
        onUpdateBusinessHours = viewModel::updateBusinessHours,
        onPickLogo = { screenActionGuard.runIfActive { imagePickerLauncher.launch("image/*") } },
        onSave = {
            screenActionGuard.runAndNavigate {
                viewModel.save()
                onNavigateBack()
            }
        },
        onCancel = { screenActionGuard.runAndNavigate(onNavigateBack) },
    )
}

@Composable
fun EditCompanyDataContent(
    form: ConfigFormState,
    isSaving: Boolean = false,
    isScreenInteractive: Boolean = true,
    onUpdateName: (String) -> Unit = {},
    onUpdateCountryCode: (String) -> Unit = {},
    onUpdateWhatsapp: (String) -> Unit = {},
    onUpdateSpecialization: (String) -> Unit = {},
    onUpdateAddress: (String) -> Unit = {},
    onUpdateBusinessHours: (String) -> Unit = {},
    onPickLogo: () -> Unit = {},
    onSave: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
    val formColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            .copy(alpha = 0.8f),
        focusedTextColor = MaterialTheme.colorScheme.onBackground
    )
    val companyNameSupportingText =
        buildLimitSupportingText(
            valueLength = form.companyName.length,
            maxLength = ConfigFormState.MAX_COMPANY_NAME,
            errorText = form.companyNameError,
        )
    val whatsappSupportingText =
        buildLimitSupportingText(
            valueLength = form.whatsappNumber.length,
            maxLength = ConfigFormState.MAX_WHATSAPP_NUMBER,
            errorText = form.whatsappError,
        )
    val specializationSupportingText =
        buildLimitSupportingText(
            valueLength = form.specialization.length,
            maxLength = ConfigFormState.MAX_SPECIALIZATION,
        )
    val addressSupportingText =
        buildLimitSupportingText(
            valueLength = form.address.length,
            maxLength = ConfigFormState.MAX_ADDRESS,
        )
    val businessHoursSupportingText =
        buildLimitSupportingText(
            valueLength = form.businessHours.length,
            maxLength = ConfigFormState.MAX_BUSINESS_HOURS,
        )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        // TopBar: Cancelar | Editar Empresa | Guardar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppDimensions.mediumPadding,
                    vertical = AppDimensions.mediumPadding,
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.config_edit_cancel),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = SlateGray500,
                modifier = Modifier.clickable(enabled = isScreenInteractive) { onCancel() },
            )
            Text(
                text = stringResource(R.string.config_edit_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.save),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (form.isFormValid && !isSaving) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                },
                modifier = Modifier.clickable(
                    enabled = form.isFormValid && !isSaving && isScreenInteractive,
                ) { onSave() },
            )
        }

        // Scrollable form content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = AppDimensions.largePadding),
        ) {
            Spacer(modifier = Modifier.height(AppDimensions.largePadding))

            // Avatar with camera overlay
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box {
                    CompanyAvatar(
                        companyName = form.companyName,
                        logoUrl = form.localLogoUri ?: form.logoUrl,
                        size = AppDimensions.Config.companyLogoSize,
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-2).dp, y = (-2).dp)
                            .size(AppDimensions.mediumLargeIconSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable(enabled = isScreenInteractive) { onPickLogo() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = stringResource(R.string.config_change_logo),
                            tint = Color.White,
                            modifier = Modifier.size(AppDimensions.defaultSmallIconSize),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.extraLargePadding))

            // Name (required)
            FormFieldGroup(
                label = stringResource(R.string.name_label),
                required = true,
            ) {
                FormOutlinedTextField(
                    value = form.companyName,
                    onValueChange = onUpdateName,
                    placeholder = stringResource(R.string.placeholder_company_name),
                    leadingIcon = Icons.Outlined.Store,
                    isError = form.companyNameError != null,
                    supportingText = companyNameSupportingText,
                    colors = formColors,
                )
            }

            Spacer(modifier = Modifier.height(AppDimensions.largePadding))

            // WhatsApp (required)
            FormFieldGroup(
                label = stringResource(R.string.whatsapp_label),
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
                        colors = formColors
                    )
                    FormOutlinedTextField(
                        value = form.whatsappNumber,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }
                            onUpdateWhatsapp(filtered)
                        },
                        placeholder = stringResource(R.string.placeholder_whatsapp),
                        leadingIcon = Icons.Outlined.Sms,
                        isError = form.whatsappError != null,
                        supportingText = whatsappSupportingText,
                        keyboardType = KeyboardType.Phone,
                        colors = formColors,
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.largePadding))

            // Specialization
            FormFieldGroup(label = stringResource(R.string.config_label_specialization)) {
                FormOutlinedTextField(
                    value = form.specialization,
                    onValueChange = onUpdateSpecialization,
                    placeholder = stringResource(R.string.placeholder_specialization),
                    leadingIcon = Icons.Outlined.Category,
                    supportingText = specializationSupportingText,
                    colors = formColors,
                )
            }

            Spacer(modifier = Modifier.height(AppDimensions.largePadding))

            // Address
            FormFieldGroup(label = stringResource(R.string.config_label_address)) {
                FormOutlinedTextField(
                    value = form.address,
                    onValueChange = onUpdateAddress,
                    placeholder = stringResource(R.string.placeholder_address),
                    leadingIcon = Icons.Outlined.LocationOn,
                    supportingText = addressSupportingText,
                    colors = formColors,
                )
            }

            Spacer(modifier = Modifier.height(AppDimensions.largePadding))

            // Business hours
            FormFieldGroup(label = stringResource(R.string.config_label_hours)) {
                FormOutlinedTextField(
                    value = form.businessHours,
                    onValueChange = onUpdateBusinessHours,
                    placeholder = stringResource(R.string.placeholder_hours),
                    leadingIcon = Icons.Outlined.Schedule,
                    supportingText = businessHoursSupportingText,
                    colors = formColors,
                )
            }

            Spacer(modifier = Modifier.height(AppDimensions.extraLargePadding))
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_7_PRO)
@Composable
private fun EditCompanyDataPreview() {
    MiEmpresaTheme {
        EditCompanyDataContent(
            form = ConfigFormState(
                companyName = "MiEmpresa",
                whatsappCountryCode = "+54",
                whatsappNumber = "1112345678",
                specialization = "Vinos y Licores",
                address = "Av. Reforma 123",
                businessHours = "Lun-Vie 9am - 6pm",
            ),
        )
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_7_PRO)
@Composable
private fun EditCompanyDataEmptyPreview() {
    MiEmpresaTheme {
        EditCompanyDataContent(form = ConfigFormState())
    }
}
