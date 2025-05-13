package com.example.fittrackerapp.uielements.allworkouts

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.abstractclasses.repositories.WorkoutsAndExercisesRepository
import com.example.fittrackerapp.entities.Exercise
import com.example.fittrackerapp.entities.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AllExercisesViewModel(
    reason: String?,
    private val exerciseRepository: ExerciseRepository,
    private val workoutsAndExercisesRepository: WorkoutsAndExercisesRepository
): ViewModel() {

    val _exercisesList = MutableStateFlow<List<BaseWorkout>>(emptyList())
    val exercisesList: StateFlow<List<BaseWorkout>> = _exercisesList

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

class AllExercisesViewModelFactory(
    private val reason: String?,
    private val exerciseRepository: ExerciseRepository,
    private val workoutsAndExercisesRepository: WorkoutsAndExercisesRepository
) : ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AllExercisesViewModel::class.java) -> {
                AllExercisesViewModel(reason, exerciseRepository, workoutsAndExercisesRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}