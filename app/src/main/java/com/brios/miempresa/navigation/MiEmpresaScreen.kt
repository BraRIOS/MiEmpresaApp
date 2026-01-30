package com.brios.miempresa.navigation

enum class MiEmpresaScreen {
    SignIn,
    Initializer,
    Products,
    Product,

    Categories,
}

val basePages =
    listOf(
        MiEmpresaScreen.SignIn.name,
        MiEmpresaScreen.Initializer.name,
        MiEmpresaScreen.Products.name,
        MiEmpresaScreen.Categories.name,
    )
