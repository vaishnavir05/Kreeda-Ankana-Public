package com.example.kreedaankana.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class Team(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    val sport: String = "",
    val captainId: Int = 0,
    val status: String = "PENDING",    // PENDING, APPROVED, REJECTED
    val createdAt: Long = System.currentTimeMillis(),
    val firebaseId: String? = null
)

@Entity(tableName = "team_members")
data class TeamMember(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val teamId: Int = 0,
    val userId: Int = 0,
    val joinedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "captain_requests")
data class CaptainRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int = 0,
    val userName: String = "",
    val status: String = "PENDING",    // PENDING, APPROVED, REJECTED
    val requestedAt: Long = System.currentTimeMillis(),
    val firebaseId: String? = null
)
