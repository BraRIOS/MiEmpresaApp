package com.brios.miempresa.catalog.di

import com.brios.miempresa.catalog.data.ClientCatalogRepositoryImpl
import com.brios.miempresa.catalog.domain.ClientCatalogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CatalogModule {
    @Binds
    @Singleton
    abstract fun bindClientCatalogRepository(impl: ClientCatalogRepositoryImpl): ClientCatalogRepository
}
