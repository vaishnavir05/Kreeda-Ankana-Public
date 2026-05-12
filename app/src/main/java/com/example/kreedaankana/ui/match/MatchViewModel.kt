package com.example.kreedaankana.ui.match

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kreedaankana.data.db.entity.Match
import com.example.kreedaankana.data.db.entity.Score
import com.example.kreedaankana.data.repository.MatchRepository
import kotlinx.coroutines.launch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class MatchViewModel(private val matchRepo: MatchRepository) : ViewModel() {
    val currentMatch = MutableLiveData<Match?>()
    val currentScore = MutableLiveData<Score?>()
    val actionResult = MutableLiveData<Pair<Boolean, String>>()

    fun loadMatch(matchId: Int) {
        viewModelScope.launch {
            currentMatch.value = matchRepo.getMatchById(matchId)
            currentScore.value = matchRepo.getScoreByMatch(matchId)
        }
    }

    fun getScoreboardData(): LiveData<List<Pair<Match, Score>>> {
        val result = MediatorLiveData<List<Pair<Match, Score>>>()
        val matchesLive = matchRepo.getAllMatches()
        val scoresLive = matchRepo.getAllScores()

        fun update() {
            val matches = matchesLive.value
            val scores = scoresLive.value
            if (matches != null) {
                val list = matches.map { match ->
                    val score = scores?.find { it.matchId == match.id } ?: Score(matchId = match.id, sport = match.sport)
                    match to score
                }
                result.value = list
            }
        }

        result.addSource(matchesLive) { update() }
        result.addSource(scoresLive) { update() }

        return result
    }

    fun submitScore(matchId: Int, sport: String, teamAScore: String, teamBScore: String, winner: String) {
        viewModelScope.launch {
            matchRepo.submitScore(Score(
                matchId = matchId,
                sport = sport,
                teamAScore = teamAScore,
                teamBScore = teamBScore,
                winner = winner
            ))
            matchRepo.completeMatch(matchId)
            loadMatch(matchId)
            actionResult.value = Pair(true, "Score updated!")
        }
    }

    fun createStandaloneScore(sport: String, teamA: String, teamB: String, scoreA: String, scoreB: String, winner: String) {
        viewModelScope.launch {
            val genericMatch = Match(
                teamAName = teamA,
                teamBName = teamB,
                sport = sport,
                status = "COMPLETED"
            )
            val newMatchId = matchRepo.createMatch(genericMatch)
            matchRepo.submitScore(Score(
                matchId = newMatchId.toInt(),
                sport = sport,
                teamAScore = scoreA,
                teamBScore = scoreB,
                winner = winner
            ))
            actionResult.value = Pair(true, "Generic Score added!")
        }
    }
}
