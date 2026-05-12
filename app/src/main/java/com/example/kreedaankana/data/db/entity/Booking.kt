package com.example.kreedaankana.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int = 0,
    val groundId: Int = 0,
    val groundName: String = "",
    val sport: String = "",
    val slotType: String = "",              // PRACTICE or MATCH
    val date: String = "",                  // yyyy-MM-dd
    val startTime: String = "",             // hh:mm AM/PM
    val endTime: String = "",               // hh:mm AM/PM
    val startTimeMillis: Long = 0L,         // For overlap checks
    val endTimeMillis: Long = 0L,
    val status: String = "CONFIRMED",  // CONFIRMED, CANCELLED
    val matchId: Int = 0,              // Linked match if any
    val createdAt: Long = System.currentTimeMillis(),
    val firebaseId: String? = null
)
