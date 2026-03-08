package com.brios.miempresa.categories.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index(value = ["companyId", "dirty"])],
)
data class Category(
    @PrimaryKey val id: String,
    val name: String,
    val iconEmoji: String,
    val companyId: String,
    val deleted: Boolean = false,
    // Modified offline, pending upload
    val dirty: Boolean = false,
    // Timestamp of last successful sync
    val lastSyncedAt: Long? = null,
)
