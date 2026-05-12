package com.example.kreedaankana.ui.challenge

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kreedaankana.data.firebase.Challenge
import com.example.kreedaankana.data.repository.ChallengeRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class ChallengeViewModel(
    private val challengeRepo: ChallengeRepository
) : ViewModel() {

    val challenges = MutableLiveData<List<Challenge>>(emptyList())
    val actionResult = MutableLiveData<Pair<Boolean, String>>()
    private var listener: ListenerRegistration? = null

    fun startListening() {
        listener = challengeRepo.listenToChallenges { list ->
            challenges.postValue(list)
        }
    }

    fun postChallenge(challenge: Challenge) {
        viewModelScope.launch {
            val result = challengeRepo.postChallenge(challenge)
            actionResult.value = if (result.isSuccess) {
                Pair(true, "Challenge posted!")
            } else {
                Pair(false, result.exceptionOrNull()?.message ?: "Failed")
            }
        }
    }

    fun acceptChallenge(challenge: Challenge, captainUserId: Int) {
        viewModelScope.launch {
            val result = challengeRepo.acceptChallenge(challenge, captainUserId)
            actionResult.value = if (result.isSuccess) {
                Pair(true, "Challenge accepted! Slot reserved.")
            } else {
                Pair(false, result.exceptionOrNull()?.message ?: "Failed")
            }
        }
    }

    fun declineChallenge(challenge: Challenge) {
        viewModelScope.launch {
            val result = challengeRepo.declineChallenge(challenge.id)
            actionResult.value = if (result.isSuccess) {
                Pair(true, "Challenge declined")
            } else {
                Pair(false, result.exceptionOrNull()?.message ?: "Failed")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}
