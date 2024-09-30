package com.brios.miempresa.initializer

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.brios.miempresa.R
import com.brios.miempresa.components.MessageWithIcon
import com.brios.miempresa.data.Company

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyListView(
    username: String,
    companies:LiveData<List<Company>>,
    onSelectCompany: (Company) -> Unit,
    onCreateNewCompany: () -> Unit
) {
    val companyList by companies.observeAsState(emptyList())
    var searchText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.greeting_user, username),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.select_company_question),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth()
        )

        val filteredCompanies = companyList.filter {
            it.name.contains(searchText, ignoreCase = true)
        }

        if (filteredCompanies.isNotEmpty()) {
            LazyColumn(modifier = Modifier
                .height(140.dp)
                .fillMaxWidth()
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.surfaceContainerHighest,
                    RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            ) {
                items(filteredCompanies.size) { index ->
                    val company = filteredCompanies[index]
                    ListItem(
                        headlineContent = { Text(company.name) },
                        modifier = Modifier.clickable { onSelectCompany(company) }
                    )
                }
            }
        } else {
            MessageWithIcon(
                message = stringResource(id = R.string.no_companies_found),
                icon = Icons.Filled.Warning
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(id = R.string.or),
                modifier = Modifier.padding(horizontal = 8.dp),
                color = DividerDefaults.color
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onCreateNewCompany() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.create_new_company))
        }
    }
}

@Preview
@Composable
private fun CompanyListScreenPreview() {
    val companies = MutableLiveData(listOf(
        Company("1", "Company 1", false),
        Company("2", "Company 2", false),
        Company("3", "Company 3", false)
    ))
    Surface{
        CompanyListView(
            username = "John Doe",
            companies = companies,
            onSelectCompany = {},
            onCreateNewCompany = {},
        )
    }
}