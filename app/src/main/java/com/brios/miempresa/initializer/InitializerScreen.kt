package com.brios.miempresa.initializer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.brios.miempresa.R
import com.brios.miempresa.data.Company
import com.brios.miempresa.navigation.MiEmpresaScreen

@Composable
fun InitializerScreen(
    viewModel: InitializerViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
    when (uiState) {
        is InitializerUiState.Loading -> LoadingScreen()
        is InitializerUiState.Welcome -> WelcomeScreen(
            (uiState as InitializerUiState.Welcome).userName,
            onCompanyNameEntered = { viewModel.createCompany(it) }
        )
        is InitializerUiState.CheckingData -> LoadingScreen(
            message = stringResource(R.string.checking_existing_projects)
        )
        is InitializerUiState.CompanyList -> CompanyListScreen(
            companies = (uiState as InitializerUiState.CompanyList).companies,
            onSelectCompany = { viewModel.selectCompany(it) },
            onCreateNewCompany = { viewModel.goToWelcomeScreen() }
        )
        is InitializerUiState.SearchingSpreadsheet -> LoadingScreen(
            message = stringResource(R.string.searching_database)
        )
        is InitializerUiState.SpreadsheetNotFound -> SpreadsheetNotFoundScreen(
            onRetry = { viewModel.searchSpreadsheet() },
            onSelectAnotherCompany = { viewModel.goToCompanyListScreen() }
        )
        is InitializerUiState.CreatingCompany -> LoadingScreen()
        is InitializerUiState.CreatingSpreadsheet -> LoadingScreen(
            message = stringResource(R.string.creating_database)
        )

        InitializerUiState.NavigateToProducts -> TODO()
    }

    // Handle navigation based on state
    if (uiState is InitializerUiState.NavigateToProducts) {
        LaunchedEffect(Unit) {
            navController.navigate(MiEmpresaScreen.Products.name)
        }
    }
}

@Composable
fun LoadingScreen(message:String = "") {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
        Text(text = message)
    }
}

@Composable
fun WelcomeScreen(userName: String, onCompanyNameEntered: (String) -> Unit) {
    var companyName by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Welcome, $userName!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = companyName,
            onValueChange = { companyName = it },
            label = { Text("Company Name") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onCompanyNameEntered(companyName) }) {
            Text("Create Company")
        }
    }
}

@Composable
fun CompanyListScreen(
    companies: List<Company>,
    onSelectCompany: (Company) -> Unit,
    onCreateNewCompany: () -> Unit
) {
    // ... display company list and options ...
}

@Composable
fun SpreadsheetNotFoundScreen(
    onRetry: () -> Unit,
    onSelectAnotherCompany: () -> Unit
) {
    // ... display error message and options ...
}