package com.brios.miempresa.categories

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
class CategoriesViewModel @Inject constructor(
    private val spreadsheetsApi: SpreadsheetsApi,
    @ApplicationContext private val context: Context
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

    fun getNextAvailableRowIndex(): Int {
        val maxRowIndex = _categories.value.maxOfOrNull { it.rowIndex } ?: 0
        return maxRowIndex + 1
    }

    fun addCategory(newCategory: Category, isResultSuccess: (Boolean) -> Unit) = viewModelScope.launch{
        try {
            withContext(Dispatchers.IO) {
                spreadsheetsApi.addCategoryInSheet(newCategory)
            }
            val updatedCategories = _categories.value.toMutableList()
            updatedCategories.add(newCategory)
            _categories.value = updatedCategories
            _filteredCategories.value = updatedCategories
            isResultSuccess(true)
        } catch (e: Exception) {
            e.printStackTrace()
            isResultSuccess(false)
            Toast.makeText(context, context.getString(R.string.error_adding_category), Toast.LENGTH_SHORT).show()
        }
    }

    fun updateCategory(newCategory: Category, isResultSuccess: (Boolean) -> Unit) = viewModelScope.launch{
        try {
            withContext(Dispatchers.IO) {
                spreadsheetsApi.updateCategoryInSheet(newCategory)
            }
            val updatedCategories = _categories.value.toMutableList()
            updatedCategories.add(newCategory)
            _categories.value = updatedCategories
            _filteredCategories.value = updatedCategories
            isResultSuccess(true)
        } catch (e: Exception) {
            e.printStackTrace()
            isResultSuccess(false)
            Toast.makeText(context, context.getString(R.string.error_updating_category), Toast.LENGTH_SHORT).show()
        }
    }
}