package com.example.routeify.data.api

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.routeify.data.model.User

@Database(
    entities = [User::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}


