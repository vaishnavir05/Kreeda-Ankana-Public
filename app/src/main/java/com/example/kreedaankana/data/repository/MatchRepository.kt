package com.example.kreedaankana.data.repository

import com.example.kreedaankana.data.db.dao.MatchDao
import com.example.kreedaankana.data.db.entity.Match
import com.example.kreedaankana.data.db.entity.Score

import com.example.kreedaankana.data.firebase.FirebaseService

class MatchRepository(
    private val matchDao: MatchDao,
    private val firebaseService: FirebaseService
) {
    suspend fun createMatch(match: Match): Long {
        val id = matchDao.insertMatch(match)
        val savedMatch = match.copy(id = id.toInt())
        
        // Sync to Firebase
        val firebaseResult = firebaseService.saveMatch(savedMatch)
        if (firebaseResult.isSuccess) {
            matchDao.updateMatch(savedMatch.copy(firebaseId = firebaseResult.getOrNull()))
        }
        return id
    }

    suspend fun updateMatch(match: Match) {
        matchDao.updateMatch(match)
        firebaseService.saveMatch(match)
    }
    suspend fun getMatchById(id: Int) = matchDao.getMatchById(id)
    fun getAllMatches() = matchDao.getAllMatches()
    fun getMatchesByTeam(teamId: Int) = matchDao.getMatchesByTeam(teamId)
    suspend fun completeMatch(matchId: Int) {
        matchDao.updateMatchStatus(matchId, "COMPLETED")
        val match = matchDao.getMatchById(matchId)
        if (match != null) {
            firebaseService.saveMatch(match)
        }
    }
    fun getMatchesInRange(start: Long, end: Long) = matchDao.getMatchesInRange(start, end)

    suspend fun submitScore(score: Score): Long {
        val id = matchDao.insertScore(score)
        firebaseService.saveScore(score.copy(id = id.toInt()))
        return id
    }
    suspend fun getScoreByMatch(matchId: Int) = matchDao.getScoreByMatch(matchId)
    fun getAllScores() = matchDao.getAllScores()
}
