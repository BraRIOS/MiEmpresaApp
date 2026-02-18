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
import androidx.navigation.toRoute
import androidx.navigation.navigation
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
                    navController.navigateClearingBackStack(ClientCatalogRoute(event.companyId))
                    onDeeplinkConsumed(event.consumedSheetId)
                }

                is DeeplinkNavigationEvent.NavigateError -> {
                    navController.navigateClearingBackStack(
                        DeeplinkErrorRoute(errorType = event.error.routeValue, sheetId = event.sheetId),
                    )
                    onDeeplinkConsumed(event.sheetId)
                }

                is DeeplinkNavigationEvent.NavigateHome -> {
                    navigateToSessionEntryClearingBackStack()
                    event.consumedSheetId?.let(onDeeplinkConsumed)
                }

                DeeplinkNavigationEvent.NavigateMyStores -> {
                    navController.navigateClearingBackStack(MyStoresRoute)
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
                    navController.navigate(SignInRoute) {
                        popUpTo(MiEmpresaRoutes.adminGraph) { inclusive = true }
                    }
                }
                else -> {
                    navController.navigate(SignInRoute) {
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
                navController.navigate(OnboardingRoute()) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
                signInViewModel.consumePostAuthDestination()
            }
            is PostAuthDestination.CompanySelector -> {
                navController.navigate(OnboardingRoute(mode = "selector")) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
                signInViewModel.consumePostAuthDestination()
            }
            is PostAuthDestination.Home -> {
                navController.navigate(HomeRoute) {
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
            composable<WelcomeRoute> { backStackEntry ->
                WelcomeScreen(
                    onNavigateToSignIn = {
                        guardedResumedNavigation(backStackEntry) {
                            navController.navigate(SignInRoute)
                        }
                    },
                    onNavigateToMyStores = {
                        guardedResumedNavigation(backStackEntry) {
                            navController.navigate(MyStoresRoute)
                        }
                    },
                )
            }
            navigation(
                route = MiEmpresaRoutes.clientGraph,
                startDestination = MiEmpresaRoutes.myStores,
            ) {
                composable<ClientCatalogRoute> { backStackEntry ->
                    val companyId = backStackEntry.toRoute<ClientCatalogRoute>().companyId
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
                                navController.navigate(CartRoute(selectedCompanyId))
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
                                    navController.navigate(ProductDetailRoute(productId = productId, companyId = companyId, mode = "client"))
                                }
                            }
                        },
                    )
                }
                composable<CartRoute> { backStackEntry ->
                    val companyId = backStackEntry.toRoute<CartRoute>().companyId
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
                                    navController.navigate(ClientCatalogRoute(companyId)) {
                                        popUpTo(MiEmpresaRoutes.Cart.create(companyId)) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            } else {
                                guardedResumedNavigation(backStackEntry) {
                                    navController.navigateClearingBackStack(MyStoresRoute)
                                }
                            }
                        },
                        onNavigateToProductDetail = { productId ->
                            if (companyId.isNotBlank()) {
                                guardedResumedNavigation(backStackEntry) {
                                    navController.navigate(ProductDetailRoute(productId = productId, companyId = companyId, mode = "client"))
                                }
                            }
                        },
                    )
                }
                composable<MyStoresRoute> { backStackEntry ->
                    MyStoresScreen(
                        onNavigateBack = {
                            guardedBackNavigation(backStackEntry) {
                                navigateToSessionEntryClearingBackStack()
                            }
                        },
                        onNavigateToCatalog = { companyId ->
                            guardedResumedNavigation(backStackEntry) {
                                navController.navigate(ClientCatalogRoute(companyId))
                            }
                        },
                        onNavigateToSignIn = {
                            guardedResumedNavigation(backStackEntry) {
                                navController.navigate(SignInRoute)
                            }
                        },
                    )
                }
                composable<ProductDetailRoute> { backStackEntry ->
                    val detailRoute = backStackEntry.toRoute<ProductDetailRoute>()
                    val detailCompanyId = detailRoute.companyId
                    ProductDetailScreen(
                        onNavigateBack = { guardedBackNavigation(backStackEntry) { navController.popBackStack() } },
                        onNavigateToEdit = { productId ->
                            guardedResumedNavigation(backStackEntry) {
                                navController.navigate(ProductFormRoute(productId))
                            }
                        },
                        onNavigateToCart = { companyId ->
                            val targetCompanyId = companyId.ifBlank { detailCompanyId }
                            if (targetCompanyId.isNotBlank()) {
                                guardedResumedNavigation(backStackEntry) {
                                    navController.navigate(CartRoute(targetCompanyId))
                                }
                            }
                        },
                    )
                }
                composable<DeeplinkErrorRoute> { backStackEntry ->
                    val deeplinkErrorRoute = backStackEntry.toRoute<DeeplinkErrorRoute>()
                    val errorTypeArg = deeplinkErrorRoute.errorType
                    val sheetIdArg = deeplinkErrorRoute.sheetId

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
            composable<SignInRoute> { backStackEntry ->
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
            composable<OnboardingRoute> { backStackEntry ->
                val activity = LocalActivity.current as Activity
                OnboardingScreen(
                    onNavigateToHome = {
                        guardedResumedNavigation(backStackEntry) {
                            navController.navigateClearingBackStack(HomeRoute)
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
            composable<HomeRoute> { backStackEntry ->
                HomeAdminScreen(
                    navController = navController,
                    signInViewModel = signInViewModel,
                    onNavigateToAddProduct = {
                        guardedResumedNavigation(backStackEntry) {
                            navController.navigate(ProductFormAddRoute)
                        }
                    },
                    onNavigateToProductDetail = { productId ->
                        guardedResumedNavigation(backStackEntry) {
                            navController.navigate(ProductFormRoute(productId))
                        }
                    },
                    onNavigateToAddCategory = {
                        guardedResumedNavigation(backStackEntry) {
                            navController.navigate(CategoryFormAddRoute)
                        }
                    },
                    onNavigateToCategoryDetail = { categoryId ->
                        guardedResumedNavigation(backStackEntry) {
                            navController.navigate(CategoryFormRoute(categoryId))
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
                            navController.navigate(OrdersListRoute)
                        }
                    },
                    onNavigateToEditCompany = {
                        guardedResumedNavigation(backStackEntry) {
                            navController.navigate(EditCompanyDataRoute)
                        }
                    },
                )
            }
            composable<ProductFormAddRoute> { backStackEntry ->
                ProductFormScreen(
                    onNavigateBack = { guardedBackNavigation(backStackEntry) { navController.popBackStack() } },
                    onSaved = {
                        navController
                            .getBackStackEntry(MiEmpresaRoutes.home)
                            .savedStateHandle["products_sync_feedback"] = true
                    },
                    onNavigateToAddCategory = {
                        guardedResumedNavigation(backStackEntry) {
                            navController.navigate(CategoryFormAddRoute)
                        }
                    },
                )
            }
            composable<ProductFormRoute> { backStackEntry ->
                ProductFormScreen(
                    onNavigateBack = { guardedBackNavigation(backStackEntry) { navController.popBackStack() } },
                    onSaved = {
                        navController
                            .getBackStackEntry(MiEmpresaRoutes.home)
                            .savedStateHandle["products_sync_feedback"] = true
                    },
                    onNavigateToAddCategory = {
                        guardedResumedNavigation(backStackEntry) {
                            navController.navigate(CategoryFormAddRoute)
                        }
                    },
                )
            }
            composable<CategoryFormAddRoute> { backStackEntry ->
                CategoryFormScreen(
                    onNavigateBack = { guardedBackNavigation(backStackEntry) { navController.popBackStack() } },
                    onSaved = {
                        navController
                            .getBackStackEntry(MiEmpresaRoutes.home)
                            .savedStateHandle["categories_sync_feedback"] = true
                    },
                )
            }
            composable<CategoryFormRoute> { backStackEntry ->
                CategoryFormScreen(
                    onNavigateBack = { guardedBackNavigation(backStackEntry) { navController.popBackStack() } },
                    onSaved = {
                        navController
                            .getBackStackEntry(MiEmpresaRoutes.home)
                            .savedStateHandle["categories_sync_feedback"] = true
                    },
                )
            }
            composable<EditCompanyDataRoute> { backStackEntry ->
                EditCompanyDataScreen(
                    onNavigateBack = { guardedBackNavigation(backStackEntry) { navController.popBackStack() } },
                )
            }
            composable<OrdersListRoute> { backStackEntry ->
                OrdersListScreen(
                    onNavigateBack = { guardedBackNavigation(backStackEntry) { navController.popBackStack() } },
                    onNavigateToCreateOrder = {
                        guardedResumedNavigation(backStackEntry) {
                            navController.navigate(OrderManualRoute)
                        }
                    },
                    onNavigateToOrderDetail = { orderId ->
                        guardedResumedNavigation(backStackEntry) {
                            navController.navigate(OrderDetailRoute(orderId))
                        }
                    },
                )
            }
            composable<OrderManualRoute> { backStackEntry ->
                OrderManualScreen(
                    onOrderCreated = { guardedResumedNavigation(backStackEntry) { navController.popBackStack() } },
                    onNavigateBack = { guardedBackNavigation(backStackEntry) { navController.popBackStack() } },
                )
            }
            composable<OrderDetailRoute> { backStackEntry ->
                OrderDetailScreen(
                    onNavigateBack = { guardedBackNavigation(backStackEntry) { navController.popBackStack() } },
                )
            }
        }
    }
}

