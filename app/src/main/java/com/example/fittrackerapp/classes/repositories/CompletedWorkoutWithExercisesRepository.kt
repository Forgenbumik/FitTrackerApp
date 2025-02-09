package com.example.fittrackerapp.classes.repositories

import com.example.fittrackerapp.classes.CompletedWorkoutWithExercises
import com.example.fittrackerapp.classes.daointerfaces.CompletedWorkoutWithExercisesDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CompletedWorkoutWithExercisesRepository(private val dao: CompletedWorkoutWithExercisesDao) {

    suspend fun getById(completedWorkoutId: Int): CompletedWorkoutWithExercises? {
        return withContext(Dispatchers.IO) {
            dao.getById(completedWorkoutId)
        }
    }

    suspend fun getAll(): List<CompletedWorkoutWithExercises> {
        return withContext(Dispatchers.IO) {
            dao.getAll()
        }
    }
}