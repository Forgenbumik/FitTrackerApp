package com.example.fittrackerapp.abstractclasses.repositories

import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.entities.Exercise
import com.example.fittrackerapp.entities.ExerciseDao
import com.example.fittrackerapp.entities.FavouriteWorkoutDao
import com.example.fittrackerapp.entities.Workout
import com.example.fittrackerapp.entities.WorkoutDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class WorkoutsAndExercisesRepository(private val workoutDao: WorkoutDao,
                                     private val exerciseDao: ExerciseDao,
                                     private val favouriteWorkoutDao: FavouriteWorkoutDao
) {

    fun getUsedExceptFavourites(): Flow<List<BaseWorkout>> {
        val favouriteWorkoutsFlow = favouriteWorkoutDao.getAllFlow()
        val workoutFlow = workoutDao.getAllFlow()
        val exerciseFlow = exerciseDao.getAllExceptNotUsedFlow()

        return combine(favouriteWorkoutsFlow, workoutFlow, exerciseFlow) { favourites, workouts, exercises ->
            val allWorkouts = workouts + exercises

            allWorkouts.filter { baseWorkout ->
                // Проверяем, что workout не входит в избранные
                favourites.none { fav ->
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
}