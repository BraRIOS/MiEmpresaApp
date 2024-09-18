package com.brios.miempresa.navigation
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