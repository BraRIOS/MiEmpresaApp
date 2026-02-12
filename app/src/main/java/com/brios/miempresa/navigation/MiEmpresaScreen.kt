package com.brios.miempresa.navigation

enum class MiEmpresaScreen {
    Welcome,
    SignIn,
    Onboarding,
    Home,
    Products,
    Product,
    Categories,
    Config,
    PedidosList,
    PedidoManual,
    PedidoDetail,
}

val basePages =
    listOf(
        MiEmpresaScreen.Welcome.name,
        MiEmpresaScreen.SignIn.name,
        MiEmpresaScreen.Onboarding.name,
        MiEmpresaScreen.Home.name,
    )
