package com.example.fittrackerapp.abstractclasses.repositories

import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.entities.Exercise
import com.example.fittrackerapp.entities.ExerciseDao
import com.example.fittrackerapp.entities.FavouriteWorkoutDao
import com.example.fittrackerapp.entities.Workout
import com.example.fittrackerapp.entities.WorkoutDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WorkoutsAndExercisesRepository(private val workoutDao: WorkoutDao,
                                     private val exerciseDao: ExerciseDao,
                                     private val favouriteWorkoutDao: FavouriteWorkoutDao
) {

    suspend fun getAll(): List<BaseWorkout> {
        return withContext(Dispatchers.IO) {
            (workoutDao.getAll() + exerciseDao.getAllExceptNotUsed()).sortedByDescending { bw -> bw.lastUsedDate }
        }
    }

    suspend fun getAllExceptFavourites(): List<BaseWorkout> = withContext(Dispatchers.IO) {
        val favouriteWorkouts = favouriteWorkoutDao.getAll()
        val allWorkouts = workoutDao.getAll() + exerciseDao.getAllExceptNotUsed()

        return@withContext allWorkouts.filter { baseWorkout ->
            // Проверяем, что workout не входит в избранные
            favouriteWorkouts.none { fav ->
                var workoutTypeId = 0
                when (baseWorkout) {
                    is Workout -> {
                        val workout = baseWorkout
                        workoutTypeId = 1
                        fav.workoutId == workout.id && workoutTypeId == fav.typeId
                    }
                    is Exercise -> {
                        workoutTypeId = 2
                        fav.workoutId == baseWorkout.id && workoutTypeId == fav.typeId
                                && baseWorkout.isUsed
                    }
                    else -> {
                        true
                    }
                }
            }
        }
    }
}