package com.example.kreedaankana.ui.booking

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kreedaankana.data.db.entity.Booking
import com.example.kreedaankana.data.db.entity.Ground
import com.example.kreedaankana.data.repository.BookingRepository
import com.example.kreedaankana.data.repository.GroundRepository
import kotlinx.coroutines.launch

sealed class BookingState {
    object Idle : BookingState()
    object Loading : BookingState()
    data class Success(val booking: Booking) : BookingState()
    data class Error(val message: String) : BookingState()
}

class BookingViewModel(
    private val bookingRepo: BookingRepository,
    private val groundRepo: GroundRepository
) : ViewModel() {

    val bookingState = MutableLiveData<BookingState>(BookingState.Idle)
    val grounds = groundRepo.getApprovedGrounds()

    fun bookSlot(
        userId: Int,
        groundId: Int,
        groundName: String,
        sport: String,
        slotType: String,
        date: String,
        startTime: String,
        durationMinutes: Int
    ) {
        bookingState.value = BookingState.Loading
        viewModelScope.launch {
            val result = bookingRepo.bookSlot(
                userId, groundId, groundName, sport, slotType, date, startTime, durationMinutes
            )
            bookingState.value = result.fold(
                onSuccess = { BookingState.Success(it) },
                onFailure = {
                    val msg = when (it.message) {
                        "max_daily" -> "max_daily"
                        "max_weekly" -> "max_weekly"
                        "overlap" -> "overlap"
                        else -> it.message ?: "Booking failed"
                    }
                    BookingState.Error(msg)
                }
            )
        }
    }
}
