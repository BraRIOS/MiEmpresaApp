package com.brios.miempresa.product

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.R
import com.brios.miempresa.domain.SpreadsheetsApi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject  constructor(
    private val spreadsheetsApi: SpreadsheetsApi,
    @ApplicationContext private val context: Context
):ViewModel() {
    private val _currentProduct = MutableStateFlow<Product?>(null)
    val currentProduct = _currentProduct.asStateFlow()

    fun updateProduct(updatedProduct: Product) = viewModelScope.launch {
        try {
            withContext(Dispatchers.IO) {spreadsheetsApi.addOrUpdateProductInSheet(updatedProduct)}
            _currentProduct.value = updatedProduct
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context,
                context.getString(R.string.error_updating_product), Toast.LENGTH_SHORT).show()
        }
    }

    fun loadProduct(rowIndex: Int) = viewModelScope.launch {
        try {
            val response : Product? = withContext(Dispatchers.IO) {
                spreadsheetsApi.readProductFromSheet(rowIndex)
            }
            _currentProduct.value = response
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
