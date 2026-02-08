package com.brios.miempresa.core.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey
    val id: String,
    val name: String,
    val isOwned: Boolean = false,
    val selected: Boolean = false,
    val lastVisited: Long? = null,
    val lastSyncedAt: Long? = null,
    val logoUrl: String? = null,
    val whatsappNumber: String? = null,
    val whatsappCountryCode: String = "+54",
    val address: String? = null,
    val businessHours: String? = null,
    val publicSheetId: String? = null,
    val privateSheetId: String? = null,
    val driveFolderId: String? = null,
    val specialization: String? = null,
)

@Entity(
    tableName = "categories",
    indices = [Index(value = ["companyId", "dirty"])],
)
data class Category(
    @PrimaryKey val id: String,
    val name: String,
    val iconEmoji: String,
    val companyId: String,
    // Modified offline, pending upload
    val dirty: Boolean = false,
    // Timestamp of last successful sync
    val lastSyncedAt: Long? = null,
)

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

@Entity(
    tableName = "products",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = androidx.room.ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["companyId", "dirty"]),
        Index(value = ["categoryId"]), // Performance for FK queries
    ],
)
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val companyId: String,
    val description: String? = null,
    val categoryId: String? = null,
    val imageUrl: String? = null,
    val localImagePath: String? = null,
    val driveImageId: String? = null,
    val isPublic: Boolean = true,
    val deleted: Boolean = false,
    // Modified offline, pending upload
    val dirty: Boolean = false,
    // Timestamp of last successful sync
    val lastSyncedAt: Long? = null,
)
