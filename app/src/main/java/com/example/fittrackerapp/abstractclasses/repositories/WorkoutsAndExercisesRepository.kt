package com.example.fittrackerapp.abstractclasses.repositories

import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.entities.FavouriteWorkout
import com.example.fittrackerapp.entities.daoInterfaces.ExerciseDao
import com.example.fittrackerapp.entities.daoInterfaces.FavouriteWorkoutDao
import com.example.fittrackerapp.entities.daoInterfaces.WorkoutDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WorkoutsAndExercisesRepository(private val workoutDao: WorkoutDao,
                                     private val exerciseDao: ExerciseDao,
                                     private val favouriteWorkoutDao: FavouriteWorkoutDao) {

    suspend fun getAll(): List<BaseWorkout> {
        return withContext(Dispatchers.IO) {
            (workoutDao.getAll() + exerciseDao.getAll()).sortedByDescending { bw -> bw.lastUsedDate }
        }
    }

    suspend fun getFavourites(): List<FavouriteWorkout> {
        return withContext(Dispatchers.IO) {
            favouriteWorkoutDao.getAll()
        }
    }
}