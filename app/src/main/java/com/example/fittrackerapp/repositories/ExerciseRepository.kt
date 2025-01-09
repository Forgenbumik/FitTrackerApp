package com.example.fittrackerapp.repositories

import com.example.fittrackerapp.daoInterfaces.ExerciseDao
import com.example.fittrackerapp.entities.Exercise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ExerciseRepository(private val dao: ExerciseDao) {
    suspend fun insert(exercise: Exercise) {
        withContext(Dispatchers.IO) {
            dao.insert(exercise)
        }
    }

    suspend fun getById(exerciseId: Int): Exercise? {
        return withContext(Dispatchers.IO) {
            getById(exerciseId)
        }
    }

    suspend fun delete(exercise: Exercise) {
        withContext(Dispatchers.IO) {
            dao.delete(exercise)
        }
    }

    suspend fun update(exercise: Exercise) {
        withContext(Dispatchers.IO) {
            dao.update(exercise)
        }
    }

    suspend fun getAll(): Flow<List<Exercise>> {
        return withContext(Dispatchers.IO) {
            dao.getAll()
        }
    }
}