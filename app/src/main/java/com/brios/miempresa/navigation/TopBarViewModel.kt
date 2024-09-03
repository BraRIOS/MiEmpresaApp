package com.brios.miempresa.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class TopBarViewModel : ViewModel() {
    var topBarTitle by mutableStateOf("")
}