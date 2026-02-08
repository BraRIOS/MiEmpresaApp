package com.brios.miempresa.core.di

import com.brios.miempresa.categories.data.CategoriesRepositoryImpl
import com.brios.miempresa.categories.domain.CategoriesRepository
import com.brios.miempresa.products.data.ProductsRepositoryImpl
import com.brios.miempresa.products.domain.ProductsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindCategoriesRepository(impl: CategoriesRepositoryImpl): CategoriesRepository

    @Binds
    abstract fun bindProductsRepository(impl: ProductsRepositoryImpl): ProductsRepository
}
