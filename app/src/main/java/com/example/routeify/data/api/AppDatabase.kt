package com.example.routeify.data.api

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.routeify.data.model.User

// The AppDatabase class for Room database
@Database(
    entities = [User::class],
    version = 2,
    exportSchema = true
)

// Abstract class extending RoomDatabase
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}


