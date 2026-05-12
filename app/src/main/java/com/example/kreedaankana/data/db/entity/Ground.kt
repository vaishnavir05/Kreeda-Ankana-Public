package com.example.kreedaankana.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grounds")
data class Ground(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    val address: String = "",
    val sportsSupported: String = "",   // Comma-separated sport names
    val openTime: String = "06:00 AM",
    val closeTime: String = "10:00 PM",
    val description: String = "",
    val addedBy: Int = 0,          // userId of admin/supervisor
    val isApproved: Boolean = false,
    val firebaseId: String? = null
)
