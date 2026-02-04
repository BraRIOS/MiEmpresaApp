package com.brios.miempresa.spike

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.brios.miempresa.data.CartItemDao
import com.brios.miempresa.data.CompanyDao
import com.brios.miempresa.data.ProductDao
import com.brios.miempresa.data.CartItemEntity
import com.brios.miempresa.data.Company
import com.brios.miempresa.data.ProductEntity
import com.brios.miempresa.data.CartRepository
import com.brios.miempresa.cart.domain.PriceValidationResult
import com.brios.miempresa.ui.theme.MiEmpresaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SpikePriceValidationActivity : ComponentActivity() {

    @Inject
    lateinit var cartRepository: CartRepository

    @Inject
    lateinit var companyDao: CompanyDao

    @Inject
    lateinit var productDao: ProductDao

    @Inject
    lateinit var cartItemDao: CartItemDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Seed test data on first launch
        lifecycleScope.launch {
            seedTestDataIfNeeded()
        }

        setContent {
            MiEmpresaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SpikePriceValidationScreen()
                }
            }
        }
    }

    private suspend fun seedTestDataIfNeeded() {
        val company = companyDao.getCompanyById("spike-company-1")
        if (company == null) {
            // Create test company
            val newCompany = Company(
                id = "spike-company-1",
                name = "Spike Test Company",
                selected = true,
                lastSyncedAt = System.currentTimeMillis()
            )
            companyDao.insert(newCompany)

            // Create 2 test products
            val product1 = ProductEntity(
                id = "prod-1",
                companyId = "spike-company-1",
                name = "Test Product 1",
                price = 100.0,
                isAvailable = true,
                lastSyncedAt = System.currentTimeMillis()
            )
            val product2 = ProductEntity(
                id = "prod-2",
                companyId = "spike-company-1",
                name = "Test Product 2",
                price = 200.0,
                isAvailable = true,
                lastSyncedAt = System.currentTimeMillis()
            )
            productDao.upsertAll(listOf(product1, product2))

            // Create 2 cart items
            val cartItem1 = CartItemEntity(
                companyId = "spike-company-1",
                productId = "prod-1",
                quantity = 2,
                addedAt = System.currentTimeMillis()
            )
            val cartItem2 = CartItemEntity(
                companyId = "spike-company-1",
                productId = "prod-2",
                quantity = 1,
                addedAt = System.currentTimeMillis()
            )
            cartItemDao.insert(cartItem1)
            cartItemDao.insert(cartItem2)
        }
    }

    @Composable
    fun SpikePriceValidationScreen() {
        var lastSyncedAt by remember { mutableStateOf<Long?>(null) }
        var validationResult by remember { mutableStateOf<PriceValidationResult?>(null) }
        var isLoading by remember { mutableStateOf(false) }

        // Load initial lastSyncedAt
        LaunchedEffect(Unit) {
            val company = companyDao.getCompanyById("spike-company-1")
            lastSyncedAt = company?.lastSyncedAt
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Spike S4: Price Validation",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Current timestamp display
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Current lastSyncedAt:",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = lastSyncedAt?.let { java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date(it)) } ?: "Not set",
                        fontSize = 14.sp
                    )
                    lastSyncedAt?.let { timestamp ->
                        val hoursAgo = (System.currentTimeMillis() - timestamp) / (1000 * 60 * 60)
                        Text(
                            text = "Age: ${hoursAgo}h ago",
                            fontSize = 14.sp,
                            color = if (hoursAgo < 24) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }
            }

            Divider()

            Text(
                text = "Test Cases:",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Test Case 1: Fresh (<24h)
            TestCaseButton(
                title = "Test 1: Fresh (<24h)",
                description = "Set lastSynced = now() - 12h",
                expectedResult = "Expected: AllValid",
                isLoading = isLoading,
                onClick = {
                    lifecycleScope.launch {
                        isLoading = true
                        validationResult = null
                        
                        // Update timestamp to 12 hours ago
                        val newTimestamp = System.currentTimeMillis() - (12 * 60 * 60 * 1000)
                        companyDao.updateLastSyncedAt("spike-company-1", newTimestamp)
                        lastSyncedAt = newTimestamp
                        
                        delay(500) // Simulate processing
                        
                        // Run validation
                        val result = cartRepository.validateCartPrices("spike-company-1", "fake-public-id")
                        validationResult = result
                        isLoading = false
                    }
                }
            )

            // Test Case 2: Stale + Online (no changes)
            TestCaseButton(
                title = "Test 2: Stale + Online (no changes)",
                description = "Set lastSynced = now() - 30h\nAssume online, no price changes",
                expectedResult = "Expected: AllValid (after sync)",
                isLoading = isLoading,
                onClick = {
                    lifecycleScope.launch {
                        isLoading = true
                        validationResult = null
                        
                        // Update timestamp to 30 hours ago
                        val newTimestamp = System.currentTimeMillis() - (30 * 60 * 60 * 1000)
                        companyDao.updateLastSyncedAt("spike-company-1", newTimestamp)
                        lastSyncedAt = newTimestamp
                        
                        delay(500)
                        
                        // Simulate sync: update lastSyncedAt to now (no price changes)
                        val syncedTimestamp = System.currentTimeMillis()
                        companyDao.updateLastSyncedAt("spike-company-1", syncedTimestamp)
                        lastSyncedAt = syncedTimestamp
                        
                        // Run validation
                        val result = cartRepository.validateCartPrices("spike-company-1", "fake-public-id")
                        validationResult = result
                        isLoading = false
                    }
                }
            )

            // Test Case 3: Stale + Online (price changed)
            TestCaseButton(
                title = "Test 3: Stale + Online (price changed)",
                description = "Set lastSynced = now() - 30h\nModify product price (100 → 150)",
                expectedResult = "Expected: PricesUpdated",
                isLoading = isLoading,
                onClick = {
                    lifecycleScope.launch {
                        isLoading = true
                        validationResult = null
                        
                        // Update timestamp to 30 hours ago
                        val newTimestamp = System.currentTimeMillis() - (30 * 60 * 60 * 1000)
                        companyDao.updateLastSyncedAt("spike-company-1", newTimestamp)
                        lastSyncedAt = newTimestamp
                        
                        delay(500)
                        
                        // Modify product price (simulate Sheets change)
                        val products = productDao.getAllByCompany("spike-company-1")
                        val product = products.find { it.id == "prod-1" }
                        product?.let {
                            productDao.upsertAll(listOf(it.copy(price = 150.0)))
                        }
                        
                        // Simulate sync: update lastSyncedAt to now
                        val syncedTimestamp = System.currentTimeMillis()
                        companyDao.updateLastSyncedAt("spike-company-1", syncedTimestamp)
                        lastSyncedAt = syncedTimestamp
                        
                        // Run validation
                        val result = cartRepository.validateCartPrices("spike-company-1", "fake-public-id")
                        validationResult = result
                        isLoading = false
                    }
                }
            )

            // Test Case 4: Stale + Offline
            TestCaseButton(
                title = "Test 4: Stale + Offline",
                description = "Set lastSynced = now() - 30h\n⚠️ Turn OFF WiFi before clicking!",
                expectedResult = "Expected: Blocked",
                isLoading = isLoading,
                onClick = {
                    lifecycleScope.launch {
                        isLoading = true
                        validationResult = null
                        
                        // Update timestamp to 30 hours ago
                        val newTimestamp = System.currentTimeMillis() - (30 * 60 * 60 * 1000)
                        companyDao.updateLastSyncedAt("spike-company-1", newTimestamp)
                        lastSyncedAt = newTimestamp
                        
                        delay(500)
                        
                        // Run validation (will check connectivity and block)
                        val result = cartRepository.validateCartPrices("spike-company-1", "fake-public-id")
                        validationResult = result
                        isLoading = false
                    }
                }
            )

            Divider()

            // Result display
            validationResult?.let { result ->
                ResultCard(result = result)
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Reset button
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    lifecycleScope.launch {
                        // Reset product prices
                        val products = productDao.getAllByCompany("spike-company-1")
                        val product1 = products.find { it.id == "prod-1" }
                        product1?.let {
                            productDao.upsertAll(listOf(it.copy(price = 100.0)))
                        }
                        
                        // Reset timestamp to now
                        val resetTimestamp = System.currentTimeMillis()
                        companyDao.updateLastSyncedAt("spike-company-1", resetTimestamp)
                        lastSyncedAt = resetTimestamp
                        
                        validationResult = null
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Reset Test Data")
            }
        }
    }

    @Composable
    fun TestCaseButton(
        title: String,
        description: String,
        expectedResult: String,
        isLoading: Boolean,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = expectedResult,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onClick,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Run Test")
                }
            }
        }
    }

    @Composable
    fun ResultCard(result: PriceValidationResult) {
        val backgroundColor = when (result) {
            is PriceValidationResult.AllValid -> Color(0xFFC8E6C9) // Light green
            is PriceValidationResult.PricesUpdated -> Color(0xFFFFF9C4) // Light yellow
            is PriceValidationResult.ItemsUnavailable -> Color(0xFFFFCDD2) // Light red
            is PriceValidationResult.Blocked -> Color(0xFFFFCDD2) // Light red
        }

        val textColor = when (result) {
            is PriceValidationResult.AllValid -> Color(0xFF2E7D32)
            is PriceValidationResult.PricesUpdated -> Color(0xFFF57F17)
            is PriceValidationResult.ItemsUnavailable -> Color(0xFFC62828)
            is PriceValidationResult.Blocked -> Color(0xFFC62828)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Validation Result:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                when (result) {
                    is PriceValidationResult.AllValid -> {
                        Text(
                            text = "✅ AllValid",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "All cart items have valid prices. Safe to proceed.",
                            fontSize = 14.sp,
                            color = textColor
                        )
                    }
                    is PriceValidationResult.PricesUpdated -> {
                        Text(
                            text = "⚠️ PricesUpdated",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "Updated items: ${result.changes.size}",
                            fontSize = 14.sp,
                            color = textColor
                        )
                        Text(
                            text = "New total: $${result.newTotal}",
                            fontSize = 14.sp,
                            color = textColor
                        )
                        result.changes.forEach { change ->
                            Text(
                                text = "• ${change.productName}: $${change.oldPrice} → $${change.newPrice}",
                                fontSize = 12.sp,
                                color = textColor,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                    is PriceValidationResult.ItemsUnavailable -> {
                        Text(
                            text = "🚫 ItemsUnavailable",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "Unavailable items: ${result.unavailableProducts.size}",
                            fontSize = 14.sp,
                            color = textColor
                        )
                        Text(
                            text = "Available total: $${result.availableTotal}",
                            fontSize = 14.sp,
                            color = textColor
                        )
                        result.unavailableProducts.forEach { product ->
                            Text(
                                text = "• ${product.productName} ($${product.lastKnownPrice})",
                                fontSize = 12.sp,
                                color = textColor,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                    is PriceValidationResult.Blocked -> {
                        Text(
                            text = "🚫 Blocked",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "Data is stale and device is offline. Cannot validate prices.",
                            fontSize = 14.sp,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
