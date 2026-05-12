package com.example.kreedaankana.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.kreedaankana.data.db.entity.Match
import com.example.kreedaankana.data.db.entity.Score

@Dao
interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: Match): Long

    @Update
    suspend fun updateMatch(match: Match)

    @Query("SELECT * FROM matches ORDER BY dateTimeMillis ASC")
    fun getAllMatches(): LiveData<List<Match>>

    @Query("SELECT * FROM matches WHERE teamAId = :teamId OR teamBId = :teamId ORDER BY dateTimeMillis DESC")
    fun getMatchesByTeam(teamId: Int): LiveData<List<Match>>

    @Query("SELECT * FROM matches WHERE id = :id LIMIT 1")
    suspend fun getMatchById(id: Int): Match?

    @Query("UPDATE matches SET status = :status WHERE id = :matchId")
    suspend fun updateMatchStatus(matchId: Int, status: String)

    @Query("SELECT * FROM matches WHERE dateTimeMillis >= :start AND dateTimeMillis <= :end ORDER BY dateTimeMillis ASC")
    fun getMatchesInRange(start: Long, end: Long): LiveData<List<Match>>

    // Scores
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: Score): Long

    @Query("SELECT * FROM scores WHERE matchId = :matchId LIMIT 1")
    suspend fun getScoreByMatch(matchId: Int): Score?

    @Query("SELECT * FROM scores ORDER BY recordedAt DESC")
    fun getAllScores(): LiveData<List<Score>>

    @Query("""
        SELECT matches.* FROM matches 
        INNER JOIN scores ON matches.id = scores.matchId 
        ORDER BY scores.recordedAt DESC
    """)
    fun getMatchesWithScores(): LiveData<List<Match>>
}
