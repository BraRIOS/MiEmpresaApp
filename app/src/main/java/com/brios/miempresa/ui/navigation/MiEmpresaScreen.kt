package com.brios.miempresa.ui.navigation
enum class MiEmpresaScreen {
    Welcome,
    Products,
    Product,

    Categories,
}

val basePages = listOf(
    MiEmpresaScreen.Welcome.name,
    MiEmpresaScreen.Products.name,
    MiEmpresaScreen.Categories.name,
)