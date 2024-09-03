package com.brios.miempresa.navigation
enum class MiEmpresaScreen {
    Products,
    Product,

    Categories,
}

val basePages = listOf(
    MiEmpresaScreen.Products.name,
    MiEmpresaScreen.Categories.name,
)