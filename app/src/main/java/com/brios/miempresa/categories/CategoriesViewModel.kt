package com.brios.miempresa.categories

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
class CategoriesViewModel @Inject constructor(
    private val spreadsheetsApi: SpreadsheetsApi
) : ViewModel() {
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _filteredCategories = MutableStateFlow<List<Category>>(emptyList())
    val filteredCategories = _filteredCategories.asStateFlow()

    init {
        _isLoading.value = true
    }

    fun loadData() = viewModelScope.launch {
        try{
            val data = withContext(Dispatchers.IO) {
                spreadsheetsApi.readCategoriesFromSheet()
            }
            _categories.value = data
            _filteredCategories.value = _categories.value
            _isLoading.value = false
        } catch (e: Exception) {
            e.printStackTrace()
            _isLoading.value = false
        }
    }
}