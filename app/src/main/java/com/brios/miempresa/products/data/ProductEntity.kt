package com.brios.miempresa.products.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.brios.miempresa.categories.data.Category

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
        Index(value = ["companyId", "categoryName"]), // Performance for client flow filter
    ],
)
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val companyId: String,
    val description: String? = null,
    val categoryId: String? = null,
    val categoryName: String? = null, // For client flow filtering
    val imageUrl: String? = null,
    val localImagePath: String? = null,
    val driveImageId: String? = null,
    val isPublic: Boolean = true,
    val hidePrice: Boolean = false,
    val deleted: Boolean = false,
    // Modified offline, pending upload
    val dirty: Boolean = false,
    // Timestamp of last successful sync
    val lastSyncedAt: Long? = null,
)
