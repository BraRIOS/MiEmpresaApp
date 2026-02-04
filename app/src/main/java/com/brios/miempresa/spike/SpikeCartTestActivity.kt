package com.brios.miempresa.spike

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.brios.miempresa.cart.domain.CartEvent
import com.brios.miempresa.cart.domain.CartUiState
import com.brios.miempresa.cart.presentation.CartViewModel
import com.brios.miempresa.data.Company
import com.brios.miempresa.data.CompanyDao
import com.brios.miempresa.ui.theme.MiEmpresaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * DUMMY ACTIVITY - WILL BE DELETED
 *
 * Spike S3: Cart System Test Activity
 * Purpose: Manual validation of Cart + Repository + ViewModel pattern
 *
 * Tests:
 * - Cart persistence (Room)
 * - Multitenancy (companyId filtering)
 * - Reactive state (StateFlow + SharedFlow)
 * - CRUD operations via ViewModel
 */
@AndroidEntryPoint
class SpikeCartTestActivity : ComponentActivity() {
    @Inject
    lateinit var companyDao: CompanyDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SpikeS3", "onCreate: SpikeCartTestActivity started")

        // Seed test data if needed
        lifecycleScope.launch {
            seedTestDataIfNeeded()
        }

        setContent {
            MiEmpresaTheme {
                SpikeCartTestScreen()
            }
        }
    }

    private suspend fun seedTestDataIfNeeded() {
        withContext(Dispatchers.IO) {
            val existingCompany = companyDao.getCompanyById("test-company-123")
            if (existingCompany == null) {
                Log.d("SpikeS3", "No company found, seeding test data...")
                val testCompany =
                    Company(
                        id = "test-company-123",
                        name = "Test Company (Spike S3)",
                        selected = true,
                    )
                companyDao.insert(testCompany)
                Log.d("SpikeS3", "Test company seeded: ${testCompany.name}")
            } else {
                Log.d("SpikeS3", "Using existing company: ${existingCompany.name}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("SpikeS3", "onDestroy: SpikeCartTestActivity destroyed")
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SpikeCartTestScreen(viewModel: CartViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cartCount by viewModel.cartCount.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle one-off events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is CartEvent.ShowError -> {
                    Log.d("SpikeS3", "Event: ShowError - ${event.message}")
                    snackbarHostState.showSnackbar(event.message)
                }
                is CartEvent.ShowSnackbar -> {
                    Log.d("SpikeS3", "Event: ShowSnackbar - ${event.message}")
                    snackbarHostState.showSnackbar(event.message)
                }
                is CartEvent.CartCleared -> {
                    Log.d("SpikeS3", "Event: CartCleared")
                    snackbarHostState.showSnackbar("Cart cleared")
                }
                is CartEvent.NavigateToCheckout -> {
                    Log.d("SpikeS3", "Event: NavigateToCheckout")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spike S3: Cart Test") },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
        ) {
            // Cart count badge
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
            ) {
                Text(
                    text = "Cart Items: $cartCount",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = {
                        Log.d("SpikeS3", "Action: Add Test Product (dummy-product-123)")
                        viewModel.addProduct("dummy-product-123", 1)
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Test")
                }

                OutlinedButton(
                    onClick = {
                        Log.d("SpikeS3", "Action: Clear Cart")
                        viewModel.clearCart()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear Cart")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cart items content
            when (val state = uiState) {
                is CartUiState.Loading -> {
                    Log.d("SpikeS3", "State: Loading")
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is CartUiState.Empty -> {
                    Log.d("SpikeS3", "State: Empty")
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Cart is empty",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "Add test products to begin",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                is CartUiState.Success -> {
                    Log.d("SpikeS3", "State: Success - totalItems=${state.totalItems}, totalPrice=${state.totalPrice}")
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Cart has ${state.totalItems} item(s)",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Total: $${String.format("%.2f", state.totalPrice)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Company: ${state.companyName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "NOTE: Full cart item display requires Product mapping (Task incomplete)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                is CartUiState.Error -> {
                    Log.e("SpikeS3", "State: Error - ${state.message}")
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.error,
                            )
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
