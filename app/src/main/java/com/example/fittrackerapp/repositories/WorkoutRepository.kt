package com.example.fittrackerapp.repositories

import com.example.fittrackerapp.daoInterfaces.WorkoutDao
import com.example.fittrackerapp.entities.Workout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WorkoutRepository(private val dao: WorkoutDao) {
    suspend fun insert(workout: Workout) {
        withContext(Dispatchers.IO) {
            dao.insert(workout)
        }
    }

    suspend fun getAll(): Flow<List<Workout>> {
        return withContext(Dispatchers.IO) {
            dao.getAll()
        }
    }
}