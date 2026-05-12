package com.example.kreedaankana.ui.grounds

import androidx.lifecycle.ViewModel
import com.example.kreedaankana.data.repository.BookingRepository
import com.example.kreedaankana.data.repository.GroundRepository
import com.example.kreedaankana.utils.TimeUtils

class GroundViewModel(
    private val groundRepo: GroundRepository,
    private val bookingRepo: BookingRepository
) : ViewModel() {
    fun getAllGrounds() = groundRepo.getApprovedGrounds()
    fun getTodayScheduleForGround(groundId: Int) =
        bookingRepo.getBookingsOnDate(TimeUtils.todayString())
}
