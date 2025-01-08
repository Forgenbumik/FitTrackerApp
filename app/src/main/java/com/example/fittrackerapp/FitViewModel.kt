package com.example.fittrackerapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.abstractclasses.BaseCompletedWorkout
import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.entities.Workout
import com.example.fittrackerapp.repositories.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FitViewModel: ViewModel() {

    private val workoutRepository: WorkoutRepository

    private val _workouts = MutableStateFlow<List<BaseWorkout>>(emptyList())
    val workouts: StateFlow<List<BaseWorkout>> = _workouts.asStateFlow()

    private val _completedWorkouts = MutableStateFlow<List<BaseCompletedWorkout>>(emptyList())
    val completedWorkouts: StateFlow<List<BaseCompletedWorkout>> = _completedWorkouts.asStateFlow()

    fun insertWorkout(workout: Workout) {
        viewModelScope.launch {

        }
    }

}