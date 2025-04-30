package com.example.fittrackerapp.uielements.allworkouts

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.entities.Exercise
import com.example.fittrackerapp.entities.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AllExercisesViewModel(
    private val exerciseRepository: ExerciseRepository
): ViewModel() {

    val _exercisesList = MutableStateFlow<List<Exercise>>(emptyList())
    val exercisesList: StateFlow<List<Exercise>> = _exercisesList

    init {
        viewModelScope.launch {
            _exercisesList.value = exerciseRepository.getAllExceptAdded()
        }
    }

    fun addExercisetoUsed(exercise: Exercise) {
        exercise.isUsed = true
        viewModelScope.launch {
            exerciseRepository.update(exercise)
        }
    }
}

class AllExercisesViewModelFactory(
    private val exerciseRepository: ExerciseRepository
) : ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AllExercisesViewModel::class.java) -> {
                AllExercisesViewModel(exerciseRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}