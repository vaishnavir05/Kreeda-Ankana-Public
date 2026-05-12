package com.example.kreedaankana.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.kreedaankana.data.db.entity.Ground

@Dao
interface GroundDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGround(ground: Ground): Long

    @Update
    suspend fun updateGround(ground: Ground)

    @Delete
    suspend fun deleteGround(ground: Ground)

    @Query("SELECT * FROM grounds WHERE isApproved = 1 ORDER BY name ASC")
    fun getApprovedGrounds(): LiveData<List<Ground>>

    @Query("SELECT * FROM grounds WHERE isApproved = 0 ORDER BY id DESC")
    fun getPendingGrounds(): LiveData<List<Ground>>

    @Query("UPDATE grounds SET isApproved = 1 WHERE id = :groundId")
    suspend fun approveGround(groundId: Int)

    @Query("SELECT * FROM grounds WHERE isApproved = 1")
    suspend fun getAllGroundsSync(): List<Ground>

    @Query("SELECT * FROM grounds WHERE id = :id LIMIT 1")
    suspend fun getGroundById(id: Int): Ground?

    @Query("SELECT * FROM grounds WHERE sportsSupported LIKE '%' || :sport || '%' AND isApproved = 1")
    fun getGroundsBySport(sport: String): LiveData<List<Ground>>
}
