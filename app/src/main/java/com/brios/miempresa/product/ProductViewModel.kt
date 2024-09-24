package com.brios.miempresa.product

import androidx.lifecycle.ViewModel
import com.brios.miempresa.domain.SpreadsheetsApi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject  constructor(
    private val spreadsheetsApi: SpreadsheetsApi
):ViewModel() {
    fun updateProduct(updatedProduct: Product) {
        TODO("Not yet implemented")
    }

}
