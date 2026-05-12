package com.example.kreedaankana.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.kreedaankana.data.db.entity.CaptainRequest
import com.example.kreedaankana.data.db.entity.Team
import com.example.kreedaankana.data.db.entity.TeamMember

@Dao
interface TeamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: Team): Long

    @Update
    suspend fun updateTeam(team: Team)

    @Delete
    suspend fun deleteTeam(team: Team)

    @Query("SELECT * FROM teams WHERE status = 'APPROVED' ORDER BY name ASC")
    fun getApprovedTeams(): LiveData<List<Team>>

    @Query("SELECT * FROM teams WHERE status = 'PENDING' ORDER BY createdAt ASC")
    fun getPendingTeamRequests(): LiveData<List<Team>>

    @Query("UPDATE teams SET status = :status WHERE id = :teamId")
    suspend fun updateTeamStatus(teamId: Int, status: String)

    @Query("SELECT * FROM teams ORDER BY name ASC")
    fun getAllTeams(): LiveData<List<Team>>

    @Query("SELECT * FROM teams")
    suspend fun getAllTeamsSync(): List<Team>

    @Query("SELECT * FROM teams WHERE id = :id LIMIT 1")
    suspend fun getTeamById(id: Int): Team?

    @Query("SELECT * FROM teams WHERE captainId = :captainId LIMIT 1")
    suspend fun getTeamByCaptain(captainId: Int): Team?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMember(teamMember: TeamMember): Long

    @Delete
    suspend fun removeMember(teamMember: TeamMember)

    @Query("SELECT * FROM team_members WHERE teamId = :teamId")
    fun getMembersByTeam(teamId: Int): LiveData<List<TeamMember>>

    @Query("SELECT * FROM team_members WHERE userId = :userId")
    suspend fun getMembershipsByUser(userId: Int): List<TeamMember>

    @Query("SELECT t.* FROM teams t INNER JOIN team_members tm ON t.id = tm.teamId WHERE tm.userId = :userId")
    suspend fun getTeamsForUser(userId: Int): List<Team>

    @Query("DELETE FROM team_members WHERE userId = :userId AND teamId = :teamId")
    suspend fun removeMemberByIds(userId: Int, teamId: Int)

    // Captain requests
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaptainRequest(req: CaptainRequest): Long

    @Query("SELECT * FROM captain_requests WHERE status = 'PENDING' ORDER BY requestedAt ASC")
    fun getPendingCaptainRequests(): LiveData<List<CaptainRequest>>

    @Query("UPDATE captain_requests SET status = :status WHERE id = :requestId")
    suspend fun updateCaptainRequestStatus(requestId: Int, status: String)

    @Query("SELECT * FROM captain_requests WHERE userId = :userId ORDER BY requestedAt DESC LIMIT 1")
    suspend fun getCaptainRequestByUser(userId: Int): CaptainRequest?
}
