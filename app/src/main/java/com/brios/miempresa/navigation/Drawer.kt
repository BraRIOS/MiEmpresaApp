package com.brios.miempresa.navigation

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.brios.miempresa.R
import com.brios.miempresa.auth.ui.SignInViewModel
import com.brios.miempresa.core.auth.UserData
import com.brios.miempresa.core.data.local.entities.Company
import com.brios.miempresa.core.ui.components.CompanyAvatar
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import kotlinx.coroutines.launch

@Composable
fun DrawerComposable(
    navController: NavHostController,
    drawerState: DrawerState,
    signInViewModel: SignInViewModel? = null,
    content: @Composable () -> Unit,
) {
    val viewModel = signInViewModel ?: if (!LocalInspectionMode.current) hiltViewModel() else null
    val user = viewModel?.getSignedInUser()
    val selectedCompanyState = viewModel?.getSelectedCompany()?.observeAsState()
    val selectedCompany: Company? by selectedCompanyState ?: remember { mutableStateOf(null) }
    val context = LocalActivity.current
    DrawerContent(drawerState, user, selectedCompany, context, viewModel, navController, content)
}

@Composable
private fun DrawerContent(
    drawerState: DrawerState,
    user: UserData?,
    selectedCompany: Company?,
    context: Activity?,
    signInViewModel: SignInViewModel?,
    navController: NavHostController,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.7f),
                drawerShape =
                    RoundedCornerShape(
                        topEnd = AppDimensions.Drawer.drawerCornerRadius,
                        bottomEnd = AppDimensions.Drawer.drawerCornerRadius,
                    ),
                drawerContainerColor = MaterialTheme.colorScheme.background,
                windowInsets = WindowInsets(0),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // Header: Company logo + name
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            Color.Transparent,
                                        ),
                                    ),
                                )
                                .windowInsetsPadding(WindowInsets.statusBars)
                                .padding(
                                    horizontal = AppDimensions.largePadding,
                                    vertical = AppDimensions.largePadding,
                                ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Company avatar
                        CompanyAvatar(
                            companyName = selectedCompany?.name
                                ?: stringResource(R.string.app_name),
                            logoUrl = selectedCompany?.logoUrl,
                            size = AppDimensions.Drawer.companyLogoSize,
                        )
                        Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))
                        Text(
                            text =
                                selectedCompany?.name
                                    ?: stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    HorizontalDivider(color= MaterialTheme.colorScheme.outline)

                    // Menu items
                    Column(
                        modifier =
                            Modifier
                                .weight(1f)
                                .padding(
                                    horizontal = AppDimensions.mediumSmallPadding,
                                    vertical = AppDimensions.mediumPadding,
                                ),
                        verticalArrangement = Arrangement.spacedBy(AppDimensions.extraSmallPadding),
                    ) {
                        // Switch company - goes to CompanySelector (CompanyListView)
                        DrawerMenuItem(
                            icon = Icons.Filled.Domain,
                            label = stringResource(R.string.switch_company),
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("${MiEmpresaScreen.Onboarding.name}?mode=selector")
                                }
                            },
                        )

                        // Create another company - goes to WizardStep1 (first step)
                        DrawerMenuItem(
                            icon = Icons.Filled.AddBusiness,
                            label = stringResource(R.string.create_another_company),
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("${MiEmpresaScreen.Onboarding.name}?mode=create")
                                }
                            },
                        )

                        HorizontalDivider(
                            modifier =
                                Modifier.padding(
                                    horizontal = AppDimensions.mediumPadding,
                                    vertical = AppDimensions.smallPadding,
                                ),
                        )

                        // Sign out
                        DrawerMenuItem(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            label = stringResource(R.string.log_out),
                            tint = MaterialTheme.colorScheme.error,
                            onClick = {
                                scope.launch { drawerState.close() }
                                showLogoutDialog = true
                            },
                        )
                    }

                    // Footer: User info
                    user?.let {
                        HorizontalDivider()
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(AppDimensions.mediumPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumSmallPadding),
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(model = user.profilePictureUrl),
                                contentDescription = stringResource(R.string.profile_picture),
                                modifier =
                                    Modifier
                                        .size(AppDimensions.Drawer.userAvatarSize)
                                        .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text =
                                        user.username
                                            ?: stringResource(id = R.string.user),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                user.email?.let { email ->
                                    Text(
                                        text = email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
    ) { content() }

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
                    if (signInViewModel != null && context != null) {
                        signInViewModel.signOut(context)
                        navController.navigate(MiEmpresaScreen.Welcome.name) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
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
private fun DrawerMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit,
) {
    val backgroundColor =
        if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        } else {
            androidx.compose.ui.graphics.Color.Transparent
        }
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else tint
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(backgroundColor)
                .clickable(
                    onClick = onClick,
                    role = androidx.compose.ui.semantics.Role.Button,
                )
                .padding(
                    horizontal = AppDimensions.mediumPadding,
                    vertical = AppDimensions.mediumSmallPadding,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = fontWeight,
            color = contentColor,
        )
    }
}

@Preview
@Composable
fun DrawerContentPreview() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
    val scope = rememberCoroutineScope()
    MiEmpresaTheme {
    DrawerContent(
        drawerState = drawerState,
        UserData("Test", "Test username", "test@test.com", "url"),
        Company("Test", "Test Company", selected = true),
        null,
        null,
        NavHostController(LocalContext.current),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = { scope.launch { drawerState.open() } },
                ) {
                    Text(text = "Open Drawer")
                }
            }
        }
    }
        }
}
