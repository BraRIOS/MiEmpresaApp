package com.brios.miempresa.navigation
enum class MiEmpresaScreen {
    Welcome,
    Initializer,
    Products,
    Product,

    Categories,
}

val basePages = listOf(
    MiEmpresaScreen.Welcome.name,
    MiEmpresaScreen.Initializer.name,
    MiEmpresaScreen.Products.name,
    MiEmpresaScreen.Categories.name,
)