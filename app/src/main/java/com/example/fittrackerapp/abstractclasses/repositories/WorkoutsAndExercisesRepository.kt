package com.example.fittrackerapp.abstractclasses.repositories

import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.entities.exercise.Exercise
import com.example.fittrackerapp.entities.exercise.ExerciseDao
import com.example.fittrackerapp.entities.workout.Workout
import com.example.fittrackerapp.entities.workout.WorkoutDao
import com.example.fittrackerapp.entities.workoutdetail.WorkoutDetailDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class WorkoutsAndExercisesRepository(
    private var workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
    private val workoutDetailDao: WorkoutDetailDao
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

    suspend fun getNotUsed(): List<BaseWorkout> {
        return workoutDao.getNotUsed() + exerciseDao.getNotUsed()
    }

    suspend fun getUsedExceptFavourites(): List<BaseWorkout> {
        return withContext(Dispatchers.IO) {
            workoutDao.getUsedExceptFavourites() + exerciseDao.getUsedExceptFavourites()
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
        if (workout is Workout) {
            val workoutToDelete = workout.copy(isFavourite = false)
            workoutDao.update(workoutToDelete)
        } else if (workout is Exercise) {
            val exerciseToDelete = workout.copy(isFavourite = false)
            exerciseDao.update(exerciseToDelete)
        }
    }

    suspend fun removeFromUsed(workout: BaseWorkout) {
        if (workout is Workout) {
            val workoutToRemove = workout.copy(isUsed = false)
            workoutDao.update(workoutToRemove)
        }
        else if (workout is Exercise){
            val exerciseToRemove = workout.copy(isUsed = false)
            exerciseDao.update(exerciseToRemove)
        }
    }

    suspend fun delete(workout: BaseWorkout) {
        if (workout is Workout) {
            val workoutToDelete = workout.copy(isDeleted = true, isFavourite = false, isUsed = false)
            workoutDao.update(workoutToDelete)
        }
        else if (workout is Exercise) {
            val exerciseToDelete = workout.copy(isDeleted = true, isFavourite = false, isUsed = false)
            exerciseDao.update(exerciseToDelete)
            workoutDetailDao.deleteExerciseFromWorkout(workout.id)
            exerciseDao.update(workout)
        }
    }


}