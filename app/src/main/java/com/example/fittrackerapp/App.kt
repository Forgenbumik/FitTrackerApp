package com.example.fittrackerapp

import android.app.Application
import androidx.room.Room

class App: Application() {
    lateinit var appDatabase: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()

        // Создание БД внутри Application
        appDatabase = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database.db"
        ).build()
    }
}