package com.example.kreedaankana.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val role: String = "MEMBER",       // MEMBER, CAPTAIN, SUPERVISOR, ADMIN
    val sport: String = "",
    val homeGroundId: Int = 0,
    val teamId: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
