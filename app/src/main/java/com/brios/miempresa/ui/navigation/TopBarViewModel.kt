package com.brios.miempresa.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TopBarViewModel @Inject constructor(): ViewModel() {
    var topBarTitle by mutableStateOf("")
}