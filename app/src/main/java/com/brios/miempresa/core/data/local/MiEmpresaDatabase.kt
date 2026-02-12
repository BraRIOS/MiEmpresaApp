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
import com.brios.miempresa.pedidos.data.OrderDao
import com.brios.miempresa.pedidos.data.OrderEntity
import com.brios.miempresa.pedidos.data.OrderItemEntity
import com.brios.miempresa.products.data.ProductDao
import com.brios.miempresa.products.data.ProductEntity

@Database(
    entities = [
        Company::class,
        Category::class,
        CartItemEntity::class,
        ProductEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
    ],
    version = 12,
    exportSchema = false,
)
abstract class MiEmpresaDatabase : RoomDatabase() {
    abstract fun companyDao(): CompanyDao

    abstract fun categoryDao(): CategoryDao

    abstract fun cartItemDao(): CartItemDao

    abstract fun productDao(): ProductDao

    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: MiEmpresaDatabase? = null

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE companies ADD COLUMN productsFolderId TEXT")
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `orders` (
                        `id` TEXT NOT NULL,
                        `companyId` TEXT NOT NULL,
                        `customerName` TEXT NOT NULL,
                        `customerPhone` TEXT,
                        `notes` TEXT,
                        `totalAmount` REAL NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `dirty` INTEGER NOT NULL DEFAULT 0,
                        `lastSyncedAt` INTEGER,
                        PRIMARY KEY(`id`)
                    )""",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_orders_companyId_dirty` ON `orders` (`companyId`, `dirty`)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_orders_companyId_createdAt` ON `orders` (`companyId`, `createdAt`)",
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `order_items` (
                        `id` TEXT NOT NULL,
                        `orderId` TEXT NOT NULL,
                        `productId` TEXT,
                        `productName` TEXT NOT NULL,
                        `priceAtOrder` REAL NOT NULL,
                        `quantity` INTEGER NOT NULL,
                        `thumbnailUrl` TEXT,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`orderId`) REFERENCES `orders`(`id`) ON DELETE CASCADE
                    )""",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_order_items_orderId` ON `order_items` (`orderId`)",
                )
            }
        }

        fun getDatabase(context: Context): MiEmpresaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        MiEmpresaDatabase::class.java,
                        "miempresa_database",
                    ).addMigrations(MIGRATION_10_11, MIGRATION_11_12)
                        .fallbackToDestructiveMigration()
                        .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
