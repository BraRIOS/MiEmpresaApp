package com.brios.miempresa.orders.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "orders",
    indices = [
        Index(value = ["companyId", "dirty"]),
        Index(value = ["companyId", "createdAt"]),
    ],
)
data class OrderEntity(
    @PrimaryKey val id: String,
    val companyId: String,
    val customerName: String,
    val customerPhone: String? = null,
    val notes: String? = null,
    val totalAmount: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val dirty: Boolean = false,
    val lastSyncedAt: Long? = null,
)
