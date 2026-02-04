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
