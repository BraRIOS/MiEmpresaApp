package com.brios.miempresa.onboarding.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brios.miempresa.R
import com.brios.miempresa.core.data.local.entities.Company
import com.brios.miempresa.core.ui.components.CompanyAvatar
import com.brios.miempresa.core.ui.components.MessageWithIcon
import com.brios.miempresa.core.ui.components.NotFoundView
import com.brios.miempresa.core.ui.components.SearchBar
import com.brios.miempresa.core.ui.components.SearchBarVariant
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme

@Composable
fun CompanySelectionView(
    username: String,
    companies: List<Company>,
    onSelectCompany: (Company) -> Unit,
    onCreateNewCompany: () -> Unit,
    onBack: (() -> Unit)? = null,
    initialSearchText: String = "",
) {
    var searchText by remember { mutableStateOf(initialSearchText) }

    val filteredCompanies = remember(searchText, companies) {
        companies.filter {
            it.name.contains(searchText, ignoreCase = true)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Column {
                    // Header Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppDimensions.mediumPadding)
                            .padding(bottom = AppDimensions.mediumPadding)
                    ) {
                        if (onBack != null) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.offset(x = (-12).dp) // Align visually with text
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.go_back),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(AppDimensions.extraSmallPadding))

                        val greetingTemplate = stringResource(R.string.greeting_user)
                        val greetingParts = greetingTemplate.split($$"%1$s")

                        Text(
                            text = buildAnnotatedString {
                                if (greetingParts.isNotEmpty()) append(greetingParts[0])
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                    append(username)
                                }
                                if (greetingParts.size > 1) append(greetingParts[1])
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(AppDimensions.smallPadding))

                        Text(
                            text = stringResource(R.string.select_company_question),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.8f)
                        )
                    }

                    // Search Bar
                    SearchBar(
                        query = searchText,
                        onQueryChange = { searchText = it },
                        placeholderText = stringResource(R.string.search),
                        variant = SearchBarVariant.Outlined,
                        modifier =
                            Modifier
                                .padding(horizontal = AppDimensions.mediumPadding)
                                .padding(bottom = AppDimensions.mediumPadding),
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Box(
                    modifier = Modifier
                        .padding(AppDimensions.mediumPadding)
                        .padding(bottom = AppDimensions.smallPadding)
                ) {
                    Button(
                        onClick = onCreateNewCompany,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(AppDimensions.smallPadding))
                        Text(
                            text = stringResource(R.string.create_another_company),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Content
            if (companies.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    MessageWithIcon(
                        message = stringResource(id = R.string.no_companies_found),
                        icon = Icons.Filled.Warning,
                    )
                }
            } else if (filteredCompanies.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    NotFoundView(
                        message = stringResource(id = R.string.no_companies_found),
                        onAction = { searchText = "" }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = AppDimensions.mediumPadding,
                        end = AppDimensions.mediumPadding,
                        top = AppDimensions.mediumPadding,
                        bottom = AppDimensions.mediumPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding)
                ) {
                    items(filteredCompanies) { company ->
                        CompanyCard(
                            company = company,
                            onClick = { onSelectCompany(company) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompanyCard(
    company: Company,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(AppDimensions.largeCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(AppDimensions.mediumPadding)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompanyAvatar(
                companyName = company.name,
                logoUrl = company.logoUrl,
                size = AppDimensions.CompanyListView.companyAvatarSize
            )

            Spacer(modifier = Modifier.width(AppDimensions.mediumPadding))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.extraSmallPadding)
            ) {
                Text(
                    text = company.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                company.specialization?.let { specialization ->
                    Text(
                        text = specialization,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompanySelectionScreenPreview() {
    val companies =
        listOf(
            Company("1", "Vinoteca \"El Roble\"", specialization = "Vinos y Licores Artesanales"),
            Company("2", "Panadería La Espiga", specialization = "Panes de masa madre y pastelería"),
            Company("3", "Logística Express", specialization = "Envíos nacionales e internacionales"),
            Company("4", "Logística Express", specialization = "Envíos nacionales e internacionales"),
            Company("35", "Logística Express", specialization = "Envíos nacionales e internacionales"),
            Company("36", "Logística Express", specialization = "Envíos nacionales e internacionales"),
            Company("37", "Logística Express", specialization = "Envíos nacionales e internacionales"),
            Company("38", "Logística Express", specialization = "Envíos nacionales e internacionales"),
            )
    MiEmpresaTheme {
        Surface {
            CompanySelectionView(
                username = "John Doe",
                companies = companies,
                onSelectCompany = {},
                onCreateNewCompany = {},
                onBack = {},
            )
        }
    }
}

@Preview(name = "Empty State", showBackground = true)
@Composable
private fun CompanySelectionScreenEmptyPreview() {
    MiEmpresaTheme {
        Surface {
            CompanySelectionView(
                username = "John Doe",
                companies = emptyList(),
                onSelectCompany = {},
                onCreateNewCompany = {},
            )
        }
    }
}

@Preview(name = "Search Not Found", showBackground = true)
@Composable
private fun CompanySelectionScreenNotFoundPreview() {
    val companies =
        listOf(
            Company("1", "Vinoteca \"El Roble\"", specialization = "Vinos y Licores Artesanales"),
            Company("2", "Panadería La Espiga", specialization = "Panes de masa madre y pastelería"),
        )
    MiEmpresaTheme {
        Surface {
            CompanySelectionView(
                username = "John Doe",
                companies = companies,
                onSelectCompany = {},
                onCreateNewCompany = {},
                initialSearchText = "NonExistentCompany"
            )
        }
    }
}
