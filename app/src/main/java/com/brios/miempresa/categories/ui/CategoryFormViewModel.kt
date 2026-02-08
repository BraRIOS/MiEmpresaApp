package com.brios.miempresa.categories.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.categories.data.Category
import com.brios.miempresa.categories.domain.CategoriesRepository
import com.brios.miempresa.categories.domain.EmojiData
import com.brios.miempresa.core.data.local.daos.CompanyDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryFormViewModel
    @Inject
    constructor(
        private val categoriesRepository: CategoriesRepository,
        private val companyDao: CompanyDao,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val categoryId: String? = savedStateHandle["categoryId"]
        val isEditMode: Boolean = categoryId != null

        private val _name = MutableStateFlow("")
        val name: StateFlow<String> = _name

        private val _selectedEmoji = MutableStateFlow(EmojiData.allEmojis.first())
        val selectedEmoji: StateFlow<String> = _selectedEmoji

        private val _nameError = MutableStateFlow<String?>(null)
        val nameError: StateFlow<String?> = _nameError

        private val _isSaving = MutableStateFlow(false)
        val isSaving: StateFlow<Boolean> = _isSaving

        private val _saveComplete = MutableStateFlow(false)
        val saveComplete: StateFlow<Boolean> = _saveComplete

        private val _productCount = MutableStateFlow(0)
        val productCount: StateFlow<Int> = _productCount

        private var companyId: String? = null

        init {
            viewModelScope.launch {
                companyId = companyDao.getSelectedOwnedCompany()?.id
                if (isEditMode && categoryId != null && companyId != null) {
                    val category = categoriesRepository.getById(categoryId, companyId!!)
                    category?.let {
                        _name.value = it.name
                        _selectedEmoji.value = it.iconEmoji
                    }
                    _productCount.value =
                        categoriesRepository.getProductCount(
                            categoryId,
                            companyId!!,
                        )
                }
            }
        }

        fun onNameChanged(newName: String) {
            if (newName.length <= MAX_NAME_LENGTH) {
                _name.value = newName
                _nameError.value = null
            }
        }

        fun onEmojiSelected(emoji: String) {
            _selectedEmoji.value = emoji
        }

        fun save() {
            val currentCompanyId = companyId ?: return
            val currentName = _name.value.trim()

            if (currentName.isBlank()) {
                _nameError.value = "El nombre es obligatorio"
                return
            }

            viewModelScope.launch {
                _isSaving.value = true
                if (isEditMode && categoryId != null) {
                    categoriesRepository.update(
                        Category(
                            id = categoryId,
                            name = currentName,
                            iconEmoji = _selectedEmoji.value,
                            companyId = currentCompanyId,
                            dirty = true,
                        ),
                    )
                } else {
                    categoriesRepository.create(
                        Category(
                            id = "",
                            name = currentName,
                            iconEmoji = _selectedEmoji.value,
                            companyId = currentCompanyId,
                        ),
                    )
                }
                _isSaving.value = false
                _saveComplete.value = true
            }
        }

        fun delete() {
            val currentCompanyId = companyId ?: return
            if (categoryId == null) return
            viewModelScope.launch {
                categoriesRepository.delete(categoryId, currentCompanyId)
                _saveComplete.value = true
            }
        }

        companion object {
            const val MAX_NAME_LENGTH = 50
        }
    }
