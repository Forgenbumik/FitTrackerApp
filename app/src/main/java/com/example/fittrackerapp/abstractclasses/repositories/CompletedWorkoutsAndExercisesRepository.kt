package com.example.fittrackerapp.abstractclasses.repositories

import com.example.fittrackerapp.abstractclasses.BaseCompletedWorkout
import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.entities.completedexercise.CompletedExerciseDao
import com.example.fittrackerapp.entities.completedworkout.CompletedWorkoutDao
import com.example.fittrackerapp.entities.LastWorkout
import com.example.fittrackerapp.entities.LastWorkoutDao
import com.example.fittrackerapp.entities.completedworkout.CompletedWorkout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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

    suspend fun getAllFlow(): Flow<List<BaseCompletedWorkout>> {
        val completedWorkoutsFlow = completedWorkoutDao.getAllFlow()
        val completedExercisesFlow = completedExerciseDao.getSeparateCompletedFlow()

        return completedWorkoutsFlow.combine(completedExercisesFlow) { workouts, exercises ->
            val combinedList = mutableListOf<BaseCompletedWorkout>()
            combinedList.addAll(workouts)
            combinedList.addAll(exercises)
            combinedList
        }
    }
}