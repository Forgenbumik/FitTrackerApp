package com.example.fittrackerapp

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = "active_workout")

object ActiveWorkoutPrefs {

    val ACTIVE_WORKOUT_ID = stringPreferencesKey("active_workout_id")
    val ACTIVE_WORKOUT_START_TIME = longPreferencesKey("active_workout_start_time")
}