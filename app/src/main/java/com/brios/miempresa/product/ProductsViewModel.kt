package com.brios.miempresa.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.domain.SpreadsheetsApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val spreadsheetsApi: SpreadsheetsApi
) : ViewModel() {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    private val products = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _filteredProducts = MutableStateFlow<List<Product>>(emptyList())
    val filteredProducts = _filteredProducts.asStateFlow()

    init {
        _isLoading.value = true
    }

    fun loadData() = viewModelScope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    spreadsheetsApi.readProductsFromSheet()
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

    fun addProduct(newProduct: Product) = viewModelScope.launch {
        try {
            withContext(Dispatchers.IO) {
                spreadsheetsApi.addOrUpdateProductInSheet(newProduct)
            }
            _products.value += newProduct
            _filteredProducts.value = _products.value
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}