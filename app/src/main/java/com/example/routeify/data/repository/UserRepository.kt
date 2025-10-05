package com.example.routeify.data.repository

import com.example.routeify.data.api.UserDao
import com.example.routeify.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class UserRepository(private val userDao: UserDao) {
    suspend fun register(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        val existing = userDao.findByEmail(email)
        if (existing != null) return@withContext Result.failure(IllegalStateException("Email already in use"))
        val user = User(email = email, passwordHash = hash(password))
        val id = userDao.insert(user)
        Result.success(user.copy(id = id))
    }

    suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        val user = userDao.findByEmail(email)
        if (user == null || user.passwordHash != hash(password)) {
            return@withContext Result.failure(IllegalArgumentException("Invalid credentials"))
        }
        Result.success(user)
    }

    private fun hash(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}


