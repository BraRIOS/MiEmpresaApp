package com.brios.miempresa.categories.ui

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.R
import com.brios.miempresa.categories.data.Category
import com.brios.miempresa.categories.domain.CategoriesRepository
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.sync.SyncManager
import com.brios.miempresa.core.sync.SyncType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryFormViewModel
    @Inject
    constructor(
        @ApplicationContext private val appContext: Context,
        private val categoriesRepository: CategoriesRepository,
        private val companyDao: CompanyDao,
        private val syncManager: SyncManager,
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val categoryId: String? = savedStateHandle["categoryId"]
        val isEditMode: Boolean = categoryId != null

        private val _name = MutableStateFlow("")
        val name: StateFlow<String> = _name

        private val _selectedEmoji = MutableStateFlow("")
        val selectedEmoji: StateFlow<String> = _selectedEmoji

        private val _nameError = MutableStateFlow<String?>(null)
        val nameError: StateFlow<String?> = _nameError

        private val _isSaving = MutableStateFlow(false)
        val isSaving: StateFlow<Boolean> = _isSaving

        private val _saveCompleted = MutableStateFlow(savedStateHandle[SAVE_COMPLETED_KEY] ?: false)
        val saveCompleted: StateFlow<Boolean> = _saveCompleted

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
            if (_isSaving.value || _saveCompleted.value) return

            val currentCompanyId = companyId ?: return
            val currentName = _name.value.trim()

            if (currentName.isBlank()) {
                _nameError.value = appContext.getString(R.string.error_name_required)
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
                syncManager.syncNow(SyncType.CATEGORIES)
                markSaveCompleted()
            }
        }

        fun delete() {
            val currentCompanyId = companyId ?: return
            if (_saveCompleted.value) return
            if (categoryId == null) return
            viewModelScope.launch {
                categoriesRepository.delete(categoryId, currentCompanyId)
                syncManager.syncNow(SyncType.CATEGORIES)
                markSaveCompleted()
            }
        }

        fun onSaveNavigationHandled() {
            _saveCompleted.value = false
            savedStateHandle[SAVE_COMPLETED_KEY] = false
        }

        private fun markSaveCompleted() {
            _isSaving.value = false
            _saveCompleted.value = true
            savedStateHandle[SAVE_COMPLETED_KEY] = true
        }

        companion object {
            private const val SAVE_COMPLETED_KEY = "category_form_save_completed"
            const val MAX_NAME_LENGTH = 50
        }
    }
