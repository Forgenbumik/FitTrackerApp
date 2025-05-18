package com.example.fittrackerapp.uielements.completedworkouts

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.abstractclasses.BaseCompletedWorkout
import com.example.fittrackerapp.abstractclasses.repositories.CompletedWorkoutsAndExercisesRepository
import com.example.fittrackerapp.entities.CompletedExercise
import com.example.fittrackerapp.entities.CompletedExerciseRepository
import com.example.fittrackerapp.entities.CompletedWorkout
import com.example.fittrackerapp.entities.CompletedWorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompletedWorkoutsViewModel @Inject constructor(
    private val completedWorkoutRepository: CompletedWorkoutRepository,
    private val completedWorkoutsAndExercisesRepository: CompletedWorkoutsAndExercisesRepository,
    private val completedExerciseRepository: CompletedExerciseRepository
): ViewModel() {

    private val _completedWorkouts = MutableStateFlow<List<BaseCompletedWorkout>>(emptyList())
    val completedWorkouts: StateFlow<List<BaseCompletedWorkout>> = _completedWorkouts

    init {
        viewModelScope.launch {
            _completedWorkouts.value = completedWorkoutsAndExercisesRepository.getAll()
        }
    }

    suspend fun getWorkoutName(baseCompletedWorkout: BaseCompletedWorkout): String {
        var workoutName = ""

        if (baseCompletedWorkout is CompletedWorkout) {
            workoutName = completedWorkoutRepository.getWorkoutName(baseCompletedWorkout.id)
        }
        else if (baseCompletedWorkout is CompletedExercise) {
            workoutName = completedExerciseRepository.getExerciseName(baseCompletedWorkout.exerciseId)
        }
        return workoutName
    }
}