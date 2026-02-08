package com.brios.miempresa.products.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.categories.domain.CategoriesRepository
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.data.local.entities.Category
import com.brios.miempresa.core.data.local.entities.ProductEntity
import com.brios.miempresa.products.domain.ProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductFormViewModel
    @Inject
    constructor(
        private val productsRepository: ProductsRepository,
        private val categoriesRepository: CategoriesRepository,
        private val companyDao: CompanyDao,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val productId: String? = savedStateHandle["productId"]
        val isEditMode: Boolean = productId != null

        private val _name = MutableStateFlow("")
        val name: StateFlow<String> = _name

        private val _price = MutableStateFlow("")
        val price: StateFlow<String> = _price

        private val _description = MutableStateFlow("")
        val description: StateFlow<String> = _description

        private val _selectedCategoryId = MutableStateFlow<String?>(null)
        val selectedCategoryId: StateFlow<String?> = _selectedCategoryId

        private val _isPublic = MutableStateFlow(true)
        val isPublic: StateFlow<Boolean> = _isPublic

        private val _localImagePath = MutableStateFlow<String?>(null)
        val localImagePath: StateFlow<String?> = _localImagePath

        private val _isSaving = MutableStateFlow(false)
        val isSaving: StateFlow<Boolean> = _isSaving

        private val _saveComplete = MutableStateFlow(false)
        val saveComplete: StateFlow<Boolean> = _saveComplete

        private val _nameError = MutableStateFlow<String?>(null)
        val nameError: StateFlow<String?> = _nameError

        private val _priceError = MutableStateFlow<String?>(null)
        val priceError: StateFlow<String?> = _priceError

        private val _categoryError = MutableStateFlow<String?>(null)
        val categoryError: StateFlow<String?> = _categoryError

        private var companyId: String? = null
        private var originalProduct: ProductEntity? = null

        val categories: StateFlow<List<Category>> =
            MutableStateFlow<List<Category>>(emptyList()).also { flow ->
                viewModelScope.launch {
                    companyId = companyDao.getSelectedOwnedCompany()?.id
                    companyId?.let { id ->
                        categoriesRepository.getAll(id)
                            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
                            .collect { flow.value = it }
                    }
                }
            }

        init {
            viewModelScope.launch {
                companyId = companyDao.getSelectedOwnedCompany()?.id
                if (isEditMode && productId != null && companyId != null) {
                    val product = productsRepository.getById(productId, companyId!!)
                    product?.let {
                        originalProduct = it
                        _name.value = it.name
                        _price.value = it.price.toString()
                        _description.value = it.description ?: ""
                        _selectedCategoryId.value = it.categoryId
                        _isPublic.value = it.publico
                        _localImagePath.value = it.localImagePath
                    }
                }
            }
        }

        fun onNameChanged(newName: String) {
            _name.value = newName
            _nameError.value = null
        }

        fun onPriceChanged(newPrice: String) {
            _price.value = newPrice
            _priceError.value = null
        }

        fun onDescriptionChanged(newDescription: String) {
            _description.value = newDescription
        }

        fun onCategorySelected(categoryId: String) {
            _selectedCategoryId.value = categoryId
            _categoryError.value = null
        }

        fun onPublicChanged(isPublic: Boolean) {
            _isPublic.value = isPublic
        }

        fun onImageSelected(path: String) {
            _localImagePath.value = path
        }

        fun save() {
            val currentCompanyId = companyId ?: return
            val currentName = _name.value.trim()
            val currentPrice = _price.value.toDoubleOrNull()
            val currentCategoryId = _selectedCategoryId.value

            var hasError = false
            if (currentName.isBlank()) {
                _nameError.value = "El nombre es obligatorio"
                hasError = true
            }
            if (currentPrice == null || currentPrice <= 0) {
                _priceError.value = "Ingresá un precio válido"
                hasError = true
            }
            if (currentCategoryId == null) {
                _categoryError.value = "Seleccioná una categoría"
                hasError = true
            }
            if (hasError) return

            viewModelScope.launch {
                _isSaving.value = true
                if (isEditMode && productId != null) {
                    val existing = originalProduct ?: return@launch
                    productsRepository.update(
                        existing.copy(
                            name = currentName,
                            price = currentPrice!!,
                            description = _description.value.ifBlank { null },
                            categoryId = currentCategoryId,
                            publico = _isPublic.value,
                            localImagePath = _localImagePath.value,
                        ),
                    )
                } else {
                    productsRepository.create(
                        ProductEntity(
                            id = "",
                            name = currentName,
                            price = currentPrice!!,
                            companyId = currentCompanyId,
                            description = _description.value.ifBlank { null },
                            categoryId = currentCategoryId,
                            publico = _isPublic.value,
                            localImagePath = _localImagePath.value,
                        ),
                    )
                }
                _isSaving.value = false
                _saveComplete.value = true
            }
        }

        fun delete() {
            val currentCompanyId = companyId ?: return
            if (productId == null) return
            viewModelScope.launch {
                productsRepository.delete(productId, currentCompanyId)
                _saveComplete.value = true
            }
        }
    }
