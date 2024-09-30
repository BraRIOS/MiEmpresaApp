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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val spreadsheetsApi: SpreadsheetsApi,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    private val products = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _filteredProducts = MutableStateFlow<List<Product>>(emptyList())
    val filteredProducts = _filteredProducts.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val spreadsheetId: StateFlow<String?> = getFromDataStore(context, PreferencesKeys.SPREADSHEET_ID_KEY)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    init {
        _isLoading.value = true
    }

    fun loadData() = viewModelScope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    spreadsheetsApi.readProductsFromSheet(spreadsheetId.value!!)
                }
                _products.value = data
                _isLoading.value = false
                _filteredProducts.value = _products.value
            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false
            }
    }


    fun onSearchQueryChange(query: String) {
        _filteredProducts.value = products.value.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }

    fun getNextAvailableRowIndex(): Int {
        val lastRowIndex = _products.value.lastOrNull()?.rowIndex ?: 0
        return lastRowIndex + 1

    }

    fun addProduct(newProduct: Product, selectedCategories: List<Category>, isResultSuccess: (Boolean) -> Unit) = viewModelScope.launch {
        try {
            withContext(Dispatchers.IO) {
                spreadsheetsApi.addProductInSheet(spreadsheetId.value!!, newProduct, selectedCategories)
            }
            _isLoading.value = true
            loadData()
            isResultSuccess(true)
        } catch (e: Exception) {
            _isLoading.value = false
            e.printStackTrace()
            Toast.makeText(context, context.getString(R.string.error_adding_product), Toast.LENGTH_SHORT).show()
            isResultSuccess(false)
        }
    }

    fun loadCategories() = viewModelScope.launch {
        try{
            val data = withContext(Dispatchers.IO) {
                spreadsheetsApi.readCategoriesFromSheet(spreadsheetId.value!!)
            }
            _categories.value = data
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, context.getString(R.string.error_loading_categories), Toast.LENGTH_SHORT).show()
        }
    }
}