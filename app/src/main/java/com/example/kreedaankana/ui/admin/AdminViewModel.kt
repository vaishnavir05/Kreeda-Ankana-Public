package com.example.kreedaankana.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kreedaankana.data.db.entity.Ground
import com.example.kreedaankana.data.repository.BookingRepository
import com.example.kreedaankana.data.repository.GroundRepository
import com.example.kreedaankana.data.repository.TeamRepository
import com.example.kreedaankana.data.repository.UserRepository
import com.example.kreedaankana.utils.TimeUtils
import kotlinx.coroutines.launch

class AdminViewModel(
    private val teamRepo: TeamRepository,
    private val groundRepo: GroundRepository,
    private val userRepo: UserRepository,
    private val bookingRepo: BookingRepository
) : ViewModel() {

    fun getPendingCaptainRequests() = teamRepo.getPendingCaptainRequests()
    fun getTodayBookings() = bookingRepo.getBookingsOnDate(TimeUtils.todayString())

    fun approveCaptainRequest(requestId: Int, userId: Int) {
        viewModelScope.launch {
            teamRepo.approveCaptainRequest(requestId)
            userRepo.updateRole(userId, "CAPTAIN")
        }
    }

    fun rejectCaptainRequest(requestId: Int) {
        viewModelScope.launch {
            teamRepo.rejectCaptainRequest(requestId)
        }
    }

    fun getPendingTeamRequests() = teamRepo.getPendingTeamRequests()

    fun approveTeamRequest(teamId: Int) {
        viewModelScope.launch {
            teamRepo.approveTeamRequest(teamId)
        }
    }

    fun rejectTeamRequest(teamId: Int) {
        viewModelScope.launch {
            teamRepo.rejectTeamRequest(teamId)
        }
    }

    fun addGround(name: String, address: String, sports: String, userId: Int, openTime: String, closeTime: String, isApproved: Boolean) {
        viewModelScope.launch {
            groundRepo.insertGround(Ground(
                name = name,
                address = address,
                sportsSupported = sports,
                addedBy = userId,
                openTime = openTime,
                closeTime = closeTime,
                isApproved = isApproved
            ))
        }
    }

    fun getPendingGrounds() = groundRepo.getPendingGrounds()

    fun approveGround(groundId: Int) {
        viewModelScope.launch {
            groundRepo.approveGround(groundId)
        }
    }
}
