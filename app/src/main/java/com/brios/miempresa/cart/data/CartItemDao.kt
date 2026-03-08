package com.brios.miempresa.cart.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class CartItemWithProduct(
    val id: Long,
    val productId: String,
    val quantity: Int,
    val addedAt: Long,
    val productName: String?,
    val productPrice: Double?,
    val productImageUrl: String?,
    val productHidePrice: Boolean,
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
            p.name as productName,
            p.price as productPrice,
            p.imageUrl as productImageUrl,
            p.hidePrice as productHidePrice
        FROM cart_items c
        INNER JOIN products p ON c.productId = p.id AND c.companyId = p.companyId
        WHERE c.companyId = :companyId
        ORDER BY c.addedAt DESC
        """,
    )
    suspend fun getAllWithProducts(companyId: String): List<CartItemWithProduct>

    @Query(
        """
        SELECT 
            c.id,
            c.productId,
            c.quantity,
            c.addedAt,
            p.name as productName,
            p.price as productPrice,
            p.imageUrl as productImageUrl,
            p.hidePrice as productHidePrice
        FROM cart_items c
        INNER JOIN products p ON c.productId = p.id AND c.companyId = p.companyId
        WHERE c.companyId = :companyId
        ORDER BY c.addedAt DESC
        """,
    )
    fun observeAllWithProducts(companyId: String): Flow<List<CartItemWithProduct>>

    @Query("SELECT * FROM cart_items WHERE id = :id AND companyId = :companyId")
    suspend fun getById(
        id: Long,
        companyId: String,
    ): CartItemEntity?

    @Query("SELECT * FROM cart_items WHERE companyId = :companyId AND productId = :productId LIMIT 1")
    suspend fun getByProductId(
        companyId: String,
        productId: String,
    ): CartItemEntity?

    @Query("SELECT quantity FROM cart_items WHERE companyId = :companyId AND productId = :productId LIMIT 1")
    suspend fun getQuantityByProductId(
        companyId: String,
        productId: String,
    ): Int?

    @Query("DELETE FROM cart_items WHERE companyId = :companyId")
    suspend fun deleteAll(companyId: String)

    @Query("SELECT CAST(COALESCE(SUM(quantity), 0) AS INTEGER) FROM cart_items WHERE companyId = :companyId")
    fun observeItemCount(companyId: String): Flow<Int>

    @Query("SELECT companyId FROM cart_items GROUP BY companyId HAVING SUM(quantity) > 0")
    suspend fun getCompaniesWithItems(): List<String>
}
