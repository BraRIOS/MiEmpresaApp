package com.brios.miempresa.navigation

enum class MiEmpresaScreen {
    Welcome,
    SignIn,
    Onboarding,
    Home,
    ClientCatalog,
    MyStores,
    Cart,
    ProductDetail,
    DeeplinkError,
    Products,
    Product,
    Categories,
    Config,
    EditCompanyData,
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
