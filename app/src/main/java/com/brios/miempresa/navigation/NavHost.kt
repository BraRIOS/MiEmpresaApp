package com.brios.miempresa.navigation

import android.app.Activity
import android.content.Context
import android.net.Uri
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
    val runWhenResumed: (NavBackStackEntry, () -> Unit) -> Unit = { entry, action ->
        if (entry.lifecycle.currentState == Lifecycle.State.RESUMED) {
            action()
        }
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
                    navController.navigate("${MiEmpresaScreen.ClientCatalog.name}/${event.companyId}") {
                        popUpTo(0) { inclusive = true }
                    }
                    onDeeplinkConsumed(event.consumedSheetId)
                }

                is DeeplinkNavigationEvent.NavigateError -> {
                    navController.navigate(
                        "${MiEmpresaScreen.DeeplinkError.name}/${event.error.routeValue}?sheetId=${Uri.encode(event.sheetId)}",
                    ) {
                        popUpTo(0) { inclusive = true }
                    }
                    onDeeplinkConsumed(event.sheetId)
                }

                is DeeplinkNavigationEvent.NavigateHome -> {
                    navController.navigate(MiEmpresaScreen.Home.name) {
                        popUpTo(0) { inclusive = true }
                    }
                    event.consumedSheetId?.let(onDeeplinkConsumed)
                }

                DeeplinkNavigationEvent.NavigateMyStores -> {
                    navController.navigate(MiEmpresaScreen.MyStores.name) {
                        popUpTo(0) { inclusive = true }
                    }
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
                    navController.navigate(MiEmpresaScreen.SignIn.name) {
                        popUpTo(MiEmpresaScreen.Onboarding.name) { inclusive = true }
                    }
                }
                else -> {
                    navController.navigate(MiEmpresaScreen.SignIn.name) {
                        popUpTo(MiEmpresaScreen.Onboarding.name) { inclusive = true }
                    }
                }
            }
        }
    }

    val postAuthDest by signInViewModel.postAuthDestination.collectAsStateWithLifecycle()

    LaunchedEffect(postAuthDest) {
        when (postAuthDest) {
            is PostAuthDestination.Onboarding -> {
                navController.navigate(MiEmpresaScreen.Onboarding.name) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
                signInViewModel.consumePostAuthDestination()
            }
            is PostAuthDestination.CompanySelector -> {
                navController.navigate("${MiEmpresaScreen.Onboarding.name}?mode=selector") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
                signInViewModel.consumePostAuthDestination()
            }
            is PostAuthDestination.Home -> {
                navController.navigate(MiEmpresaScreen.Home.name) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
                signInViewModel.consumePostAuthDestination()
            }
            null -> {}
        }
    }

    // Start at Onboarding if already signed in (shows discovery loading),
    // otherwise start at Welcome for new/signed-out users
    val startDestination =
        if (isAlreadySignedIn && !suppressDefaultStartupRouting) {
            MiEmpresaScreen.Onboarding.name
        } else {
            MiEmpresaScreen.Welcome.name
        }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable(route = MiEmpresaScreen.Welcome.name) { backStackEntry ->
            WelcomeScreen(
                onNavigateToSignIn = {
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate(MiEmpresaScreen.SignIn.name)
                    }
                },
                onNavigateToMyStores = {
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate(MiEmpresaScreen.MyStores.name)
                    }
                },
            )
        }
        composable(
            route = "${MiEmpresaScreen.ClientCatalog.name}/{companyId}",
            arguments = listOf(navArgument("companyId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val companyId = backStackEntry.arguments?.getString("companyId").orEmpty()
            ClientCatalogScreen(
                onNavigateBack = {
                    guardedResumedNavigation(backStackEntry) {
                        if (!navController.popBackStack()) {
                            val fallbackRoute =
                                if (isAlreadySignedIn) {
                                    MiEmpresaScreen.Home.name
                                } else {
                                    MiEmpresaScreen.Welcome.name
                                }
                            navController.navigate(fallbackRoute) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                },
                onNavigateToCart = { selectedCompanyId ->
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate("${MiEmpresaScreen.Cart.name}/$selectedCompanyId")
                    }
                },
                onNavigateToHome = {
                    val targetRoute =
                        if (isAlreadySignedIn) {
                            MiEmpresaScreen.Home.name
                        } else {
                            MiEmpresaScreen.Welcome.name
                        }
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate(targetRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onNavigateToProductDetail = { productId ->
                    if (companyId.isNotBlank()) {
                        guardedResumedNavigation(backStackEntry) {
                            navController.navigate("${MiEmpresaScreen.ProductDetail.name}/$productId/$companyId?mode=client")
                        }
                    }
                },
            )
        }
        composable(
            route = "${MiEmpresaScreen.Cart.name}/{companyId}",
            arguments = listOf(navArgument("companyId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val companyId = backStackEntry.arguments?.getString("companyId").orEmpty()
            CartScreen(
                onNavigateBack = {
                    guardedResumedNavigation(backStackEntry) {
                        if (!navController.popBackStack()) {
                            val fallbackRoute =
                                if (isAlreadySignedIn) {
                                    MiEmpresaScreen.Home.name
                                } else {
                                    MiEmpresaScreen.Welcome.name
                                }
                            navController.navigate(fallbackRoute) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                },
                onNavigateToCatalog = {
                    if (companyId.isNotBlank()) {
                        guardedResumedNavigation(backStackEntry) {
                            navController.navigate("${MiEmpresaScreen.ClientCatalog.name}/$companyId") {
                                popUpTo("${MiEmpresaScreen.Cart.name}/$companyId") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    } else {
                        guardedResumedNavigation(backStackEntry) {
                            navController.navigate(MiEmpresaScreen.MyStores.name) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                },
                onNavigateToProductDetail = { productId ->
                    if (companyId.isNotBlank()) {
                        guardedResumedNavigation(backStackEntry) {
                            navController.navigate("${MiEmpresaScreen.ProductDetail.name}/$productId/$companyId?mode=client")
                        }
                    }
                },
            )
        }
        composable(route = MiEmpresaScreen.MyStores.name) { backStackEntry ->
            MyStoresScreen(
                onNavigateBack = {
                    val targetRoute =
                        if (isAlreadySignedIn) {
                            MiEmpresaScreen.Home.name
                        } else {
                            MiEmpresaScreen.Welcome.name
                        }
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate(targetRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onNavigateToCatalog = { companyId ->
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate("${MiEmpresaScreen.ClientCatalog.name}/$companyId")
                    }
                },
                onNavigateToSignIn = {
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate(MiEmpresaScreen.SignIn.name)
                    }
                },
            )
        }
        composable(
            route = "${MiEmpresaScreen.ProductDetail.name}/{productId}/{companyId}?mode={mode}",
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType },
                navArgument("companyId") { type = NavType.StringType },
                navArgument("mode") {
                    type = NavType.StringType
                    defaultValue = "admin"
                },
            ),
        ) { backStackEntry ->
            val detailCompanyId = backStackEntry.arguments?.getString("companyId").orEmpty()
            ProductDetailScreen(
                onNavigateBack = { guardedResumedNavigation(backStackEntry) { navController.popBackStack() } },
                onNavigateToEdit = { productId ->
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate("${MiEmpresaScreen.Product.name}/$productId")
                    }
                },
                onNavigateToCart = { companyId ->
                    val targetCompanyId = companyId.ifBlank { detailCompanyId }
                    if (targetCompanyId.isNotBlank()) {
                        guardedResumedNavigation(backStackEntry) {
                            navController.navigate("${MiEmpresaScreen.Cart.name}/$targetCompanyId")
                        }
                    }
                },
            )
        }
        composable(
            route = "${MiEmpresaScreen.DeeplinkError.name}/{errorType}?sheetId={sheetId}",
            arguments = listOf(
                navArgument("errorType") { type = NavType.StringType },
                navArgument("sheetId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { backStackEntry ->
            val errorTypeArg = backStackEntry.arguments?.getString("errorType")
            val sheetIdArg = backStackEntry.arguments?.getString("sheetId")

            DeeplinkErrorScreen(
                error = CatalogAccessError.fromRouteValue(errorTypeArg),
                showRetry = !sheetIdArg.isNullOrBlank() && deeplinkRoutingViewModel.isOnlineNow(),
                onRetry = {
                    runWhenResumed(backStackEntry) {
                        sheetIdArg?.let { deeplinkRoutingViewModel.retryDeeplink(it) }
                    }
                },
                onGoHome = {
                    if (isAlreadySignedIn) {
                        guardedResumedNavigation(backStackEntry) {
                            navController.navigate(MiEmpresaScreen.Home.name) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    } else {
                        guardedResumedNavigation(backStackEntry) {
                            navController.navigate(MiEmpresaScreen.Welcome.name) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                },
            )
        }
        composable(route = MiEmpresaScreen.SignIn.name) { backStackEntry ->
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
                        // Authorization denied — sign out and return to Welcome
                        signInViewModel.signOut(activity)
                        navController.navigate(MiEmpresaScreen.Welcome.name) {
                            popUpTo(0) { inclusive = true }
                        }
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
        composable(
            route = "${MiEmpresaScreen.Onboarding.name}?mode={mode}",
            arguments = listOf(navArgument("mode") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }),
        ) { backStackEntry ->
            val activity = LocalActivity.current as Activity
            OnboardingScreen(
                onNavigateToHome = {
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate(MiEmpresaScreen.Home.name) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onNavigateBack = { guardedResumedNavigation(backStackEntry) { navController.popBackStack() } },
                onSignOutRequested = {
                    guardedResumedNavigation(backStackEntry) {
                        signInViewModel.signOut(activity)
                        navController.navigate(MiEmpresaScreen.Welcome.name) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
            )
        }
        composable(route = MiEmpresaScreen.Home.name) { backStackEntry ->
            HomeAdminScreen(
                navController = navController,
                signInViewModel = signInViewModel,
                onNavigateToAddProduct = {
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate("${MiEmpresaScreen.Product.name}/add")
                    }
                },
                onNavigateToProductDetail = { productId ->
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate("${MiEmpresaScreen.Product.name}/$productId")
                    }
                },
                onNavigateToAddCategory = {
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate("${MiEmpresaScreen.Categories.name}/add")
                    }
                },
                onNavigateToCategoryDetail = { categoryId ->
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate("${MiEmpresaScreen.Categories.name}/$categoryId")
                    }
                },
                onNavigateToWelcome = {
                    guardedResumedNavigation(backStackEntry) {
                        signInViewModel.resetStates()
                        navController.navigate(MiEmpresaScreen.Welcome.name) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onNavigateToOrders = {
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate(MiEmpresaScreen.PedidosList.name)
                    }
                },
                onNavigateToEditCompany = {
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate(MiEmpresaScreen.EditCompanyData.name)
                    }
                },
            )
        }
        composable(route = "${MiEmpresaScreen.Product.name}/add") { backStackEntry ->
            ProductFormScreen(
                onNavigateBack = { guardedResumedNavigation(backStackEntry) { navController.popBackStack() } },
                onSaved = {
                    navController
                        .getBackStackEntry(MiEmpresaScreen.Home.name)
                        .savedStateHandle["products_sync_feedback"] = true
                },
                onNavigateToAddCategory = {
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate("${MiEmpresaScreen.Categories.name}/add")
                    }
                },
            )
        }
        composable(
            route = "${MiEmpresaScreen.Product.name}/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType }),
        ) { backStackEntry ->
            ProductFormScreen(
                onNavigateBack = { guardedResumedNavigation(backStackEntry) { navController.popBackStack() } },
                onSaved = {
                    navController
                        .getBackStackEntry(MiEmpresaScreen.Home.name)
                        .savedStateHandle["products_sync_feedback"] = true
                },
                onNavigateToAddCategory = {
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate("${MiEmpresaScreen.Categories.name}/add")
                    }
                },
            )
        }
        composable(route = "${MiEmpresaScreen.Categories.name}/add") { backStackEntry ->
            CategoryFormScreen(
                onNavigateBack = { guardedResumedNavigation(backStackEntry) { navController.popBackStack() } },
                onSaved = {
                    navController
                        .getBackStackEntry(MiEmpresaScreen.Home.name)
                        .savedStateHandle["categories_sync_feedback"] = true
                },
            )
        }
        composable(
            route = "${MiEmpresaScreen.Categories.name}/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType }),
        ) { backStackEntry ->
            CategoryFormScreen(
                onNavigateBack = { guardedResumedNavigation(backStackEntry) { navController.popBackStack() } },
                onSaved = {
                    navController
                        .getBackStackEntry(MiEmpresaScreen.Home.name)
                        .savedStateHandle["categories_sync_feedback"] = true
                },
            )
        }
        composable(route = MiEmpresaScreen.EditCompanyData.name) { backStackEntry ->
            EditCompanyDataScreen(
                onNavigateBack = { guardedResumedNavigation(backStackEntry) { navController.popBackStack() } },
            )
        }
        composable(route = MiEmpresaScreen.PedidosList.name) { backStackEntry ->
            OrdersListScreen(
                onNavigateBack = { guardedResumedNavigation(backStackEntry) { navController.popBackStack() } },
                onNavigateToCreateOrder = {
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate(MiEmpresaScreen.PedidoManual.name)
                    }
                },
                onNavigateToOrderDetail = { orderId ->
                    guardedResumedNavigation(backStackEntry) {
                        navController.navigate("${MiEmpresaScreen.PedidoDetail.name}/$orderId")
                    }
                },
            )
        }
        composable(route = MiEmpresaScreen.PedidoManual.name) { backStackEntry ->
            OrderManualScreen(
                onOrderCreated = { guardedResumedNavigation(backStackEntry) { navController.popBackStack() } },
                onNavigateBack = { guardedResumedNavigation(backStackEntry) { navController.popBackStack() } },
            )
        }
        composable(
            route = "${MiEmpresaScreen.PedidoDetail.name}/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) { backStackEntry ->
            OrderDetailScreen(
                onNavigateBack = { guardedResumedNavigation(backStackEntry) { navController.popBackStack() } },
            )
        }
    }
}
