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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CompletedWorkoutsViewModel(
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

    fun getWorkoutName(baseCompletedWorkout: BaseCompletedWorkout): String {
        var workoutName = ""

        if (baseCompletedWorkout !is CompletedWorkout) {
            viewModelScope.launch {
                workoutName = completedWorkoutRepository.getWorkoutName(baseCompletedWorkout.id)
            }.let { return workoutName }
        }
        else if (baseCompletedWorkout is CompletedExercise) {
            viewModelScope.launch {
                workoutName = completedExerciseRepository.getExerciseName(baseCompletedWorkout.exerciseId)
            }.let { return workoutName }
        }
        return workoutName
    }
}

class CompletedWorkoutsViewModelFactory(
    private val completedWorkoutRepository: CompletedWorkoutRepository,
    private val completedWorkoutsAndExercisesRepository: CompletedWorkoutsAndExercisesRepository,
    private val completedExerciseRepository: CompletedExerciseRepository
) : ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(CompletedWorkoutsViewModel::class.java) -> {
                CompletedWorkoutsViewModel(completedWorkoutRepository, completedWorkoutsAndExercisesRepository, completedExerciseRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}