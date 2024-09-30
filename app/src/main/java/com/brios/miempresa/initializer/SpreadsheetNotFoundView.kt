package com.brios.miempresa.initializer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brios.miempresa.R
import com.brios.miempresa.data.Company

@Composable
fun SpreadsheetNotFoundView(
    company: Company,
    onRetry: (String) -> Unit,
    onCreateSpreadsheet: (String) -> Unit,
    onDeleteCompany: (Company) -> Unit,
    onSelectAnotherCompany: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.database_not_found_for_company),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = company.name,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { onRetry(company.id) }) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = stringResource(R.string.retry_search),modifier = Modifier.size(
                    ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = stringResource(R.string.retry_search))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onCreateSpreadsheet(company.id) }) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.create_database),
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = stringResource(R.string.create_database))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onDeleteCompany(company) }) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(R.string.delete_company),
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = stringResource(R.string.delete_company))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onSelectAnotherCompany) {
            Icon(
                imageVector = Icons.Filled.SwapHoriz,
                contentDescription = stringResource(R.string.select_another_company),
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = stringResource(R.string.select_another_company))
        }
    }
}

@Preview
@Composable
fun SpreadsheetNotFoundViewPreview(){
    val company = Company("123", "Test Company", selected = true)
    Surface {
        SpreadsheetNotFoundView(
            company = company,
            onRetry = {},
            onCreateSpreadsheet = {},
            onDeleteCompany = {},
            onSelectAnotherCompany = {})
    }
}