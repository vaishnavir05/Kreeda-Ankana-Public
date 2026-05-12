package com.example.kreedaankana.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class Challenge(
    val id: String = "",
    val challengerTeamId: Int = 0,
    val challengerTeamName: String = "",
    val opponentTeamId: Int = 0,          // 0 = open challenge
    val opponentTeamName: String = "",
    val sport: String = "",
    val groundId: Int = 0,
    val groundName: String = "",
    val proposedTime: String = "",         // "yyyy-MM-dd hh:mm a"
    val proposedDate: String = "",
    val status: String = "PENDING",        // PENDING, ACCEPTED, DECLINED
    val createdAt: Long = 0L,
    val createdBy: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "challengerTeamId" to challengerTeamId,
        "challengerTeamName" to challengerTeamName,
        "opponentTeamId" to opponentTeamId,
        "opponentTeamName" to opponentTeamName,
        "sport" to sport,
        "groundId" to groundId,
        "groundName" to groundName,
        "proposedTime" to proposedTime,
        "proposedDate" to proposedDate,
        "status" to status,
        "createdAt" to createdAt,
        "createdBy" to createdBy
    )

    companion object {
        fun fromMap(id: String, data: Map<String, Any>): Challenge {
            return Challenge(
                id = id,
                challengerTeamId = (data["challengerTeamId"] as? Long)?.toInt() ?: 0,
                challengerTeamName = data["challengerTeamName"] as? String ?: "",
                opponentTeamId = (data["opponentTeamId"] as? Long)?.toInt() ?: 0,
                opponentTeamName = data["opponentTeamName"] as? String ?: "",
                sport = data["sport"] as? String ?: "",
                groundId = (data["groundId"] as? Long)?.toInt() ?: 0,
                groundName = data["groundName"] as? String ?: "",
                proposedTime = data["proposedTime"] as? String ?: "",
                proposedDate = data["proposedDate"] as? String ?: "",
                status = data["status"] as? String ?: "PENDING",
                createdAt = data["createdAt"] as? Long ?: 0L,
                createdBy = data["createdBy"] as? String ?: ""
            )
        }
    }
}

class FirebaseService {
    private val db = FirebaseFirestore.getInstance()
    private val challengeCollection = db.collection("ChallengeBoard")
    private val groundCollection = db.collection("Grounds")
    private val teamCollection = db.collection("Teams")
    private val matchCollection = db.collection("Matches")
    private val bookingCollection = db.collection("Bookings")
    private val scoreCollection = db.collection("Scores")

    // --- Challenges ---
    suspend fun postChallenge(challenge: Challenge): Result<String> {
        return try {
            val doc = challengeCollection.add(challenge.toMap()).await()
            Result.success(doc.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateChallengeStatus(challengeId: String, status: String): Result<Unit> {
        return try {
            challengeCollection.document(challengeId).update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listenToChallenges(onUpdate: (List<Challenge>) -> Unit): ListenerRegistration {
        return challengeCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val challenges = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { Challenge.fromMap(doc.id, it) }
                }
                onUpdate(challenges)
            }
    }

    fun listenToChallengesForTeam(
        teamId: Int,
        onUpdate: (List<Challenge>) -> Unit
    ): ListenerRegistration {
        return challengeCollection
            .whereEqualTo("opponentTeamId", teamId)
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val challenges = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { Challenge.fromMap(doc.id, it) }
                }
                onUpdate(challenges)
            }
    }

    // --- Generic Sync Methods ---
    suspend fun <T : Any> syncToFirebase(collectionName: String, id: String?, data: T): Result<String> {
        return try {
            val col = db.collection(collectionName)
            val docId = if (id.isNullOrEmpty()) {
                col.add(data).await().id
            } else {
                col.document(id).set(data).await()
                id
            }
            Result.success(docId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun <T : Any> listenToCollection(
        collectionName: String,
        clazz: Class<T>,
        onUpdate: (List<Pair<String, T>>) -> Unit
    ): ListenerRegistration {
        return db.collection(collectionName)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val items = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(clazz)?.let { doc.id to it }
                }
                onUpdate(items)
            }
    }

    // --- Specific Listeners for Room Integration ---
    fun listenToGrounds(onUpdate: (List<com.example.kreedaankana.data.db.entity.Ground>) -> Unit): ListenerRegistration {
        return groundCollection.addSnapshotListener { snapshot, _ ->
            val grounds = snapshot?.documents?.mapNotNull { it.toObject(com.example.kreedaankana.data.db.entity.Ground::class.java)?.copy(firebaseId = it.id) }
            if (grounds != null) onUpdate(grounds)
        }
    }

    fun listenToMatches(onUpdate: (List<com.example.kreedaankana.data.db.entity.Match>) -> Unit): ListenerRegistration {
        return matchCollection.addSnapshotListener { snapshot, _ ->
            val matches = snapshot?.documents?.mapNotNull { it.toObject(com.example.kreedaankana.data.db.entity.Match::class.java)?.copy(firebaseId = it.id) }
            if (matches != null) onUpdate(matches)
        }
    }

    suspend fun saveBooking(booking: com.example.kreedaankana.data.db.entity.Booking): Result<String> {
        return syncToFirebase("Bookings", booking.firebaseId, booking)
    }

    suspend fun saveMatch(match: com.example.kreedaankana.data.db.entity.Match): Result<String> {
        return syncToFirebase("Matches", match.firebaseId, match)
    }

    suspend fun saveTeam(team: com.example.kreedaankana.data.db.entity.Team): Result<String> {
        return syncToFirebase("Teams", team.firebaseId, team)
    }

    suspend fun saveScore(score: com.example.kreedaankana.data.db.entity.Score): Result<String> {
        return syncToFirebase("Scores", null, score)
    }

    suspend fun saveUser(user: com.example.kreedaankana.data.db.entity.User): Result<String> {
        return syncToFirebase("Users", user.id.toString(), user)
    }

    suspend fun saveSport(sport: com.example.kreedaankana.data.db.entity.Sport): Result<String> {
        return syncToFirebase("Sports", sport.id.toString(), sport)
    }
}
