package com.example.fittrackerapp.repositories

import com.example.fittrackerapp.daoInterfaces.CompletedExerciseDao
import com.example.fittrackerapp.entities.CompletedExercise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CompletedExerciseRepository(private val dao: CompletedExerciseDao) {

    suspend fun insert(completedExercise: CompletedExercise) {
        withContext(Dispatchers.IO) {
            dao.insert(completedExercise)
        }
    }

    suspend fun delete(completedExercise: CompletedExercise) {
        withContext(Dispatchers.IO) {
            dao.delete(completedExercise)
        }
    }

    suspend fun update(completedExercise: CompletedExercise) {
        withContext(Dispatchers.IO) {
            dao.update(completedExercise)
        }
    }

    suspend fun getById(completedExerciseId: Int): CompletedExercise? {
        return withContext(Dispatchers.IO) {
            dao.getById(completedExerciseId)
        }
    }

}