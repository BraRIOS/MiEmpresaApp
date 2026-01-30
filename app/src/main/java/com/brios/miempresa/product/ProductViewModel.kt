package com.brios.miempresa.product

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.R
import com.brios.miempresa.categories.Category
import com.brios.miempresa.data.PreferencesKeys
import com.brios.miempresa.data.getFromDataStore
import com.brios.miempresa.domain.SpreadsheetsApi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProductViewModel
    @Inject
    constructor(
        private val spreadsheetsApi: SpreadsheetsApi,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {
        private val _currentProduct = MutableStateFlow<Product?>(null)
        val currentProduct = _currentProduct.asStateFlow()

        private val _isLoading = MutableStateFlow(false)
        val isLoading = _isLoading.asStateFlow()

        private val _categories = MutableStateFlow<List<Category>>(emptyList())
        val categories = _categories.asStateFlow()

        private var retryCount = 0
        private val maxRetries = 3

        init {
            _isLoading.value = true
        }

        fun updateProduct(
            updatedProduct: Product,
            selectedCategories: List<Category>,
            onResultSuccess: (Boolean) -> Unit,
        ) = viewModelScope.launch {
            val spreadsheetId = getFromDataStore(context, PreferencesKeys.SPREADSHEET_ID_KEY).firstOrNull()
            try {
                withContext(Dispatchers.IO) {
                    spreadsheetsApi.updateProductInSheet(spreadsheetId!!, updatedProduct, selectedCategories)
                }
                _isLoading.value = true
                loadProduct(updatedProduct.rowIndex)
                onResultSuccess(true)
            } catch (e: Exception) {
                _isLoading.value = false
                onResultSuccess(false)
                e.printStackTrace()
                Toast.makeText(context, context.getString(R.string.error_updating_product), Toast.LENGTH_SHORT).show()
            }
        }

        fun loadProduct(rowIndex: Int): Job =
            viewModelScope.launch {
                val spreadsheetId = getFromDataStore(context, PreferencesKeys.SPREADSHEET_ID_KEY).firstOrNull()
                try {
                    val response: Product? =
                        withContext(Dispatchers.IO) {
                            spreadsheetsApi.readProductFromSheet(spreadsheetId!!, rowIndex)
                        }
                    _isLoading.value = false
                    _currentProduct.value = response
                    retryCount = 0
                } catch (e: Exception) {
                    _isLoading.value = false
                    e.printStackTrace()
                    if (retryCount < maxRetries) {
                        retryCount++
                        delay(1000)
                        loadProduct(rowIndex)
                    }
                }
            }

        fun loadCategories() =
            viewModelScope.launch {
                val spreadsheetId = getFromDataStore(context, PreferencesKeys.SPREADSHEET_ID_KEY).firstOrNull()
                try {
                    val data =
                        withContext(Dispatchers.IO) {
                            spreadsheetsApi.readCategoriesFromSheet(spreadsheetId!!)
                        }
                    _categories.value = data
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        context,
                        context.getString(R.string.error_loading_categories),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }

        fun deleteProduct(
            rowIndex: Int,
            onResultSuccess: (Boolean) -> Unit,
        ) = viewModelScope.launch {
            val spreadsheetId = getFromDataStore(context, PreferencesKeys.SPREADSHEET_ID_KEY).firstOrNull()
            try {
                withContext(Dispatchers.IO) {
                    val sheetId = spreadsheetsApi.getSheetId(spreadsheetId!!, context.getString(R.string.sheet_1_name))
                    if (sheetId != null) {
                        spreadsheetsApi.deleteElementFromSheet(spreadsheetId, rowIndex, sheetId)
                    } else {
                        onResultSuccess(false)
                        Toast.makeText(
                            context,
                            context.getString(R.string.error_deleting_product),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
                onResultSuccess(true)
            } catch (e: Exception) {
                onResultSuccess(false)
                e.printStackTrace()
                Toast.makeText(context, context.getString(R.string.error_deleting_product), Toast.LENGTH_SHORT).show()
            }
        }
    }
