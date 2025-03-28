package com.example.fittrackerapp.abstractclasses.repositories

import com.example.fittrackerapp.entities.LastWorkout
import com.example.fittrackerapp.entities.LastWorkoutDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CompletedWorkoutsAndExercisesRepository(
    private val dao: LastWorkoutDao,
) {

    suspend fun getLastWorkouts(): List<LastWorkout> {
        return withContext(Dispatchers.IO) {
            dao.getAll()
        }
    }
}