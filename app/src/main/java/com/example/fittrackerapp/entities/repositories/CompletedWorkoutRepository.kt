package com.example.fittrackerapp.entities.repositories

import com.example.fittrackerapp.entities.daoInterfaces.CompletedWorkoutDao
import com.example.fittrackerapp.entities.CompletedWorkout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CompletedWorkoutRepository(private val dao: CompletedWorkoutDao) {

    suspend fun insert(completedWorkout: CompletedWorkout) {
        withContext(Dispatchers.IO) {
            dao.insert(completedWorkout)
        }
    }

    suspend fun getById(completedWorkoutId: Int): CompletedWorkout? {
        return withContext(Dispatchers.IO) {
            dao.getById(completedWorkoutId)
        }
    }

    suspend fun delete(completedWorkout: CompletedWorkout) {
        withContext(Dispatchers.IO) {
            dao.delete(completedWorkout)
        }
    }

    suspend fun update(completedWorkout: CompletedWorkout) {
        withContext(Dispatchers.IO) {
            dao.update(completedWorkout)
        }
    }
}