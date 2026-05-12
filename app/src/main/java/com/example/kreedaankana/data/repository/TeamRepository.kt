package com.example.kreedaankana.data.repository

import com.example.kreedaankana.data.db.dao.TeamDao
import com.example.kreedaankana.data.db.entity.CaptainRequest
import com.example.kreedaankana.data.db.entity.Team
import com.example.kreedaankana.data.db.entity.TeamMember

import com.example.kreedaankana.data.firebase.FirebaseService

class TeamRepository(
    private val teamDao: TeamDao,
    private val firebaseService: FirebaseService
) {

    suspend fun createTeam(name: String, sport: String, captainId: Int, status: String = "PENDING"): Result<Team> {
        val userTeams = teamDao.getTeamsForUser(captainId)
        if (userTeams.any { it.sport == sport }) {
            return Result.failure(Exception("You already have a team for $sport"))
        }
        val team = Team(name = name, sport = sport, captainId = captainId, status = status)
        val id = teamDao.insertTeam(team)
        val savedTeam = team.copy(id = id.toInt())
        
        // Add captain as first member
        teamDao.addMember(TeamMember(teamId = id.toInt(), userId = captainId))
        
        // Sync to Firebase
        val firebaseResult = firebaseService.saveTeam(savedTeam)
        if (firebaseResult.isSuccess) {
            val updatedTeam = savedTeam.copy(firebaseId = firebaseResult.getOrNull())
            teamDao.updateTeam(updatedTeam)
            return Result.success(updatedTeam)
        }

        return Result.success(savedTeam)
    }
    
    suspend fun updateTeam(team: Team) {
        teamDao.updateTeam(team)
        firebaseService.saveTeam(team)
    }

    suspend fun joinTeam(teamId: Int, userId: Int): Result<Unit> {
        val targetTeam = teamDao.getTeamById(teamId) ?: return Result.failure(Exception("Team not found"))
        val userTeams = teamDao.getTeamsForUser(userId)
        
        if (userTeams.any { it.sport == targetTeam.sport }) {
            return Result.failure(Exception("Already in a ${targetTeam.sport} team"))
        }

        teamDao.addMember(TeamMember(teamId = teamId, userId = userId))
        return Result.success(Unit)
    }

    suspend fun leaveTeam(teamId: Int, userId: Int) {
        teamDao.removeMemberByIds(userId, teamId)
    }

    suspend fun requestCaptain(userId: Int, userName: String): Result<Unit> {
        val existing = teamDao.getCaptainRequestByUser(userId)
        if (existing != null && existing.status == "PENDING") {
            return Result.failure(Exception("Request already pending"))
        }
        teamDao.insertCaptainRequest(CaptainRequest(userId = userId, userName = userName))
        return Result.success(Unit)
    }

    suspend fun approveCaptainRequest(requestId: Int) =
        teamDao.updateCaptainRequestStatus(requestId, "APPROVED")

    suspend fun rejectCaptainRequest(requestId: Int) =
        teamDao.updateCaptainRequestStatus(requestId, "REJECTED")

    fun getPendingTeamRequests() = teamDao.getPendingTeamRequests()
    suspend fun approveTeamRequest(teamId: Int) = teamDao.updateTeamStatus(teamId, "APPROVED")
    suspend fun rejectTeamRequest(teamId: Int) = teamDao.updateTeamStatus(teamId, "REJECTED")

    fun getApprovedTeams() = teamDao.getApprovedTeams()
    fun getAllTeams() = teamDao.getAllTeams()
    suspend fun getTeamById(id: Int) = teamDao.getTeamById(id)
    suspend fun getTeamByCaptain(captainId: Int) = teamDao.getTeamByCaptain(captainId)
    fun getMembersByTeam(teamId: Int) = teamDao.getMembersByTeam(teamId)
    suspend fun getTeamsForUser(userId: Int) = teamDao.getTeamsForUser(userId)
    fun getPendingCaptainRequests() = teamDao.getPendingCaptainRequests()
}
