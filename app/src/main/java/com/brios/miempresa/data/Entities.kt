package com.brios.miempresa.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey
    val id: String,
    val name: String,
    val selected: Boolean,
)

@Entity(
    tableName = "categories",
    indices = [Index(value = ["companyId", "dirty"])],
)
data class Category(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
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
    ],
    indices = [
        Index(value = ["companyId"]),
        Index(value = ["productId"]), // Performance for JOIN queries
    ],
)
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyId: String,
    // TODO: Add FK to Product when Product entity is migrated to Room (post-spike)
    val productId: String,
    val quantity: Int,
    val addedAt: Long = System.currentTimeMillis(),
)
