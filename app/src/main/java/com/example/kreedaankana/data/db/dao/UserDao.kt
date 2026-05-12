package com.example.kreedaankana.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.kreedaankana.data.db.entity.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash LIMIT 1")
    suspend fun login(email: String, passwordHash: String): User?

    @Query("SELECT * FROM users")
    fun getAllUsers(): LiveData<List<User>>

    @Query("UPDATE users SET role = :role WHERE id = :userId")
    suspend fun updateRole(userId: Int, role: String)

    @Query("UPDATE users SET teamId = :teamId WHERE id = :userId")
    suspend fun updateTeam(userId: Int, teamId: Int)

    @Query("UPDATE users SET homeGroundId = :groundId WHERE id = :userId")
    suspend fun updateHomeGround(userId: Int, groundId: Int)

    @Query("UPDATE users SET sport = :sport WHERE id = :userId")
    suspend fun updateSport(userId: Int, sport: String)
}
