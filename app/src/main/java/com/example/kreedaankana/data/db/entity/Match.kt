package com.example.kreedaankana.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class Match(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val teamAId: Int = 0,
    val teamAName: String = "",
    val teamBId: Int = 0,
    val teamBName: String = "",
    val groundId: Int = 0,
    val groundName: String = "",
    val sport: String = "",
    val dateTime: String = "",           // display string
    val dateTimeMillis: Long = 0L,       // for sorting
    val status: String = "SCHEDULED", // SCHEDULED, COMPLETED, CANCELLED
    val challengeFirebaseId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val firebaseId: String? = null
)

@Entity(tableName = "scores")
data class Score(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val matchId: Int = 0,
    val sport: String = "",
    val teamAScore: String = "",   // Format depends on sport
    val teamBScore: String = "",
    val details: String = "", // Extra details (sets, rounds, etc.)
    val winner: String = "",  // Team name or "Draw"
    val recordedAt: Long = System.currentTimeMillis(),
    val firebaseId: String? = null
)
