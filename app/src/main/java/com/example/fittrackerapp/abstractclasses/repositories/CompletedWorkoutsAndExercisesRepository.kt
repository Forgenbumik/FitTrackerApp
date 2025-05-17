package com.example.fittrackerapp.abstractclasses.repositories

import com.example.fittrackerapp.abstractclasses.BaseCompletedWorkout
import com.example.fittrackerapp.entities.CompletedExerciseDao
import com.example.fittrackerapp.entities.CompletedWorkoutDao
import com.example.fittrackerapp.entities.LastWorkout
import com.example.fittrackerapp.entities.LastWorkoutDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CompletedWorkoutsAndExercisesRepository(
    private val lastWorkoutDao: LastWorkoutDao,
    private val completedWorkoutDao: CompletedWorkoutDao,
    private val completedExerciseDao: CompletedExerciseDao
) {

    suspend fun getLastWorkouts(): List<LastWorkout> {
        return withContext(Dispatchers.IO) {
            lastWorkoutDao.getAll()
        }
    }

    suspend fun getAll(): List<BaseCompletedWorkout> {
        return withContext(Dispatchers.IO) {
            completedWorkoutDao.getAll() + completedExerciseDao.getSeparateCompleted()
        }
    }
}