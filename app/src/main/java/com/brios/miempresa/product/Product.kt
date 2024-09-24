package com.brios.miempresa.product

data class Product(
    val rowIndex: Int,
    val name: String,
    val description: String,
    val price: String,
    val category: String,
    val imageUrl: String
)