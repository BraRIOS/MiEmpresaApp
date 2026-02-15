package com.brios.miempresa.products.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

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

    @Query(
        """
        SELECT * FROM products
        WHERE companyId = :companyId
          AND deleted = 0
          AND (:searchQuery = '' OR name LIKE '%' || :searchQuery || '%')
          AND (:categoryId IS NULL OR categoryId = :categoryId)
          AND (:isPublicFilter IS NULL OR isPublic = :isPublicFilter)
        ORDER BY name COLLATE NOCASE ASC
        """,
    )
    fun getFilteredByCompany(
        companyId: String,
        searchQuery: String,
        categoryId: String?,
        isPublicFilter: Boolean?,
    ): Flow<List<ProductEntity>>

    @Query(
        """
        SELECT categoryId, COUNT(*) AS productCount
        FROM products
        WHERE companyId = :companyId
          AND deleted = 0
          AND categoryId IS NOT NULL
          AND (:searchQuery = '' OR name LIKE '%' || :searchQuery || '%')
          AND (:isPublicFilter IS NULL OR isPublic = :isPublicFilter)
        GROUP BY categoryId
        """,
    )
    fun getCategoryCountsByFilter(
        companyId: String,
        searchQuery: String,
        isPublicFilter: Boolean?,
    ): Flow<List<CategoryProductCount>>

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

    @Query("DELETE FROM products WHERE id = :id AND companyId = :companyId")
    suspend fun deleteById(
        id: String,
        companyId: String,
    )

    @Query(
        "UPDATE products SET categoryId = NULL, dirty = 1 " +
            "WHERE categoryId = :categoryId AND companyId = :companyId",
    )
    suspend fun clearCategoryId(
        categoryId: String,
        companyId: String,
    )

    // Client flow queries
    @Query("SELECT * FROM products WHERE companyId = :companyId AND isPublic = 1 AND deleted = 0")
    fun getByCompanyIdPublic(companyId: String): Flow<List<ProductEntity>>

    @Query(
        """
        SELECT * FROM products
        WHERE companyId = :companyId
          AND isPublic = 1
          AND deleted = 0
          AND (:searchQuery = '' OR name LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%')
          AND (:categoryName IS NULL OR categoryName = :categoryName)
        ORDER BY name COLLATE NOCASE ASC
        """,
    )
    fun getPublicFiltered(
        companyId: String,
        searchQuery: String,
        categoryName: String?,
    ): Flow<List<ProductEntity>>

    @Query(
        """
        SELECT categoryName, COUNT(*) AS productCount
        FROM products
        WHERE companyId = :companyId
          AND isPublic = 1
          AND deleted = 0
          AND categoryName IS NOT NULL
          AND categoryName != ''
          AND (:searchQuery = '' OR name LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%')
        GROUP BY categoryName
        ORDER BY categoryName COLLATE NOCASE ASC
        """,
    )
    fun getPublicCategoryCounts(
        companyId: String,
        searchQuery: String,
    ): Flow<List<PublicCategoryCount>>

    @Query("SELECT COUNT(*) FROM products WHERE companyId = :companyId AND isPublic = 1 AND deleted = 0")
    fun observePublicCount(companyId: String): Flow<Int>

    @Query(
        "SELECT * FROM products WHERE companyId = :companyId AND categoryName = :categoryName AND deleted = 0",
    )
    fun getProductsByCategory(
        companyId: String,
        categoryName: String,
    ): Flow<List<ProductEntity>>

    @Query("DELETE FROM products WHERE companyId = :companyId")
    suspend fun deleteByCompanyId(companyId: String)
}

data class CategoryProductCount(
    val categoryId: String,
    val productCount: Int,
)

data class PublicCategoryCount(
    val categoryName: String,
    val productCount: Int,
)
