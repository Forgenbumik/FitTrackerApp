package com.example.fittrackerapp.viewmodels

import androidx.lifecycle.ViewModel
import com.example.fittrackerapp.entities.BaseCompletedWorkout
import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.abstractclasses.repositories.CompletedWorkoutsAndExercisesRepository
import com.example.fittrackerapp.abstractclasses.repositories.WorkoutsAndExercisesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainScreenModel(
    private val baseWorkoutsRepository: WorkoutsAndExercisesRepository,
    private val completedWorkoutsRepository: CompletedWorkoutsAndExercisesRepository
): ViewModel() {

    private val _favouriteWorkoutsList = MutableStateFlow<List<BaseWorkout>>(emptyList())
    val favouriteWorkoutsList: StateFlow<List<BaseWorkout>> = _favouriteWorkoutsList

    private val _completedWorkoutsList = MutableStateFlow<List<BaseCompletedWorkout>>(emptyList())
    val completedWorkoutsList: StateFlow<List<BaseCompletedWorkout>> = _completedWorkoutsList

    suspend fun loadFavourites(): Unit {
        _favouriteWorkoutsList.value = baseWorkoutsRepository.getFavourites()
    }

    suspend fun loadCompletedWorkouts(): Unit {
        _completedWorkoutsList.value = completedWorkoutsRepository.getLastCompletedWorkouts()
    }
}