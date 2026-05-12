package com.example.kreedaankana.ui.team

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kreedaankana.data.db.entity.Team
import com.example.kreedaankana.data.repository.TeamRepository
import com.example.kreedaankana.data.repository.UserRepository
import kotlinx.coroutines.launch

class TeamViewModel(
    private val teamRepo: TeamRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    val teams = teamRepo.getApprovedTeams()
    val actionResult = MutableLiveData<Pair<Boolean, String>>()
    val myTeam = MutableLiveData<Team?>()

    fun loadMyTeam(userId: Int) {
        viewModelScope.launch {
            val userTeams = teamRepo.getTeamsForUser(userId)
            myTeam.value = if (userTeams.isNotEmpty()) userTeams[0] else null
        }
    }

    fun createTeam(name: String, sport: String, captainId: Int, status: String = "PENDING") {
        viewModelScope.launch {
            val result = teamRepo.createTeam(name, sport, captainId, status)
            if (result.isSuccess) {
                val team = result.getOrNull()!!
                // Add captain as member
                teamRepo.joinTeam(team.id, captainId)
                // Update user's team
                userRepo.updateTeam(captainId, team.id)
                actionResult.value = Pair(true, "Team created!")
                loadMyTeam(captainId)
            } else {
                actionResult.value = Pair(false, result.exceptionOrNull()?.message ?: "Failed")
            }
        }
    }

    fun joinTeam(teamId: Int, userId: Int) {
        viewModelScope.launch {
            val result = teamRepo.joinTeam(teamId, userId)
            if (result.isSuccess) {
                userRepo.updateTeam(userId, teamId)
                actionResult.value = Pair(true, "Joined team!")
                loadMyTeam(userId)
            } else {
                actionResult.value = Pair(false, result.exceptionOrNull()?.message ?: "Failed")
            }
        }
    }

    fun requestCaptain(userId: Int, userName: String) {
        viewModelScope.launch {
            val result = teamRepo.requestCaptain(userId, userName)
            actionResult.value = if (result.isSuccess) {
                Pair(true, "Captain request submitted!")
            } else {
                Pair(false, result.exceptionOrNull()?.message ?: "Failed")
            }
        }
    }
}
