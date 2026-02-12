package com.brios.miempresa.core.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.room.Room
import com.brios.miempresa.cart.data.CartItemDao
import com.brios.miempresa.categories.data.CategoryDao
import com.brios.miempresa.core.data.local.MiEmpresaDatabase
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.pedidos.data.OrderDao
import com.brios.miempresa.products.data.ProductDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideMiEmpresaDatabase(
        @ApplicationContext context: Context,
    ): MiEmpresaDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            MiEmpresaDatabase::class.java,
            "miempresa_database",
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCompanyDao(database: MiEmpresaDatabase): CompanyDao {
        return database.companyDao()
    }

    @Provides
    fun provideCategoryDao(database: MiEmpresaDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideCartItemDao(database: MiEmpresaDatabase): CartItemDao {
        return database.cartItemDao()
    }

    @Provides
    fun provideProductDao(database: MiEmpresaDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    fun provideOrderDao(database: MiEmpresaDatabase): OrderDao {
        return database.orderDao()
    }

    @Provides
    @Singleton
    fun providesConnectivityManager(
        @ApplicationContext context: Context,
    ): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
}
