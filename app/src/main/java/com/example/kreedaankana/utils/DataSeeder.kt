package com.example.kreedaankana.utils

import android.util.Log
import com.example.kreedaankana.data.db.AppDatabase
import com.example.kreedaankana.data.db.entity.*
import com.example.kreedaankana.data.firebase.Challenge
import com.example.kreedaankana.data.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class DataSeeder(
    private val db: AppDatabase,
    private val userRepo: UserRepository,
    private val teamRepo: TeamRepository,
    private val groundRepo: GroundRepository,
    private val bookingRepo: BookingRepository,
    private val matchRepo: MatchRepository,
    private val challengeRepo: ChallengeRepository,
    private val firebaseService: com.example.kreedaankana.data.firebase.FirebaseService
) {

    suspend fun seedAll() = withContext(Dispatchers.IO) {
        Log.d("DataSeeder", "Starting seeding process...")
        runSafe("Users") { seedUsers() }
        runSafe("Sports") { seedSports() }
        runSafe("Grounds") { seedGrounds() }
        runSafe("Teams") { seedTeams() }
        runSafe("Bookings") { seedBookings() }
        runSafe("Matches") { seedMatchesAndScores() }
        runSafe("Challenges") { seedChallenges() }
        Log.d("DataSeeder", "Seeding process finished.")
    }

    private suspend fun runSafe(label: String, block: suspend () -> Unit) {
        try {
            block()
            Log.d("DataSeeder", "Successfully seeded $label")
        } catch (e: Exception) {
            Log.e("DataSeeder", "Error seeding $label: ${e.message}")
            e.printStackTrace()
        }
    }

    private suspend fun seedUsers() {
        // Admin
        val admin = User(
            id = 1, 
            name = "Admin User", 
            email = "admin@kreeda.com", 
            phone = "0000000000", 
            passwordHash = ValidationUtils.hashPassword("admin123"), 
            role = "ADMIN"
        )
        db.userDao().insertUser(admin)
        firebaseService.saveUser(admin)
        
        // 30 Dummy Users
        val sports = listOf("Cricket", "Football", "Volleyball", "Kabaddi", "Badminton", "Basketball")
        for (i in 2..31) {
            val sport = sports[i % sports.size]
            val user = User(
                id = i,
                name = "User $i",
                email = "user$i@example.com",
                phone = "90000000%02d".format(i),
                passwordHash = ValidationUtils.hashPassword("pass$i"),
                role = if (i < 15) "CAPTAIN" else "MEMBER",
                sport = sport
            )
            db.userDao().insertUser(user)
            firebaseService.saveUser(user)
        }
    }

    private suspend fun seedSports() {
        Constants.SPORTS_LIST.forEach { sport ->
            val sportEntity = Sport(name = sport)
            db.sportDao().insertSport(sportEntity)
            firebaseService.saveSport(sportEntity)
        }
    }

    private suspend fun seedGrounds() {
        val grounds = listOf(
            Ground(id = 1, name = "Village Green Stadium", address = "Main Square", sportsSupported = "Cricket,Football,Basketball", isApproved = true),
            Ground(id = 2, name = "River Side Court", address = "North Bank", sportsSupported = "Volleyball,Kabaddi", isApproved = true),
            Ground(id = 3, name = "Community Play Area", address = "East Wing", sportsSupported = "Cricket,Badminton,Basketball", isApproved = true),
            Ground(id = 4, name = "High School Ground", address = "West End", sportsSupported = "Kabaddi,Football,Volleyball", isApproved = true)
        )
        grounds.forEach { groundRepo.insertGround(it) }
    }

    private suspend fun seedTeams() {
        // 5 Cricket teams
        for (i in 1..5) {
            teamRepo.createTeam("Cricket Team $i", "Cricket", i + 1, "APPROVED")
        }
        // 3 Basketball teams
        for (i in 1..3) {
            teamRepo.createTeam("Basketball Team $i", "Basketball", i + 6, "APPROVED")
        }
        // 3 Volleyball teams
        for (i in 1..3) {
            teamRepo.createTeam("Volleyball Team $i", "Volleyball", i + 9, "APPROVED")
        }
        // 10 other teams
        val otherSports = listOf("Football", "Kabaddi", "Badminton")
        for (i in 1..10) {
            val sport = otherSports[i % otherSports.size]
            teamRepo.createTeam("$sport Team $i", sport, i + 12, "APPROVED")
        }
    }

    private suspend fun seedBookings() {
        val today = TimeUtils.todayString()
        // Random bookings for the first 10 users
        for (i in 2..11) {
            val groundId = (i % 4) + 1
            bookingRepo.bookSlot(i, groundId, "Ground $groundId", "Cricket", "Practice", today, "0%d:00 PM".format((i % 5) + 1), 60)
        }
    }

    private suspend fun seedMatchesAndScores() {
        // Scores for 8th and 9th May
        val dates = listOf("2026-05-08", "2026-05-09")
        var matchId = 100
        for (date in dates) {
            for (i in 1..2) {
                matchId++
                val match = Match(
                    id = matchId,
                    teamAId = 1,
                    teamAName = "Cricket Team 1",
                    teamBId = 2,
                    teamBName = "Cricket Team 2",
                    groundId = 1,
                    groundName = "Village Green Stadium",
                    sport = "Cricket",
                    dateTime = "$date 10:00 AM",
                    dateTimeMillis = TimeUtils.toMillis(date, "10:00 AM"),
                    status = "COMPLETED"
                )
                matchRepo.createMatch(match)
                
                val score = Score(
                    matchId = matchId,
                    sport = "Cricket",
                    teamAScore = "${100 + i*10}",
                    teamBScore = "${90 + i*5}",
                    winner = if (i % 2 == 0) "Cricket Team 1" else "Cricket Team 2"
                )
                matchRepo.submitScore(score)
            }
        }
        
        // Some current/upcoming matches
        matchRepo.createMatch(Match(id = 201, teamAId = 1, teamAName = "Cricket Team 1", teamBId = 0, teamBName = "TBD", groundId = 1, groundName = "Village Green Stadium", sport = "Cricket", dateTime = "${TimeUtils.todayString()} 04:00 PM", dateTimeMillis = System.currentTimeMillis() + 3600000, status = "SCHEDULED"))
    }

    private suspend fun seedChallenges() {
        val challenges = listOf(
            Challenge(
                challengerTeamId = 1, challengerTeamName = "Cricket Team 1",
                opponentTeamId = 0, opponentTeamName = "Open Challenge",
                sport = "Cricket", groundId = 1, groundName = "Village Green Stadium",
                proposedTime = "09:00 AM", proposedDate = TimeUtils.todayString(),
                createdAt = System.currentTimeMillis() - 86400000, createdBy = "2"
            ),
            Challenge(
                challengerTeamId = 6, challengerTeamName = "Basketball Team 1",
                opponentTeamId = 7, opponentTeamName = "Basketball Team 2",
                sport = "Basketball", groundId = 3, groundName = "Community Play Area",
                proposedTime = "04:00 PM", proposedDate = TimeUtils.todayString(),
                createdAt = System.currentTimeMillis() - 4000000, createdBy = "7"
            ),
            Challenge(
                challengerTeamId = 9, challengerTeamName = "Volleyball Team 1",
                opponentTeamId = 0, opponentTeamName = "Open Challenge",
                sport = "Volleyball", groundId = 2, groundName = "River Side Court",
                proposedTime = "06:00 PM", proposedDate = "2026-05-15",
                createdAt = System.currentTimeMillis() - 100000, createdBy = "10"
            ),
            Challenge(
                challengerTeamId = 13, challengerTeamName = "Football Team 2",
                opponentTeamId = 0, opponentTeamName = "Open Challenge",
                sport = "Football", groundId = 4, groundName = "High School Ground",
                proposedTime = "07:00 AM", proposedDate = "2026-05-16",
                createdAt = System.currentTimeMillis() - 200000, createdBy = "14"
            ),
            Challenge(
                challengerTeamId = 14, challengerTeamName = "Kabaddi Team 3",
                opponentTeamId = 15, opponentTeamName = "Kabaddi Team 4",
                sport = "Kabaddi", groundId = 2, groundName = "River Side Court",
                proposedTime = "05:00 PM", proposedDate = "2026-05-18",
                createdAt = System.currentTimeMillis() - 500000, createdBy = "15"
            ),
            Challenge(
                challengerTeamId = 12, challengerTeamName = "Badminton Team 1",
                opponentTeamId = 0, opponentTeamName = "Open Challenge",
                sport = "Badminton", groundId = 3, groundName = "Community Play Area",
                proposedTime = "08:00 AM", proposedDate = "2026-05-20",
                createdAt = System.currentTimeMillis() - 600000, createdBy = "13"
            )
        )
        
        challenges.forEach { challengeRepo.postChallenge(it) }
    }
}
