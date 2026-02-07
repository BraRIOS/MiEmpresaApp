package com.brios.miempresa.onboarding.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
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
                .verticalScroll(rememberScrollState())
                .padding(AppDimensions.mediumPadding),
        verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
    ) {
        // Hero text
        Text(
            text = stringResource(R.string.onboarding_hero),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        // Card 1 — Company info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        ) {
            Column(
                modifier = Modifier.padding(AppDimensions.mediumPadding),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumSmallPadding),
            ) {
                // Company name
                OutlinedTextField(
                    value = form.companyName,
                    onValueChange = onUpdateName,
                    label = { Text(stringResource(R.string.onboarding_company_name_hint)) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Business, contentDescription = null)
                    },
                    isError = form.companyNameError != null,
                    supportingText =
                        form.companyNameError?.let {
                            { Text(stringResource(R.string.onboarding_name_required)) }
                        },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                // WhatsApp: country code + number
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
                    verticalAlignment = Alignment.Top,
                ) {
                    CountryCodeDropdown(
                        selectedCode = form.whatsappCountryCode,
                        onCodeSelected = onUpdateCountryCode,
                    )

                    OutlinedTextField(
                        value = form.whatsappNumber,
                        onValueChange = onUpdateWhatsapp,
                        label = { Text(stringResource(R.string.onboarding_whatsapp_hint)) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Phone, contentDescription = null)
                        },
                        isError = form.whatsappError != null,
                        supportingText =
                            if (form.whatsappError != null) {
                                { Text(stringResource(R.string.onboarding_whatsapp_invalid)) }
                            } else {
                                { Text(stringResource(R.string.onboarding_whatsapp_helper)) }
                            },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }

                // Specialization
                OutlinedTextField(
                    value = form.specialization,
                    onValueChange = onUpdateSpecialization,
                    label = { Text(stringResource(R.string.onboarding_specialization_hint)) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Category, contentDescription = null)
                    },
                    supportingText = {
                        Text(stringResource(R.string.onboarding_specialization_helper))
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Card 2 — Logo
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        ) {
            Column(
                modifier = Modifier.padding(AppDimensions.mediumPadding),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumSmallPadding),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_logo_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.onboarding_logo_recommended),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
                ) {
                    // Logo preview
                    Box(
                        modifier =
                            Modifier
                                .size(AppDimensions.itemCardImageSize)
                                .clip(CircleShape)
                                .border(
                                    AppDimensions.smallBorderWidth,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    CircleShape,
                                )
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (form.logoUri != null) {
                            AsyncImage(
                                model = form.logoUri,
                                contentDescription = stringResource(R.string.onboarding_logo_title),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Image,
                                contentDescription = null,
                                modifier = Modifier.size(AppDimensions.mediumIconSize),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                    ) {
                        Text(stringResource(R.string.onboarding_logo_open_gallery))
                    }
                }
            }
        }

        // Card 3 — Additional details (collapsible)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        ) {
            Column(
                modifier = Modifier.padding(AppDimensions.mediumPadding),
            ) {
                TextButton(
                    onClick = { detailsExpanded = !detailsExpanded },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_details_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Icon(
                            imageVector =
                                if (detailsExpanded) {
                                    Icons.Outlined.ExpandLess
                                } else {
                                    Icons.Outlined.ExpandMore
                                },
                            contentDescription = null,
                        )
                    }
                }

                AnimatedVisibility(visible = detailsExpanded) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumSmallPadding),
                        modifier = Modifier.padding(top = AppDimensions.smallPadding),
                    ) {
                        OutlinedTextField(
                            value = form.address,
                            onValueChange = onUpdateAddress,
                            label = { Text(stringResource(R.string.onboarding_address_hint)) },
                            leadingIcon = {
                                Icon(Icons.Outlined.LocationOn, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        OutlinedTextField(
                            value = form.businessHours,
                            onValueChange = onUpdateBusinessHours,
                            label = { Text(stringResource(R.string.onboarding_hours_hint)) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Schedule, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Sticky continue button
        Button(
            onClick = onContinue,
            enabled = form.isFormValid,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(AppDimensions.largeIconSize),
        ) {
            Text(
                text = stringResource(R.string.onboarding_continue),
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))
    }
}
