package com.example.kreedaankana.data.db.dao

import androidx.room.*
import com.example.kreedaankana.data.db.entity.Sport

@Dao
interface SportDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSport(sport: Sport): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(sports: List<Sport>)

    @Query("SELECT * FROM sports ORDER BY name ASC")
    suspend fun getAllSports(): List<Sport>
}
