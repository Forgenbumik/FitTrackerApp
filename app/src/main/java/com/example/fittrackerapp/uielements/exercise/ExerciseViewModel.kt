package com.example.fittrackerapp.uielements.exercise

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

@RequiresApi(Build.VERSION_CODES.O)
class ExerciseViewModel(
    private val exerciseId: Long,
    private val exerciseRepository: ExerciseRepository,
): ViewModel() {

    private val _exercise = MutableStateFlow<Exercise?>(null)
    val exercise: StateFlow<Exercise?> = _exercise

    val _plannedSets = MutableStateFlow(0)
    val plannedSets: StateFlow<Int> = _plannedSets

    val _plannedReps = MutableStateFlow(0)
    val plannedReps: StateFlow<Int> = _plannedReps

    init {
        viewModelScope.launch {
            exerciseRepository.getByIdFlow(exerciseId).collect {
                _exercise.value = it
            }
        }
    }
}

class ExerciseViewModelFactory(
    private val exerciseId: Long,
    private val exerciseRepository: ExerciseRepository
) : ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExerciseViewModel(exerciseId, exerciseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}