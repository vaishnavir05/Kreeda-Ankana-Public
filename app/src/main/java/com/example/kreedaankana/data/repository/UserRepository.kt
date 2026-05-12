package com.example.kreedaankana.data.repository

import com.example.kreedaankana.data.db.dao.UserDao
import com.example.kreedaankana.data.db.entity.User
import com.example.kreedaankana.utils.ValidationUtils

import com.example.kreedaankana.data.firebase.FirebaseService

class UserRepository(private val userDao: UserDao, private val firebaseService: FirebaseService) {

    suspend fun register(
        name: String, email: String, phone: String, password: String
    ): Result<User> {
        if (!ValidationUtils.isValidName(name)) return Result.failure(Exception("Invalid name"))
        if (!ValidationUtils.isValidEmail(email)) return Result.failure(Exception("Invalid email"))
        if (!ValidationUtils.isValidPhone(phone)) return Result.failure(Exception("Invalid phone"))
        if (!ValidationUtils.isValidPassword(password)) return Result.failure(Exception("Invalid password"))

        val existing = userDao.getUserByEmail(email)
        if (existing != null) return Result.failure(Exception("Email already registered"))

        val user = User(
            name = name.trim(),
            email = email.trim(),
            phone = phone.trim(),
            passwordHash = ValidationUtils.hashPassword(password),
            role = "MEMBER"
        )
        val id = userDao.insertUser(user)
        val savedUser = user.copy(id = id.toInt())
        
        firebaseService.saveUser(savedUser)
        
        return Result.success(savedUser)
    }

    suspend fun login(email: String, password: String): Result<User> {
        val passwordHash = ValidationUtils.hashPassword(password)
        val user = userDao.login(email.trim(), passwordHash)
        return if (user != null) Result.success(user)
        else Result.failure(Exception("Invalid credentials"))
    }

    suspend fun getUserById(id: Int): User? = userDao.getUserById(id)

    suspend fun updateRole(userId: Int, role: String) = userDao.updateRole(userId, role)
    suspend fun updateTeam(userId: Int, teamId: Int) = userDao.updateTeam(userId, teamId)
    suspend fun updateHomeGround(userId: Int, groundId: Int) = userDao.updateHomeGround(userId, groundId)
    suspend fun updateSport(userId: Int, sport: String) = userDao.updateSport(userId, sport)
    suspend fun updateUser(user: User) = userDao.updateUser(user)

    fun getAllUsers() = userDao.getAllUsers()
}
