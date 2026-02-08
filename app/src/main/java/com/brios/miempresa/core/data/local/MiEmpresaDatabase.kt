package com.brios.miempresa.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.brios.miempresa.core.data.local.daos.CartItemDao
import com.brios.miempresa.core.data.local.daos.CategoryDao
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.data.local.daos.ProductDao
import com.brios.miempresa.core.data.local.entities.CartItemEntity
import com.brios.miempresa.core.data.local.entities.Category
import com.brios.miempresa.core.data.local.entities.Company
import com.brios.miempresa.core.data.local.entities.ProductEntity

@Database(
    entities = [
        Company::class,
        Category::class,
        CartItemEntity::class,
        ProductEntity::class,
    ],
    version = 8,
    exportSchema = false,
)
abstract class MiEmpresaDatabase : RoomDatabase() {
    abstract fun companyDao(): CompanyDao

    abstract fun categoryDao(): CategoryDao

    abstract fun cartItemDao(): CartItemDao

    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: MiEmpresaDatabase? = null

        fun getDatabase(context: Context): MiEmpresaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        MiEmpresaDatabase::class.java,
                        "miempresa_database",
                    ).fallbackToDestructiveMigration()
                        .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
