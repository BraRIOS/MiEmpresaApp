package com.brios.miempresa.onboarding.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.SuccessGreen
import com.brios.miempresa.onboarding.ui.OnboardingFormState

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

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            onUpdateLogoUri(uri?.toString())
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = AppDimensions.mediumPadding),
    ) {
        // Top bar: Cancel + Step indicator
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = AppDimensions.mediumPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.onboarding_cancel),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable(onClick = onCancel),
            )
            Text(
                text = stringResource(R.string.onboarding_step_indicator, 1, 3),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Scrollable content
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(AppDimensions.largePadding))

            // Hero text — split color
            Text(
                text = stringResource(R.string.onboarding_step1_title_line1),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.onboarding_step1_title_line2),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(AppDimensions.smallPadding))
            Text(
                text = stringResource(R.string.onboarding_step1_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(AppDimensions.largePadding))

            // Card 1 — Company info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
            ) {
                Column(
                    modifier = Modifier.padding(AppDimensions.mediumLargePadding),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.largePadding),
                ) {
                    // Company name field group
                    Column {
                        Text(
                            text = stringResource(R.string.label_company_name),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = AppDimensions.extraSmallPadding, bottom = AppDimensions.smallPadding),
                        )
                        OutlinedTextField(
                            value = form.companyName,
                            onValueChange = onUpdateName,
                            placeholder = {
                                Text(stringResource(R.string.placeholder_company_name))
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Store, contentDescription = null)
                            },
                            isError = form.companyNameError != null,
                            supportingText =
                                form.companyNameError?.let {
                                    { Text(stringResource(R.string.onboarding_name_required)) }
                                },
                            singleLine = true,
                            shape = RoundedCornerShape(AppDimensions.smallCornerRadius),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    // WhatsApp field group
                    Column {
                        Text(
                            text = stringResource(R.string.label_whatsapp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = AppDimensions.extraSmallPadding, bottom = AppDimensions.smallPadding),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumSmallPadding),
                            verticalAlignment = Alignment.Top,
                        ) {
                            CountryCodeDropdown(
                                selectedCode = form.whatsappCountryCode,
                                onCodeSelected = onUpdateCountryCode,
                            )

                            OutlinedTextField(
                                value = form.whatsappNumber,
                                onValueChange = onUpdateWhatsapp,
                                placeholder = {
                                    Text(stringResource(R.string.placeholder_whatsapp))
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Sms, contentDescription = null)
                                },
                                isError = form.whatsappError != null,
                                supportingText =
                                    if (form.whatsappError != null) {
                                        { Text(stringResource(R.string.onboarding_whatsapp_invalid)) }
                                    } else {
                                        { Text(stringResource(R.string.onboarding_whatsapp_helper)) }
                                    },
                                singleLine = true,
                                shape = RoundedCornerShape(AppDimensions.smallCornerRadius),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    // Specialization field group
                    Column {
                        Text(
                            text = stringResource(R.string.label_specialization),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = AppDimensions.extraSmallPadding, bottom = AppDimensions.smallPadding),
                        )
                        OutlinedTextField(
                            value = form.specialization,
                            onValueChange = onUpdateSpecialization,
                            placeholder = {
                                Text(stringResource(R.string.placeholder_specialization))
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Category, contentDescription = null)
                            },
                            supportingText = {
                                Text(stringResource(R.string.onboarding_specialization_helper))
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(AppDimensions.smallCornerRadius),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))

            // Card 2 — Logo
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
            ) {
                Column(
                    modifier = Modifier.padding(AppDimensions.mediumLargePadding),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.label_logo),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = AppDimensions.extraSmallPadding),
                        )
                        Text(
                            text = stringResource(R.string.onboarding_logo_recommended),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
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
                        // Logo preview
                        Box(
                            modifier =
                                Modifier
                                    .size(AppDimensions.itemCardImageSize)
                                    .clip(CircleShape)
                                    .border(
                                        AppDimensions.mediumBorderWidth,
                                        MaterialTheme.colorScheme.surface,
                                        CircleShape,
                                    )
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (form.logoUri != null) {
                                AsyncImage(
                                    model = form.logoUri,
                                    contentDescription =
                                        stringResource(R.string.label_logo),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                val initials =
                                    form.companyName
                                        .split(" ")
                                        .take(2)
                                        .joinToString("") { it.firstOrNull()?.uppercase() ?: "" }
                                        .ifEmpty { "?" }
                                Text(
                                    text = initials,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(AppDimensions.smallCornerRadius),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Image,
                                contentDescription = null,
                                modifier = Modifier.size(AppDimensions.smallIconSize),
                            )
                            Spacer(modifier = Modifier.width(AppDimensions.smallPadding))
                            Text(
                                text = stringResource(R.string.onboarding_logo_open_gallery),
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))

            // Card 3 — Additional details (collapsible)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
            ) {
                Column {
                    // Header row with "Opcional" badge
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
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
                        ) {
                            Text(
                                text = stringResource(R.string.optional_badge),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier =
                                    Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(AppDimensions.extraSmallPadding),
                                        )
                                        .padding(
                                            horizontal = AppDimensions.smallPadding,
                                            vertical = AppDimensions.extraSmallPadding,
                                        ),
                            )
                            Icon(
                                imageVector =
                                    if (detailsExpanded) {
                                        Icons.Outlined.ExpandLess
                                    } else {
                                        Icons.Outlined.ExpandMore
                                    },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            // Address
                            Column {
                                Text(
                                    text = stringResource(R.string.label_address),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = AppDimensions.extraSmallPadding, bottom = AppDimensions.smallPadding),
                                )
                                OutlinedTextField(
                                    value = form.address,
                                    onValueChange = onUpdateAddress,
                                    placeholder = {
                                        Text(stringResource(R.string.placeholder_address))
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.LocationOn,
                                            contentDescription = null,
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(AppDimensions.smallCornerRadius),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }

                            // Business hours
                            Column {
                                Text(
                                    text = stringResource(R.string.label_hours),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = AppDimensions.extraSmallPadding, bottom = AppDimensions.smallPadding),
                                )
                                OutlinedTextField(
                                    value = form.businessHours,
                                    onValueChange = onUpdateBusinessHours,
                                    placeholder = {
                                        Text(stringResource(R.string.placeholder_hours))
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.Schedule,
                                            contentDescription = null,
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(AppDimensions.smallCornerRadius),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
            }

            // Bottom padding so content doesn't hide behind the sticky button
            Spacer(modifier = Modifier.height(AppDimensions.largePadding))
        }

        // STICKY BOTTOM BUTTON (outside scroll)
        Button(
            onClick = onContinue,
            enabled = form.isFormValid,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(AppDimensions.OnboardingSuccess.ctaButtonHeight),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
        ) {
            Text(
                text = stringResource(R.string.onboarding_continue),
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.width(AppDimensions.smallPadding))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))
    }
}
