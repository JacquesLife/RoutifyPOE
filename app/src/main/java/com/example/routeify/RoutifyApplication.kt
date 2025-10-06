package com.example.routeify

import android.app.Application
import androidx.room.Room
import com.example.routeify.data.api.AppDatabase

class RoutifyApplication : Application() {
    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "routeify.db"
        ).fallbackToDestructiveMigration().build()
    }
}