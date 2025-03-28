package com.example.fittrackerapp.abstractclasses.repositories

import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.entities.ExerciseDao
import com.example.fittrackerapp.entities.WorkoutDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WorkoutsAndExercisesRepository(private val workoutDao: WorkoutDao,
                                     private val exerciseDao: ExerciseDao
) {

    suspend fun getAll(): List<BaseWorkout> {
        return withContext(Dispatchers.IO) {
            (workoutDao.getAll() + exerciseDao.getAll()).sortedByDescending { bw -> bw.lastUsedDate }
        }
    }
}