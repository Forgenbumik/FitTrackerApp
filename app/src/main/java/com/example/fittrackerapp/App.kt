package com.example.fittrackerapp

import android.app.Application
import android.util.Log
import androidx.room.Room

class App : Application() {

    companion object {
        lateinit var instance: App
            private set
    }

    val appDatabase: AppDatabase by lazy {

        Room.databaseBuilder(
            applicationContext,  // Используем встроенный applicationContext
            AppDatabase::class.java, "database.db"
        )
            .fallbackToDestructiveMigration()
            .createFromAsset("asset.db")
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        appDatabase.openHelper.writableDatabase
        Log.d("DatabaseTest", "Database created!")
    }
}