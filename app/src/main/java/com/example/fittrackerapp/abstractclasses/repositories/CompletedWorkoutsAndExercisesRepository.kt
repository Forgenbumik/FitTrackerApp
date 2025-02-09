package com.example.fittrackerapp.abstractclasses.repositories

import com.example.fittrackerapp.entities.BaseCompletedWorkout
import com.example.fittrackerapp.entities.daoInterfaces.CompletedWorkoutDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CompletedWorkoutsAndExercisesRepository(
    private val dao: CompletedWorkoutDao,
) {

    suspend fun getLastCompletedWorkouts(): List<BaseCompletedWorkout> {
        return withContext(Dispatchers.IO) {
            dao.getLastCompletedWorkouts()
        }
    }
}