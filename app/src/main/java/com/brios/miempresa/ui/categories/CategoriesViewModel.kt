package com.brios.miempresa.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor() : ViewModel() {
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _filteredCategories = MutableStateFlow<List<Category>>(emptyList())
    val filteredCategories = _filteredCategories.asStateFlow()

    init {
        viewModelScope.launch {
            _isLoading.value = true
            _categories.value = listOf(
                Category("Meme", 4, "https://img4.s3wfg.com/web/img/images_uploaded/1/9/pepecoin-min.JPG"),
                Category("Meme", 4, "https://img4.s3wfg.com/web/img/images_uploaded/1/9/pepecoin-min.JPG"),
                Category("Meme", 4, "https://img4.s3wfg.com/web/img/images_uploaded/1/9/pepecoin-min.JPG"),
                Category("Meme", 4, "https://img4.s3wfg.com/web/img/images_uploaded/1/9/pepecoin-min.JPG"),
                Category("Meme", 4, "https://img4.s3wfg.com/web/img/images_uploaded/1/9/pepecoin-min.JPG")
            )
            _isLoading.value = false
            _filteredCategories.value = _categories.value
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _filteredCategories.value = categories.value.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }
}