package com.brios.miempresa.navigation

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navigation
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.brios.miempresa.R
import com.brios.miempresa.auth.domain.AuthState
import com.brios.miempresa.auth.ui.PostAuthDestination
import com.brios.miempresa.auth.ui.SignInScreen
import com.brios.miempresa.auth.ui.SignInViewModel
import com.brios.miempresa.auth.ui.WelcomeScreen
import com.brios.miempresa.cart.ui.CartScreen
import com.brios.miempresa.catalog.domain.CatalogAccessError
import com.brios.miempresa.catalog.ui.ClientCatalogScreen
import com.brios.miempresa.catalog.ui.DeeplinkErrorScreen
import com.brios.miempresa.catalog.ui.DeeplinkNavigationEvent
import com.brios.miempresa.catalog.ui.DeeplinkRoutingViewModel
import com.brios.miempresa.catalog.ui.MyStoresScreen
import com.brios.miempresa.categories.ui.CategoryFormScreen
import com.brios.miempresa.config.ui.EditCompanyDataScreen
import com.brios.miempresa.onboarding.ui.OnboardingScreen
import com.brios.miempresa.orders.ui.OrderDetailScreen
import com.brios.miempresa.orders.ui.OrderManualScreen
import com.brios.miempresa.orders.ui.OrdersListScreen
import com.brios.miempresa.products.ui.ProductDetailScreen
import com.brios.miempresa.products.ui.ProductFormScreen

@Composable
fun NavHostComposable(
    applicationContext: Context,
    navController: NavHostController,
    pendingDeeplinkSheetId: String? = null,
    onDeeplinkConsumed: (String) -> Unit = {},
) {
    val signInViewModel = hiltViewModel<SignInViewModel>()
    val deeplinkRoutingViewModel = hiltViewModel<DeeplinkRoutingViewModel>()
    val isAlreadySignedIn = signInViewModel.getSignedInUser() != null
    var suppressDefaultStartupRouting by rememberSaveable { mutableStateOf(pendingDeeplinkSheetId != null) }
    var checkedVisitedStores by rememberSaveable { mutableStateOf(false) }
    val navigationTapGuard = remember { NavigationTapGuard() }
    val guardedResumedNavigation: (NavBackStackEntry, () -> Unit) -> Unit = { entry, action ->
        if (
            entry.lifecycle.currentState == Lifecycle.State.RESUMED &&
            navigationTapGuard.canNavigateNow()
        ) {
            action()
        }
    }
    val guardedBackNavigation: (NavBackStackEntry, () -> Unit) -> Unit = { entry, action ->
        if (entry.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            action()
        }
    }
    val runWhenResumed: (NavBackStackEntry, () -> Unit) -> Unit = { entry, action ->
        if (entry.lifecycle.currentState == Lifecycle.State.RESUMED) {
            action()
        }
    }
    val signedOutEntryRoute = MiEmpresaRoutes.authGraph
    val signedInEntryRoute = MiEmpresaRoutes.home
    val resolveSessionEntryRoute: () -> String = {
        if (isAlreadySignedIn) signedInEntryRoute else signedOutEntryRoute
    }
    val navigateToSessionEntryClearingBackStack: () -> Unit = {
        navController.navigateClearingBackStack(resolveSessionEntryRoute())
    }
    val navigateToSignedOutEntryClearingBackStack: () -> Unit = {
        navController.navigateClearingBackStack(signedOutEntryRoute)
    }

    LaunchedEffect(pendingDeeplinkSheetId) {
        pendingDeeplinkSheetId?.let { sheetId ->
            suppressDefaultStartupRouting = true
            deeplinkRoutingViewModel.handleDeeplink(sheetId)
        }
    }

    LaunchedEffect(Unit) {
        deeplinkRoutingViewModel.navigationEvents.collect { event ->
            when (event) {
                is DeeplinkNavigationEvent.NavigateClientCatalog -> {
                    navController.navigateClearingBackStack(MiEmpresaRoutes.ClientCatalog.create(event.companyId))
                    onDeeplinkConsumed(event.consumedSheetId)
                }

                is DeeplinkNavigationEvent.NavigateError -> {
                    navController.navigateClearingBackStack(
                        MiEmpresaRoutes.DeeplinkError.create(event.error.routeValue, event.sheetId),
                    )
                    onDeeplinkConsumed(event.sheetId)
                }

                is DeeplinkNavigationEvent.NavigateHome -> {
                    navigateToSessionEntryClearingBackStack()
                    event.consumedSheetId?.let(onDeeplinkConsumed)
                }

                DeeplinkNavigationEvent.NavigateMyStores -> {
                    navController.navigateClearingBackStack(MiEmpresaRoutes.myStores)
                }
            }
        }
    }

    LaunchedEffect(isAlreadySignedIn, suppressDefaultStartupRouting) {
        if (!checkedVisitedStores && !isAlreadySignedIn && !suppressDefaultStartupRouting) {
            checkedVisitedStores = true
            deeplinkRoutingViewModel.routeToMyStoresIfVisited()
        }
    }

    // If already signed in, skip Welcome and check Drive authorization
    if (isAlreadySignedIn && !suppressDefaultStartupRouting) {
        LaunchedEffect(Unit) {
            when (val authState = signInViewModel.checkDriveAuthorization()) {
                is AuthState.Authorized -> {
                    // Startup flow for signed-in users starts at Onboarding and is resolved there.
                    // Avoid duplicate navigation decisions that can recreate Onboarding route.
                }
                is AuthState.PendingAuth -> {
                    signInViewModel.updateAuthState(authState)
                    navController.navigate(MiEmpresaRoutes.signIn) {
                        popUpTo(MiEmpresaRoutes.adminGraph) { inclusive = true }
                    }
                }
                else -> {
                    navController.navigate(MiEmpresaRoutes.signIn) {
                        popUpTo(MiEmpresaRoutes.adminGraph) { inclusive = true }
                    }
                }
            }
        }
    }

    val postAuthDest by signInViewModel.postAuthDestination.collectAsStateWithLifecycle()

    LaunchedEffect(postAuthDest) {
        when (postAuthDest) {
            is PostAuthDestination.Onboarding -> {
                navController.navigate(MiEmpresaRoutes.onboarding) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
                signInViewModel.consumePostAuthDestination()
            }
            is PostAuthDestination.CompanySelector -> {
                navController.navigate(MiEmpresaRoutes.onboarding("selector")) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
                signInViewModel.consumePostAuthDestination()
            }
            is PostAuthDestination.Home -> {
                navController.navigate(MiEmpresaRoutes.home) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
                signInViewModel.consumePostAuthDestination()
            }
            null -> {}
        }
    }

    // Signed-in users start in admin graph (onboarding is graph start),
    // otherwise users start in auth graph (welcome is graph start).
    val startDestination =
        if (isAlreadySignedIn && !suppressDefaultStartupRouting) {
            MiEmpresaRoutes.adminGraph
        } else {
            MiEmpresaRoutes.authGraph
        }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize(),
    ) {
        navigation(
            route = MiEmpresaRoutes.authGraph,
            startDestination = MiEmpresaRoutes.welcome,
        ) {
            composable(route = MiEmpresaRoutes.welcome) { backStackEntry ->
                WelcomeScreen(
                    onNavigateToSignIn = {
                        guardedResumedNavigation(backStackEntry) {
                            navController.navigate(MiEmpresaRoutes.signIn)
                        }
                    },
                    onNavigateToMyStores = {
                        guardedResumedNavigation(backStackEntry) {
                            navController.navigate(MiEmpresaRoutes.myStores)
                        }
                    },
                )
            }
            navigation(
                route = MiEmpresaRoutes.clientGraph,
                startDestination = MiEmpresaRoutes.myStores,
            ) {
                composable(
                    route = MiEmpresaRoutes.ClientCatalog.pattern,
                    arguments = listOf(navArgument(MiEmpresaRoutes.ClientCatalog.companyIdArg) { type = NavType.StringType }),
                ) { backStackEntry ->
                    val companyId =
                        backStackEntry.arguments
                            ?.getString(MiEmpresaRoutes.ClientCatalog.companyIdArg)
                            .orEmpty()
                    ClientCatalogScreen(
                        onNavigateBack = {
                            guardedBackNavigation(backStackEntry) {
                                if (!navController.popBackStack()) {
                                    navigateToSessionEntryClearingBackStack()
                                }
                            }
                        },
                        onNavigateToCart = { selectedCompanyId ->
                            guardedResumedNavigation(backStackEntry) {
                                navController.navigate(MiEmpresaRoutes.Cart.create(selectedCompanyId))
                            }
                        },
                        onNavigateToHome = {
                            guardedResumedNavigation(backStackEntry) {
                                navigateToSessionEntryClearingBackStack()
                            }
                        },
                        onNavigateToProductDetail = { productId ->
                            if (companyId.isNotBlank()) {
                                guardedResumedNavigation(backStackEntry) {
                                    navController.navigate(MiEmpresaRoutes.ProductDetail.create(productId, companyId, "client"))
                                }
                            }
                        },
                    )
                }
                composable(
                    route = MiEmpresaRoutes.Cart.pattern,
                    arguments = listOf(navArgument(MiEmpresaRoutes.Cart.companyIdArg) { type = NavType.StringType }),
                ) { backStackEntry ->
                    val companyId =
                        backStackEntry.arguments
                            ?.getString(MiEmpresaRoutes.Cart.companyIdArg)
                            .orEmpty()
                    CartScreen(
                        onNavigateBack = {
                            guardedBackNavigation(backStackEntry) {
                                if (!navController.popBackStack()) {
                                    navigateToSessionEntryClearingBackStack()
                                }
                            }
                        },
                        onNavigateToCatalog = {
                            if (companyId.isNotBlank()) {
                                guardedResumedNavigation(backStackEntry) {
                                    navController.navigate(MiEmpresaRoutes.ClientCatalog.create(companyId)) {
                                        popUpTo(MiEmpresaRoutes.Cart.create(companyId)) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            } else {
                                guardedResumedNavigation(backStackEntry) {
                                    navController.navigateClearingBackStack(MiEmpresaRoutes.myStores)
                                }
                            }
                        },
                        onNavigateToProductDetail = { productId ->
                            if (companyId.isNotBlank()) {
                                guardedResumedNavigation(backStackEntry) {
                                    navController.navigate(MiEmpresaRoutes.ProductDetail.create(productId, companyId, "client"))
                                }
                            }
                        },
                    )
                }
                composable(route = MiEmpresaRoutes.myStores) { backStackEntry ->
                    MyStoresScreen(
                        onNavigateBack = {
                            guardedBackNavigation(backStackEntry) {
                                navigateToSessionEntryClearingBackStack()
                            }
                        },
                        onNavigateToCatalog = { companyId ->
                            guardedResumedNavigation(backStackEntry) {
                                navController.navigate(MiEmpresaRoutes.ClientCatalog.create(companyId))
                            }
                        },
                        onNavigateToSignIn = {
                            guardedResumedNavigation(backStackEntry) {
                                navController.navigate(MiEmpresaRoutes.signIn)
                            }
                        },
                    )
                }
                composable(
                    route = MiEmpresaRoutes.ProductDetail.pattern,
                    arguments = listOf(
                        navArgument(MiEmpresaRoutes.ProductDetail.productIdArg) { type = NavType.StringType },
                        navArgument(MiEmpresaRoutes.ProductDetail.companyIdArg) { type = NavType.StringType },
                        navArgument(MiEmpresaRoutes.ProductDetail.modeArg) {
                            type = NavType.StringType
                            defaultValue = "admin"
                        },
                    ),
                ) { backStackEntry ->
                    val detailCompanyId =
                        backStackEntry.arguments
                            ?.getString(MiEmpresaRoutes.ProductDetail.companyIdArg)
                            .orEmpty()
                    ProductDetailScreen(
                        onNavigateBack = { guardedBackNavigation(backStackEntry) { navController.popBackStack() } },
                        onNavigateToEdit = { productId ->
                            guardedResumedNavigation(backStackEntry) {
                                navController.navigate(MiEmpresaRoutes.ProductForm.create(productId))
                            }
                        },
                        onNavigateToCart = { companyId ->
                            val targetCompanyId = companyId.ifBlank { detailCompanyId }
                            if (targetCompanyId.isNotBlank()) {
                                guardedResumedNavigation(backStackEntry) {
                                    navController.navigate(MiEmpresaRoutes.Cart.create(targetCompanyId))
                                }
                            }
                        },
                    )
                }
                composable(
                    route = MiEmpresaRoutes.DeeplinkError.pattern,
                    arguments = listOf(
                        navArgument(MiEmpresaRoutes.DeeplinkError.errorTypeArg) { type = NavType.StringType },
                        navArgument(MiEmpresaRoutes.DeeplinkError.sheetIdArg) {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                    ),
                ) { backStackEntry ->
                    val errorTypeArg = backStackEntry.arguments?.getString(MiEmpresaRoutes.DeeplinkError.errorTypeArg)
                    val sheetIdArg = backStackEntry.arguments?.getString(MiEmpresaRoutes.DeeplinkError.sheetIdArg)

                    DeeplinkErrorScreen(
                        error = CatalogAccessError.fromRouteValue(errorTypeArg),
                        showRetry = !sheetIdArg.isNullOrBlank() && deeplinkRoutingViewModel.isOnlineNow(),
                        onRetry = {
                            runWhenResumed(backStackEntry) {
                                sheetIdArg?.let { deeplinkRoutingViewModel.retryDeeplink(it) }
                            }
                        },
                        onGoHome = {
                            guardedResumedNavigation(backStackEntry) {
                                navigateToSessionEntryClearingBackStack()
                            }
                        },
                    )
                }
            }
            composable(route = MiEmpresaRoutes.signIn) { backStackEntry ->
                val signInState by signInViewModel.signInStateFlow.collectAsStateWithLifecycle()
                val authState by signInViewModel.authStateFlow.collectAsStateWithLifecycle()
                val activity = LocalActivity.current as Activity

                val authorizationSuccess = stringResource(R.string.authorization_success)
                val authorizationFailed = stringResource(R.string.authorization_failed)

                LaunchedEffect(key1 = signInState.isSignInSuccessful) {
                    if (signInState.isSignInSuccessful) {
                        signInViewModel.authorizeDriveAndSheets(activity)
                        signInViewModel.resetSignInState()
                    }
                }

                val authorizationLauncher =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartIntentSenderForResult(),
                    ) { result ->
                        if (result.resultCode == Activity.RESULT_OK) {
                            Toast.makeText(applicationContext, authorizationSuccess, Toast.LENGTH_LONG).show()
                            signInViewModel.updateAuthState(AuthState.Authorized)
                        } else {
                            Toast.makeText(applicationContext, authorizationFailed, Toast.LENGTH_LONG).show()
                            signInViewModel.updateAuthState(AuthState.Unauthorized)
                            signInViewModel.signOut(activity)
                        }
                    }

                LaunchedEffect(key1 = authState) {
                    when (authState) {
                        is AuthState.PendingAuth -> {
                            (authState as AuthState.PendingAuth).intentSender?.let { intentSender ->
                                authorizationLauncher.launch(
                                    IntentSenderRequest.Builder(intentSender).build(),
                                )
                            }
                        }
                        is AuthState.Authorized -> {
                            signInViewModel.determinePostAuthDestination()
                        }
                        is AuthState.Unauthorized, is AuthState.Failed -> {
                            // Authorization denied — sign out and return to auth entry
                            signInViewModel.signOut(activity)
                            navigateToSignedOutEntryClearingBackStack()
                        }
                        null -> {}
                    }
                }

                SignInScreen(
                    signInState,
                    onSignInClick = {
                        runWhenResumed(backStackEntry) {
                            signInViewModel.signIn(activity)
                        }
                    },
                )
            }
        }
        navigation(
            route = MiEmpresaRoutes.adminGraph,
            startDestination = MiEmpresaRoutes.onboarding,
        ) {
            composable(
            route = MiEmpresaRoutes.onboardingWithModePattern,
            arguments = listOf(navArgument(MiEmpresaRoutes.onboardingModeArg) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }),
        ) { backStackEntry ->
            val activity = LocalActivity.current as Activity
            OnboardingScreen(
                onNavigateToHome = {
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigateClearingBackStack(MiEmpresaRoutes.home)
                    }
                },
                onNavigateBack = { guardedBackNavigation(backStackEntry) { navController.popBackStack() } },
                onSignOutRequested = {
                    guardedResumedNavigation(backStackEntry) {
                        signInViewModel.signOut(activity)
                        navigateToSignedOutEntryClearingBackStack()
                    }
                },
            )
        }
        composable(route = MiEmpresaRoutes.home) { backStackEntry ->
            HomeAdminScreen(
                navController = navController,
                signInViewModel = signInViewModel,
                onNavigateToAddProduct = {
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate(MiEmpresaRoutes.ProductForm.add)
                    }
                },
                onNavigateToProductDetail = { productId ->
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate(MiEmpresaRoutes.ProductForm.create(productId))
                    }
                },
                onNavigateToAddCategory = {
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate(MiEmpresaRoutes.CategoryForm.add)
                    }
                },
                onNavigateToCategoryDetail = { categoryId ->
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate(MiEmpresaRoutes.CategoryForm.create(categoryId))
                    }
                },
                onNavigateToWelcome = {
                    guardedResumedNavigation(backStackEntry) {
                        signInViewModel.resetStates()
                        navigateToSignedOutEntryClearingBackStack()
                    }
                },
                onNavigateToOrders = {
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate(MiEmpresaRoutes.ordersList)
                    }
                },
                onNavigateToEditCompany = {
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate(MiEmpresaRoutes.editCompanyData)
                    }
                },
            )
        }
        composable(route = MiEmpresaRoutes.ProductForm.add) { backStackEntry ->
            ProductFormScreen(
                onNavigateBack = { guardedBackNavigation(backStackEntry) { navController.popBackStack() } },
                onSaved = {
                    navController
                        .getBackStackEntry(MiEmpresaRoutes.home)
                        .savedStateHandle["products_sync_feedback"] = true
                },
                onNavigateToAddCategory = {
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate(MiEmpresaRoutes.CategoryForm.add)
                    }
                },
            )
        }
        composable(
            route = MiEmpresaRoutes.ProductForm.pattern,
            arguments = listOf(navArgument(MiEmpresaRoutes.ProductForm.productIdArg) { type = NavType.StringType }),
        ) { backStackEntry ->
            ProductFormScreen(
                onNavigateBack = { guardedBackNavigation(backStackEntry) { navController.popBackStack() } },
                onSaved = {
                    navController
                        .getBackStackEntry(MiEmpresaRoutes.home)
                        .savedStateHandle["products_sync_feedback"] = true
                },
                onNavigateToAddCategory = {
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate(MiEmpresaRoutes.CategoryForm.add)
                    }
                },
            )
        }
        composable(route = MiEmpresaRoutes.CategoryForm.add) { backStackEntry ->
            CategoryFormScreen(
                onNavigateBack = { guardedBackNavigation(backStackEntry) { navController.popBackStack() } },
                onSaved = {
                    navController
                        .getBackStackEntry(MiEmpresaRoutes.home)
                        .savedStateHandle["categories_sync_feedback"] = true
                },
            )
        }
        composable(
            route = MiEmpresaRoutes.CategoryForm.pattern,
            arguments = listOf(navArgument(MiEmpresaRoutes.CategoryForm.categoryIdArg) { type = NavType.StringType }),
        ) { backStackEntry ->
            CategoryFormScreen(
                onNavigateBack = { guardedBackNavigation(backStackEntry) { navController.popBackStack() } },
                onSaved = {
                    navController
                        .getBackStackEntry(MiEmpresaRoutes.home)
                        .savedStateHandle["categories_sync_feedback"] = true
                },
            )
        }
        composable(route = MiEmpresaRoutes.editCompanyData) { backStackEntry ->
            EditCompanyDataScreen(
                onNavigateBack = { guardedBackNavigation(backStackEntry) { navController.popBackStack() } },
            )
        }
        composable(route = MiEmpresaRoutes.ordersList) { backStackEntry ->
            OrdersListScreen(
                onNavigateBack = { guardedBackNavigation(backStackEntry) { navController.popBackStack() } },
                onNavigateToCreateOrder = {
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate(MiEmpresaRoutes.orderManual)
                    }
                },
                onNavigateToOrderDetail = { orderId ->
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate(MiEmpresaRoutes.OrderDetail.create(orderId))
                    }
                },
            )
        }
        composable(route = MiEmpresaRoutes.orderManual) { backStackEntry ->
            OrderManualScreen(
                onOrderCreated = { guardedResumedNavigation(backStackEntry) { navController.popBackStack() } },
                onNavigateBack = { guardedBackNavigation(backStackEntry) { navController.popBackStack() } },
            )
        }
        composable(
            route = MiEmpresaRoutes.OrderDetail.pattern,
            arguments = listOf(navArgument(MiEmpresaRoutes.OrderDetail.orderIdArg) { type = NavType.StringType }),
        ) { backStackEntry ->
            OrderDetailScreen(
                onNavigateBack = { guardedBackNavigation(backStackEntry) { navController.popBackStack() } },
            )
        }
        }
    }
}

