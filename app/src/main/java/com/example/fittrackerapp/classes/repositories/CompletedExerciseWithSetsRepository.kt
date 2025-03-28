package com.example.fittrackerapp.classes.repositories

import com.example.fittrackerapp.classes.CompletedExerciseWithSets
import com.example.fittrackerapp.classes.daointerfaces.CompletedExerciseWithSetsDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CompletedExerciseWithSetsRepository(private val dao: CompletedExerciseWithSetsDao) {

    suspend fun getById(completedExerciseId: Int): CompletedExerciseWithSets {
        return withContext(Dispatchers.IO) {
            dao.getById(completedExerciseId)
        }
    }

    suspend fun getAll(): List<CompletedExerciseWithSets> {
        return withContext(Dispatchers.IO) {
            dao.getAll()
        }
    }
}