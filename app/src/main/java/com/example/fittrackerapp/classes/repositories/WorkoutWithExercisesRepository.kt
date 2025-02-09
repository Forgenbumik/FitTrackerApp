package com.example.fittrackerapp.classes.repositories

import com.example.fittrackerapp.classes.WorkoutWithExercises
import com.example.fittrackerapp.classes.daointerfaces.WorkoutWithExercisesDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WorkoutWithExercisesRepository(private val dao: WorkoutWithExercisesDao) {

    suspend fun getById(workoutId: Int): WorkoutWithExercises? {
        return withContext(Dispatchers.IO) {
            dao.getById(workoutId)
        }
    }

    suspend fun getAll(): List<WorkoutWithExercises> {
        return withContext(Dispatchers.IO) {
            dao.getAll()
        }
    }
}