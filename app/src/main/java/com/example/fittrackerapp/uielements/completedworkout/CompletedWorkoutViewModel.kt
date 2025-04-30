package com.example.fittrackerapp.uielements.completedworkout

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.entities.CompletedExercise
import com.example.fittrackerapp.entities.CompletedExerciseRepository
import com.example.fittrackerapp.entities.CompletedWorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CompletedWorkoutViewModel(
    private val completedWorkoutId: Long,
    private val completedWorkoutRepository: CompletedWorkoutRepository,
    private val completedExerciseRepository: CompletedExerciseRepository
): ViewModel() {

    private val _completedExercises = MutableStateFlow<List<CompletedExercise>>(emptyList())
    val completedExercises = _completedExercises

    init {
        viewModelScope.launch {
            completedExerciseRepository.getByCompletedWorkoutId(completedWorkoutId)
                .collect { exercises ->
                    _completedExercises.value = exercises
                }
        }
    }

    suspend fun getExerciseName(exerciseId: Long): String {
        return completedExerciseRepository.getExerciseName(exerciseId)
    }
}

class CompletedWorkoutViewModelFactory(
    private val completedWorkoutId: Long,
    private val completedWorkoutRepository: CompletedWorkoutRepository,
    private val completedExerciseRepository: CompletedExerciseRepository
) : ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(CompletedWorkoutViewModel::class.java) -> {
                CompletedWorkoutViewModel(completedWorkoutId, completedWorkoutRepository, completedExerciseRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}