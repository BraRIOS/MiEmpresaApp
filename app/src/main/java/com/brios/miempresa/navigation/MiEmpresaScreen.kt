package com.brios.miempresa.navigation

enum class MiEmpresaScreen {
    Welcome,
    SignIn,
    Onboarding,
    Products,
    Product,
    Categories,
}

val basePages =
    listOf(
        MiEmpresaScreen.Welcome.name,
        MiEmpresaScreen.SignIn.name,
        MiEmpresaScreen.Onboarding.name,
        MiEmpresaScreen.Products.name,
        MiEmpresaScreen.Categories.name,
    )
