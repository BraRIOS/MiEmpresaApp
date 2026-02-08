package com.brios.miempresa.cart.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.brios.miempresa.core.data.local.entities.Company
import com.brios.miempresa.products.data.ProductEntity

@Entity(
    tableName = "cart_items",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = Company::class,
            parentColumns = ["id"],
            childColumns = ["companyId"],
            onDelete = androidx.room.ForeignKey.CASCADE,
        ),
        androidx.room.ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = androidx.room.ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["companyId"]),
        Index(value = ["productId"]), // Performance for FK + JOIN queries
    ],
)
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyId: String,
    val productId: String,
    val quantity: Int,
    val addedAt: Long = System.currentTimeMillis(),
)
