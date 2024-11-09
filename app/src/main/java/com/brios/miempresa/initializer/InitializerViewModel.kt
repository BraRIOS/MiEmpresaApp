package com.brios.miempresa.initializer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.R
import com.brios.miempresa.data.Company
import com.brios.miempresa.data.MiEmpresaDatabase
import com.brios.miempresa.data.PreferencesKeys
import com.brios.miempresa.data.getFromDataStore
import com.brios.miempresa.data.saveToDataStore
import com.brios.miempresa.domain.BiometricAuthManager
import com.brios.miempresa.domain.DriveApi
import com.brios.miempresa.domain.GoogleAuthClient
import com.google.api.services.drive.model.File
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.Serializable

@RequiresApi(Build.VERSION_CODES.R)
@HiltViewModel( assistedFactory = InitializerViewModelFactory::class )
class InitializerViewModel @AssistedInject constructor(
    private val driveApi: DriveApi,
    @Assisted private val context: Context,
    @Assisted private val startUIState: InitializerUiState?,
    private val googleAuthClient: GoogleAuthClient,
    private val biometricAuthManager: BiometricAuthManager
) : ViewModel() {

    private val miEmpresaDatabase = MiEmpresaDatabase.getDatabase(context)
    private val _uiState = MutableStateFlow(startUIState ?: InitializerUiState.Loading)
    val uiState: StateFlow<InitializerUiState> = _uiState.asStateFlow()
    private val companyDao = miEmpresaDatabase.companyDao()

    init {
        viewModelScope.launch {
            if (_uiState.value is InitializerUiState.ShowCompanyList) {
                goToCompanyListScreen()
            } else {
                val alreadySelectedCompany =
                    companyDao.getSelectedCompany().asFlow().firstOrNull() != null
                if (alreadySelectedCompany) {
                    reauthenticate()
                } else {
                    _uiState.value = InitializerUiState.Loading
                    checkForMainFolder()
                }
            }
        }
    }

    private fun reauthenticate() {
        val biometricStatus = BiometricManager.from(context)
            .canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        when (biometricStatus) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                tryBiometricAuthentication()

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                _uiState.value =
                    InitializerUiState.Error("No biometric features available on this device")

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                _uiState.value =
                    InitializerUiState.Error("Biometric features are currently unavailable")

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                    )
                }
                startActivityForResult(context as Activity, enrollIntent, 100, null)
            }

            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                _uiState.value =
                    InitializerUiState.Error("You can't use biometric auth until you have updated your security details")
            }

            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                _uiState.value =
                    InitializerUiState.Error("You can't use biometric auth with this Android version")
            }

            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                _uiState.value = InitializerUiState.Error("Biometric auth status unknown")
            }
        }
    }

    private fun tryBiometricAuthentication() {
        biometricAuthManager.authenticate(
            context,
            onError = {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_authentication), Toast.LENGTH_SHORT
                ).show()
            },
            onSuccess = {
                viewModelScope.launch {
                    _uiState.value = InitializerUiState.Loading
                    checkForMainFolder()
                }
            },
            onFail = {
                Toast.makeText(
                    context,
                    context.getString(R.string.try_again), Toast.LENGTH_SHORT
                ).show()
                tryBiometricAuthentication()
            }
        )
    }

    private suspend fun checkForMainFolder(){
        val mainFolder = driveApi.findMainFolder()
        if (mainFolder != null) {
            checkDataAndFindCompanies(mainFolder.id)
        } else {
            val user = googleAuthClient.getSignedInUser()
            _uiState.value = InitializerUiState.Welcome(
                username = user?.username?: context.getString(R.string.user))
        }
    }

    private suspend fun checkDataAndFindCompanies(mainFolderId: String) {
        val spreadsheetId = getFromDataStore(context, PreferencesKeys.SPREADSHEET_ID_KEY).firstOrNull()
        val selectedCompany = companyDao.getSelectedCompany().asFlow().firstOrNull()

        if (spreadsheetId != null && selectedCompany != null) {
            _uiState.value = InitializerUiState.Loading
            findCompanyFolders(mainFolderId)
        } else {
            _uiState.value = InitializerUiState.CheckingData
            findCompanyFolders(mainFolderId)
        }
    }

    private suspend fun findCompanyFolders(mainFolderId: String) {
        val companyFolders = driveApi.listFoldersInFolder(mainFolderId)
        if (!companyFolders.isNullOrEmpty()) {
            saveCompaniesToRoom(companyFolders)
        } else {
            val user = googleAuthClient.getSignedInUser()
            _uiState.value = InitializerUiState.Welcome(
                username = user?.username?: context.getString(R.string.user))
        }
    }

    private suspend fun saveCompaniesToRoom(companyFolders: List<File>) {
        val existingCompanies= companyDao.getCompanies().asFlow().first()
        companyFolders.forEach { folder ->
            val existingCompany = existingCompanies.find { it.id == folder.id }
            if (existingCompany != null) {
                if (existingCompany.selected) {
                    companyDao.update(existingCompany.copy(name = folder.name))
                }
            } else {
                companyDao.insert(Company(folder.id, folder.name, selected = false))
            }
        }
        val selectedCompany = companyDao.getSelectedCompany().asFlow().firstOrNull()
        if (selectedCompany != null) {
            searchSpreadsheet(selectedCompany)
        } else {
            _uiState.value = InitializerUiState.CompanyList(
                companies = companyDao.getCompanies(),
                username = googleAuthClient.getSignedInUser()?.username?: context.getString(R.string.user)
            )
        }
    }

    fun searchSpreadsheet(company: Company) = viewModelScope.launch {
        _uiState.value = InitializerUiState.SearchingSpreadsheet
        val spreadsheet = driveApi.findSpreadsheetInFolder(company.id)
        if (spreadsheet != null) {
            saveSpreadsheetIdAndCompany(spreadsheet.id, company)
        } else {
            _uiState.value = InitializerUiState.SpreadsheetNotFound(company)
        }
    }


    private suspend fun saveSpreadsheetIdAndCompany(spreadsheetId: String, company: Company) {
        saveToDataStore(context, spreadsheetId, PreferencesKeys.SPREADSHEET_ID_KEY)
        companyDao.update(company.copy(selected = true))
        _uiState.value = InitializerUiState.NavigateToProducts
    }

    fun goToCreateCompanyScreen() {
        val user = googleAuthClient.getSignedInUser()
        _uiState.value = InitializerUiState.Welcome(
            username = user?.username?: context.getString(R.string.user),
            isFirstTime = false)
    }

    fun goToCompanyListScreen() {
        _uiState.value = InitializerUiState.CompanyList(
            companies = companyDao.getCompanies(),
            username = googleAuthClient.getSignedInUser()?.username?: context.getString(R.string.user)
        )
    }

    fun createCompany(companyName: String) {
        viewModelScope.launch {
            _uiState.value =InitializerUiState.CreatingCompany
            val mainFolder = driveApi.createMainFolder()
            val companyFolder = driveApi.createCompanyFolder(mainFolder!!.id, companyName)
            companyDao.insert(Company(companyFolder!!.id, companyName, selected = true))
            _uiState.value = InitializerUiState.CreatingSpreadsheet
            createAndInitializeSpreadsheet(companyFolder.id)
        }
    }

    fun createAndInitializeSpreadsheet(companyId: String) = viewModelScope.launch {
        val company = companyDao.getCompanyById(companyId)
        if (company==null) {
            _uiState.value = InitializerUiState.CompanyList(
                companies = companyDao.getCompanies(),
                username = googleAuthClient.getSignedInUser()?.username?: context.getString(R.string.user)
            )
            return@launch
        }
        val spreadsheet = driveApi.createAndInitializeSpreadsheet(companyId)
        if (spreadsheet != null) {
            saveSpreadsheetIdAndCompany(spreadsheet.spreadsheetId, company)
        } else {
            _uiState.value = InitializerUiState.SpreadsheetNotFound(company)
        }
    }

    fun retrySearchSpreadsheet(companyId: String) = viewModelScope.launch {
        _uiState.value = InitializerUiState.SearchingSpreadsheet
        val company = companyDao.getCompanyById(companyId)
        if (company == null) {
            val user = googleAuthClient.getSignedInUser()
            _uiState.value = InitializerUiState.CompanyList(
                companies = companyDao.getCompanies(),
                username = user?.username?: context.getString(R.string.user)
            )
            return@launch
        }
        val spreadsheet = driveApi.findSpreadsheetInFolder(companyId)
        if (spreadsheet != null) {
            saveSpreadsheetIdAndCompany(spreadsheet.id, company)
        }else{
            _uiState.value = InitializerUiState.SpreadsheetNotFound(company)
        }
    }

    fun deleteCompany(company: Company) = viewModelScope.launch {
        companyDao.delete(company)
    }
}

sealed class InitializerUiState {
    data object Loading : InitializerUiState()
    data class Error(val message: String) : InitializerUiState()
    data class Welcome(val username: String, val isFirstTime: Boolean = true) : InitializerUiState()
    data object CheckingData : InitializerUiState()
    data class CompanyList(val companies: LiveData<List<Company>>, val username: String) : InitializerUiState()
    data object SearchingSpreadsheet : InitializerUiState()
    data class SpreadsheetNotFound(val company: Company) : InitializerUiState()
    data object CreatingCompany : InitializerUiState()
    data object CreatingSpreadsheet : InitializerUiState()
    data object NavigateToProducts : InitializerUiState()
    data object ShowCompanyList : InitializerUiState(), Serializable {
        private fun readResolve(): Any = ShowCompanyList
    }
}

@AssistedFactory
interface InitializerViewModelFactory {
    fun create(context: Context, startUIState: InitializerUiState? = null): InitializerViewModel
}