package com.example.kreedaankana.utils

object Constants {
    // Roles
    const val ROLE_MEMBER = "MEMBER"
    const val ROLE_CAPTAIN = "CAPTAIN"
    const val ROLE_SUPERVISOR = "SUPERVISOR"
    const val ROLE_ADMIN = "ADMIN"

    // Sports
    val SPORTS_LIST = listOf("Cricket", "Football", "Volleyball", "Throwball", "Dodgeball", "Running", "Badminton", "Basketball")

    // Slot types
    const val SLOT_PRACTICE = "PRACTICE"
    const val SLOT_MATCH = "MATCH"

    // Booking limits
    const val MAX_PRACTICE_PER_DAY = 2
    const val MAX_PRACTICE_PER_WEEK = 14
    const val MAX_MATCH_PER_DAY = 2
    const val MAX_MATCH_PER_WEEK = 10

    // Durations (in minutes)
    val PRACTICE_DURATIONS = listOf(30, 60, 90, 120)
    val MATCH_DURATIONS = listOf(30, 60, 90, 120, 150, 180, 210, 240, 270, 300)

    // Challenge statuses
    const val STATUS_PENDING = "PENDING"
    const val STATUS_ACCEPTED = "ACCEPTED"
    const val STATUS_DECLINED = "DECLINED"

    // Match statuses
    const val MATCH_SCHEDULED = "SCHEDULED"
    const val MATCH_COMPLETED = "COMPLETED"
    const val MATCH_CANCELLED = "CANCELLED"

    // Captain request statuses
    const val REQUEST_PENDING = "PENDING"
    const val REQUEST_APPROVED = "APPROVED"
    const val REQUEST_REJECTED = "REJECTED"

    // Score format hints per sport
    fun getScoreHint(sport: String): Pair<String, String> {
        return when (sport) {
            "Cricket" -> Pair("e.g. 145/6 (20 ov)", "e.g. 132/8 (20 ov)")
            "Football" -> Pair("e.g. 3", "e.g. 2")
            "Volleyball" -> Pair("e.g. 25-18, 25-20", "e.g. 18-25, 20-25")
            "Throwball" -> Pair("e.g. 25-20, 22-25, 15-10", "e.g. 20-25, 25-22, 10-15")
            "Dodgeball" -> Pair("e.g. Round Wins: 3", "e.g. Round Wins: 1")
            "Running" -> Pair("e.g. 00:12:34 (time)", "e.g. 00:13:02 (time)")
            "Badminton" -> Pair("e.g. 21-18, 21-15", "e.g. 18-21, 15-21")
            "Basketball" -> Pair("e.g. 102", "e.g. 98")
            else -> Pair("Score", "Score")
        }
    }
}
