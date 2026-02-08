package com.brios.miempresa.core.data.local.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.brios.miempresa.core.data.local.entities.CartItemEntity
import com.brios.miempresa.core.data.local.entities.Category
import com.brios.miempresa.core.data.local.entities.Company
import com.brios.miempresa.core.data.local.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    @Insert
    suspend fun insert(friend: Company)

    @Update
    suspend fun update(friend: Company)

    @Delete
    suspend fun delete(friend: Company)

    @Query("SELECT * FROM companies WHERE id = :id")
    suspend fun getCompanyById(id: String): Company?

    @Query("UPDATE companies SET selected = 0")
    suspend fun unselectAllCompanies()

    @Query("SELECT * FROM companies WHERE selected = 1 LIMIT 1")
    fun getSelectedCompany(): LiveData<Company?>

    @Query("SELECT * FROM companies WHERE selected = 1 AND isOwned = 1 LIMIT 1")
    suspend fun getSelectedOwnedCompany(): Company?

    @Query("SELECT * FROM companies WHERE isOwned = 1 ORDER BY selected DESC, name")
    suspend fun getOwnedCompaniesList(): List<Company>

    @Query("SELECT * FROM companies")
    fun getCompanies(): LiveData<List<Company>>

    @Query("DELETE FROM companies")
    suspend fun clear()

    @Query("UPDATE companies SET lastSyncedAt = :timestamp WHERE id = :companyId")
    suspend fun updateLastSyncedAt(
        companyId: String,
        timestamp: Long,
    )

    @Query("SELECT * FROM companies WHERE isOwned = 1 ORDER BY selected DESC, name")
    fun getOwnedCompanies(): Flow<List<Company>>

    @Query("SELECT COUNT(*) FROM companies WHERE isOwned = 1")
    suspend fun getOwnedCompanyCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(company: Company)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE companyId = :companyId")
    suspend fun getAll(companyId: String): List<Category>

    @Query("SELECT * FROM categories WHERE companyId = :companyId")
    fun getAllFlow(companyId: String): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id AND companyId = :companyId")
    suspend fun getById(
        id: String,
        companyId: String,
    ): Category?

    @Upsert
    suspend fun upsertAll(categories: List<Category>)

    @Upsert
    suspend fun upsert(category: Category)

    @Query("SELECT * FROM categories WHERE dirty = 1 AND companyId = :companyId")
    suspend fun getDirty(companyId: String): List<Category>

    suspend fun markSynced(
        ids: List<String>,
        timestamp: Long,
        companyId: String,
    ) {
        if (ids.isEmpty()) return
        markSyncedInternal(ids, timestamp, companyId)
    }

    @Query("UPDATE categories SET dirty = 0, lastSyncedAt = :timestamp WHERE id IN (:ids) AND companyId = :companyId")
    suspend fun markSyncedInternal(
        ids: List<String>,
        timestamp: Long,
        companyId: String,
    )

    @Query("DELETE FROM categories WHERE companyId = :companyId")
    suspend fun deleteAll(companyId: String)
}

data class CartItemWithProduct(
    val id: Long,
    val productId: String,
    val quantity: Int,
    val addedAt: Long,
    val productName: String?,
    val productPrice: Double?,
)

@Dao
interface CartItemDao {
    @Insert
    suspend fun insert(item: CartItemEntity): Long

    @Update
    suspend fun update(item: CartItemEntity)

    @Delete
    suspend fun delete(item: CartItemEntity)

    @Query("SELECT * FROM cart_items WHERE companyId = :companyId")
    suspend fun getAll(companyId: String): List<CartItemEntity>

    @Query(
        """
        SELECT 
            c.id,
            c.productId,
            c.quantity,
            c.addedAt,
            NULL as productName,
            NULL as productPrice
        FROM cart_items c
        WHERE c.companyId = :companyId
        ORDER BY c.addedAt DESC
        """,
    )
    suspend fun getAllWithProducts(companyId: String): List<CartItemWithProduct>

    @Query("SELECT * FROM cart_items WHERE id = :id AND companyId = :companyId")
    suspend fun getById(
        id: Long,
        companyId: String,
    ): CartItemEntity?

    @Query("DELETE FROM cart_items WHERE companyId = :companyId")
    suspend fun deleteAll(companyId: String)
}

@Dao
interface ProductDao {
    @Upsert
    suspend fun upsertAll(products: List<ProductEntity>)

    @Upsert
    suspend fun upsert(product: ProductEntity)

    @Query("SELECT * FROM products WHERE id IN (:productIds) AND companyId = :companyId")
    suspend fun getByIds(
        productIds: List<String>,
        companyId: String,
    ): List<ProductEntity>

    @Query("SELECT * FROM products WHERE companyId = :companyId")
    suspend fun getAllByCompany(companyId: String): List<ProductEntity>

    @Query("SELECT * FROM products WHERE companyId = :companyId AND deleted = 0")
    fun getAllByCompanyFlow(companyId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id AND companyId = :companyId")
    suspend fun getById(
        id: String,
        companyId: String,
    ): ProductEntity?

    @Query("SELECT * FROM products WHERE dirty = 1 AND companyId = :companyId")
    suspend fun getDirty(companyId: String): List<ProductEntity>

    suspend fun markSynced(
        ids: List<String>,
        timestamp: Long,
        companyId: String,
    ) {
        if (ids.isEmpty()) return
        markSyncedInternal(ids, timestamp, companyId)
    }

    @Query(
        "UPDATE products SET dirty = 0, lastSyncedAt = :timestamp " +
            "WHERE id IN (:ids) AND companyId = :companyId",
    )
    suspend fun markSyncedInternal(
        ids: List<String>,
        timestamp: Long,
        companyId: String,
    )

    @Query(
        "SELECT COUNT(*) FROM products " +
            "WHERE categoryId = :categoryId AND companyId = :companyId AND deleted = 0",
    )
    suspend fun countByCategory(
        categoryId: String,
        companyId: String,
    ): Int

    @Query("DELETE FROM products WHERE companyId = :companyId")
    suspend fun deleteAll(companyId: String)
}
