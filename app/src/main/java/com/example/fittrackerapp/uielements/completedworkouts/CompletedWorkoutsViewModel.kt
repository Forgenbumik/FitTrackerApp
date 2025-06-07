package com.example.fittrackerapp.uielements.completedworkouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.abstractclasses.BaseCompletedWorkout
import com.example.fittrackerapp.abstractclasses.repositories.CompletedWorkoutsAndExercisesRepository
import com.example.fittrackerapp.entities.completedexercise.CompletedExercise
import com.example.fittrackerapp.entities.completedexercise.CompletedExerciseRepository
import com.example.fittrackerapp.entities.completedworkout.CompletedWorkout
import com.example.fittrackerapp.entities.completedworkout.CompletedWorkoutRepository
import com.example.fittrackerapp.entities.exercise.ExerciseRepository
import com.example.fittrackerapp.entities.workout.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompletedWorkoutsViewModel @Inject constructor(
    private val completedWorkoutRepository: CompletedWorkoutRepository,
    private val completedWorkoutsAndExercisesRepository: CompletedWorkoutsAndExercisesRepository,
    private val completedExerciseRepository: CompletedExerciseRepository,
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository
): ViewModel() {

    private val _completedWorkouts = MutableStateFlow<List<BaseCompletedWorkout>>(emptyList())
    val completedWorkouts: StateFlow<List<BaseCompletedWorkout>> = _completedWorkouts

    init {
        viewModelScope.launch {
            completedWorkoutsAndExercisesRepository.getAllFlow().collect {
                _completedWorkouts.value = it
            }
        }
    }

    suspend fun getWorkoutName(baseCompletedWorkout: BaseCompletedWorkout): String {
        var workoutName = ""

        if (baseCompletedWorkout is CompletedWorkout) {
            workoutName = workoutRepository.getWorkoutName(baseCompletedWorkout.workoutId)
        }
        else if (baseCompletedWorkout is CompletedExercise) {
            workoutName = exerciseRepository.getExerciseName(baseCompletedWorkout.exerciseId)
        }
        return workoutName
    }

    fun deleteCompletedWorkout(completedWorkout: BaseCompletedWorkout) {
        when (completedWorkout) {
            is CompletedWorkout -> {
                viewModelScope.launch {
                    completedWorkoutRepository.delete(completedWorkout)
                }
            }
            is CompletedExercise -> {
                viewModelScope.launch {
                    completedExerciseRepository.delete(completedWorkout)
                }
            }
        }
    }

    fun formatTime(secs: Int): String {
        val seconds = secs % 60
        val minutes = secs / 60 % 60
        val hours = secs / 3600

        return if (hours > 0) {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }
}