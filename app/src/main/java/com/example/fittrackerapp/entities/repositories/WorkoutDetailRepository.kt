package com.example.fittrackerapp.entities.repositories

import com.example.fittrackerapp.entities.daoInterfaces.WorkoutDetailDao
import com.example.fittrackerapp.entities.WorkoutDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WorkoutDetailRepository(private val dao: WorkoutDetailDao) {

    suspend fun insert(workoutDetail: WorkoutDetail) {
        withContext(Dispatchers.IO) {
            dao.insert(workoutDetail)
        }
    }

    suspend fun getById(workoutDetailId: Int): WorkoutDetail? {
        return withContext(Dispatchers.IO) {
            dao.getById(workoutDetailId)
        }
    }

    suspend fun delete(workoutDetail: WorkoutDetail) {
        withContext(Dispatchers.IO) {
            dao.delete(workoutDetail)
        }
    }

    suspend fun update(workoutDetail: WorkoutDetail) {
        withContext(Dispatchers.IO) {
            dao.update(workoutDetail)
        }
    }
}