package com.example.kreedaankana

import android.app.Application
import com.example.kreedaankana.data.db.AppDatabase
import com.example.kreedaankana.data.db.entity.*
import com.example.kreedaankana.data.firebase.FirebaseService
import com.example.kreedaankana.data.repository.*
import com.example.kreedaankana.utils.SessionManager
import com.example.kreedaankana.utils.ValidationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KreedaApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val sessionManager by lazy { SessionManager(this) }
    val firebaseService by lazy { FirebaseService() }

    // Repositories
    val userRepository by lazy { UserRepository(database.userDao(), firebaseService) }
    val groundRepository by lazy { GroundRepository(database.groundDao(), firebaseService) }
    val bookingRepository by lazy { BookingRepository(database.bookingDao(), firebaseService) }
    val teamRepository by lazy { TeamRepository(database.teamDao(), firebaseService) }
    val matchRepository by lazy { MatchRepository(database.matchDao(), firebaseService) }
    val challengeRepository by lazy {
        ChallengeRepository(firebaseService, bookingRepository, matchRepository)
    }

    override fun onCreate() {
        super.onCreate()
        // Seed initial data safely AFTER database instance is fully built
        CoroutineScope(Dispatchers.IO).launch {
            seedIfNeeded()
        }
    }

    private suspend fun seedIfNeeded() {
        val db = database
        
        // Seed default admin first so they can always log in
        val existingAdmin = db.userDao().getUserByEmail("admin@kreeda.com")
        if (existingAdmin == null) {
            db.userDao().insertUser(User(
                id = 1,
                name = "Admin User",
                email = "admin@kreeda.com",
                phone = "0000000000",
                passwordHash = ValidationUtils.hashPassword("admin123"),
                role = "ADMIN"
            ))
        }

        // Check if teams exist. If not, assume fresh install and run massive seed
        val existingTeams = db.teamDao().getAllTeamsSync()
        if (existingTeams.isEmpty()) {
            android.util.Log.d("KreedaApp", "No teams found. Running massive initial DataSeeder...")
            try {
                com.example.kreedaankana.utils.DataSeeder(
                    db,
                    userRepository,
                    teamRepository,
                    groundRepository,
                    bookingRepository,
                    matchRepository,
                    challengeRepository,
                    firebaseService
                ).seedAll()
            } catch (e: Exception) {
                android.util.Log.e("KreedaApp", "Initial seeder failed", e)
            }
        }
    }
}
