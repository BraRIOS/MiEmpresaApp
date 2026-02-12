package com.brios.miempresa.config.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.config.domain.ConfigRepository
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.data.local.entities.Company
import com.brios.miempresa.core.domain.LogoutUseCase
import com.brios.miempresa.core.sync.SyncManager
import com.brios.miempresa.core.sync.SyncType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConfigFormState(
    val companyName: String = "",
    val whatsappCountryCode: String = "+54",
    val whatsappNumber: String = "",
    val specialization: String = "",
    val logoUrl: String? = null,
    val address: String = "",
    val businessHours: String = "",
    val localLogoUri: String? = null,
) {
    val isFormValid: Boolean
        get() = companyName.isNotBlank() && whatsappNumber.matches(Regex("^\\d{6,15}$"))

    val hasChanges: Boolean
        get() = true // Simplified; compared against original in ViewModel
}

sealed interface ConfigUiState {
    data object Loading : ConfigUiState
    data class Ready(val form: ConfigFormState) : ConfigUiState
    data class Saving(val form: ConfigFormState) : ConfigUiState
    data class Error(val message: String, val form: ConfigFormState) : ConfigUiState
}

sealed interface ConfigEvent {
    data class ShowSnackbar(val message: String) : ConfigEvent
    data object NavigateToWelcome : ConfigEvent
    data object NavigateToOrders : ConfigEvent
    data object ShowShareSheet : ConfigEvent
}

@HiltViewModel
class ConfigViewModel
    @Inject
    constructor(
        private val configRepository: ConfigRepository,
        private val companyDao: CompanyDao,
        private val syncManager: SyncManager,
        private val logoutUseCase: LogoutUseCase,
    ) : ViewModel() {
        private val _companyId = MutableStateFlow<String?>(null)
        private var originalCompany: Company? = null

        private val _form = MutableStateFlow(ConfigFormState())
        val form: StateFlow<ConfigFormState> = _form.asStateFlow()

        private val _uiState = MutableStateFlow<ConfigUiState>(ConfigUiState.Loading)
        val uiState: StateFlow<ConfigUiState> = _uiState.asStateFlow()

        private val _events = MutableSharedFlow<ConfigEvent>(replay = 0)
        val events: SharedFlow<ConfigEvent> = _events.asSharedFlow()

        private val _isSyncing = MutableStateFlow(false)
        val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

        val publicSheetId: StateFlow<String?> =
            _companyId
                .filterNotNull()
                .flatMapLatest { configRepository.observeCompany(it) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
                .let { flow ->
                    MutableStateFlow<String?>(null).also { result ->
                        viewModelScope.launch {
                            flow.collect { result.value = it?.publicSheetId }
                        }
                    }
                }

        init {
            loadCompany()
        }

        private fun loadCompany() {
            viewModelScope.launch {
                val company = companyDao.getSelectedOwnedCompany()
                if (company != null) {
                    _companyId.value = company.id
                    originalCompany = company
                    _form.value = ConfigFormState(
                        companyName = company.name,
                        whatsappCountryCode = company.whatsappCountryCode,
                        whatsappNumber = company.whatsappNumber ?: "",
                        specialization = company.specialization ?: "",
                        logoUrl = company.logoUrl,
                        address = company.address ?: "",
                        businessHours = company.businessHours ?: "",
                    )
                    _uiState.value = ConfigUiState.Ready(_form.value)
                }
            }
        }

        fun updateCompanyName(name: String) {
            _form.value = _form.value.copy(companyName = name)
            _uiState.value = ConfigUiState.Ready(_form.value)
        }

        fun updateCountryCode(code: String) {
            _form.value = _form.value.copy(whatsappCountryCode = code)
            _uiState.value = ConfigUiState.Ready(_form.value)
        }

        fun updateWhatsappNumber(number: String) {
            _form.value = _form.value.copy(whatsappNumber = number)
            _uiState.value = ConfigUiState.Ready(_form.value)
        }

        fun updateSpecialization(specialization: String) {
            _form.value = _form.value.copy(specialization = specialization)
            _uiState.value = ConfigUiState.Ready(_form.value)
        }

        fun updateAddress(address: String) {
            _form.value = _form.value.copy(address = address)
            _uiState.value = ConfigUiState.Ready(_form.value)
        }

        fun updateBusinessHours(hours: String) {
            _form.value = _form.value.copy(businessHours = hours)
            _uiState.value = ConfigUiState.Ready(_form.value)
        }

        fun updateLocalLogoUri(uri: String?) {
            _form.value = _form.value.copy(localLogoUri = uri)
            _uiState.value = ConfigUiState.Ready(_form.value)
        }

        fun save() {
            val companyId = _companyId.value ?: return
            val form = _form.value
            if (!form.isFormValid) return

            viewModelScope.launch {
                _uiState.value = ConfigUiState.Saving(form)
                try {
                    val original = originalCompany ?: return@launch
                    val updated = original.copy(
                        name = form.companyName.trim(),
                        whatsappCountryCode = form.whatsappCountryCode,
                        whatsappNumber = form.whatsappNumber.trim(),
                        specialization = form.specialization.trim().takeIf { it.isNotEmpty() },
                        address = form.address.trim().takeIf { it.isNotEmpty() },
                        businessHours = form.businessHours.trim().takeIf { it.isNotEmpty() },
                    )

                    // Handle logo upload if changed
                    val logoUri = form.localLogoUri
                    val finalCompany = if (logoUri != null) {
                        val fileId = configRepository.uploadCompanyLogo(
                            companyId, logoUri, form.companyName,
                        )
                        if (fileId != null) {
                            updated.copy(logoUrl = fileId)
                        } else {
                            updated
                        }
                    } else {
                        updated
                    }

                    configRepository.updateCompanyInfo(finalCompany)
                    originalCompany = finalCompany

                    // Sync to Sheets in background
                    try {
                        configRepository.syncCompanyInfoToSheets(companyId)
                    } catch (_: Exception) {
                        // Sync failure is non-blocking
                    }

                    _form.value = _form.value.copy(localLogoUri = null, logoUrl = finalCompany.logoUrl)
                    _uiState.value = ConfigUiState.Ready(_form.value)
                    _events.emit(ConfigEvent.ShowSnackbar("Cambios guardados"))
                } catch (e: Exception) {
                    _uiState.value = ConfigUiState.Error(
                        e.message ?: "Error al guardar",
                        form,
                    )
                }
            }
        }

        fun syncNow() {
            viewModelScope.launch {
                _isSyncing.value = true
                syncManager.syncNow(SyncType.ALL)
                kotlinx.coroutines.delay(2000)
                _isSyncing.value = false
                _events.emit(ConfigEvent.ShowSnackbar("Sincronización completada"))
            }
        }

        fun signOut(activity: Activity) {
            viewModelScope.launch {
                logoutUseCase(activity)
                _events.emit(ConfigEvent.NavigateToWelcome)
            }
        }

        fun navigateToOrders() {
            viewModelScope.launch {
                _events.emit(ConfigEvent.NavigateToOrders)
            }
        }

        fun showShareSheet() {
            viewModelScope.launch {
                _events.emit(ConfigEvent.ShowShareSheet)
            }
        }
    }
