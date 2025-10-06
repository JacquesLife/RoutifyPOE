package com.example.routeify.data.repository

import com.example.routeify.data.api.UserDao
import com.example.routeify.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class UserRepository(private val userDao: UserDao) {
    suspend fun register(email: String, username: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        val existingEmail = userDao.findByEmail(email)
        if (existingEmail != null) return@withContext Result.failure(IllegalStateException("Email already in use"))
        val existingUsername = userDao.findByUsername(username)
        if (existingUsername != null) return@withContext Result.failure(IllegalStateException("Username already in use"))
        val user = User(email = email, username = username, passwordHash = hash(password))
        val id = userDao.insert(user)
        Result.success(user.copy(id = id))
    }

    suspend fun loginWithEmail(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        val user = userDao.findByEmail(email)
        if (user == null || user.passwordHash != hash(password)) {
            return@withContext Result.failure(IllegalArgumentException("Invalid credentials"))
        }
        Result.success(user)
    }

    suspend fun loginWithUsername(username: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        val user = userDao.findByUsername(username)
        if (user == null || user.passwordHash != hash(password)) {
            return@withContext Result.failure(IllegalArgumentException("Invalid credentials"))
        }
        Result.success(user)
    }

    suspend fun upsertSsoUser(email: String, username: String): Result<User> = withContext(Dispatchers.IO) {
        val existingEmail = userDao.findByEmail(email)
        if (existingEmail != null) return@withContext Result.success(existingEmail)
        val existingUsername = userDao.findByUsername(username)
        val uniqueUsername = if (existingUsername == null) username else generateUniqueUsername(username)
        val user = User(email = email, username = uniqueUsername, passwordHash = "")
        val id = userDao.insert(user)
        Result.success(user.copy(id = id))
    }

    private suspend fun generateUniqueUsername(base: String, attempt: Int = 1): String {
        val candidate = if (attempt == 1) base else "$base$attempt"
        val exists = userDao.findByUsername(candidate) != null
        return if (!exists) candidate else generateUniqueUsername(base, attempt + 1)
    }

    private fun hash(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}


