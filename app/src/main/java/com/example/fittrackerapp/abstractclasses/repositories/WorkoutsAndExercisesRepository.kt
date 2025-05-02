package com.example.fittrackerapp.abstractclasses.repositories

import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.entities.Exercise
import com.example.fittrackerapp.entities.ExerciseDao
import com.example.fittrackerapp.entities.Workout
import com.example.fittrackerapp.entities.WorkoutDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class WorkoutsAndExercisesRepository(private val workoutDao: WorkoutDao,
                                     private val exerciseDao: ExerciseDao
) {

    fun getUsedExceptFavouritesFlow(): Flow<List<BaseWorkout>> {
        val workoutFlow = workoutDao.getUsedExceptFavouritesFlow()
        val exerciseFlow = exerciseDao.getUsedExceptFavouritesFlow()

        return workoutFlow.combine(exerciseFlow) { workouts, exercises ->
            val combinedList = mutableListOf<BaseWorkout>()
            combinedList.addAll(workouts)
            combinedList.addAll(exercises)
            combinedList
        }
    }

    fun getFavouritesFlow(): Flow<List<BaseWorkout>> {
        val workoutFlow = workoutDao.getFavouritesFlow()
        val exerciseFlow = exerciseDao.getFavouritesFlow()

        return workoutFlow.combine(exerciseFlow) { workouts, exercises ->
            val combinedList = mutableListOf<BaseWorkout>()
            combinedList.addAll(workouts)
            combinedList.addAll(exercises)
            combinedList
        }
    }

    suspend fun addFavourite(workout: BaseWorkout) {
        if (workout is Workout) {
            workoutDao.update(workout)
        } else if (workout is Exercise) {
            exerciseDao.update(workout)
        }
    }

    suspend fun deleteFavourite(workout: BaseWorkout) {
        workout.isFavourite = false
        if (workout is Workout) {
            workoutDao.update(workout)
        } else if (workout is Exercise) {
            exerciseDao.update(workout)
        }
    }
}