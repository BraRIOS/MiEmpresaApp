package com.brios.miempresa.initializer

import android.content.Context
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
import com.brios.miempresa.domain.DriveApi
import com.brios.miempresa.domain.GoogleAuthClient
import com.google.api.services.drive.model.File
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InitializerViewModel @Inject constructor(
    private val driveApi: DriveApi,
    @ApplicationContext private val context: Context,
    private val googleAuthClient: GoogleAuthClient,
) : ViewModel() {

    private val miEmpresaDatabase = MiEmpresaDatabase.getDatabase(context)
    private val _uiState = MutableStateFlow<InitializerUiState>(InitializerUiState.Loading)
    val uiState: StateFlow<InitializerUiState> = _uiState.asStateFlow()
    private val companyDao = miEmpresaDatabase.companyDao()

    init {
        viewModelScope.launch {
            _uiState.value = InitializerUiState.Loading
            val mainFolder = driveApi.findMainFolder()
            if (mainFolder != null) {
                checkDataAndFindCompanies(mainFolder.id)
            } else {
                val user = googleAuthClient.getSignedInUser()
                _uiState.value = InitializerUiState.Welcome(
                    username = user?.username?: context.getString(R.string.user))
            }
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
        val spreadsheetId = getFromDataStore(context, PreferencesKeys.SPREADSHEET_ID_KEY).firstOrNull()
        val selectedCompany = companyDao.getSelectedCompany().asFlow().firstOrNull()
        if (spreadsheetId != null && selectedCompany != null) {
            _uiState.value = InitializerUiState.NavigateToProducts
        } else {
            _uiState.value = InitializerUiState.CompanyList(
                companies = companyDao.getCompanies(),
                username = googleAuthClient.getSignedInUser()?.username?: context.getString(R.string.user)
            )
        }
    }

    fun selectCompany(company: Company) {
        viewModelScope.launch {
            _uiState.value = InitializerUiState.SearchingSpreadsheet
            val spreadsheet = driveApi.findSpreadsheetInFolder(company.id)
            if (spreadsheet != null) {
                saveSpreadsheetIdAndCompany(spreadsheet.id, company)
            } else {
                _uiState.value = InitializerUiState.SpreadsheetNotFound(company)
            }
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
        viewModelScope.launch {
            _uiState.value = InitializerUiState.CompanyList(
                companies = companyDao.getCompanies(),
                username = googleAuthClient.getSignedInUser()?.username?: context.getString(R.string.user)
            )
        }
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

    fun searchSpreadsheet(companyId: String) = viewModelScope.launch {
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
    data class Welcome(val username: String, val isFirstTime: Boolean = true) : InitializerUiState()
    data object CheckingData : InitializerUiState()
    data class CompanyList(val companies: LiveData<List<Company>>, val username: String) : InitializerUiState()
    data object SearchingSpreadsheet : InitializerUiState()
    data class SpreadsheetNotFound(val company: Company) : InitializerUiState()
    data object CreatingCompany : InitializerUiState()
    data object CreatingSpreadsheet : InitializerUiState()
    data object NavigateToProducts : InitializerUiState()
}