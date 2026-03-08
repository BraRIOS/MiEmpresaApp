package com.brios.miempresa.core.data.local.entities

import androidx.room.Entity
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
    val productsFolderId: String? = null,
    val specialization: String? = null,
)
