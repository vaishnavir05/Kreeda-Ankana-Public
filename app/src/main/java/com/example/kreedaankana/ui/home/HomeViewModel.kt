package com.example.kreedaankana.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kreedaankana.data.db.entity.Booking
import com.example.kreedaankana.data.db.entity.Match
import com.example.kreedaankana.data.repository.BookingRepository
import com.example.kreedaankana.data.repository.MatchRepository
import com.example.kreedaankana.utils.TimeUtils
import com.example.kreedaankana.data.repository.ChallengeRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val bookingRepo: BookingRepository,
    private val matchRepo: MatchRepository,
    private val challengeRepo: ChallengeRepository
) : ViewModel() {

    fun getTodayBookings(userId: Int) = bookingRepo.getBookingsForUserOnDate(userId, TimeUtils.todayString())

    fun getUpcomingMatches(): LiveData<List<Match>> {
        val now = System.currentTimeMillis()
        val weekEnd = now + (7L * 24 * 60 * 60 * 1000)
        return matchRepo.getMatchesInRange(now, weekEnd)
    }

    private val _recentChallenges = MutableLiveData<List<com.example.kreedaankana.data.firebase.Challenge>>()
    val recentChallenges: LiveData<List<com.example.kreedaankana.data.firebase.Challenge>> = _recentChallenges

    private var challengeListener: com.google.firebase.firestore.ListenerRegistration? = null

    fun startListeningToChallenges() {
        challengeListener?.remove()
        challengeListener = challengeRepo.listenToChallenges { list: List<com.example.kreedaankana.data.firebase.Challenge> ->
            _recentChallenges.postValue(list.take(5))
        }
    }

    override fun onCleared() {
        super.onCleared()
        challengeListener?.remove()
    }
}
