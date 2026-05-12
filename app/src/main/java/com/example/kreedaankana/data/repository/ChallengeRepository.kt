package com.example.kreedaankana.data.repository

import com.example.kreedaankana.data.db.entity.Booking
import com.example.kreedaankana.data.db.entity.Match
import com.example.kreedaankana.data.firebase.Challenge
import com.example.kreedaankana.data.firebase.FirebaseService
import com.example.kreedaankana.utils.Constants
import com.example.kreedaankana.utils.TimeUtils
import com.google.firebase.firestore.ListenerRegistration

class ChallengeRepository(
    private val firebaseService: FirebaseService,
    private val bookingRepo: BookingRepository,
    private val matchRepo: MatchRepository
) {
    suspend fun postChallenge(challenge: Challenge): Result<String> {
        return firebaseService.postChallenge(challenge)
    }

    suspend fun acceptChallenge(
        challenge: Challenge,
        captainUserId: Int
    ): Result<Unit> {
        // Update Firebase status to ACCEPTED
        val updateResult = firebaseService.updateChallengeStatus(challenge.id, Constants.STATUS_ACCEPTED)
        if (updateResult.isFailure) return Result.failure(updateResult.exceptionOrNull()!!)

        // Create a booking in Room DB (match slot)
        val startMs = TimeUtils.toMillis(challenge.proposedDate, challenge.proposedTime)
        val endMs = startMs + (90 * 60 * 1000L) // Default 90 min match slot

        val booking = Booking(
            userId = captainUserId,
            groundId = challenge.groundId,
            groundName = challenge.groundName,
            sport = challenge.sport,
            slotType = Constants.SLOT_MATCH,
            date = challenge.proposedDate,
            startTime = challenge.proposedTime,
            endTime = TimeUtils.millisToDisplayTime(endMs),
            startTimeMillis = startMs,
            endTimeMillis = endMs
        )
        bookingRepo.bookSlotForMatch(booking)

        // Create match in Room DB
        val match = Match(
            teamAId = challenge.challengerTeamId,
            teamAName = challenge.challengerTeamName,
            teamBId = challenge.opponentTeamId,
            teamBName = challenge.opponentTeamName,
            groundId = challenge.groundId,
            groundName = challenge.groundName,
            sport = challenge.sport,
            dateTime = TimeUtils.formatDateTime(challenge.proposedDate, challenge.proposedTime),
            dateTimeMillis = startMs,
            challengeFirebaseId = challenge.id
        )
        matchRepo.createMatch(match)

        return Result.success(Unit)
    }

    suspend fun declineChallenge(challengeId: String): Result<Unit> {
        return firebaseService.updateChallengeStatus(challengeId, Constants.STATUS_DECLINED)
    }

    fun listenToChallenges(onUpdate: (List<Challenge>) -> Unit): ListenerRegistration {
        return firebaseService.listenToChallenges(onUpdate)
    }

    fun listenToChallengesForTeam(teamId: Int, onUpdate: (List<Challenge>) -> Unit): ListenerRegistration {
        return firebaseService.listenToChallengesForTeam(teamId, onUpdate)
    }
}
