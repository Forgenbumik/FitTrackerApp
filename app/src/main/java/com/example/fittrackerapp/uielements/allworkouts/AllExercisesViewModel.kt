package com.example.fittrackerapp.uielements.allworkouts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.abstractclasses.repositories.WorkoutsAndExercisesRepository
import com.example.fittrackerapp.entities.exercise.Exercise
import com.example.fittrackerapp.entities.exercise.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllExercisesViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
    private val workoutsAndExercisesRepository: WorkoutsAndExercisesRepository,
): ViewModel() {

    val _exercisesList = MutableStateFlow<List<BaseWorkout>>(emptyList())
    val exercisesList: StateFlow<List<BaseWorkout>> = _exercisesList

    val reason: String? get() = savedStateHandle["reason"]

    init {
        if (reason == "exerciseAdding") {
            viewModelScope.launch {
                _exercisesList.value = workoutsAndExercisesRepository.getNotUsed()
            }
        }
        else if (reason == "workoutCreating")
        viewModelScope.launch {
            _exercisesList.value = exerciseRepository.getAll()
        }
    }

    fun addExerciseToUsed(exercise: Exercise) {
        val exerciseToChange = exercise.copy(isUsed = true)
        viewModelScope.launch {
            exerciseRepository.update(exerciseToChange)
        }
    }
}