package com.example.fittrackerapp

import android.app.Application
import com.example.fittrackerapp.uielements.executingworkout.WorkoutRecordingCommunicator
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application(){
    override fun onCreate() {
        super.onCreate()
        WorkoutRecordingCommunicator.init()
    }
}