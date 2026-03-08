package com.brios.miempresa.onboarding.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.components.CompanyAvatar
import com.brios.miempresa.core.ui.components.CountryCodeDropdown
import com.brios.miempresa.core.ui.components.FormFieldGroup
import com.brios.miempresa.core.ui.components.FormLabel
import com.brios.miempresa.core.ui.components.FormOutlinedTextField
import com.brios.miempresa.core.ui.components.buildLimitSupportingText
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray100
import com.brios.miempresa.core.ui.theme.SlateGray200
import com.brios.miempresa.core.ui.theme.SlateGray400
import com.brios.miempresa.core.ui.theme.SlateGray500
import com.brios.miempresa.core.ui.theme.SlateGray700
import com.brios.miempresa.core.ui.theme.SuccessGreen
import com.brios.miempresa.onboarding.ui.OnboardingFormState

private val cardShape = RoundedCornerShape(AppDimensions.mediumCornerRadius)
private val cardBorder = BorderStroke(1.dp, SlateGray100)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CompanyFormStep(
    form: OnboardingFormState,
    onUpdateName: (String) -> Unit,
    onUpdateCountryCode: (String) -> Unit,
    onUpdateWhatsapp: (String) -> Unit,
    onUpdateSpecialization: (String) -> Unit,
    onUpdateLogoUri: (String?) -> Unit,
    onUpdateAddress: (String) -> Unit,
    onUpdateBusinessHours: (String) -> Unit,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var detailsExpanded by rememberSaveable { mutableStateOf(false) }
    var showCancelDialog by rememberSaveable { mutableStateOf(false) }

    val logoInteractionSource = remember { MutableInteractionSource() }
    val isLogoPressed by logoInteractionSource.collectIsPressedAsState()

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            uri?.toString()?.let { onUpdateLogoUri(it) }
        }

    val companyNameSupportingText =
        buildLimitSupportingText(
            valueLength = form.companyName.length,
            maxLength = OnboardingFormState.MAX_COMPANY_NAME,
            errorText = form.companyNameError?.let { stringResource(R.string.onboarding_name_required) },
        )
    val whatsappSupportingText =
        buildLimitSupportingText(
            valueLength = form.whatsappNumber.length,
            maxLength = OnboardingFormState.MAX_WHATSAPP_NUMBER,
            errorText = form.whatsappError?.let { stringResource(R.string.onboarding_whatsapp_invalid) },
        )
    val specializationSupportingText =
        buildLimitSupportingText(
            valueLength = form.specialization.length,
            maxLength = OnboardingFormState.MAX_SPECIALIZATION,
        )
    val addressSupportingText =
        buildLimitSupportingText(
            valueLength = form.address.length,
            maxLength = OnboardingFormState.MAX_ADDRESS,
        )
    val businessHoursSupportingText =
        buildLimitSupportingText(
            valueLength = form.businessHours.length,
            maxLength = OnboardingFormState.MAX_BUSINESS_HOURS,
        )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(
                            horizontal = AppDimensions.mediumPadding,
                            vertical = AppDimensions.mediumPadding,
                        ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.onboarding_cancel),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateGray500,
                    modifier = Modifier.clickable { showCancelDialog = true },
                )
            }
        },
        bottomBar = {
            Button(
                onClick = onContinue,
                enabled = form.isFormValid,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = AppDimensions.mediumPadding)
                        .padding(
                            top = AppDimensions.smallPadding,
                            bottom = AppDimensions.mediumPadding,
                        )
                        .height(AppDimensions.OnboardingSuccess.ctaButtonHeight),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_continue),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(AppDimensions.smallPadding))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(AppDimensions.defaultSmallIconSize),
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = AppDimensions.mediumPadding)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .imeNestedScroll(),
        ) {
            // Hero text
            Text(
                text = stringResource(R.string.onboarding_step1_title_line1),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.onboarding_step1_title_line2),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(AppDimensions.smallPadding))
            Text(
                text = stringResource(R.string.onboarding_step1_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                color = SlateGray500,
            )

            Spacer(modifier = Modifier.height(AppDimensions.largePadding))

            // Card 1 — Company info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                shape = cardShape,
                border = cardBorder,
            ) {
                Column(
                    modifier = Modifier.padding(AppDimensions.mediumLargePadding),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.largePadding),
                ) {
                    // Company name
                        FormFieldGroup(
                            label = stringResource(R.string.label_company_name),
                            required = true,
                        ) {
                            FormOutlinedTextField(
                            value = form.companyName,
                            onValueChange = onUpdateName,
                                placeholder = stringResource(R.string.placeholder_company_name),
                                leadingIcon = Icons.Outlined.Store,
                                isError = form.companyNameError != null,
                                supportingText = companyNameSupportingText,
                            )
                        }

                    // WhatsApp
                    FormFieldGroup(
                        label = stringResource(R.string.label_whatsapp),
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
                                    // Filter to digits and dashes only
                                    val filtered = input.filter { it.isDigit() }
                                    onUpdateWhatsapp(filtered)
                                },
                                placeholder = stringResource(R.string.placeholder_whatsapp),
                                leadingIcon = Icons.Outlined.Sms,
                                isError = form.whatsappError != null,
                                supportingText = whatsappSupportingText,
                                keyboardType = KeyboardType.Phone,
                            )
                        }
                        // Section helper text with info icon (outside the field)
                        Row(
                            modifier = Modifier.padding(start = AppDimensions.extraSmallPadding, top = AppDimensions.smallPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppDimensions.extraSmallPadding),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = SlateGray400,
                            )
                            Text(
                                text = stringResource(R.string.onboarding_whatsapp_helper),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = SlateGray400,
                            )
                        }
                    }

                    // Specialization
                    FormFieldGroup(
                        label = stringResource(R.string.label_specialization),
                    ) {
                        FormOutlinedTextField(
                            value = form.specialization,
                            onValueChange = onUpdateSpecialization,
                            placeholder = stringResource(R.string.placeholder_specialization),
                            leadingIcon = Icons.Outlined.Category,
                            supportingText = specializationSupportingText,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))

            // Card 2 — Logo
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                shape = cardShape,
                border = cardBorder,
            ) {
                Column(
                    modifier = Modifier.padding(AppDimensions.mediumLargePadding),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        FormLabel(
                            text = stringResource(R.string.label_logo),
                        )
                        Text(
                            text = stringResource(R.string.onboarding_logo_recommended),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = SuccessGreen,
                            modifier =
                                Modifier
                                    .background(
                                        color = SuccessGreen.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(AppDimensions.extraSmallPadding),
                                    )
                                    .padding(
                                        horizontal = AppDimensions.smallPadding,
                                        vertical = AppDimensions.extraSmallPadding,
                                    ),
                        )
                    }

                    Spacer(modifier = Modifier.height(AppDimensions.largePadding))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
                    ) {
                        CompanyAvatar(
                            companyName = form.companyName,
                            logoUrl = form.logoUri,
                            size = AppDimensions.Drawer.companyLogoSize,
                        )

                        val backgroundRipple =
                            RippleConfiguration(
                                color = MaterialTheme.colorScheme.background,
                            )
                        CompositionLocalProvider(
                            LocalRippleConfiguration provides backgroundRipple,
                        ) {
                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                                border = BorderStroke(1.dp, SlateGray200),
                                interactionSource = logoInteractionSource,
                                colors =
                                    ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isLogoPressed) MaterialTheme.colorScheme.background else Color.Transparent,
                                    ),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(AppDimensions.defaultSmallIconSize),
                                    tint = if (isLogoPressed) MaterialTheme.colorScheme.primary else SlateGray500,
                                )
                                Spacer(modifier = Modifier.width(AppDimensions.smallPadding))
                                Text(
                                    text = stringResource(R.string.onboarding_logo_open_gallery),
                                    fontWeight = FontWeight.Bold,
                                    color = SlateGray700,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))

            // Card 3 — Additional details (collapsible)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                shape = cardShape,
                border = cardBorder,
            ) {
                Column {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { detailsExpanded = !detailsExpanded }
                                .padding(AppDimensions.mediumLargePadding),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.additional_details),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SlateGray700,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
                        ) {
                            Text(
                                text = stringResource(R.string.optional_badge),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = SlateGray400,
                                modifier =
                                    Modifier
                                        .background(
                                            color = SlateGray200.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(AppDimensions.extraSmallPadding),
                                        )
                                        .padding(
                                            horizontal = AppDimensions.smallPadding,
                                            vertical = AppDimensions.extraSmallPadding,
                                        ),
                            )
                            Icon(
                                imageVector =
                                    if (detailsExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                contentDescription = null,
                                tint = SlateGray400,
                            )
                        }
                    }

                    AnimatedVisibility(visible = detailsExpanded) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumLargePadding),
                            modifier =
                                Modifier
                                    .padding(horizontal = AppDimensions.mediumLargePadding)
                                    .padding(bottom = AppDimensions.mediumLargePadding),
                        ) {
                            FormFieldGroup(label = stringResource(R.string.label_address)) {
                                FormOutlinedTextField(
                                    value = form.address,
                                    onValueChange = onUpdateAddress,
                                    placeholder = stringResource(R.string.placeholder_address),
                                    leadingIcon = Icons.Outlined.LocationOn,
                                    supportingText = addressSupportingText,
                                )
                            }
                            FormFieldGroup(label = stringResource(R.string.label_hours)) {
                                FormOutlinedTextField(
                                    value = form.businessHours,
                                    onValueChange = onUpdateBusinessHours,
                                    placeholder = stringResource(R.string.placeholder_hours),
                                    leadingIcon = Icons.Outlined.Schedule,
                                    supportingText = businessHoursSupportingText,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.largePadding))
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(stringResource(R.string.onboarding_cancel_dialog_title)) },
            text = { Text(stringResource(R.string.onboarding_cancel_dialog_message)) },
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            containerColor = MaterialTheme.colorScheme.background,
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    onCancel()
                }) {
                    Text(stringResource(R.string.onboarding_cancel_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text(stringResource(R.string.onboarding_cancel_dialog_dismiss))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, device = Devices.PIXEL_7_PRO)
@Composable
fun CompanyFormStepPreview() {
    MiEmpresaTheme {
        CompanyFormStep(
            form = OnboardingFormState(),
            onUpdateName = {},
            onUpdateCountryCode = {},
            onUpdateWhatsapp = {},
            onUpdateSpecialization = {},
            onUpdateLogoUri = {},
            onUpdateAddress = {},
            onUpdateBusinessHours = {},
            onContinue = {},
            onCancel = {},
        )
    }
}
