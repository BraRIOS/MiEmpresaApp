package com.brios.miempresa.products.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.categories.data.Category
import com.brios.miempresa.categories.domain.CategoriesRepository
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.products.data.ProductEntity
import com.brios.miempresa.products.domain.ProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
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

        private val _saveComplete = MutableSharedFlow<Unit>(replay = 0)
        val saveComplete: SharedFlow<Unit> = _saveComplete.asSharedFlow()

        private val _nameError = MutableStateFlow<String?>(null)
        val nameError: StateFlow<String?> = _nameError

        private val _priceError = MutableStateFlow<String?>(null)
        val priceError: StateFlow<String?> = _priceError

        private val _categoryError = MutableStateFlow<String?>(null)
        val categoryError: StateFlow<String?> = _categoryError

        private var companyId: String? = null
        private var originalProduct: ProductEntity? = null

        private val _companyIdFlow = MutableStateFlow<String?>(null)

        val categories: StateFlow<List<Category>> =
            _companyIdFlow
                .flatMapLatest { id ->
                    if (id != null) categoriesRepository.getAll(id) else flowOf(emptyList())
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        init {
            viewModelScope.launch {
                companyId = companyDao.getSelectedOwnedCompany()?.id
                _companyIdFlow.value = companyId
                if (isEditMode && productId != null && companyId != null) {
                    val product = productsRepository.getById(productId, companyId!!)
                    product?.let {
                        originalProduct = it
                        _name.value = it.name
                        _price.value = it.price.toString()
                        _description.value = it.description ?: ""
                        _selectedCategoryId.value = it.categoryId
                        _isPublic.value = it.isPublic
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
                            isPublic = _isPublic.value,
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
                            isPublic = _isPublic.value,
                            localImagePath = _localImagePath.value,
                        ),
                    )
                }
                _isSaving.value = false
                _saveComplete.emit(Unit)
            }
        }

        fun delete() {
            val currentCompanyId = companyId ?: return
            if (productId == null) return
            viewModelScope.launch {
                productsRepository.delete(productId, currentCompanyId)
                _saveComplete.emit(Unit)
            }
        }
    }
