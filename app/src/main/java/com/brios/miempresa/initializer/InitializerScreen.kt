package com.brios.miempresa.initializer

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.brios.miempresa.R
import com.brios.miempresa.components.LoadingView
import com.brios.miempresa.navigation.MiEmpresaScreen

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun InitializerScreen(
    navController: NavHostController,
    startUIState: InitializerUiState? = null
) {
    val context = LocalContext.current
    val viewModel: InitializerViewModel = hiltViewModel<InitializerViewModel, InitializerViewModelFactory> { factory ->
        factory.create(context, startUIState)
    }
    val uiState by viewModel.uiState.collectAsState()
    when (uiState) {
        is InitializerUiState.Loading -> LoadingView()
        is InitializerUiState.Error -> ErrorView((uiState as InitializerUiState.Error).message)
        is InitializerUiState.Welcome -> {
            val welcomeState = uiState as InitializerUiState.Welcome
            WelcomeView(
                welcomeState.username,
                welcomeState.isFirstTime,
                onCompanyNameEntered = { viewModel.createCompany(it) }
            )
        }
        is InitializerUiState.CheckingData -> LoadingView(
            message = stringResource(R.string.checking_existing_companies)
        )
        is InitializerUiState.ShowCompanyList -> LoadingView()
        is InitializerUiState.CompanyList -> {
            val companyListState = uiState as InitializerUiState.CompanyList
            CompanyListView(
                username = companyListState.username,
                companies = companyListState.companies,
                onSelectCompany = { viewModel.searchSpreadsheet(it) },
                onCreateNewCompany = { viewModel.goToCreateCompanyScreen() }
            )
        }
        is InitializerUiState.SearchingSpreadsheet -> LoadingView(
            message = stringResource(R.string.searching_database)
        )
        is InitializerUiState.SpreadsheetNotFound -> {
            SpreadsheetNotFoundView(
                (uiState as InitializerUiState.SpreadsheetNotFound).company,
                onRetry = { viewModel.retrySearchSpreadsheet(it) },
                onCreateSpreadsheet = { viewModel.createAndInitializeSpreadsheet(it) },
                onSelectAnotherCompany = { viewModel.goToCompanyListScreen() },
                onDeleteCompany = { viewModel.deleteCompany(it) }
            )
        }
        is InitializerUiState.CreatingCompany -> LoadingView()
        is InitializerUiState.CreatingSpreadsheet -> LoadingView(
            message = stringResource(R.string.creating_database)
        )

        is InitializerUiState.NavigateToProducts -> navController.navigate(MiEmpresaScreen.Products.name)
    }
}