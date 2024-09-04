package com.brios.miempresa.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor() : ViewModel() {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _filteredProducts = MutableStateFlow<List<Product>>(emptyList())
    val filteredProducts = _filteredProducts.asStateFlow()

    init {
        // Simulamos la carga de productos desde una fuente de datos
        viewModelScope.launch {
            _isLoading.value = true
            _products.value = listOf(
                Product(
                    "Pepe CEO",
                    "Meme de Pepe el sapo que se convirtió en CEO exitoso",
                    "$10.00",
                    "Meme",
                    "https://img4.s3wfg.com/web/img/images_uploaded/1/9/pepecoin-min.JPG"
                ),
                Product(
                    "Pedro pedro pedro",
                    "Meme del mapache con la canción de pedro pedro pedro",
                    "$20.00",
                    "Meme",
                    "https://mixradio.co/wp-content/uploads/2024/05/pedro-mapache.jpg"
                ),
                Product(
                    "Huh",
                    "Meme de gato huh",
                    "$30.00",
                    "Meme",
                    "https://media.tenor.com/vmSP8owuOYYAAAAM/huh-cat-huh-m4rtin.gif"
                ),
                Product(
                    "Shrek",
                    "Meme de Shrek sospechoso",
                    "$40.00",
                    "Meme",
                    "https://media.tenor.com/mtiOW6O-k8YAAAAM/shrek-shrek-rizz.gif"
                ),
                Product(
                    "Shrek",
                    "Meme de Shrek sospechoso",
                    "$40.00",
                    "Meme",
                    "https://media.tenor.com/mtiOW6O-k8YAAAAM/shrek-shrek-rizz.gif"
                ),
                Product(
                    "Shrek",
                    "Meme de Shrek sospechoso",
                    "$40.00",
                    "Meme",
                    "https://media.tenor.com/mtiOW6O-k8YAAAAM/shrek-shrek-rizz.gif"
                ),
                Product(
                    "Shrek",
                    "Meme de Shrek sospechoso",
                    "$40.00",
                    "Meme",
                    "https://media.tenor.com/mtiOW6O-k8YAAAAM/shrek-shrek-rizz.gif"
                ),
            )
            _isLoading.value = false
            _filteredProducts.value = _products.value
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _filteredProducts.value = products.value.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }
}