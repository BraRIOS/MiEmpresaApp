package com.brios.miempresa.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.brios.miempresa.cart.data.CartItemDao
import com.brios.miempresa.cart.data.CartItemEntity
import com.brios.miempresa.categories.data.Category
import com.brios.miempresa.categories.data.CategoryDao
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.data.local.entities.Company
import com.brios.miempresa.products.data.ProductDao
import com.brios.miempresa.products.data.ProductEntity

@Database(
    entities = [
        Company::class,
        Category::class,
        CartItemEntity::class,
        ProductEntity::class,
    ],
    version = 11,
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

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE companies ADD COLUMN productsFolderId TEXT")
            }
        }

        fun getDatabase(context: Context): MiEmpresaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        MiEmpresaDatabase::class.java,
                        "miempresa_database",
                    ).addMigrations(MIGRATION_10_11)
                        .fallbackToDestructiveMigration()
                        .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
