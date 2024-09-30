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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val spreadsheetId: StateFlow<String?> = getFromDataStore(context, PreferencesKeys.SPREADSHEET_ID_KEY)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    private var retryCount = 0
    private val maxRetries = 3

    init {
        _isLoading.value = true
    }

    fun updateProduct(updatedProduct: Product, selectedCategories: List<Category>, onResultSuccess: (Boolean) -> Unit) = viewModelScope.launch {
        try {
            withContext(Dispatchers.IO) {spreadsheetsApi.updateProductInSheet(spreadsheetId.value!!, updatedProduct, selectedCategories)}
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

    fun loadProduct(rowIndex: Int):Job = viewModelScope.launch {
        try {
            val response: Product? = withContext(Dispatchers.IO) {
                spreadsheetsApi.readProductFromSheet(spreadsheetId.value!!, rowIndex)
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

    fun deleteProduct(rowIndex: Int, onResultSuccess: (Boolean) -> Unit) = viewModelScope.launch {
        try {
            withContext(Dispatchers.IO) {spreadsheetsApi.deleteProductFromSheet(spreadsheetId.value!!, rowIndex)}
            onResultSuccess(true)
        } catch (e: Exception) {
            onResultSuccess(false)
            e.printStackTrace()
            Toast.makeText(context, context.getString(R.string.error_deleting_product), Toast.LENGTH_SHORT).show()
        }

    }

}
