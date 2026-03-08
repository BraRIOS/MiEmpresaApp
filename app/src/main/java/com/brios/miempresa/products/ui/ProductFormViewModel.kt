package com.brios.miempresa.products.ui

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.R
import com.brios.miempresa.categories.data.Category
import com.brios.miempresa.categories.domain.CategoriesRepository
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.sync.SyncManager
import com.brios.miempresa.core.sync.SyncType
import com.brios.miempresa.core.util.formatPlainDecimal
import com.brios.miempresa.products.data.ProductEntity
import com.brios.miempresa.products.domain.ProductSaveMode
import com.brios.miempresa.products.domain.ProductsRepository
import com.brios.miempresa.products.domain.SaveProductRequest
import com.brios.miempresa.products.domain.SaveProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ProductFormViewModel
@Inject
    constructor(
        private val productsRepository: ProductsRepository,
        private val categoriesRepository: CategoriesRepository,
        private val companyDao: CompanyDao,
        private val syncManager: SyncManager,
        private val saveProductUseCase: SaveProductUseCase,
        @param:ApplicationContext private val appContext: Context,
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
    private val productId: String? = savedStateHandle["productId"]
    val isEditMode: Boolean = productId != null

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _price = MutableStateFlow("")
    val price: StateFlow<String> = _price

    private val _hidePrice = MutableStateFlow(false)
    val hidePrice: StateFlow<Boolean> = _hidePrice

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId

    private val _isPublic = MutableStateFlow(true)
    val isPublic: StateFlow<Boolean> = _isPublic

    private val _localImagePath = MutableStateFlow<String?>(null)
    val localImagePath: StateFlow<String?> = _localImagePath
    private val _imageRemoved = MutableStateFlow(false)
    val imageRemoved: StateFlow<Boolean> = _imageRemoved

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _saveCompleted = MutableStateFlow(savedStateHandle[SAVE_COMPLETED_KEY] ?: false)
    val saveCompleted: StateFlow<Boolean> = _saveCompleted

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError

    private val _priceError = MutableStateFlow<String?>(null)
    val priceError: StateFlow<String?> = _priceError

    private val _categoryError = MutableStateFlow<String?>(null)
    val categoryError: StateFlow<String?> = _categoryError

    private var companyId: String? = null
    private var originalProduct: ProductEntity? = null

    val originalImageUrl: String? get() = originalProduct?.imageUrl

    private val _companyIdFlow = MutableStateFlow<String?>(null)

    val categories: StateFlow<List<Category>> =
        _companyIdFlow
            .flatMapLatest { id ->
                if (id != null) categoriesRepository.getAll(id) else flowOf(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var saveJob: Job? = null
    private var pendingNewCategorySnapshot: Set<String>? = null

    init {
        viewModelScope.launch {
            savedStateHandle.getStateFlow<String?>(CREATED_CATEGORY_RESULT_KEY, null).collect { createdCategoryId ->
                if (!createdCategoryId.isNullOrBlank()) {
                    _selectedCategoryId.value = createdCategoryId
                    _categoryError.value = null
                    pendingNewCategorySnapshot = null
                    savedStateHandle[CREATED_CATEGORY_RESULT_KEY] = null
                }
            }
        }

        viewModelScope.launch {
            categories.collect { currentCategories ->
                val snapshot = pendingNewCategorySnapshot ?: return@collect
                val createdCategory = currentCategories.firstOrNull { it.id !in snapshot } ?: return@collect
                _selectedCategoryId.value = createdCategory.id
                _categoryError.value = null
                pendingNewCategorySnapshot = null
            }
        }

        viewModelScope.launch {
            companyId = companyDao.getSelectedOwnedCompany()?.id
            _companyIdFlow.value = companyId
            if (isEditMode && productId != null && companyId != null) {
                val product = productsRepository.getById(productId, companyId!!)
                product?.let {
                    originalProduct = it
                    _name.value = it.name
                    _price.value = formatPlainDecimal(it.price)
                    _hidePrice.value = it.hidePrice
                    _description.value = it.description ?: ""
                    _selectedCategoryId.value = it.categoryId
                    _isPublic.value = it.isPublic
                    _localImagePath.value = it.localImagePath
                    _imageRemoved.value = false
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

    fun onHidePriceChanged(newHidePrice: Boolean) {
        _hidePrice.value = newHidePrice
        _priceError.value = null
    }

    fun onDescriptionChanged(newDescription: String) {
        _description.value = newDescription
    }

    fun onCategorySelected(categoryId: String) {
        _selectedCategoryId.value = categoryId
        _categoryError.value = null
        pendingNewCategorySnapshot = null
    }

    fun onCreateCategoryFlowStarted() {
        pendingNewCategorySnapshot = categories.value.map { it.id }.toSet()
    }

    fun onPublicChanged(isPublic: Boolean) {
        _isPublic.value = isPublic
    }

    fun onImageSelected(uriString: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val uri = uriString.toUri()
            val localFile = copyUriToInternalStorage(uri)
            val previousPath = _localImagePath.value
            val newPath = localFile?.absolutePath
            if (!previousPath.isNullOrBlank() && previousPath != newPath) {
                File(previousPath).delete()
            }
            _localImagePath.value = newPath
            _imageRemoved.value = false
        }
    }

    fun onImageRemoved() {
        val currentPath = _localImagePath.value
        if (currentPath != null) {
            viewModelScope.launch(Dispatchers.IO) {
                File(currentPath).delete()
            }
        }
        _localImagePath.value = null
        _imageRemoved.value = true
    }

    private fun copyUriToInternalStorage(uri: Uri): File? {
        return try {
            val inputStream = appContext.contentResolver.openInputStream(uri) ?: return null
            val imagesDir = File(appContext.filesDir, "product_images").apply { mkdirs() }
            val fileName = "product_${System.currentTimeMillis()}.jpg"
            val outputFile = File(imagesDir, fileName)
            inputStream.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            outputFile
        } catch (e: Exception) {
            null
        }
    }

    fun save() {
        if (_isSaving.value || _saveCompleted.value) return

        val currentCompanyId = companyId ?: return
        val currentName = _name.value.trim()
        val currentPrice = _price.value.toDoubleOrNull()
        val currentHidePrice = _hidePrice.value
        val currentCategoryId = _selectedCategoryId.value
        val currentIsPublic = _isPublic.value
        val currentLocalImagePath = _localImagePath.value
        val currentImageRemoved = _imageRemoved.value

        var hasError = false
        if (currentName.isBlank()) {
            _nameError.value = appContext.getString(R.string.error_name_required)
            hasError = true
        }
        if (!currentHidePrice && (currentPrice == null || currentPrice < 0)) {
            _priceError.value = appContext.getString(R.string.error_invalid_price)
            hasError = true
        }
        if (currentCategoryId == null) {
            _categoryError.value = appContext.getString(R.string.error_category_required)
            hasError = true
        }
        if (hasError) return

        val finalPrice =
            if (currentHidePrice) {
                currentPrice?.coerceAtLeast(0.0) ?: 0.0
            } else {
                currentPrice!!
            }

        _isSaving.value = true
        saveJob = viewModelScope.launch {
            val saveResult =
                saveProductUseCase(
                    SaveProductRequest(
                        mode = if (isEditMode) ProductSaveMode.Update else ProductSaveMode.Create,
                        companyId = currentCompanyId,
                        name = currentName,
                        price = finalPrice,
                        hidePrice = currentHidePrice,
                        description = _description.value.ifBlank { null },
                        categoryId = currentCategoryId,
                        isPublic = currentIsPublic,
                        localImagePath = currentLocalImagePath,
                        imageRemoved = currentImageRemoved,
                        existingProduct = originalProduct,
                    ),
                )

            if (saveResult != null) {
                if (isEditMode) {
                    originalProduct = saveResult.product
                }
                syncManager.syncNow(SyncType.PRODUCTS)
                markSaveCompleted()
            } else {
                _isSaving.value = false
            }
        }
    }

    fun cancelSave() {
        saveJob?.cancel()
        _isSaving.value = false
    }

    fun delete() {
        val currentCompanyId = companyId ?: return
        if (productId == null) return
        viewModelScope.launch {
            productsRepository.delete(productId, currentCompanyId)
            syncManager.syncNow(SyncType.PRODUCTS)
            markSaveCompleted()
        }
    }

    fun onSaveNavigationHandled() {
        _saveCompleted.value = false
        savedStateHandle[SAVE_COMPLETED_KEY] = false
    }

    private fun markSaveCompleted() {
        _isSaving.value = false
        _imageRemoved.value = false
        _saveCompleted.value = true
        savedStateHandle[SAVE_COMPLETED_KEY] = true
    }

    companion object {
        private const val SAVE_COMPLETED_KEY = "product_form_save_completed"
        const val CREATED_CATEGORY_RESULT_KEY = "created_category_id"
    }
}
