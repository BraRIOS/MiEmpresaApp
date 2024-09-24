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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

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
                _products.value = data?.getValues()?.mapIndexed{
                    index, it ->
                    Product(
                        rowIndex = index,
                        name = it[0] as String,
                        description = it[1] as String,
                        price = it[2] as String,
                        category = it[3] as String,
                        imageUrl = it[4] as String
                    )
                } ?: emptyList()
                _isLoading.value = false
                _filteredProducts.value = _products.value
            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false
            }
    }


    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _filteredProducts.value = products.value.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }
}