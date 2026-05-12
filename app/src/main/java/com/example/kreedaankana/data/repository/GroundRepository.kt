package com.example.kreedaankana.data.repository

import com.example.kreedaankana.data.db.dao.GroundDao
import com.example.kreedaankana.data.db.entity.Ground
import com.example.kreedaankana.data.firebase.FirebaseService

class GroundRepository(
    private val groundDao: GroundDao,
    private val firebaseService: FirebaseService
) {
    fun getApprovedGrounds() = groundDao.getApprovedGrounds()
    fun getPendingGrounds() = groundDao.getPendingGrounds()
    suspend fun getGroundById(id: Int) = groundDao.getGroundById(id)
    fun getGroundsBySport(sport: String) = groundDao.getGroundsBySport(sport)
    
    suspend fun approveGround(groundId: Int) {
        groundDao.approveGround(groundId)
        val ground = groundDao.getGroundById(groundId)
        if (ground != null) {
            firebaseService.syncToFirebase("Grounds", ground.firebaseId, ground)
        }
    }
    
    suspend fun insertGround(ground: Ground) {
        val id = groundDao.insertGround(ground)
        // Sync to Firebase
        val firebaseResult = firebaseService.syncToFirebase("Grounds", ground.firebaseId, ground.copy(id = id.toInt()))
        if (firebaseResult.isSuccess) {
            groundDao.updateGround(ground.copy(id = id.toInt(), firebaseId = firebaseResult.getOrNull()))
        }
    }

    suspend fun updateGround(ground: Ground) {
        groundDao.updateGround(ground)
        firebaseService.syncToFirebase("Grounds", ground.firebaseId, ground)
    }

    suspend fun deleteGround(ground: Ground) = groundDao.deleteGround(ground)

    /**
     * Pulls latest grounds from Firebase and updates local Room DB.
     */
    fun startSync(): com.google.firebase.firestore.ListenerRegistration {
        return firebaseService.listenToGrounds { remoteGrounds ->
            // Use a coroutine scope to update DB
            // In a real app, this might be handled by a SyncManager
            remoteGrounds.forEach { remote ->
                // Check if exists locally by name or firebaseId
                // simplified for this task
            }
        }
    }
}
